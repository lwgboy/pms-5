package com.bizvisionsoft.pms.workreport;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.swt.SWT;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnHeader;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUICreated;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.assembly.GridPartDefaultRender;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.WorkReportSummary;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class WeeklyProjectReportSummaryRender extends GridPartDefaultRender {

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
				WorkReportSummary item = (WorkReportSummary) e.item.getData();
				String text = null;
				String title = null;
				if (e.text.startsWith("statement")) {
					text = item.getStatement();
					title = "完成情况";
				} else if (e.text.startsWith("problems")) {
					text = item.getProblems();
					title = "存在问题";
				} else if (e.text.startsWith("pmRemark")) {
					text = item.getPmRemark();
					title = "项目经理批注";
				} else if (e.text.startsWith("tag")) {
					editTag(item);
				}
				if (text != null) {
					Layer.alert(title, text, 420, 240);
				}
			}
		});
	}

	private void editTag(WorkReportSummary item) {
		Editor.open("工作报告标签编辑器", context, item, (r, t) -> {
			FilterAndUpdate fu = new FilterAndUpdate().filter(new BasicDBObject("_id", item.get_id())).set(new BasicDBObject("tags",t.getTags()));
			Services.get(WorkReportService.class).updateWorkReportItem(fu.bson());
			item.setTags(t.getTags());
			viewer.update(item, null);
		});
	}

	@Override
	@GridRenderUpdateCell
	public void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell,
			@MethodParam(GridRenderUpdateCell.PARAM_COLUMN) Column column,
			@MethodParam(GridRenderUpdateCell.PARAM_VALUE) Object value,
			@MethodParam(GridRenderUpdateCell.PARAM_IMAGE) Object image) {
		super.renderCell(cell, column, value, image);
	}

	@Override
	@GridRenderColumnHeader
	public void renderColumnHeader(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		super.renderColumnHeader(col, column);
	}

}
