package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Workspace;

public class OpenGantt {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		Workspace workspace = project.getWorkspace();
		
		if (brui.getCurrentUserId().equals(workspace.getCheckoutBy())) {
			brui.switchContent("项目甘特图(编辑)", null);
		} else {
			brui.switchContent("项目甘特图", null);
		}
	}

}
