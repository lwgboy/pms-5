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

public class SwitchReadonyMember {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) OBSItem obs) {
			ObjectId org_id = obs.getOrg_id();
			if (org_id == null) {
				bruiService.openContent("�Ŷӳ�Ա���鿴��", obs);
			} else {
				Organization org = Services.get(OrganizationService.class).get(org_id);
				bruiService.openContent("��֯��Ա�������", org);
			}
	}

}
