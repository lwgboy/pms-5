package com.bizvisionsoft.pms.stage.action;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class StartStage {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Work stage = (Work) context.getRootInput();
		Shell shell = brui.getCurrentShell();

		Project project = stage.getProject();
		if (project != null && ProjectStatus.Processing.equals(project.getStatus())) {

			boolean ok = MessageDialog.openConfirm(shell, "�����׶�",
					"��ȷ�������׶�" + stage + "��\nϵͳ����¼����ʱ��Ϊ�׶ε�ʵ�ʿ�ʼʱ�䣬���򱾽׶���Ŀ���Ա��������֪ͨ��");
			if (!ok) {
				return;
			}
			List<Result> result = Services.get(WorkService.class).start(stage.get_id(), brui.getCurrentUserId());
			if (result.isEmpty()) {
				MessageDialog.openInformation(shell, "�����׶�", "�׶�������ɡ�");

				brui.switchPage("�׶���ҳ��ִ�У�", ((Work) stage).get_id().toHexString());
			}
			// TODO ��ʾ����������Ϣ��ͨ�÷���
		} else {
			MessageDialog.openError(shell, "�����׶�", "�׶�������Ŀδ�������޷������׶Ρ�");
		}
	}

}
