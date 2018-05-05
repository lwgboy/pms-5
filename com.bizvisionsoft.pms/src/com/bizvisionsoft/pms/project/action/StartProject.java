package com.bizvisionsoft.pms.project.action;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class StartProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "启动项目",
				"请确认启动项目" + project + "。\n系统将记录现在时刻为项目实际开始时间，并向项目组成员发出启动通知。");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(ProjectService.class).startProject(project.get_id(),
				brui.getCurrentUserId());
		if (result.isEmpty()) {
			MessageDialog.openInformation(shell, "启动项目", "项目启动完成。");
			brui.switchPage("项目首页（执行）", ((Project) project).get_id().toHexString());
		}
		// TODO 显示多条错误信息的通用方法
	}

}
