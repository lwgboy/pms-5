package com.bizvisionsoft.pms.project.action;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.CommandHandler;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class CloseProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Project project) {
		final ObjectId id = project.get_id();
		ProjectService service = Services.get(ProjectService.class);

		CommandHandler.run(ICommand.Close_Project, //
				"请确认关闭项目" + project + "。<br/>项目关闭后将禁止所有的项目有关操作，包括项目财务结算，创建或更改项目文档。工作包历史跟踪记录将被清除。", //
				"项目关闭完成", "项目关闭失败", //
				() -> service.closeProject(brui.command(id, new Date(), ICommand.Close_Project)), //
				() -> service.closeProject(brui.command(id, new Date(), ICommand.Close_Project_Ignore_Warrning)), //
				code -> brui.switchPage("项目首页（关闭）", id.toHexString()));

	}

}
