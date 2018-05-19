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

public class CloseStage {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Work stage = (Work) context.getRootInput();
		Shell shell = brui.getCurrentShell();

		boolean ok = MessageDialog.openConfirm(shell, "关闭阶段",
				"请确认关闭阶段" + stage + "。\n系统将记录现在时刻为阶段的关闭时间，并向本阶段项目组成员发出关闭通知。");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(WorkService.class).closeStage(stage.get_id(), brui.getCurrentUserId());
		if (result.isEmpty()) {
			MessageDialog.openInformation(shell, "关闭阶段", "阶段关闭完成。");

			brui.switchPage("阶段首页（关闭）", ((Work) stage).get_id().toHexString());
		}
		// TODO 显示多条错误信息的通用方法
	}

}
