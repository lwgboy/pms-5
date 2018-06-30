package com.bizvisionsoft.pms.projectchange;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubmitProjectChangeACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		ProjectChange input = (ProjectChange) context.getInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "�ύ�������", "��ȷ���ύ������롣\nϵͳ����¼����ʱ��Ϊ��������ύʱ�䣬�ύ��ñ�����뽫�޷������޸ġ�");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(ProjectService.class).submitProjectChange(Arrays.asList(input.get_id()));
		if (result.isEmpty()) {
			Layer.message("����������ύ��");
			brui.closeCurrentContent();
		} else {
			if (result.get(0).code == Result.CODE_PROJECTCHANGE_NOTASKUSER)
				Layer.message("��Ϊ������˻���ָ�������Ա���ٽ����ύ��", Layer.ICON_CANCEL);
		}
	}
}
