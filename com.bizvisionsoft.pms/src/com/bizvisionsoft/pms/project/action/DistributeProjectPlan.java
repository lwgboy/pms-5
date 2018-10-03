package com.bizvisionsoft.pms.project.action;

import java.util.Date;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.CommandHandler;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;

public class DistributeProjectPlan {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		Object rootInput = context.getRootInput();
		ProjectService service = Services.get(ProjectService.class);
		CommandHandler.run(ICommand.Distribute_Project_Plan, //
				"项目：" + (Project) rootInput + "，请确认下达计划。", //
				"项目计划已下达", "项目计划下达失败", //
				() -> service.distributeProjectPlan(
						brui.command(((Project) rootInput).get_id(), new Date(), ICommand.Distribute_Project_Plan)), //
				code -> Check.instanceThen(context.getContent(), GridPart.class, g->g.setViewerInput()));
	}

}
