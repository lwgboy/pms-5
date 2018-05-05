package com.bizvisionsoft.pms.user;

import java.util.List;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class UserQueryBuilder {

	@Inject
	@Exclude
	private IBruiService bruiService;

	@Inject
	@Exclude
	private IBruiContext context;

	@WriteValue
	public Object userId;

	@WriteValue
	public Object name;

	public Object org_id;

	@WriteValue
	public Object activated;
	
	@WriteValue("orgIds")
	public void setOrgIds(List<Organization> orgs) {
		if (orgs == null || orgs.isEmpty()) {
			org_id = null;
		} else {
			org_id = new BasicDBObject("$in", Util.getList(orgs, Organization::get_id));
		}
	}

	@WriteValue("orgText")
	public void setOrgText(Object orgText) {
		if (orgText==null) {
			org_id = null;
		} else {
			BasicDBObject condition = new Query().filter(new BasicDBObject("name",orgText)).bson();
			List<Organization> orgs = Services.get(OrganizationService.class).createDataSet(condition);
			org_id = new BasicDBObject("$in", Util.getList(orgs, Organization::get_id));
		}
	}

}
