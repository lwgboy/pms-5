package com.bizvisionsoft.pms.baseline;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class OpenBaselineACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		context.selected(em -> {
			// brui.switchContent("项目基线甘特图", em);
			brui.openContent(brui.getAssembly("项目基线甘特图"), em);
		});
	}
}
