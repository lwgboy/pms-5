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

public class StartProject {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Project project) {
		/////////////////////////////////////////////////////////////////////////////
		// 不忽略警告，启动项目
		final ObjectId id = project.get_id();
		ProjectService service = Services.get(ProjectService.class);

		CommandHandler.run(ICommand.Start_Project, //
				"请确认启动项目：" + project + "。<br/>已启动的项目将无法删除。", "项目启动完成", "项目启动失败", //
				() -> service.startProject(br.command(id, new Date(), ICommand.Start_Project), br.getDomain()), //
				() -> service.startProject(br.command(id, new Date(), ICommand.Start_Project_Ignore_Warrning), br.getDomain()), //
				code -> br.switchPage("项目首页（执行）", id.toHexString()));

	}

}
