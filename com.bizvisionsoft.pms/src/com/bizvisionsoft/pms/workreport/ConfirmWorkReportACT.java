package com.bizvisionsoft.pms.workreport;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.InfopadPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.serviceconsumer.Services;

public class ConfirmWorkReportACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {

		WorkReport input = (WorkReport) context.getInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "ȷ��" + input.getType(),
				"��ȷ�ϱ��棺" + input.getLabel() + "��\nϵͳ����¼����ʱ��Ϊ����ȷ��ʱ�䡣");
		if (!ok) {
			return;
		}

		List<Result> result = Services.get(WorkReportService.class).confirmWorkReport(Arrays.asList(input.get_id()),
				brui.getCurrentUserId());
		if (result.isEmpty()) {
			Layer.message("������ȷ��");
			InfopadPart ip = (InfopadPart) context.getChildContextByAssemblyName("�������������Ϣ���").getContent();
			ip.reload();
			brui.closeCurrentContent();
			brui.updateSidebarActionBudget("ȷ�ϱ���");
		}
	}
}
