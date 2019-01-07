package com.bizvisionsoft.pms.org;

import java.util.Calendar;
import java.util.Date;

import com.bizivisionsoft.widgets.datetime.DateTimeSetting;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.DateTimeInputDialog;

public class SearchOrganizationSARACT {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		// �򿪲�ѯ�ɱ��ڼ�༭��
		DateTimeInputDialog dt = new DateTimeInputDialog(br.getCurrentShell(), "�����ڼ�", "�����üƻ�����ʲ�ѯ�ڼ�", null,
				d -> d == null ? "����ѡ��ʱ��" : null).setDateSetting(DateTimeSetting.year());
		if (dt.open() == DateTimeInputDialog.OK) {
			// ��ȡ��ѯ�ĳɱ��ڼ�
			String startPeriod = getPeriod(dt.getValue());
			OrganizationSARASM content1 = (OrganizationSARASM) context.getChildContextByAssemblyName("�ҹ������Ŀ�ƻ���������").getContent();
			content1.setYear(startPeriod);
			content1.refresh();

			OrganizationSAR1ASM content2 = (OrganizationSAR1ASM) context.getChildContextByAssemblyName("�ҹ����һ���ƻ���������").getContent();
			content2.setYear(startPeriod);
			content2.refresh();

			OrganizationSAR2ASM content3 = (OrganizationSAR2ASM) context.getChildContextByAssemblyName("�ҹ���Ķ����ƻ���������").getContent();
			content3.setYear(startPeriod);
			content3.refresh();
		}

	}

	private String getPeriod(Date date) {
		Calendar period = Calendar.getInstance();
		period.setTime(date);
		String result = "" + period.get(Calendar.YEAR);
		return result;
	}
}
