package com.bizvisionsoft.pms.org.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateSubOrg {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(em -> {
			if (em instanceof Organization) {
				Organization org = br.newInstance(Organization.class);
				org.setParentId(((Organization) em).get_id());

				new Editor<Organization>(br.getAssembly("组织编辑器"), context).setTitle("创建组织")

						.setInput(org)

						.ok((r, t) -> {
							Organization result = Services.get(OrganizationService.class).insert(t, br.getDomain());
							GridPart grid = (GridPart) context.getContent();
							grid.add(em, result);
						});
			}
		});
	}

}
