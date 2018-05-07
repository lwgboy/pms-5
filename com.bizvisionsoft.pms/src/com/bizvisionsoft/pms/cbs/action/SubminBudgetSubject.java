package com.bizvisionsoft.pms.cbs.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubminBudgetSubject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		CBSItem cbsItem = (CBSItem) context.getInput();
		CBSItem newCBSItem = Services.get(CBSService.class).calculationBudget(cbsItem.get_id());
		AUtil.simpleCopy(newCBSItem, cbsItem);
//		BudgetSubject grid = (BudgetSubject) context.getChildContextByName("cbssubject").getContent();
		
		//TODO 刷新当前页面foot
		
		//TODO 刷新上一页面
	}

}
