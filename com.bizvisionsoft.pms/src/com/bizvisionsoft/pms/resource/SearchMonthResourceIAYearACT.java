package com.bizvisionsoft.pms.resource;

import java.util.Calendar;
import java.util.Date;

import com.bizivisionsoft.widgets.datetime.DateTimeSetting;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.DateTimeInputDialog;

public class SearchMonthResourceIAYearACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		// 打开查询销售利润期间编辑器
		DateTimeInputDialog dt = new DateTimeInputDialog(brui.getCurrentShell(), "设置期间", "请设置销售利润分析期间", null,
				d -> d == null ? "必须选择时间" : null).setDateSetting(DateTimeSetting.year());
		if (dt.open() == DateTimeInputDialog.OK) {
			// 获取查询的成本期间
			String startPeriod = getPeriod(dt.getValue());
			DeptResourceAllAnalysisASM content = (DeptResourceAllAnalysisASM) context
					.getChildContextByAssemblyName("部门资源用量对比分析组件").getContent();
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
