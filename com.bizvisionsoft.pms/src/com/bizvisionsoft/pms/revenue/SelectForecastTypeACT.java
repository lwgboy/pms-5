package com.bizvisionsoft.pms.revenue;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class SelectForecastTypeACT {
	
	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		ForecastASM grid = (ForecastASM) context.getChildContextByAssemblyName("项目收益预测").getContent();
		grid.selectType();
		grid.reset();
	}

}
