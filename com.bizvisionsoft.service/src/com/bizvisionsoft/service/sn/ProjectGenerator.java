package com.bizvisionsoft.service.sn;

import com.bizvisionsoft.annotations.md.mongocodex.IAutoGenerator;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.Project;

public class ProjectGenerator implements IAutoGenerator<Project> {

	@Override
	public Object generate(Project project, String name, String key, Class<?> t) {
		int index = ServicesLoader.get(CommonService.class).generateCode(name, key);
		return String.format("%05d", index);
	}

}
