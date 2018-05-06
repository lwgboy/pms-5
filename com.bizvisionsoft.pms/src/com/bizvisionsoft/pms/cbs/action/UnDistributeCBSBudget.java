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
import com.bizvisionsoft.serviceconsumer.Services;

public class UnDistributeCBSBudget {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(parent -> {
			CBSItem item = (CBSItem) parent;
			CBSItem parentItem = item.getParent();
			if (item.isScopeRoot()) {
				String scopeName = parentItem.getScopeName();
				CBSItem cbsItem = Services.get(CBSService.class).allocateBudget(item.get_id(), parentItem.getScope_id(),
						scopeName, false);
				BudgetCBS grid = (BudgetCBS) context.getContent();
				grid.replaceItem(parent, cbsItem);
				grid.refresh(parent);
			}

		});
	}

}
