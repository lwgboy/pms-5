package com.bizvisionsoft.pms.obs.action;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.TreePart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class DeleteOBSItem {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em, @MethodParam(Execute.CONTEXT) IBruiContext context) {
		if (MessageDialog.openConfirm(br.getCurrentShell(), "ɾ��", "��ȷ�Ͻ�Ҫɾ�� " + em)) {
			if (check((OBSItem) em)) {
				TreePart part = (TreePart) context.getContent();
				part.doDelete(em);
			}
		}
	}

	private boolean check(OBSItem obsItem) {
		List<String> userIds = Services.get(OBSService.class).getAllSubOBSItemMember(obsItem.get_id());
		if (userIds.size() > 0) {
			//TODO �������ɾ�����ڵ�ʱ���޷���鵽�ӽڵ㡣
			List<Result> result = Services.get(OBSService.class).deleteProjectMemberCheck(userIds, obsItem.getScope_id(), obsItem.get_id());
			boolean hasError = false;
			boolean hasWarning = false;
			String message = "";
			if (!result.isEmpty()) {
				for (Result r : result)
					if (Result.TYPE_ERROR == r.type) {
						hasError = true;
						message += "<span class='layui-badge'>����</span> " + r.message + "<br>";
					} else if (Result.TYPE_WARNING == r.type) {
						hasWarning = true;
						message += "<span class='layui-badge layui-bg-orange'>����</span> " + r.message + "<br>";
					} else {
						message += "<span class='layui-badge layui-bg-blue'>��Ϣ</span> " + r.message + "<br>";
					}
			}
			if (!message.isEmpty()) {
				if (hasError) {
					MessageDialog.openError(br.getCurrentShell(), "�Ƴ��Ŷӳ�Ա", message);
					return false;
				} else if (hasWarning) {
					if (!MessageDialog.openQuestion(br.getCurrentShell(), "�Ƴ��Ŷӳ�Ա", message + "<br>�Ƿ������"))
						return false;
					else
						Services.get(WorkService.class).removeUnStartWorkUser(userIds, obsItem.getScope_id());

				}
			}
		}

		return true;
	}
}
