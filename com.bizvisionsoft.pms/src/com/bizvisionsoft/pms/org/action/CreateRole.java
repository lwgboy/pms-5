package com.bizvisionsoft.pms.org.action;

import org.bson.types.ObjectId;

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
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		ObjectId org_id = ((Organization) context.getInput()).get_id();
		Role role = br.newInstance(Role.class).setOrg_id(org_id);

		new Editor<Role>(br.getAssembly("角色编辑器"), context).setTitle("创建角色")

				.setInput(role)

				.ok((r, t) -> {
					Role newRole = Services.get(OrganizationService.class).insertRole(t, br.getDomain());
					GridPart grid = (GridPart) context.getContent();
					grid.insert(newRole);
				});
	}

}
