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
		boolean ok = MessageDialog.openConfirm(shell, "项目收尾", "请确认收尾项目" + project
				+ "。<br/>系统将标记当前时间为项目完工日期。<br/>收尾中的项目将不能修改计划，增加新的工作或项目变更。<br/>收尾期间仍可进行项目财务结算，整理项目文档。");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(ProjectService.class)
				.finishProject(brui.command(project.get_id(), new Date()));

		boolean hasError = false;
		boolean hasWarning = false;

		String message = "";
		if (!result.isEmpty()) {
			for (Result r : result)
				if (Result.TYPE_ERROR == r.type) {
					hasError = true;
					message += "错误：" + r.message + "<br>";
				} else if (Result.TYPE_WARNING == r.type) {
					hasError = true;
					message += "警告：" + r.message + "<br>";
				} else {
					message += "信息：" + r.message + "<br>";
				}
		}

		if (message.isEmpty()) {
			Layer.message("项目已收尾");
		} else {
			if (hasError) {
				MessageDialog.openError(shell, "项目收尾", message);
				return;
			} else if (hasWarning) {
				MessageDialog.openWarning(shell, "项目收尾", "项目已收尾，请注意以下提示信息：<br>" + message);
			} else {
				MessageDialog.openInformation(shell, "项目收尾", "项目已收尾，请注意以下提示信息：<br>" + message);
			}
		}

		brui.switchPage("项目首页（收尾）", ((Project) project).get_id().toHexString());
	}

}
