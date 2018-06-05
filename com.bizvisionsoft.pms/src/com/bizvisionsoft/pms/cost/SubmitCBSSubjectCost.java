package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubmitCBSSubjectCost {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		//TODO �жϵ�ǰʱ�����ڼ���ͬ�޷��ύ
		
		
		CBSItem cbsItem = (CBSItem) context.getInput();
		Date settlementDate = cbsItem.getSettlementDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(settlementDate);
		String id = "" + cal.get(Calendar.YEAR);
		id += String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1);
		if (MessageDialog.openConfirm(brui.getCurrentShell(), "�ύ�ڼ�ɱ�", "��ȷ���ύ��ǰ�ڼ䣨" + cal.get(Calendar.YEAR) + "��"
				+ String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1) + "��" + "���ĳɱ���\n�ύ���޷��޸ĸ��ڼ�ĳɱ����ݡ�")) {
			List<Result> result = Services.get(CBSService.class).submitCBSSubjectCost(cbsItem.getScope_id(), id);
			if (result.isEmpty()) {
				Layer.message("����ɵ�ǰ�ڼ䣨" + id + "���ĳɱ��ύ��");
				GridPart grid = (GridPart) context.getContent();
				grid.refreshAll();
				grid.getViewer().expandAll();
			}
		}
	}
}
