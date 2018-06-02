package com.bizvisionsoft.pms.project.action;

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

public class CloseProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "�ر���Ŀ",
				"��ȷ�Ϲر���Ŀ" + project + "��\n��Ŀ�رպ󣬽����ٽ��ܲ������ݵ��ύ���ĵ��Ĳ�����޸ġ�������¼����ʱ��Ϊ��Ŀ�ر�ʱ�䣬������Ŀ���Ա�����ر�֪ͨ��");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(ProjectService.class).closeProject(project.get_id(),
				brui.getCurrentUserId());
		if (result.isEmpty()) {
			Layer.message("��Ŀ�ѹرա�");
			brui.switchPage("��Ŀ��ҳ���رգ�", ((Project) project).get_id().toHexString());
		}
		// TODO ��ʾ����������Ϣ��ͨ�÷���
	}

}