package com.bizvisionsoft.pms.obs.action;

import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.TreePart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class DeleteOBSItem {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em, @MethodParam(Execute.CONTEXT) IBruiContext context) {
		if (MessageDialog.openConfirm(br.getCurrentShell(), "删除", "请确认将要删除 " + em)) {
			if (check((OBSItem) em)) {
				TreePart part = (TreePart) context.getContent();
				part.doDelete(em);
			}
		}
	}

	private boolean check(OBSItem obsItem) {
		List<Result> result = Services.get(OBSService.class)
				.deleteProjectMemberCheck(br.command(obsItem.get_id(), new Date(), ICommand.Remove_OBSItem));
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
		String type = obsItem.isRole() ? "角色" : "团队";
		if (hasError) {
			MessageDialog.openError(br.getCurrentShell(), "删除" + type,
					"<span class='layui-badge'>错误</span> " + type + ": " + obsItem + " 的成员有工作需要移交，请移交后再进行删除。");
			return false;
		} else if (hasWarning) {
			if (!MessageDialog.openQuestion(br.getCurrentShell(), "删除" + type,
					"<span class='layui-badge layui-bg-orange'>警告</span> 从项目组中移除: " + obsItem + " 。将取消该" + type + "所有成员的工作任命。 <br>是否继续？"))
				return false;
			else {
				Services.get(OBSService.class).removeUnStartWorkUser(obsItem, br.getCurrentUserId());
			}

		}

		return true;
	}
}
