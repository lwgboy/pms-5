package com.bizvisionsoft.pms.project.action;

import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class FinishProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "项目收尾",
				"请确认项目" + project + "进入收尾。\n系统将记录现在时刻为项目完工时间，并向项目组成员发出项目完工通知。");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(ProjectService.class)
				.finishProject(brui.command(project.get_id(), new Date()));
		if (result.isEmpty()) {
			Layer.message("项目已完工。");
			brui.switchPage("项目首页（收尾）", ((Project) project).get_id().toHexString());
		}
		// TODO 显示多条错误信息的通用方法
	}

}
