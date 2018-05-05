package com.bizvisionsoft.pms.org.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class RemoveMember {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(elem -> {
			if (elem instanceof User) {
				if (MessageDialog.openConfirm(bruiService.getCurrentShell(), "移除", "请确认将要从组织中移除选择的成员。")) {
					try {
						BasicDBObject fu = new FilterAndUpdate()
								.filter(new BasicDBObject("userId", ((User) elem).getUserId()))
								.set(new BasicDBObject("org_id", null)).bson();
						Services.get(UserService.class).update(fu);

						GridPart grid = (GridPart) context.getContent();
						grid.remove(grid.getParentElement(elem),elem);
					} catch (Exception e) {
						MessageDialog.openError(bruiService.getCurrentShell(), "移除", e.getMessage());
					}
				}
			}
		});

	}

}
