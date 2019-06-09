package com.bizvisionsoft.pms.projecttemplate;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.WBSModule;
import com.bizvisionsoft.service.model.WorkInTemplate;

public class CreateRootTaskACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		WorkInTemplate workInT = createWork(context);

		Editor.create("项目模板工作编辑器.editorassy", context,workInT,false).setTitle("创建工作").ok((r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.addTask(wi);
		});
	}

	private WorkInTemplate createWork(IBruiContext context) {
		Object input = context.getInput();
		if (input instanceof WBSModule) {
			return WorkInTemplate.newInstance((WBSModule) input);
		} else {
			return WorkInTemplate.newInstance(context.getRootInput(ProjectTemplate.class, false));
		}
	}

}
