package com.bizvisionsoft.pms.work.gantt.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Workspace;

public class OpenGantt {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) IWBSScope rootInput,
			@MethodParam(Execute.ACTION) Action action) {
		Workspace workspace = rootInput.getWorkspace();
		Assembly config;
		if (workspace != null && br.getCurrentUserId().equals(workspace.getCheckoutBy())) {
			config = br.getAssembly("项目甘特图（编辑）");
		} else {
			config = br.getAssembly("项目甘特图");
		}

		if (action.isOpenContent()) {
			br.openContent(config, null);
		} else {
			br.switchContent(config, null);
		}
	}

}
