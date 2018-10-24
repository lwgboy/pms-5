package com.bizvisionsoft.pms.project.action;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.CommandHandler;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class FinishProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Project project) {
		final ObjectId id = project.get_id();
		ProjectService service = Services.get(ProjectService.class);

		CommandHandler.run(ICommand.Finish_Project, //
				"��ȷ����β��Ŀ" + project + "��<br/>��β�е���Ŀ�������޸ļƻ��������µĹ�������Ŀ�����<br/>��β�ڼ��Կɽ�����Ŀ������㣬������Ŀ�ĵ���", //
				"��Ŀ��β���", "��Ŀ��βʧ��", //
				() -> service.finishProject(brui.command(id, new Date(), ICommand.Finish_Project)), //
				() -> service.finishProject(brui.command(id, new Date(), ICommand.Finish_Project_Ignore_Warrning)), //
				code -> brui.switchPage("��Ŀ��ҳ����β��", id.toHexString()));

	}

}
