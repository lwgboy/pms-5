package com.bizvisionsoft.pms.workreport;

import java.util.Arrays;
import java.util.List;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.InfopadPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubmitWorkReportACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {

		WorkReport input = (WorkReport) context.getInput();
		boolean ok = brui.confirm("提交" + input.getType(),
				"请确认提交报告：" + input.getLabel() + "。\n系统将记录现在时刻为报告提交时间，提交后该报告将无法进行修改。");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(WorkReportService.class).submitWorkReport(Arrays.asList(input.get_id()));
		if (result.isEmpty()) {
			Layer.message("报告已提交");
			InfopadPart ip = (InfopadPart) context.getChildContextByAssemblyName("工作报告基本信息面板").getContent();
			ip.reload();
			brui.closeCurrentContent();
		} else {
			if (result.get(0).code == Result.CODE_WORKREPORT_HASNOSTATEMENTWORK)
				Layer.message("请为所有工作填写完成情況后，再进行提交。", Layer.ICON_CANCEL);
		}
	}
}
