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

public class StartProject {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Project project) {
		/////////////////////////////////////////////////////////////////////////////
		// �����Ծ��棬������Ŀ
		final ObjectId id = project.get_id();
		ProjectService service = Services.get(ProjectService.class);

		CommandHandler.run(ICommand.Start_Project, //
				"��ȷ��������Ŀ��" + project + "��<br/>����������Ŀ���޷�ɾ����", "��Ŀ�������", "��Ŀ����ʧ��", //
				() -> service.startProject(br.command(id, new Date(), ICommand.Start_Project), br.getDomain()), //
				() -> service.startProject(br.command(id, new Date(), ICommand.Start_Project_Ignore_Warrning), br.getDomain()), //
				code -> br.switchPage("��Ŀ��ҳ��ִ�У�", id.toHexString()));

	}

}
