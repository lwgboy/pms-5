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

public class AddTask {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) GanttEvent event) {
		// IWBSScope wbsScope = (IWBSScope) context.getRootInput();
		// ÏÔÊ¾±à¼­Æ÷

		new Editor<WorkInfo>(br.getAssembly("¸ÊÌØÍ¼¹¤×÷±à¼­Æ÷"), context)
				.setInput(WorkInfo.newInstance((WorkInfo) event.task)).ok((r, wi) -> {
					GanttPart content = (GanttPart) context.getContent();
					content.addTask(wi);
				});
	}

}
