package com.bizvisionsoft.pms.cbs.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
/**
 * 未找到任何地方使用
 * 
 * @author gdiyang
 * @date 2018/10/27
 *
 */
@Deprecated
public class EstimationSetting {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		// BudgetCBS budgetCBS = (BudgetCBS)
		// context.getChildContextByName("cbs").getContent();
		// budgetCBS.setEstimation();
	}

}
