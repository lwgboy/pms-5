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
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class DistributeStagePlan {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Work work = (Work) context.getRootInput();

		Shell s = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(s, "�´�׶μƻ�",
				"��ȷ���´�׶�" + work + "��ǰ�ļƻ���</p>ϵͳ��֪ͨ���������������ձ��ƻ�ִ�й�����</p>[��ʾ]δȷ�������˵Ĺ���������ȷ�������´�ƻ���");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(WorkService.class).distributeWorkPlan(work.get_id(),
				brui.getCurrentUserId());
		if (result.isEmpty()) {
			MessageDialog.openInformation(s, "�´�׶μƻ�", "�׶μƻ��´���ɡ�");
		} else {
			// TODO ��ʾ����������Ϣ��ͨ�÷���
			MessageDialog.openError(s, "�´�׶μƻ�", "�׶μƻ��´�ʧ�ܡ�</p>" + result.get(0).message);
		}
	}

}
