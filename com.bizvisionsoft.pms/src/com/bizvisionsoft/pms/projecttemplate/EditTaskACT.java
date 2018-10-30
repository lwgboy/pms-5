package com.bizvisionsoft.pms.projecttemplate;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.WorkInTemplate;

public class EditTaskACT {
	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) GanttEvent event) {
		WorkInTemplate workinfo = (WorkInTemplate) event.task;
		String editor = "��Ŀģ�幤���༭��";
		if (workinfo.isMilestone()) {
			editor = "��Ŀģ����̱������༭��";
		} else {
			editor = "��Ŀģ�幤���༭��";
		}
		Editor.create(editor, context, workinfo, false).setTitle(workinfo.toString()).ok((r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.updateTask(wi);
		});

	}
}
