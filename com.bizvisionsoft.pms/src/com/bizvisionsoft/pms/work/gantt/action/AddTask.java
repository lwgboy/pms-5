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

public class AddTask {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		// IWBSScope wbsScope = (IWBSScope) context.getRootInput();
		// ÏÔÊ¾±à¼­Æ÷

		new Editor<WorkInfo>(bruiService.getAssembly("¸ÊÌØÍ¼¹¤×÷±à¼­Æ÷"), context)
				.setInput(WorkInfo.newInstance((WorkInfo) ((GanttEvent) event).task)).ok((r, wi) -> {
					GanttPart content = (GanttPart) context.getContent();
					content.addTask(wi, wi.index());
				});
	}

}
