package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditGantt {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) IWBSScope rootInput) {
		Workspace workspace = rootInput.getWorkspace();
		if (workspace != null) {
			// ��ʾ�༭��
			String checkoutUserId = workspace.getCheckoutBy();
			if (checkoutUserId == null || "".equals(checkoutUserId) || br.getCurrentUserId().equals(checkoutUserId)) {
				// ��������������ƣ�checkoutSchedulePlan������Ҫ�������£�
				Result result = Services.get(WorkSpaceService.class).checkout(workspace, br.getCurrentUserId(), false, br.getDomain());
				if (Result.CODE_WORK_SUCCESS == result.code) {
					br.switchContent("��Ŀ����ͼ���༭��", workspace);
				} else if (Result.CODE_HASCHECKOUTSUB == result.code) {
					if (MessageDialog.openConfirm(br.getCurrentShell(), "��ʾ",
							"���ƻ��е�  <b style='color: red;'>" + result.data.get("name") + "</b>  ����������   <b style='color: red;'>"
									+ result.data.get("username") + "</b>  ���мƻ��༭��" + "������༭���ƻ������������û�δ�ύ�ļƻ���")) {
						result = Services.get(WorkSpaceService.class).checkout(workspace, br.getCurrentUserId(), true, br.getDomain());
						if (Result.CODE_WORK_SUCCESS == result.code) {
							br.switchContent("��Ŀ����ͼ���༭��", workspace);
						}
					}
				}
			} else {
				User checkoutUser = Services.get(UserService.class).get(checkoutUserId, br.getDomain());
				br.error("��ʾ", checkoutUser + "�Ѿ�����ƻ����ڸüƻ������ȡ�������ǰ�����ܽ��б༭��");
			}
		}
	}

}
