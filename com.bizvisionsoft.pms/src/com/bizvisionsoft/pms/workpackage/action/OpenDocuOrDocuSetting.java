package com.bizvisionsoft.pms.workpackage.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiService;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.service.model.ProjectTemplate;

public class OpenDocuOrDocuSetting {

	@Inject
	private BruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		String name;
		if (context.getRootInput() instanceof ProjectTemplate) {
			name = "����ĵ�����";
		} else {
			name = "����ĵ�";
		}
		br.openContent(br.getAssembly(name), context.getSelection().getFirstElement());
	}

}
