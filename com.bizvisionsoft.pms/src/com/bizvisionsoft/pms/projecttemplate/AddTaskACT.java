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

public class AddTaskACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) GanttEvent event) {
		new Editor<WorkInTemplate>(bruiService.getAssembly("项目模板工作编辑器"), context)
				.setInput(WorkInTemplate.newInstance((WorkInTemplate) event.task)).ok((r, wi) -> {
					GanttPart content = (GanttPart) context.getContent();
					content.addTask(wi);
				});
	}

}
