package com.bizvisionsoft.pms.project.dataset;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ProjectInfo {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private ProjectService service;

	private ObjectId project_id;

	@Init
	private void init() {
		project_id = ((Project) context.getRootInput()).get_id();
		service = Services.get(ProjectService.class);
	}

	@DataSet("list")
	public List<Project> data() {
		return service.createDataSet(new BasicDBObject("_id", project_id));
	}

}
