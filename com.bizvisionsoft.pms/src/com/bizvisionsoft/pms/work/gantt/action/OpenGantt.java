package com.bizvisionsoft.pms.work.gantt.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Workspace;

public class OpenGantt {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.ACTION) Action action) {
		IWBSScope rootInput = (IWBSScope) context.getRootInput();
		Assembly config;
		Workspace workspace = rootInput.getWorkspace();
		if (workspace != null && brui.getCurrentUserId().equals(workspace.getCheckoutBy())) {
			config = brui.getAssembly("项目甘特图（编辑）");
		} else {
			config = brui.getAssembly("项目甘特图");
		}

		if (action.isOpenContent()) {
			brui.openContent(config, null);
		} else {
			brui.switchContent(config, null);
		}
	}

}
