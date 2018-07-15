package com.bizvisionsoft.pms.stage.action;

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
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class StartStage {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		Work stage = (Work) context.getRootInput();
		Shell shell = brui.getCurrentShell();

		Project project = stage.getProject();
		if (project != null && ProjectStatus.Processing.equals(project.getStatus())) {

			boolean ok = MessageDialog.openConfirm(shell, "启动阶段",
					"请确认启动阶段" + stage + "。\n系统将记录现在时刻为阶段的启动时间，并向本阶段项目组成员发出启动通知。");
			if (!ok) {
				return;
			}
			List<Result> result = Services.get(WorkService.class).startStage(brui.command(stage.get_id()));
			boolean b = true;
			String message = "";
			if (!result.isEmpty()) {
				for (Result r : result)
					if (Result.TYPE_ERROR == r.type) {
						Layer.message(r.message, Layer.ICON_CANCEL);
						b = false;
					} else {
						message += r.message + "<br>";
					}
			}

			if (b) {
				message = "阶段已启动。<br>" + message;
				Layer.message(message);
				brui.switchPage("阶段首页（执行）", ((Work) stage).get_id().toHexString());
			}
			// TODO 显示多条错误信息的通用方法
		} else {
			Layer.message("阶段所在项目未启动，无法启动阶段。", Layer.ICON_CANCEL);
		}
	}

}
