package com.bizvisionsoft.pms.stage.action;

import java.util.Date;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.CommandHandler;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class StartStage {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Work stage = context.getRootInput(Work.class, false);
		CommandHandler.run(ICommand.Start_Stage, //
				"��ȷ�������׶Σ�" + stage + "��", "�׶��������", "�׶�����ʧ��", //
				() -> Services.get(WorkService.class)
						.startStage(brui.command(stage.get_id(), new Date(), ICommand.Start_Stage)), //
				code -> brui.switchPage("�׶���ҳ��ִ�У�", ((Work) stage).get_id().toHexString()));
	}

}
