package com.bizvisionsoft.pms.forecast;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class AddForecastPeriodACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		RevenueForecastASM grid = (RevenueForecastASM) context.getChildContextByAssemblyName("项目收益预测").getContent();
		grid.appendAmountColumn();
	}

}
