package com.bizvisionsoft.pms.resourcetype.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.service.model.WorkReportItem;

public class OpenResourceActualACT {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(elem -> {
			ResourceAssignment resourceAssignment = null;
			if (elem instanceof WorkReportItem) {
				Work work = ((WorkReportItem) elem).getWork();
				WorkReport workReport = ((WorkReportItem) elem).getWorkReport();
				resourceAssignment = new ResourceAssignment().setWork_id(work.get_id());
				resourceAssignment.from = workReport.getPeriodForm();
				resourceAssignment.to = workReport.getPeriodTo();
			}
			if (resourceAssignment != null) {
				brui.openContent(brui.getAssembly("编辑资源用量"), resourceAssignment);
			}
		});
	}
}
