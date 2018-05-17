package com.bizvisionsoft.pms.work.action;

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

public class StartWork {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Work work = (Work) context.getRootInput();
		Shell shell = brui.getCurrentShell();

		boolean ok = MessageDialog.openConfirm(shell, "启动工作", "请确认启动工作" + work + "。\n系统将记录现在时刻为工作的实际开始时间。");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(WorkService.class).startWork(work.get_id());
		if (result.isEmpty()) {
			MessageDialog.openInformation(shell, "启动工作", "工作启动完成。");
		}
	}

}
