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

public class TerminateProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(se -> {
			Project project = (Project) se;
			Shell shell = brui.getCurrentShell();
			boolean ok = MessageDialog.openConfirm(shell, "中止项目",
					"请确认中止项目" + project + "。\n系统将取消所有未完成的工作，并向项目组成员发出中止通知。");
			if (!ok) {
				return;
			}
			List<Result> result = Services.get(ProjectService.class)
					.terminateProject(brui.command(project.get_id(), new Date()));
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
				message = "项目已中止。<br>" + message;
				Layer.message(message);
				((GridPart) context.getContent()).refreshAll();
			}
		});
	}
}
