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
		// 打开查询成本期间编辑器
		DateTimeInputDialog dt = new DateTimeInputDialog(br.getCurrentShell(), "设置期间", "请设置计划完成率查询期间", null,
				d -> d == null ? "必须选择时间" : null).setDateSetting(DateTimeSetting.year());
		if (dt.open() == DateTimeInputDialog.OK) {
			// 获取查询的成本期间
			String startPeriod = getPeriod(dt.getValue());
			OrganizationSARASM content1 = (OrganizationSARASM) context.getChildContextByAssemblyName("我管理的项目计划完成率组件").getContent();
			content1.setYear(startPeriod);
			content1.refresh();

			OrganizationSAR1ASM content2 = (OrganizationSAR1ASM) context.getChildContextByAssemblyName("我管理的一级计划完成率组件").getContent();
			content2.setYear(startPeriod);
			content2.refresh();

			OrganizationSAR2ASM content3 = (OrganizationSAR2ASM) context.getChildContextByAssemblyName("我管理的二级计划完成率组件").getContent();
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
