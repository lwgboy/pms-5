package com.bizvisionsoft.serviceimpl;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.mongodb.BasicDBObject;

public class ProjectTemplateServiceImpl extends BasicServiceImpl implements ProjectTemplateService {

	@Override
	public ProjectTemplate insert(ProjectTemplate prjt) {
		return insert(prjt, ProjectTemplate.class);
	}

	@Override
	public ProjectTemplate get(ObjectId _id) {
		return get(_id, ProjectTemplate.class);
	}

	@Override
	public long count(BasicDBObject filter) {
		return count(filter, ProjectTemplate.class);
	}

	@Override
	public List<ProjectTemplate> createDataSet(BasicDBObject condition) {
		return createDataSet(condition,ProjectTemplate.class);
	}

}
