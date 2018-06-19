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
		// 打开查询成本期间编辑器
		DateTimeInputDialog dt = new DateTimeInputDialog(brui.getCurrentShell(), "设置期间", "请设置资金投入分析期间", null,
				d -> d == null ? "必须选择时间" : null).setDateSetting(DateTimeSetting.year());
		if (dt.open() == DateTimeInputDialog.OK) {
			// 获取查询的成本期间
			String startPeriod = getPeriod(dt.getValue());
			MonthCostIAASM content = (MonthCostIAASM) context.getChildContextByAssemblyName("资金投入分析组件")
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
