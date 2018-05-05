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

public class DistributeProjectPlan {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();

		// 如果项目是按阶段推进的
		if (project.isStageEnable()) {
			Shell s = brui.getCurrentShell();
			boolean ok = MessageDialog.openConfirm(s, "下达项目阶段计划",
					"请确认下达项目" + project + "当前的阶段计划。</p>系统将通知各阶段负责人依照本计划制定详细计划。</p>[提示]未确定负责人的阶段，可在确定后再下达计划。");
			if (!ok) {
				return;
			}
			List<Result> result = Services.get(ProjectService.class).distributeProjectPlan(project.get_id(),
					brui.getCurrentUserId());
			if (result.isEmpty()) {
				MessageDialog.openInformation(s, "下达项目阶段计划", "项目阶段计划下达完成。");
			}else {
				// TODO 显示多条错误信息的通用方法
				MessageDialog.openError(s, "下达项目阶段计划", "项目阶段计划下达失败。</p>"+result.get(0).message);
			}
		} else {
			// TODO 不是按阶段推进的下达
		}
	}

}
