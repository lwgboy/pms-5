package com.bizvisionsoft.pms.obs.action;

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public abstract class AbstractChangeOBSItemMember {

	protected boolean checkDelete(IBruiService br, OBSItem obsItem) {
		List<Result> result = Services.get(OBSService.class).deleteProjectMemberCheck(obsItem.get_id(),
				"deleteobsitem");
		String type = obsItem.isRole() ? "��ɫ" : "�Ŷ�";
		boolean hasError = false;
		boolean hasWarning = false;
		String message = "";
		if (!result.isEmpty()) {
			for (Result r : result)
				if (Result.TYPE_ERROR == r.type) {
					message = "<span class='layui-badge'>����</span> " + type + ": " + obsItem + " �ĳ�Ա�й�����Ҫ�ƽ������ƽ����ٽ���ɾ����";
					hasError = true;
					break;
				} else if (Result.TYPE_WARNING == r.type) {
					message = "<span class='layui-badge layui-bg-orange'>����</span> ����Ŀ�����Ƴ�: " + obsItem + " ����ȡ����"
							+ type + "���г�Ա�Ĺ���������";
					hasWarning = true;
					break;
				}
		}
		if (hasError) {
			MessageDialog.openError(br.getCurrentShell(), "ɾ��" + type, message);
			return false;
		} else if (hasWarning) {
			if (!MessageDialog.openQuestion(br.getCurrentShell(), "ɾ��" + type, message + "<br>�Ƿ������"))
				return false;
			else {
				Services.get(OBSService.class).removeUnStartWorkUser(obsItem, br.getCurrentUserId());
			}

		}
		return true;
	}

	protected boolean checkChange(IBruiService br, ObjectId obsItem_id, ObjectId scope_id, String userId, String type,
			String title) {
		List<Result> result = Services.get(OBSService.class).deleteProjectMemberCheck(obsItem_id, type);
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
				MessageDialog.openError(br.getCurrentShell(), title, message);
				return false;
			} else if (hasWarning) {
				if (!MessageDialog.openQuestion(br.getCurrentShell(), title, message + "<br>�Ƿ������"))
					return false;
				else
					Services.get(WorkService.class).removeUnStartWorkUser(Arrays.asList(userId), scope_id,
							br.getCurrentUserId());
			}
		}
		return true;
	}
}
