package com.bizvisionsoft.pms.cbs.action;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Work;

public class OpenBudgetSubjectACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Object rootInput = context.getRootInput();
		if (rootInput instanceof Work && ((Work) rootInput).getCBS_id() != null) {
			bruiService.openContent(bruiService.getAssembly("��Ŀ�ʽ�ƻ�"), rootInput);
		} else {
			Layer.message("�޷����ƿ�ĿԤ��", Layer.ICON_CANCEL);
		}
	}
}
