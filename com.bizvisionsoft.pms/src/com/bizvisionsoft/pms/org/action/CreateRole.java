package com.bizvisionsoft.pms.org.action;

import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.Role;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateRole {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		ObjectId org_id = ((Organization) context.getInput()).get_id();
		Role role = new Role().setOrg_id(org_id);

		new Editor<Role>(bruiService.getAssembly("角色编辑器"), context).setTitle("创建角色")

				.setInput(role)

				.ok((r, t) -> {
					Role newRole = Services.get(OrganizationService.class).insertRole(t);
					GridPart grid = (GridPart) context.getContent();
					grid.insert(newRole);
				});
	}

}
