package com.bizvisionsoft.pms.org.dataset;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.Role;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class OrganizationDataSet {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService bruiService;

	private Organization org;

	private OrganizationService service;

	@Init
	private void init() {
		org = (Organization) context.getInput();
		service = Services.get(OrganizationService.class);
	}

	@DataSet({"组织成员/" + DataSet.LIST,"组织成员（浏览）/"+ DataSet.LIST})
	public List<User> listMember(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		return service.getMember(condition, org.get_id());
	}

	@DataSet({"组织成员/" + DataSet.COUNT,"组织成员（浏览）/"+ DataSet.COUNT})
	public long countMember(@MethodParam(MethodParam.FILTER) BasicDBObject filter) {
		return service.countMember(filter, org.get_id());
	}

	@DataSet("组织角色/" + DataSet.LIST)
	public List<Role> listRole(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		return service.getRoles(condition, org.get_id());
	}

	@DataSet("组织角色/" + DataSet.COUNT)
	public long countRole(@MethodParam(MethodParam.FILTER) BasicDBObject filter) {
		return service.countRoles(filter, org.get_id());
	}

	@DataSet("组织角色/" + DataSet.DELETE)
	public long delete(@MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.PARENT_ID) ObjectId parent_id,
			@MethodParam(MethodParam.OBJECT) Object target) {
		if (target instanceof Role) {
			return service.deleteRole(_id);
		} else if (target instanceof User) {
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", parent_id))
					.update(new BasicDBObject("$pull", new BasicDBObject("users", ((User) target).getUserId())))
					.bson();
			return service.updateRole(fu);
		} else {
			return 0;
		}
	}

}
