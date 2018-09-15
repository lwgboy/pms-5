package com.bizvisionsoft.service.sn;

import com.bizvisionsoft.annotations.md.mongocodex.IAutoGenerator;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.Project;

public class WorkOrderGenerator implements IAutoGenerator<Project> {

	@Override
	public Object generate(Project project, String name, String key, Class<?> t) {
		return ServicesLoader.get(ProjectService.class).generateWorkOrder(project.get_id());
	}

}
