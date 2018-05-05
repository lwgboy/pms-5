package com.bizvisionsoft.pms.org.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Role;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class AddUserToRole {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {

		context.selected(em -> {
			if (em instanceof Role) {
				new Selector(bruiService.getAssembly("用户选择器"), context).setTitle("选择用户添加为组织角色").open(r -> {
					final List<String> ids = new ArrayList<String>();
					final List<User> users = new ArrayList<User>();
					GridPart grid = (GridPart) context.getContent();
					List<?> input = (List<?>) grid.getViewerInput();
					r.forEach(a -> {
						if (!input.contains(a)) {
							ids.add(((User) a).getUserId());
							users.add((User) a);
						}
					});
					if (!ids.isEmpty()) {
						BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", ((Role) em).get_id()))
								.update(new BasicDBObject("$addToSet",
										new BasicDBObject("users", new BasicDBObject("$each", ids))))
								.bson();
						Services.get(OrganizationService.class).updateRole(fu);
						grid.refresh(em);
					}
				});
			}
		});

	}

}
