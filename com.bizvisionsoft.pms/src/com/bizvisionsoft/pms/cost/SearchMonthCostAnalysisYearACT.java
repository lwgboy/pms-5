package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

import com.bizivisionsoft.widgets.datetime.DateTimeSetting;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.DateTimeInputDialog;

@Deprecated
public class SearchMonthCostAnalysisYearACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		// �򿪲�ѯ�ɱ��ڼ�༭��
		DateTimeInputDialog dt = new DateTimeInputDialog(br.getCurrentShell(), "�����ڼ�", "������Ԥ��ɱ����·����ڼ�", null,
				d -> d == null ? "����ѡ��ʱ��" : null).setDateSetting(DateTimeSetting.year());
		if (dt.open() == DateTimeInputDialog.OK) {
			// ��ȡ��ѯ�ĳɱ��ڼ�
			String startPeriod = getPeriod(dt.getValue());
			MonthCostCompositionAnalysisASM content = (MonthCostCompositionAnalysisASM) context
					.getChildContextByAssemblyName("Ԥ��ɱ����·������").getContent();
			content.setYear(startPeriod);
			content.refresh();
		}

	}

	private String getPeriod(Date date) {
		Calendar period = Calendar.getInstance();
		period.setTime(date);
		String result = "" + period.get(Calendar.YEAR);
		return result;
	}
}
