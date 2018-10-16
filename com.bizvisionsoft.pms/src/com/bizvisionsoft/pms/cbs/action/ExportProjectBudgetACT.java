package com.bizvisionsoft.pms.cbs.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.cbs.assembly.BudgetSubject;

public class ExportProjectBudgetACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		BudgetSubject grid = (BudgetSubject) context.getChildContextByName("cbssubject").getContent();
		grid.export();
	}
}
