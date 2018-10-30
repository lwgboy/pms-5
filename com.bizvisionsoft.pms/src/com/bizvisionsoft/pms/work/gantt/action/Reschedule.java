package com.bizvisionsoft.pms.work.gantt.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class Reschedule {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_CONTENT) GanttPart part) {
		if (br.confirm("排程", "请确认对当前计划运行排程。")) {
			part.reschedule();
		}
	}

}
