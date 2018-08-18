package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ProjectSetService;
import com.bizvisionsoft.service.model.ProjectSet;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.UpdateResult;

public class ProjectSetServiceImpl extends BasicServiceImpl implements ProjectSetService {

	@Override
	public ProjectSet insert(ProjectSet projectSet) {
		ProjectSet ps = insert(projectSet, ProjectSet.class);
		return get(ps.get_id());
	}

	@Override
	public ProjectSet get(ObjectId _id) {
		return query(null, null, new BasicDBObject("_id", _id)).get(0);
	}

	@Override
	public long count(BasicDBObject filter) {
		return count(filter, ProjectSet.class);
	}

	@Override
	public List<ProjectSet> list(BasicDBObject condition) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		return query(skip, limit, filter);
	}

	@Override
	public long countRoot(BasicDBObject filter) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("parent_id", null);
		return count(filter);
	}

	@Override
	public List<ProjectSet> listRoot(BasicDBObject condition) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("parent_id", null);
		return list(condition);
	}

	private List<ProjectSet> query(Integer skip, Integer limit, BasicDBObject filter) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendUserInfo(pipeline, "pgmId", "pgmInfo");

		List<ProjectSet> result = new ArrayList<ProjectSet>();
		c(ProjectSet.class).aggregate(pipeline).into(result);
		return result;

	}

	@Override
	public long delete(ObjectId _id) {
		// 如果有下级项目集不可被删除
		if (c("projectSet").countDocuments(new Document("parent_id", _id)) > 0)
			throw new ServiceException("不允许删除有下级项目集的项目集记录");

		// 如果有项目引用了该项目集，不可删除
		if (c("project").countDocuments(new Document("projectSet_id", _id)) > 0)
			throw new ServiceException("不允许删除有下级项目的项目集记录");

		return delete(_id, ProjectSet.class);
	}

	@Override
	public long update(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, ProjectSet.class);
	}

	@Override
	public void addProjects(List<ObjectId> pjIds, ObjectId _id) {
		if (pjIds.isEmpty())
			throw new ServiceException("没有指定项目");
		if (_id == null)
			throw new ServiceException("没有指定项目集");
		if (count(new BasicDBObject("_id", _id)) == 0)
			throw new ServiceException("指定的项目集不存在");
		UpdateResult ur = c("project").updateMany(new Document("_id", new Document("$in", pjIds)),
				new Document("$set", new Document("projectSet_id", _id)));
		if (ur.getModifiedCount() == 0)
			throw new ServiceException("没有更新");
	}

	@Override
	public void unsetProjectSet(ObjectId project_id) {
		UpdateResult ur = c("project").updateOne(new Document("_id", project_id),
				new Document("$set", new Document("projectSet_id", null)));
		if(ur.getModifiedCount()==0)
			throw new ServiceException("没有更新");
	}

	// @Override
	// public List<ProjectSet> listFinishProjectSet(BasicDBObject condition) {
	// Integer skip = (Integer) condition.get("skip");
	// Integer limit = (Integer) condition.get("limit");
	// BasicDBObject filter = (BasicDBObject) condition.get("filter");
	// List<Bson> pipeline = new ArrayList<Bson>();
	// if (filter != null)
	// pipeline.add(new BasicDBObject("$match", filter));
	//
	// pipeline.addAll(Arrays.asList(
	// new BasicDBObject("$lookup", new BasicDBObject("from", "project")
	// .append("let", new BasicDBObject("projectSet_id", "$_id"))
	// .append("pipeline", Arrays.asList(
	// new BasicDBObject("$match",
	// new BasicDBObject("$expr", new BasicDBObject("$and", Arrays.asList(
	// new BasicDBObject("$eq",
	// Arrays.asList("$projectSet_id", "$$projectSet_id")),
	// new BasicDBObject("$eq",
	// Arrays.asList(
	// new BasicDBObject("$not",
	// Arrays.asList(new BasicDBObject("$eq",
	// Arrays.asList("$status",
	// ProjectStatus.Closed)))),
	// false)))))),
	// new BasicDBObject("$group",
	// new BasicDBObject("_id", null).append("count",
	// new BasicDBObject("$sum", 1.0)))))
	// .append("as", "project")),
	// new BasicDBObject("$unwind",
	// new BasicDBObject("path", "$project").append("preserveNullAndEmptyArrays",
	// true)),
	// new BasicDBObject("$addFields", new BasicDBObject("projectCount",
	// "$project.count")),
	// new BasicDBObject("$match", new BasicDBObject("projectCount", new
	// BasicDBObject("$gt", 0.0))),
	// new BasicDBObject("$project", new BasicDBObject("project", false))));
	//
	// if (skip != null)
	// pipeline.add(new BasicDBObject("$skip", skip));
	//
	// if (limit != null)
	// pipeline.add(new BasicDBObject("$limit", limit));
	//
	// return c(ProjectSet.class).aggregate(pipeline).into(new
	// ArrayList<ProjectSet>());
	// }

}
