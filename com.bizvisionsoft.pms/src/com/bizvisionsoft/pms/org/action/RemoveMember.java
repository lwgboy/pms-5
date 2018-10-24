package com.bizvisionsoft.pms.org.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class RemoveMember {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) User user,
			@MethodParam(Execute.CONTEXT_CONTENT) GridPart grid) {
		if (br.confirm("移除", "请确认将要从组织中移除选择的成员。")) {
			try {
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("userId", user.getUserId()))
						.set(new BasicDBObject("org_id", null)).bson();
				Services.get(UserService.class).update(fu);
				grid.remove(grid.getParentElement(user), user);
			} catch (Exception e) {
				br.error("移除", e.getMessage());
				MessageDialog.openError(br.getCurrentShell(), "移除", e.getMessage());
			}
		}

	}

}
