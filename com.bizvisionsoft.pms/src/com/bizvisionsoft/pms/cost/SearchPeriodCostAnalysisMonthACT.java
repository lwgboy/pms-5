package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.datetime.DateTimeSetting;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.DateTimeInputDialog;

public class SearchPeriodCostAnalysisMonthACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		// �򿪲�ѯ�ɱ��ڼ�༭��
		DateTimeInputDialog dtid = new DateTimeInputDialog(bruiService.getCurrentShell(), "�����ڼ�", "�����óɱ���ɷ����ڼ�",
				(a, b) -> (a == null || b == null) ? "����ѡ��ʱ��" : null)
						.setDateSetting(DateTimeSetting.month().setRange(true));
		if (dtid.open() == DateTimeInputDialog.OK) {
			Date[] range = dtid.getValues();
			// ��ȡ��ѯ�ĳɱ��ڼ�
			String startPeriod = getPeriod(range[0]);
			String endPeriod = getPeriod(range[1]);

			PeriodCostCompositionAnalysisASM content = (PeriodCostCompositionAnalysisASM) context
					.getChildContextByAssemblyName("�ɱ���ɷ������-�ڼ�").getContent();

			content.setStartPeriod(startPeriod);
			content.setEndPeriod(endPeriod);
			content.refresh();
		}

	}

	private String getPeriod(Date date) {
		Calendar period = Calendar.getInstance();
		period.setTime(date);
		String result = "" + period.get(Calendar.YEAR);
		result += String.format("%02d", period.get(java.util.Calendar.MONTH) + 1);
		return result;
	}
}
