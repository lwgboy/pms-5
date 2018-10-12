package com.bizvisionsoft.pms.project.action;

import java.util.Date;

import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.CommandHandler;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class StartProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) Event event) {
		/////////////////////////////////////////////////////////////////////////////
		// �����Ծ��棬������Ŀ
		Project project = context.getRootInput(Project.class, false);
		final ObjectId id = project.get_id();
		ProjectService service = Services.get(ProjectService.class);

		CommandHandler.run(ICommand.Start_Project, //
				"��ȷ��������Ŀ��" + project + "��<br/>����������Ŀ���޷�ɾ����", "��Ŀ�������", "��Ŀ����ʧ��", //
				() -> service.startProject(brui.command(id, new Date(), ICommand.Start_Project)), //
				() -> service.startProject(brui.command(id, new Date(), ICommand.Start_Project_Ignore_Warrning)), //
				code -> brui.switchPage("��Ŀ��ҳ��ִ�У�", id.toHexString()));

	}

}
