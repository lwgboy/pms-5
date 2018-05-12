package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class CancelSchedule {
	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Workspace workspace = null;
		Object rootInput = context.getRootInput();
		if (rootInput instanceof Project) {
			workspace = ((Project) rootInput).getWorkspace();
		} else if (rootInput instanceof Work) {
			workspace = ((Work) rootInput).getWorkspace();
		}
		if (workspace != null) {
			String checkOutUserId = workspace.getCheckoutBy();
			if (checkOutUserId == null || "".equals(checkOutUserId)
					|| bruiService.getCurrentUserId().equals(checkOutUserId)) {
				Result result = Services.get(WorkSpaceService.class).cancelCheckout(workspace);
				if (Result.CODE_SUCCESS == result.code) {
					bruiService.switchContent("项目甘特图", null);
				}
			} else {
				MessageDialog.openError(bruiService.getCurrentShell(), "撤销提示", "您没有撤销计划编辑的权限。");
			}
		} else {
			MessageDialog.openError(bruiService.getCurrentShell(), "撤销提示", "您没有撤销计划编辑的权限。");
		}
	}
}
