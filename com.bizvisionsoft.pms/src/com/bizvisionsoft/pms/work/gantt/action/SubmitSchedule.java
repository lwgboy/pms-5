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
import com.bizvisionsoft.serviceconsumer.Services;

public class SubmitSchedule {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		IWBSScope wbsScope = (IWBSScope) context.getRootInput();
		String checkOutUserId = wbsScope.getCheckOutUserId();
		if (checkOutUserId == null || "".equals(checkOutUserId) || brui.getCurrentUserId().equals(checkOutUserId)) {
			Result result = Services.get(WorkSpaceService.class).schedulePlanCheck(wbsScope.getCheckOutKey(),
					brui.getCurrentUserId(), !(wbsScope instanceof Project));

			if (Result.CODE_SUCCESS == result.code) {
				result = Services.get(WorkSpaceService.class).checkInSchedulePlan(wbsScope.getCheckOutKey(),
						brui.getCurrentUserId());

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
