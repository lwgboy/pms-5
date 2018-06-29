package com.bizvisionsoft.pms.risk;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.ChartPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class MonteCarloSimulateACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		IInputValidator v = new IInputValidator() {

			@Override
			public String isValid(String newText) {
				try {
					int value = Integer.parseInt(newText);
					if (value <= 10000 && value > 0) {
						return null;
					}
				} catch (Exception e) {
				}
				return "请输入大于0小于等于10000的整数次数。";
			}
		};
		InputDialog id = new InputDialog(brui.getCurrentShell(), "运行蒙特卡洛模拟", "请输入模拟次数", "2000", v);
		if (Window.OK == id.open()) {
			String times = id.getValue();
			Services.get(RiskService.class).monteCarloSimulate(context.getRootInput(Project.class, false).get_id(),
					Integer.parseInt(times));
			ChartPart chart = (ChartPart) context.getContent();
			chart.setViewerInput();
		}
	}

}
