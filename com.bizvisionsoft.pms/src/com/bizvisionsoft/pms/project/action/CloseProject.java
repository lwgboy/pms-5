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

public class CloseProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "项目关闭",
				"请确认关闭项目" + project + "。<br/>项目关闭后将禁止所有的项目有关操作，包括项目财务结算，创建或更改项目文档。工作包历史跟踪记录将被清除。");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(ProjectService.class)
				.closeProject(brui.command(project.get_id(), new Date()));
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
			Layer.message("项目已关闭");
		} else {
			if (hasError) {
				MessageDialog.openError(shell, "项目关闭", message);
				return;
			} else if (hasWarning) {
				MessageDialog.openWarning(shell, "项目关闭", "项目已关闭，请注意以下提示信息：<br>" + message);
			} else {
				MessageDialog.openInformation(shell, "项目关闭", "项目已关闭，请注意以下提示信息：<br>" + message);
			}
		}

		brui.switchPage("项目首页（关闭）", ((Project) project).get_id().toHexString());
	}

}
