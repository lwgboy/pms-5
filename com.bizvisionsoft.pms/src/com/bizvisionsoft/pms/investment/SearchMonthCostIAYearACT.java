package com.bizvisionsoft.pms.investment;

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

public class SearchMonthCostIAYearACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		// �򿪲�ѯ�ɱ��ڼ�༭��
		DateTimeInputDialog dt = new DateTimeInputDialog(brui.getCurrentShell(), "�����ڼ�", "�������ʽ�Ͷ������ڼ�", null,
				d -> d == null ? "����ѡ��ʱ��" : null).setDateSetting(DateTimeSetting.year());
		if (dt.open() == DateTimeInputDialog.OK) {
			// ��ȡ��ѯ�ĳɱ��ڼ�
			String startPeriod = getPeriod(dt.getValue());
			MonthCostIAASM content = (MonthCostIAASM) context.getChildContextByAssemblyName("�ʽ�Ͷ��������")
					.getContent();
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
