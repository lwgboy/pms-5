package com.bizvisionsoft.pms.work;

import java.util.List;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnFooter;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnHeader;
import com.bizvisionsoft.annotations.ui.grid.GridRenderCompare;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUICreated;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.assembly.GridPartDefaultRender;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.work.action.IWorkAction;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.Check;

@Deprecated
public class MyWorkRender extends GridPartDefaultRender implements IWorkAction {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private GridTreeViewer viewer;

	@GridRenderUICreated
	private void uiCreated() {
		viewer = (GridTreeViewer) context.getContent("viewer");
		viewer.getGrid().addListener(SWT.Selection, e -> {
			if (e.text != null) {
				if (e.text.startsWith("startWork/")) {
					startWork((Work) ((GridItem) e.item).getData());
				} else if (e.text.startsWith("finishWork/")) {
					finishWork((Work) ((GridItem) e.item).getData());
				} else if (e.text.split("/").length > 1) {
					String idx = e.text.split("/")[1];
					if (e.text.startsWith("openWorkPackage/")) {
						openWorkPackage((Work) ((GridItem) e.item).getData(), idx);
					} else if (e.text.startsWith("assignWork/")) {
						assignWork((Work) ((GridItem) e.item).getData());
					}
				}
			} else {
				Check.instanceThen(((GridItem) e.item).getData(), Work.class, el -> viewer.setExpandedElements(new Object[] { el }));
			}
		});

		Check.isAssigned((List<?>) viewer.getInput(), el -> viewer.setExpandedElements(new Object[] { el.get(0) }));
	}

	private void openWorkPackage(Work work, String idx) {
		if ("default".equals(idx)) {
			brui.openContent(brui.getAssembly("工作包计划"), new Object[] { work, null });
		} else {
			List<TrackView> wps = work.getWorkPackageSetting();
			brui.openContent(brui.getAssembly("工作包计划"), new Object[] { work, wps.get(Integer.parseInt(idx)) });
		}
	}

	private void assignWork(Work work) {
		assignWork(work, context, w -> {
			viewer.update(AUtil.simpleCopy(w, work), null);
		});
	}

	private void finishWork(Work work) {
		finishWork(work, w -> {
			viewer.remove(work);
		});
	}

	private void startWork(Work work) {
		startWork(work, w -> {
			viewer.update(AUtil.simpleCopy(w, work), null);
		});
	}

	@Override
	@GridRenderUpdateCell
	public void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell,
			@MethodParam(GridRenderUpdateCell.PARAM_COLUMN) Column column, @MethodParam(GridRenderUpdateCell.PARAM_VALUE) Object value,
			@MethodParam(GridRenderUpdateCell.PARAM_IMAGE) Object image) {
		super.renderCell(cell, column, value, image);
	}

	@Override
	@GridRenderColumnHeader
	public void renderColumnHeader(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		super.renderColumnHeader(col, column);
	}

	@Override
	@GridRenderColumnFooter
	public void renderColumnFooter(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		super.renderColumnFooter(col, column);
	}

	@Override
	@GridRenderCompare
	public int compare(@MethodParam(GridRenderCompare.PARAM_COLUMN) Column col, @MethodParam(GridRenderCompare.PARAM_ELEMENT1) Object e1,
			@MethodParam(GridRenderCompare.PARAM_ELEMENT2) Object e2) {
		return super.compare(col, e1, e2);
	}

	@Override
	public IBruiService getBruiService() {
		return brui;
	}

}
