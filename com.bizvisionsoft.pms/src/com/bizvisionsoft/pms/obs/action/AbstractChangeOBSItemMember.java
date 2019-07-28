package com.bizvisionsoft.pms.obs.action;

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.InformationDialog;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public abstract class AbstractChangeOBSItemMember {

	protected boolean checkDelete(IBruiService br, OBSItem obsItem) {
		List<Result> result = Services.get(OBSService.class).deleteProjectMemberCheck(obsItem.get_id(), "deleteobsitem", br.getDomain());
		String type = obsItem.isRole() ? "角色" : "团队";
		boolean hasError = false;
		boolean hasWarning = false;
		if (!result.isEmpty()) {
			for (Result r : result)
				if (Result.TYPE_ERROR == r.type) {
					hasError = true;
					break;
				} else if (Result.TYPE_WARNING == r.type) {
					hasWarning = true;
					break;
				}
		}
		if (hasError) {
			InformationDialog.openInfo(br.getCurrentShell(), "删除" + type, "删除" + type, result);
			return false;
		} else if (hasWarning) {
			if (IDialogConstants.OK_ID != InformationDialog.openConfirm(br.getCurrentShell(), "删除" + type, "删除" + type, result))
				return false;
			else {
				Services.get(OBSService.class).removeUnStartWorkUser(obsItem, br.getCurrentUserId(), br.getDomain());
			}

		}
		return true;
	}

	protected boolean checkChange(IBruiService br, ObjectId obsItem_id, ObjectId scope_id, String userId, String type, String title) {
		List<Result> result = Services.get(OBSService.class).deleteProjectMemberCheck(obsItem_id, type, br.getDomain());
		boolean hasError = false;
		boolean hasWarning = false;
		String message = "";
		if (!result.isEmpty()) {
			for (Result r : result)
				if (Result.TYPE_ERROR == r.type) {
					hasError = true;
					message += "<span class='layui-badge'>错误</span> " + r.message + "<br>";
				} else if (Result.TYPE_WARNING == r.type) {
					hasWarning = true;
					message += "<span class='layui-badge layui-bg-orange'>警告</span> " + r.message + "<br>";
				} else {
					message += "<span class='layui-badge layui-bg-blue'>信息</span> " + r.message + "<br>";
				}
		}
		if (!message.isEmpty()) {
			if (hasError) {
				MessageDialog.openError(br.getCurrentShell(), title, message + " 存在以下问题需要解决。");
				return false;
			} else if (hasWarning) {
				if (!MessageDialog.openQuestion(br.getCurrentShell(), title, message + "指定担任者存在以下问题。<br>是否继续？"))
					return false;
				else
					Services.get(WorkService.class).removeUnStartWorkUser(Arrays.asList(userId), scope_id, br.getCurrentUserId(),
							br.getDomain());
			}
		}
		return true;
	}
}
