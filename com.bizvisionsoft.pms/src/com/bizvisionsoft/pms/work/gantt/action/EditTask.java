package com.bizvisionsoft.pms.work.gantt.action;

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
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) GanttEvent event) {
		WorkInfo workinfo = (WorkInfo) event.task;
		String editor;
		if (workinfo.isStage()) {
			if (workinfo.isSummary())
				editor = "����ͼ�ܳɽ׶α༭��.editoassy";
			else
				editor = "����ͼ�׶ι����༭��.editoassy";
		} else if (workinfo.isSummary()) {
			editor = "����ͼ�ܳɹ����༭��.editoassy";
		} else if (workinfo.isMilestone()) {
			editor = "����ͼ��̱������༭��.editoassy";
		} else {
			editor = "����ͼ�����༭��.editoassy";
		}
		Editor.create(editor, context, workinfo, false).setTitle(workinfo.toString()).ok((r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.updateTask(wi);
		});

	}

}
