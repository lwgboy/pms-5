package com.bizvisionsoft.pms.project.action;

import java.util.Date;

import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.ResultHandler;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class StartProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "项目启动",
				"请确认启动项目：" + project + "。<br/>系统将标记当前时间为项目完工日期。<br/>已启动的项目将无法删除。");
		if (!ok) {
			return;
		}
		/////////////////////////////////////////////////////////////////////////////
		// 不忽略警告，启动项目
		final ObjectId id = project.get_id();
		ProjectService service = Services.get(ProjectService.class);

		ResultHandler.run(ICommand.Start_Project, "项目启动完成", "项目启动失败",
				() -> service.startProject(brui.command(id, new Date(), ICommand.Start_Project)),//
				() -> service.startProject(brui.command(id, new Date(), ICommand.Start_Project_Ignore_Warrning)), //
				code -> brui.switchPage("项目首页（执行）", id.toHexString()));

	}

}
