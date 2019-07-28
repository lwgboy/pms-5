package com.bizvisionsoft.pms.formdef.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.formdef.FormDefTools;
import com.bizvisionsoft.service.model.FormDef;

public class CheckFormDefACT {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_INPUT_OBJECT) Object element) {
		if (element instanceof FormDef) {
			FormDef formDef = (FormDef) element;
			if (formDef.get_id() == null)
				return;

			FormDefTools.checkFormDef(br, formDef, "表单定义检查", "表单定义存在以下问题，这些问题将造成其无法启用。", //
					"表单定义存在以下问题，这些问题需要进行确认。");
		}
	}
}
