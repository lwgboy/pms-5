package com.bizvisionsoft.pms.cbs.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.cbs.assembly.BudgetCBS;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class AddCBSItemByStage {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Object rootInput = context.getRootInput();
		if(rootInput instanceof Project) {
			Project project = (Project) rootInput;
			CBSItem cbsRoot = Services.get(CBSService.class).addCBSItemByStage(project.getCBS_id(),
					project.get_id());
			BudgetCBS budgetCBS = (BudgetCBS) context.getChildContextByName("cbs").getContent();
			budgetCBS.update(cbsRoot);
		}
	}
}
