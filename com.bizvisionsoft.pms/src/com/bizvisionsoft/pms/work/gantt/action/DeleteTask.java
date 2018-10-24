package com.bizvisionsoft.pms.work.gantt.action;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.WorkInfo;

public class DeleteTask {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_CONTENT) GanttPart part,
			@MethodParam(Execute.EVENT) GanttEvent event) {
		if (br.confirm("ɾ��", "��ȷ�Ͻ�Ҫɾ��ѡ��Ĺ�����")) {
			WorkInfo task = (WorkInfo) event.task;
			part.deleteTask(task.getId());
		}
	}

}
