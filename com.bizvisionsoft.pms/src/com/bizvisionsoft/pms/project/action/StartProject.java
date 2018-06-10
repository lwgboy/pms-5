package com.bizvisionsoft.pms.project.action;

import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class StartProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "������Ŀ",
				"��ȷ��������Ŀ" + project + "��\nϵͳ����¼����ʱ��Ϊ��Ŀ����ʱ�䣬������Ŀ���Ա��������֪ͨ��");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(ProjectService.class).startProject(brui.command(project.get_id(),new Date()));
		if (result.isEmpty()) {
			Layer.message("��Ŀ��������");
			brui.switchPage("��Ŀ��ҳ��ִ�У�", ((Project) project).get_id().toHexString());
		}
		// TODO ��ʾ����������Ϣ��ͨ�÷���
	}

}
