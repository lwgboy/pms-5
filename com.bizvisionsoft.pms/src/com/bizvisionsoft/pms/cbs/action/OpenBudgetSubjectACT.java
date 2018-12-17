package com.bizvisionsoft.pms.cbs.action;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Work;

/**
 * 
 * @author gdiyang
 * @date 2018/10/27
 *
 */
public class OpenBudgetSubjectACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Object rootInput) {
		if (rootInput instanceof Work && ((Work) rootInput).getCBS_id() != null) {
			bruiService.openContent("项目资金计划", rootInput);
		} else {
			Layer.message("无法编制科目预算", Layer.ICON_ERROR);
		}
	}
}
