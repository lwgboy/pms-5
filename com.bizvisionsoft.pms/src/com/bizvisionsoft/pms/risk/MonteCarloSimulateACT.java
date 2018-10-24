package com.bizvisionsoft.pms.risk;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.ChartPart;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class MonteCarloSimulateACT {

	@Inject
	private IBruiService br;

	@Execute
	private void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Project proj,
			@MethodParam(Execute.CONTEXT_CONTENT) ChartPart chart) {

		InputDialog id = new InputDialog(br.getCurrentShell(), "运行蒙特卡洛模拟", "请输入模拟次数", "2000", t-> {
			try {
				int value = Integer.parseInt(t);
				if (value <= 10000 && value > 0) {
					return null;
				}
			} catch (Exception e) {
			}
			return "请输入大于0小于等于10000的整数次数。";
		});
		
		if (Window.OK == id.open()) {
			Services.get(RiskService.class).monteCarloSimulate(proj.get_id(), Integer.parseInt(id.getValue()));
			chart.setViewerInput();
		}
	}

}
