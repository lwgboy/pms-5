package com.bizvisionsoft.pms.obs.action;

import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.serviceconsumer.Services;

public class SwitchMember {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(em -> {
			OBSItem obs = (OBSItem) em;
			ObjectId org_id = obs.getOrg_id();
			if (org_id == null) {
				bruiService.openContent(bruiService.getAssembly("团队成员"), obs);
			} else {
				Organization org = Services.get(OrganizationService.class).get(org_id);
				bruiService.openContent(bruiService.getAssembly("组织成员（浏览）"), org);
			}
		});

	}

}
