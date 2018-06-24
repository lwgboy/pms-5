package com.bizvisionsoft.pms.projecttemplate;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.WorkInTemplate;

public class AddTaskACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		new Editor<WorkInTemplate>(bruiService.getAssembly("项目模板工作编辑器"), context)
				.setInput(WorkInTemplate.newInstance((WorkInTemplate) ((GanttEvent) event).task)).ok((r, wi) -> {
					GanttPart content = (GanttPart) context.getContent();
					content.addTask(wi, wi.index());
				});
	}

}
