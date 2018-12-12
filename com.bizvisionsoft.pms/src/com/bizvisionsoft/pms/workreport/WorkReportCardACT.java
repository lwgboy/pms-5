package com.bizvisionsoft.pms.workreport;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.serviceconsumer.Services;

public class WorkReportCardACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.CURRENT_USER_ID) String userid) {
		if (e.text == null)
			return;
		ObjectId _id = element.getObjectId("_id");
		WorkReport workReport = Services.get(WorkReportService.class).getWorkReport(_id);
		GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
		if (e.text.startsWith("confirm")) {
			confirmWorkReport(workReport, userid, element, viewer);
		}
		if (e.text.startsWith("openItem")) {
			open(workReport, userid, element, viewer);
		}
	}

	private void open(WorkReport workReport, String userid, Document element, GridTreeViewer viewer) {
		br.openContent(br.getAssembly("报告详情"), workReport, e -> {
			if (workReport instanceof WorkReport) {
				Layer.message("报告已确认");
				((List<?>) viewer.getInput()).remove(workReport);
				viewer.remove(workReport);
			}
		});
	}

	private void confirmWorkReport(WorkReport workReport, String userid, Document doc, GridTreeViewer viewer) {
		if (br.confirm("确认" + workReport.getType(), "请确认报告：" + workReport.getLabel() + "。\n系统将记录现在时刻为报告确认时间。")) {
			List<Result> result = Services.get(WorkReportService.class).confirmWorkReport(Arrays.asList(workReport.get_id()), userid);
			if (result.isEmpty()) {
				Layer.message("报告已确认");
				((List<?>) viewer.getInput()).remove(doc);
				viewer.remove(doc);
			}
		}
	}
}
