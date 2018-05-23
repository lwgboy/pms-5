package com.bizvisionsoft.pms.work.action;

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
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class FinishWork {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(elem -> {
			Work work = (Work) elem;
			Shell shell = brui.getCurrentShell();

			boolean ok = MessageDialog.openConfirm(shell, "完成工作", "请确认完成工作" + work + "。\n系统将记录现在时刻为工作的实际完成时间。");
			if (!ok) {
				return;
			}
			List<Result> result = Services.get(WorkService.class).finishWork(work.get_id());
			if (result.isEmpty()) {
				Layer.message("工作已完成。");
				GridPart grid = (GridPart) context.getContent();
				grid.remove(elem);
			}
		});
	}

}
