package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditGantt {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) IWBSScope rootInput) {
		Workspace workspace = rootInput.getWorkspace();
		if (workspace != null) {
			// ��ʾ�༭��
			String checkoutUserId = workspace.getCheckoutBy();
			if (checkoutUserId == null || "".equals(checkoutUserId) || brui.getCurrentUserId().equals(checkoutUserId)) {
				// ��������������ƣ�checkoutSchedulePlan������Ҫ�������£�
				Result result = Services.get(WorkSpaceService.class).checkout(workspace, brui.getCurrentUserId(),
						false);
				if (Result.CODE_WORK_SUCCESS == result.code) {
					brui.switchContent("��Ŀ����ͼ���༭��", workspace);
				} else if (Result.CODE_HASCHECKOUTSUB == result.code) {
					if (MessageDialog.openConfirm(brui.getCurrentShell(), "��ʾ",
							"���ƻ��е�  <b style='color: red;'>" + result.data.get("name")
									+ "</b>  ����������   <b style='color: red;'>" + result.data.get("username")
									+ "</b>  ���мƻ��༭��" + "������༭���ƻ������������û�δ�ύ�ļƻ���")) {
						result = Services.get(WorkSpaceService.class).checkout(workspace, brui.getCurrentUserId(),
								true);
						if (Result.CODE_WORK_SUCCESS == result.code) {
							brui.switchContent("��Ŀ����ͼ���༭��", workspace);
						}
					}
				}
			} else {
				brui.error("��ʾ", "�ƻ��Ѿ��������û������" + checkoutUserId);
			}
		}
	}

}
