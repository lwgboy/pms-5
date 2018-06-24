package com.bizvisionsoft.pms.projecttemplate;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.WorkInTemplate;

public class DeleteTaskACT {
	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		if (MessageDialog.openConfirm(bruiService.getCurrentShell(), "删除", "请确认将要删除选择的工作。")) {
			WorkInTemplate task = (WorkInTemplate) ((GanttEvent) event).task;
			((GanttPart) context.getContent()).deleteTask(task.getId());
		}
	}
}
