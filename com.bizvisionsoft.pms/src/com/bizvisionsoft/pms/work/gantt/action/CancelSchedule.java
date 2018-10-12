package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;

public class CancelSchedule {
	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) Event event) {
		IWBSScope rootInput = (IWBSScope) context.getRootInput();
		if (rootInput != null) {
			if (MessageDialog.openConfirm(bruiService.getCurrentShell(), "�����༭", "��ȷ�ϳ�����ǰ�༭��")) {
				Workspace workspace = rootInput.getWorkspace();
				Result result = Services.get(WorkSpaceService.class).cancelCheckout(workspace);
				if (Result.CODE_WORK_SUCCESS == result.code) {
					Layer.message("��ǰ�ƻ����޸��ѳ�����");
					bruiService.switchContent("��Ŀ����ͼ", null);
				}
			}
		}
	}
}
