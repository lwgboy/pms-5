package com.bizvisionsoft.pms.project.action;

import java.util.Date;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.CommandHandler;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;

@Deprecated
public class DistributeProjectPlan {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_CONTENT) Object content,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Project project) {
		ProjectService service = Services.get(ProjectService.class);
		CommandHandler.run(ICommand.Distribute_Project_Plan, //
				"项目：" + project + "，请确认下达计划。", //
				"项目计划已下达", "项目计划下达失败", //
				() -> service.distributeProjectPlan(brui.command(project.get_id(), new Date(), ICommand.Distribute_Project_Plan)), //
				code -> Check.instanceThen(content, GridPart.class, GridPart::setViewerInput));
	}

}
