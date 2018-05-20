package com.bizvisionsoft.pms.resource.dataset;

import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ResourceSelector {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@DataSet("选择人力资源/" + DataSet.LIST)
	public List<User> createDataSet(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.append("filter", filter);
		}
		filter.append("resourceType_id", new BasicDBObject("$ne", null));
		return Services.get(UserService.class).createDataSet(condition);
	}

	@DataSet("选择人力资源/" + DataSet.COUNT)
	public long count(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("resourceType_id", new BasicDBObject("$ne", null));
		return Services.get(UserService.class).count(filter);
	}

}
