package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditGantt {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Workspace workspace = null;
		Object rootInput = context.getRootInput();
		if (rootInput instanceof Project) {
			workspace = ((Project) rootInput).getWorkspace();
		} else if (rootInput instanceof Work) {
			workspace = ((Work) rootInput).getWorkspace();
		}
		if (workspace != null) {
			// ��ʾ�༭��
			String checkOutUserId = workspace.getCheckoutBy();
			if (checkOutUserId == null || "".equals(checkOutUserId) || brui.getCurrentUserId().equals(checkOutUserId)) {
				// ��������������ƣ�checkOutSchedulePlan������Ҫ�������£�
				Result result = Services.get(WorkSpaceService.class).checkout(workspace, brui.getCurrentUserId(),
						false);
				if (Result.CODE_SUCCESS == result.code) {
					brui.switchContent("��Ŀ����ͼ(�༭)", workspace);
				} else if (Result.CODE_HASCHECKOUTSUB == result.code) {
					if (MessageDialog.openConfirm(brui.getCurrentShell(), "��ʾ", result.message + "��ȷ���Ƿ�ȡ���¼��ļ����")) {
						result = Services.get(WorkSpaceService.class).checkout(workspace, brui.getCurrentUserId(),
								true);
						if (Result.CODE_SUCCESS == result.code) {
							brui.switchContent("��Ŀ����ͼ(�༭)", workspace);
						}
					}
				}
			} else {
				MessageDialog.openError(brui.getCurrentShell(), "��ʾ", "��û�б༭�ƻ���Ȩ�ޡ�");
			}
		}
	}

}
