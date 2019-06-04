package com.bizvisionsoft.pms.obs.action;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.serviceconsumer.Services;

public class SwitchMember {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) OBSItem obs) {
		ObjectId org_id = obs.getOrg_id();
		if (org_id == null) {
			br.openContent("团队成员.gridassy", obs);
		} else {
			Organization org = Services.get(OrganizationService.class).get(org_id, br.getDomain());
			br.openContent("组织成员（浏览）.gridassy", org);
		}
	}

}
