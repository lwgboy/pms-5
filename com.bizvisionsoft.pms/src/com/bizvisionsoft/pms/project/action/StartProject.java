package com.bizvisionsoft.pms.project.action;

import java.util.Date;

import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.ResultHandler;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class StartProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "��Ŀ����",
				"��ȷ��������Ŀ��" + project + "��<br/>ϵͳ����ǵ�ǰʱ��Ϊ��Ŀ�깤���ڡ�<br/>����������Ŀ���޷�ɾ����");
		if (!ok) {
			return;
		}
		/////////////////////////////////////////////////////////////////////////////
		// �����Ծ��棬������Ŀ
		final ObjectId id = project.get_id();
		ProjectService service = Services.get(ProjectService.class);

		ResultHandler.run(ICommand.Start_Project, "��Ŀ�������", "��Ŀ����ʧ��",
				() -> service.startProject(brui.command(id, new Date(), ICommand.Start_Project)),//
				() -> service.startProject(brui.command(id, new Date(), ICommand.Start_Project_Ignore_Warrning)), //
				code -> brui.switchPage("��Ŀ��ҳ��ִ�У�", id.toHexString()));

	}

}
