package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubmitCBSSubjectCostACT {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		// ��ȡ��ǰ��ʾ��CBSItem������ҳ��ʱ��contextInput�л�ȡ����Ŀ���׶δ�ʱ��contextRootInput�л�ȡ
		Object input = context.getInput();
		if (input == null) {
			input = context.getRootInput();
			if (input instanceof ICBSScope) {
				ICBSScope icbsScope = (ICBSScope) input;
				input = Services.get(CBSService.class).get(icbsScope.getCBS_id());
			}
		}

		if (input instanceof CBSItem) {
			CBSItem cbsItem = (CBSItem) input;
			// ��ȡ�༭�ĳɱ��ڼ�
			Date settlementDate = cbsItem.getNextSettlementDate();
			Calendar cal = Calendar.getInstance();
			int newYear = cal.get(Calendar.YEAR);
			int newMonth = cal.get(Calendar.MONTH);
			cal.setTime(settlementDate);
			// �༭�ĳɱ��ڼ�Ϊ��ǰ����ʱ����ֹ�ύ��
			if (cal.get(Calendar.YEAR) == newYear && cal.get(Calendar.MONTH) == newMonth) {
				cal.add(Calendar.MONTH, -1);
				Layer.message(
						"��ֹ�ظ��ύ�ڼ䣨" + cal.get(Calendar.YEAR) + "��"
								+ String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1) + "��" + "���ɱ���",
						Layer.ICON_CANCEL);
				return;
			}
			// �ύ��ǰ�ڼ�ɱ�
			// cal.set(Calendar.DATE, 1);
			// cal.set(Calendar.HOUR, 0);
			// cal.set(Calendar.MINUTE, 0);
			// cal.set(Calendar.SECOND, 0);
			// cal.set(Calendar.MILLISECOND, 0);
			// cal.add(Calendar.MILLISECOND, -1);

			if (MessageDialog.openConfirm(brui.getCurrentShell(), "ȷ���ڼ�ɱ�",
					"��ȷ���ύ��ǰ�ڼ䣨" + cal.get(Calendar.YEAR) + "��"
							+ String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1) + "��"
							+ "���ĳɱ���\n�ύ���޷��޸ĸ��ڼ�ĳɱ����ݡ�")) {
				List<Result> result = Services.get(CBSService.class).submitCBSSubjectCost(cal.getTime(),
						cbsItem.getScope_id());
				if (result.isEmpty()) {
					Layer.message("����ɵ�ǰ�ڼ䣨" + cal.get(Calendar.YEAR) + "��"
							+ String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1) + "��" + "���ĳɱ��ύ");
					GridPart grid = (GridPart) context.getContent();
					grid.refreshAll();
					grid.getViewer().expandAll();
				} else {
					for (Result r : result)
						if (Result.CODE_CBS_REPEATSUBMIT == r.code) {
							Layer.message("��ֹ�ظ��ύ�ڼ䣨" + cal.get(Calendar.YEAR) + "��"
									+ String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1) + "��" + "���ɱ���",
									Layer.ICON_CANCEL);
							return;
						}
				}
			}
		}
	}

}
