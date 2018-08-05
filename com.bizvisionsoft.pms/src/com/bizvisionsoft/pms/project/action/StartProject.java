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
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "��Ŀ����",
				"��ȷ����Ŀ����" + project + "��</p>ϵͳ����¼����ʱ��Ϊ��Ŀ����ʱ�䣬������Ŀ���Ա��������֪ͨ��");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(ProjectService.class)
				.startProject(brui.command(project.get_id(), new Date()));
		boolean hasError = false;
		boolean hasWarning = false;

		String message = "";
		if (!result.isEmpty()) {
			for (Result r : result)
				if (Result.TYPE_ERROR == r.type) {
					hasError = true;
					message += "����" + r.message + "<br>";
				} else if (Result.TYPE_WARNING == r.type) {
					hasError = true;
					message += "���棺" + r.message + "<br>";
				} else {
					message += "��Ϣ��" + r.message + "<br>";
				}
		}

		if (message.isEmpty()) {
			Layer.message("��Ŀ������");
		} else {
			if (hasError) {
				MessageDialog.openError(shell, "��Ŀ����", message);
				return;
			} else if (hasWarning) {
				MessageDialog.openWarning(shell, "��Ŀ����", "��Ŀ����������ע��������ʾ��Ϣ��<br>" + message);
			} else {
				MessageDialog.openInformation(shell, "��Ŀ����", "��Ŀ����������ע��������ʾ��Ϣ��<br>" + message);
			}
		}

		brui.switchPage("��Ŀ��ҳ��ִ�У�", ((Project) project).get_id().toHexString());

	}

}
