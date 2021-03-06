package com.bizvisionsoft.pms.resource;

import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ResourceSelectorDS {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	@DataSet("选择人力资源/" + DataSet.LIST)
	public List<User> createDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.append("filter", filter);
		}
		filter.append("resourceType_id", new BasicDBObject("$ne", null));
		return Services.get(UserService.class).createDataSet(condition, br.getDomain());
	}

	@DataSet("选择人力资源/" + DataSet.COUNT)
	public long count(@MethodParam(MethodParam.FILTER) BasicDBObject filter) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("resourceType_id", new BasicDBObject("$ne", null));
		return Services.get(UserService.class).count(filter, br.getDomain());
	}
	//TODO

}
