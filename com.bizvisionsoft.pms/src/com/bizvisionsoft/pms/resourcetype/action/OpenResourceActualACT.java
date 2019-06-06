package com.bizvisionsoft.pms.resourcetype.action;

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
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(elem -> {
			ResourceAssignment resourceAssignment = null;
			if (elem instanceof WorkReportItem) {
				Work work = ((WorkReportItem) elem).getWork();
				WorkReport workReport = ((WorkReportItem) elem).getWorkReport();
				resourceAssignment = new ResourceAssignment().setWork_id(work.get_id());
				resourceAssignment.from = workReport.getPeriodForm();
				resourceAssignment.to = workReport.getPeriodTo();
			}
			if (resourceAssignment != null) {//TODO ???
				br.openContent(br.getAssembly("编辑资源用量"), resourceAssignment);
			}
		});
	}
}
