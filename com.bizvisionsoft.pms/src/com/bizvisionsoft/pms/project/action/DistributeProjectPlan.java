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
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class DistributeProjectPlan {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Object rootInput = context.getRootInput();
		if (rootInput instanceof Project) {
			Project project = (Project) rootInput;

			// �����Ŀ�ǰ��׶��ƽ���
			if (project.isStageEnable()) {
				Shell s = brui.getCurrentShell();
				boolean ok = MessageDialog.openConfirm(s, "�´���Ŀ�ƻ�",
						"��ȷ���´���Ŀ" + project + "��ǰ�ƻ���</p>ϵͳ��֪ͨ���׶θ��������ձ��ƻ��ƶ���ϸ�ƻ���</p>[��ʾ]δȷ�������˵Ľ׶Σ�����ȷ�������´�ƻ���");
				if (!ok) {
					return;
				}
				List<Result> result = Services.get(ProjectService.class)
						.distributeProjectPlan(brui.command(project.get_id(), new Date()));
				if (result.isEmpty()) {
					Layer.message("��Ŀ�ƻ����´");
				} else {
					// TODO ��ʾ����������Ϣ��ͨ�÷���
					MessageDialog.openError(s, "�´���Ŀ�ƻ�", "��Ŀ�ƻ��´�ʧ�ܡ�</p>" + result.get(0).message);
				}
			} else {
				// TODO ���ǰ��׶��ƽ����´�
			}
		} else if (rootInput instanceof Work) {
			Work work = (Work) rootInput;

			Shell s = brui.getCurrentShell();
			boolean ok = MessageDialog.openConfirm(s, "�´�׶μƻ�",
					"��ȷ���´�׶�" + work + "��ǰ�ļƻ���</p>ϵͳ��֪ͨ���������������ձ��ƻ�ִ�й�����</p>[��ʾ]δȷ�������˵Ĺ���������ȷ�������´�ƻ���");
			if (!ok) {
				return;
			}
			List<Result> result = Services.get(WorkService.class)
					.distributeWorkPlan(brui.command(work.get_id(), new Date()));
			if (result.isEmpty()) {
				Layer.message("�׶μƻ����´");
			} else {
				// TODO ��ʾ����������Ϣ��ͨ�÷���
				MessageDialog.openError(s, "�´�׶μƻ�", "�׶μƻ��´�ʧ�ܡ�</p>" + result.get(0).message);
			}
		}
	}

}
