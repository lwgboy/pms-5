package com.bizvisionsoft.pms.stage.action;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class DistributeStagePlan {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Work work = (Work) context.getRootInput();

		Shell s = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(s, "下达阶段计划",
				"请确认下达阶段" + work + "当前的计划。</p>系统将通知各工作负责人依照本计划执行工作。</p>[提示]未确定负责人的工作，可在确定后再下达计划。");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(WorkService.class).distributeWorkPlan(work.get_id(),
				brui.getCurrentUserId());
		if (result.isEmpty()) {
			MessageDialog.openInformation(s, "下达阶段计划", "阶段计划下达完成。");
		} else {
			// TODO 显示多条错误信息的通用方法
			MessageDialog.openError(s, "下达阶段计划", "阶段计划下达失败。</p>" + result.get(0).message);
		}
	}

}
