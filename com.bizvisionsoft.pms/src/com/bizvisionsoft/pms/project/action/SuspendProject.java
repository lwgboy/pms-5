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
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class SuspendProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(se -> {
			Project project = (Project) se;
			Shell shell = brui.getCurrentShell();
			boolean ok = MessageDialog.openConfirm(shell, "暂停项目", "请确认暂停项目" + project + "。\n系统将向项目组成员发出暂停通知。");
			if (!ok) {
				return;
			}
			List<Result> result = Services.get(ProjectService.class)
					.suspendProject(brui.command(project.get_id(), new Date()));
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
				message = "项目已暂停。<br>" + message;
				Layer.message(message);
				((GridPart) context.getContent()).refreshAll();
			}

		});
		// TODO 显示多条错误信息的通用方法
	}
}
