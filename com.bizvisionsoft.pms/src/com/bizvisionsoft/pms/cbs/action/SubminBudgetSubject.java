package com.bizvisionsoft.pms.cbs.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubminBudgetSubject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		CBSItem cbsItem = (CBSItem) context.getInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "提交科目预算", "请确认提交预算" + cbsItem + "。");
		if (!ok) {
			return;
		}

		Result result = Services.get(CBSService.class).calculationBudget(cbsItem.get_id());
		if (result.code == Result.CODE_CBS_SUCCESS) {
			CBSItem newCBSItem = (CBSItem) result.data;
			AUtil.simpleCopy(newCBSItem, cbsItem);
			Layer.message("科目预算已提交。");
		} else {
			MessageDialog.openError(shell, "提交科目预算", "科目预算总额与分配的预算总额不一致，无法提交科目预算。");
		}
		// BudgetSubject grid = (BudgetSubject)
		// context.getChildContextByName("cbssubject").getContent();

		// TODO 刷新当前页面foot

		// TODO 刷新上一页面
	}

}
