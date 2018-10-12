package com.bizvisionsoft.pms.revenue;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class AddForecastPeriodACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		ForecastASM grid = (ForecastASM) context.getChildContextByAssemblyName("��Ŀ����Ԥ��").getContent();
		grid.appendAmountColumn();
	}

}
