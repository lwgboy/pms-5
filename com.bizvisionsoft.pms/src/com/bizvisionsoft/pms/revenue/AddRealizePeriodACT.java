package com.bizvisionsoft.pms.revenue;

import java.util.Calendar;
import java.util.Date;

import com.bizivisionsoft.widgets.datetime.DateTimeSetting;
import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.DateTimeInputDialog;
import com.bizvisionsoft.service.model.Project;

public class AddRealizePeriodACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Date as = context.getRootInput(Project.class, false).getActualStart();
		if (as == null) {
			Layer.message("项目尚未开始", Layer.ICON_CANCEL);
			return;
		}
		// 设置为当月第一天
		final Calendar cal = Calendar.getInstance();
		cal.setTime(as);
		cal.set(Calendar.DATE, 0);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		RealizeASM grid = (RealizeASM) context.getChildContextByAssemblyName("项目收益实现").getContent();
		DateTimeInputDialog input = new DateTimeInputDialog(br.getCurrentShell(), "收益录入", "请选择期间", new Date(),
				d -> (d != null && d.before(cal.getTime())) ? "收益期间不能早于项目实际开始日期" : null)
						.setDateSetting(DateTimeSetting.yearMonth());
		if (DateTimeInputDialog.OK != input.open()) {
			return;
		}
		grid.appendAmountColumn(input.getValue());
	}

}
