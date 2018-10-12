package com.bizvisionsoft.pms.stage.action;

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
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

@Deprecated
public class FinishStage {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) Event event) {
		Work stage = (Work) context.getRootInput();
		Shell shell = brui.getCurrentShell();

		Project project = stage.getProject();
		if (project != null && ProjectStatus.Processing.equals(project.getStatus())) {

			boolean ok = MessageDialog.openConfirm(shell, "�깤�׶�",
					"��ȷ���깤�׶�" + stage + "��\nϵͳ����¼����ʱ��Ϊ�׶ε��깤ʱ�䣬���򱾽׶���Ŀ���Ա�����깤֪ͨ��");
			if (!ok) {
				return;
			}
			List<Result> result = Services.get(WorkService.class)
					.finishStage(brui.command(stage.get_id(), new Date(), ICommand.Finish_Stage));
			boolean b = true;
			String message = "";
			if (!result.isEmpty()) {
				for (Result r : result)
					if (Result.TYPE_ERROR == r.type) {
						Layer.message(r.message, Layer.ICON_CANCEL);
						b = false;
					} else {
						message += r.message + "<br>";
					}
			}

			if (b) {
				message = "�׶����깤<br>" + message;
				Layer.message(message);
				brui.switchPage("�׶���ҳ����β��", ((Work) stage).get_id().toHexString());
			}
			// TODO ��ʾ����������Ϣ��ͨ�÷���
		} else {
			MessageDialog.openError(shell, "�깤�׶�", "�׶�������Ŀδ�������޷��깤�׶Ρ�");
		}
	}

}
