package com.bizvisionsoft.pms.workpackage.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiService;
import com.bizvisionsoft.service.model.ProjectTemplate;

public class OpenDocuOrDocuSetting {

	@Inject
	private BruiService br;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Object rootInput,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object sele) {
		String name;
		if (rootInput instanceof ProjectTemplate) {
			name = "输出文档设置.gridassy";
		} else {
			name = "输出文档.gridassy";
		}
		br.openContent(br.getAssembly(name), sele);
	}

}
