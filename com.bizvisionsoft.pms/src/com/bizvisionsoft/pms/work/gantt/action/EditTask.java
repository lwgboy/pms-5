package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.WorkInfo;

public class EditTask {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		WorkInfo workinfo = (WorkInfo) ((GanttEvent) event).task;
		String editor;
		if (workinfo.isStage()) {
			editor = "¸ÊÌØÍ¼½×¶Î¹¤×÷±à¼­Æ÷";
		} else if (workinfo.isSummary()) {
			editor = "¸ÊÌØÍ¼×Ü³É¹¤×÷±à¼­Æ÷";
		} else {
			editor = "¸ÊÌØÍ¼¹¤×÷±à¼­Æ÷";
		}
		Editor.create(editor, context, workinfo, false).setTitle(workinfo.toString()).ok((r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.updateTask(wi);
		});

	}

}
