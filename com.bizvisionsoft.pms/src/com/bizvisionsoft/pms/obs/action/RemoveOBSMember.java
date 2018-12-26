package com.bizvisionsoft.pms.obs.action;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class RemoveOBSMember {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em, @MethodParam(Execute.CONTEXT) IBruiContext context) {
		if (br.confirm("移除", "请确认将要从项目团队中移除选择的成员。")) {
			OBSItem obsItem = (OBSItem) context.getInput();
			if (check(obsItem, (User) em)) {
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", obsItem.get_id()))
						.update(new BasicDBObject("$pull", new BasicDBObject("member", ((User) em).getUserId()))).bson();
				Services.get(OBSService.class).update(fu);
				GridPart grid = (GridPart) context.getContent();
				grid.remove(em);
			}
		}
	}

	private boolean check(OBSItem obsItem, User user) {
		List<Result> result = Services.get(OBSService.class).deleteProjectMemberCheck(
				br.command(obsItem.get_id(), new Date(), ICommand.Remove_OBSItem_Member + "@" + user.getUserId()));
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
				MessageDialog.openError(br.getCurrentShell(), "移除团队成员", message);
				return false;
			} else if (hasWarning) {
				if (!MessageDialog.openQuestion(br.getCurrentShell(), "移除团队成员", message + "<br>是否继续？"))
					return false;
				else
					Services.get(WorkService.class).removeUnStartWorkUser(Arrays.asList(user.getUserId()), obsItem.getScope_id());

			}
		}

		return true;
	}
}
