package com.bizvisionsoft.pms.projecttemplate;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.WorkInTemplate;

public class CreateRootTaskACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		ProjectTemplate template = context.getRootInput(ProjectTemplate.class,false);
		String title = "创建工作";
		Assembly editor = bruiService.getAssembly("项目模板工作编辑器");
		WorkInTemplate workInT  = WorkInTemplate.newInstance(template);

		new Editor<WorkInTemplate>(editor, context).setTitle(title).setInput(workInT).ok((r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.addTask(wi, wi.index());
		});
	}

}
