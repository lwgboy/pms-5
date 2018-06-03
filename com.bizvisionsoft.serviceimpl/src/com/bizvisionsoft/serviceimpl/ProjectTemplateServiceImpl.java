package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.service.model.WorkLinkInTemplate;
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
		return createDataSet(condition, ProjectTemplate.class);
	}

	@Override
	public WorkInTemplate getWorkInTemplate(ObjectId work_id) {
		return get(work_id, WorkInTemplate.class);
	}

	@Override
	public List<WorkInTemplate> listWorks(ObjectId template_id) {
		return c(WorkInTemplate.class).find(new BasicDBObject("template_id", template_id))
				.into(new ArrayList<WorkInTemplate>());
	}

	@Override
	public List<WorkLinkInTemplate> listLinks(ObjectId template_id) {
		return c(WorkLinkInTemplate.class).find(new BasicDBObject("template_id", template_id))
				.into(new ArrayList<WorkLinkInTemplate>());
	}

	@Override
	public WorkInTemplate insertWork(WorkInTemplate work) {
		return insert(work);
	}

	@Override
	public long updateWork(BasicDBObject bson) {
		return update(bson, WorkInTemplate.class);
	}

	@Override
	public long deleteWork(ObjectId work_id) {
		return delete(work_id, WorkInTemplate.class);
	}

	@Override
	public WorkLinkInTemplate insertLink(WorkLinkInTemplate link) {
		return insert(link);
	}

	@Override
	public long updateLink(BasicDBObject bson) {
		return update(bson, WorkLinkInTemplate.class);
	}

	@Override
	public long deleteLink(ObjectId link_id) {
		return delete(link_id, WorkLinkInTemplate.class);
	}

	@Override
	public int nextWBSIndex(BasicDBObject condition) {
		Document doc = c("workInTemplate").find(condition).sort(new BasicDBObject("index", -1))
				.projection(new BasicDBObject("index", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("index", 0)).orElse(0) + 1;
	}

}
