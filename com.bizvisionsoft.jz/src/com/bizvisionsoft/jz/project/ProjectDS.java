package com.bizvisionsoft.jz.project;

import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ProjectDS {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@Init
	private void init() {
	}

	@DataSet("部门项目/" + DataSet.LIST)
	private List<Project> listDeptProject(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) String userid) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.put("projectType", "dept");
		return Services.get(ProjectService.class).listParticipatedProjects(condition, userid);
	}

	@DataSet("部门项目/" + DataSet.COUNT)
	private long countDeptProject(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) String userid) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.put("projectType", "dept");

		return Services.get(ProjectService.class).countParticipatedProjects(filter, userid);
	}

	@DataSet("外协项目/" + DataSet.LIST)
	private List<Project> listExternalProject(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) String userid) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.put("projectType", "external");
		return Services.get(ProjectService.class).listAllProjects(condition, userid);
	}

	@DataSet("外协项目/" + DataSet.COUNT)
	private long countExternalProject(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) String userid) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.put("projectType", "external");

		return Services.get(ProjectService.class).countAllProjects(filter, userid);
	}
}
