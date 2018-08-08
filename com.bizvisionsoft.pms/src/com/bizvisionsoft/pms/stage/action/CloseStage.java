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
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

@Deprecated
public class CloseStage {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		Work stage = (Work) context.getRootInput();
		Shell shell = brui.getCurrentShell();

		boolean ok = MessageDialog.openConfirm(shell, "�رս׶�",
				"��ȷ�Ϲرս׶�" + stage + "��\nϵͳ����¼����ʱ��Ϊ�׶εĹر�ʱ�䣬���򱾽׶���Ŀ���Ա�����ر�֪ͨ��");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(WorkService.class)
				.closeStage(brui.command(stage.get_id(), new Date(), ICommand.Close_Stage));
		if (result.isEmpty()) {
			Layer.message("�׶��ѹر�");
			brui.switchPage("�׶���ҳ���رգ�", ((Work) stage).get_id().toHexString());
		}
		// TODO ��ʾ����������Ϣ��ͨ�÷���
	}

}
