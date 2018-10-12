package com.bizvisionsoft.pms.cbs.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubminBudgetSubject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		CBSItem cbsItem = null;
		Object input = context.getInput();
		if (input instanceof CBSItem)
			cbsItem = (CBSItem) input;

		if (cbsItem == null) {
			ICBSScope rootInput = (ICBSScope) context.getRootInput();
			cbsItem = Services.get(CBSService.class).get(rootInput.getCBS_id());
		}
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "�ύ��ĿԤ��", "��ȷ���ύԤ��" + cbsItem + "��");
		if (!ok) {
			return;
		}

		Result result = Services.get(CBSService.class).calculationBudget(cbsItem.get_id(), brui.getCurrentUserId());
		if (result.code == Result.CODE_SUCCESS) {
			Layer.message("��ĿԤ�����ύ");
		} else {
			MessageDialog.openError(shell, "�ύ��ĿԤ��", "��ĿԤ���ܶ�������Ԥ���ܶһ�£��޷��ύ��ĿԤ�㡣");
		}
		// BudgetSubject grid = (BudgetSubject)
		// context.getChildContextByName("cbssubject").getContent();

		// TODO ˢ�µ�ǰҳ��foot

		// TODO ˢ����һҳ��
	}

}
