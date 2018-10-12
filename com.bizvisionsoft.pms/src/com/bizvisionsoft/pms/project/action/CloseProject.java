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

public class CloseProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		final ObjectId id = project.get_id();
		ProjectService service = Services.get(ProjectService.class);

		CommandHandler.run(ICommand.Close_Project, //
				"��ȷ�Ϲر���Ŀ" + project + "��<br/>��Ŀ�رպ󽫽�ֹ���е���Ŀ�йز�����������Ŀ������㣬�����������Ŀ�ĵ�����������ʷ���ټ�¼���������", //
				"��Ŀ�ر����", "��Ŀ�ر�ʧ��", //
				() -> service.closeProject(brui.command(id, new Date(), ICommand.Close_Project)), //
				() -> service.closeProject(brui.command(id, new Date(), ICommand.Close_Project_Ignore_Warrning)), //
				code -> brui.switchPage("��Ŀ��ҳ���رգ�", id.toHexString()));

	}

}
