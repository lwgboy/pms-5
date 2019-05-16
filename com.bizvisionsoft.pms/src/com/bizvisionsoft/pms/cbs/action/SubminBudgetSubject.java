package com.bizvisionsoft.pms.cbs.action;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

@Deprecated
public class SubminBudgetSubject {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_INPUT_OBJECT) Object input,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Object root) {
		CBSItem cbsItem = null;
		if (input instanceof CBSItem)
			cbsItem = (CBSItem) input;

		if (cbsItem == null) {
			ICBSScope rootInput = (ICBSScope) root;
			cbsItem = Services.get(CBSService.class).get(rootInput.getCBS_id(), br.getDomain());
		}
		boolean ok = br.confirm("�ύ��ĿԤ��", "��ȷ���ύԤ��" + cbsItem + "��");
		if (!ok) {
			return;
		}

		Result result = Services.get(CBSService.class).calculationBudget(cbsItem.get_id(), br.getCurrentUserId(), br.getDomain());
		if (result.code == Result.CODE_SUCCESS) {
			Layer.message("��ĿԤ�����ύ");
		} else {
			br.error("�ύ��ĿԤ��", "��ĿԤ���ܶ�������Ԥ���ܶһ�£��޷��ύ��ĿԤ�㡣");
		}
		// BudgetSubject grid = (BudgetSubject)
		// context.getChildContextByName("cbssubject").getContent();

		// TODO ˢ�µ�ǰҳ��foot

		// TODO ˢ����һҳ��
	}

}
