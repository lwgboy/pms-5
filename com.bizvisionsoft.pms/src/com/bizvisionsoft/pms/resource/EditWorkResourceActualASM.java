package com.bizvisionsoft.pms.resource;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.session.UserSession;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkResourcePlanDetail;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditWorkResourceActualASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private Calendar start;

	private Calendar end;

	private GridTableViewer viewer;

	private List<WorkResourcePlanDetail> works;

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		StickerTitlebar bar = new StickerTitlebar(parent, null, null).setActions(context.getAssembly().getActions());
		bar.addListener(SWT.Selection, e -> {
			Action action = ((Action) e.data);
			if ("close".equals(action.getName())) {
				brui.closeCurrentContent();
			}
		});
		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 48;

		Composite content = UserSession.bruiToolkit().newContentPanel(parent);
		fd = new FormData();
		content.setLayoutData(fd);
		fd.left = new FormAttachment(0, 12);
		fd.top = new FormAttachment(bar, 12);
		fd.right = new FormAttachment(100, -12);
		fd.bottom = new FormAttachment(100, -12);
		content.setLayout(new FillLayout(SWT.VERTICAL));

		ResourceAssignment ra = (ResourceAssignment) context.getInput();

		bar.setText("资源实际用量 ");

		// 取出开始和完成时间

		WorkService service = Services.get(WorkService.class);
		Work work = service.getWork(ra.work_id);
		start = Calendar.getInstance();
		start.setTime(ra.from);
		end = Calendar.getInstance();
		end.setTime(ra.to);
		//
		createViewer(content);
		//
		// TODO 封装成方法
//		works = service.listConflictWorks(ra.work_id);
//		viewer.setInput(works);
	}

	private void createViewer(Composite parent) {
		viewer = new GridTableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Grid grid = viewer.getGrid();
		grid.setHeaderVisible(true);
		grid.setFooterVisible(false);
		grid.setLinesVisible(true);
		UserSession.bruiToolkit().enableMarkup(grid);
		grid.setData(RWT.FIXED_COLUMNS, 3);

		Column c = new Column();
		c.setName("resId");
		c.setText("资源编号");
		c.setWidth(160);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);

		c = new Column();
		c.setName("type");
		c.setText("资源类型");
		c.setWidth(160);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);

		c = new Column();
		c.setName("name");
		c.setText("名称");
		c.setWidth(320);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);

		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(start.getTimeInMillis());
		createDateColumn(now.getTime());
		while (now.before(end)) {
			now.add(Calendar.DATE, 1);
			createDateColumn(now.getTime());
		}

		viewer.setContentProvider(ArrayContentProvider.getInstance());

	}

	private void createDateColumn(Date now) {
		GridColumn col = new GridColumn(viewer.getGrid(), SWT.CENTER);
		col.setText(new SimpleDateFormat("M/d").format(now.getTime()));
		col.setWidth(48);
		col.setMoveable(false);
		col.setResizeable(false);

		GridViewerColumn vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				// 某天总体工时
				return "";
			}
		});
	}

	private void createTitleColumn(Column c) {
		GridColumn col = new GridColumn(viewer.getGrid(), SWT.NONE);
		col.setText(c.getText());
		col.setWidth(c.getWidth());
		col.setMoveable(c.isMoveable());
		col.setResizeable(c.isResizeable());

		GridViewerColumn vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String name = c.getName();
				//TODO
				if ("resId".equals(name)) {
					return "";
				} else if ("type".equals(name)) {
					return "";
				} else if ("name".equals(name)) {
					return "";
				}
				return "";
			}
		});

	}
}
