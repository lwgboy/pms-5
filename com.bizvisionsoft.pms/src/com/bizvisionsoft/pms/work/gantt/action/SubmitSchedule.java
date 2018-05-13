package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubmitSchedule {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		IWBSScope rootInput = (IWBSScope) context.getRootInput();
		if (rootInput != null) {
			Workspace workspace = rootInput.getWorkspace();
			if (workspace != null) {
				String checkoutUserId = workspace.getCheckoutBy();
				if (checkoutUserId == null || "".equals(checkoutUserId)
						|| brui.getCurrentUserId().equals(checkoutUserId)) {
					Result result = Services.get(WorkSpaceService.class).schedulePlanCheck(workspace,
							!(rootInput instanceof Project));

					if (Result.CODE_SUCCESS == result.code) {
						result = Services.get(WorkSpaceService.class).checkin(workspace);

						if (Result.CODE_SUCCESS == result.code) {
							MessageDialog.openInformation(brui.getCurrentShell(), "检入提示", result.message);
							brui.switchContent("项目甘特图", null);
						}
					} else {
						MessageDialog.openError(brui.getCurrentShell(), "检入错误", result.message);
					}
				} else {
					MessageDialog.openError(brui.getCurrentShell(), "检入错误", "您没有检入计划的权限。");
				}
			}
		}
	}
}
