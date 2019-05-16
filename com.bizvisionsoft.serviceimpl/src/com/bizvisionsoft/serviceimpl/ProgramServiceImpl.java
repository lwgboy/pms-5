package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ProgramService;
import com.bizvisionsoft.service.model.Program;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.UpdateResult;

public class ProgramServiceImpl extends BasicServiceImpl implements ProgramService {

	@Override
	public Program insert(Program program, String domain) {
		Program ps = insert(program, Program.class, domain);
		return get(ps.get_id(), domain);
	}

	@Override
	public Program get(ObjectId _id, String domain) {
		return query(null, null, new BasicDBObject("_id", _id), domain).get(0);
	}

	@Override
	public long count(BasicDBObject filter, String domain) {
		return count(filter, Program.class, domain);
	}

	@Override
	public List<Program> list(BasicDBObject condition, String domain) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		return query(skip, limit, filter, domain);
	}

	@Override
	public long countRoot(BasicDBObject filter,String domain){
		if (filter == null){
			filter = new BasicDBObject();
		}
		filter.append("parent_id", null);
		return count(filter, domain);
	}

	@Override
	public List<Program> listRoot(BasicDBObject condition,String domain){
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null){
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("parent_id", null);
		return list(condition, domain);
	}

	private List<Program> query(Integer skip, Integer limit, BasicDBObject filter, String domain) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendUserInfo(pipeline, "pgmId", "pgmInfo", domain);

		List<Program> result = new ArrayList<Program>();
		c(Program.class, domain).aggregate(pipeline).into(result);
		return result;

	}

	@Override
	public long delete(ObjectId _id, String domain) {
		// ������¼���Ŀ�����ɱ�ɾ��
		if (c("program", domain).countDocuments(new Document("parent_id", _id)) > 0)
			throw new ServiceException("������ɾ�����¼���Ŀ������Ŀ����¼");

		// �������Ŀ�����˸���Ŀ��������ɾ��
		if (c("project", domain).countDocuments(new Document("program_id", _id)) > 0)
			throw new ServiceException("������ɾ�����¼���Ŀ����Ŀ����¼");

		return delete(_id, Program.class, domain);
	}

	@Override
	public long update(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, Program.class, domain);
	}

	@Override
	public void addProjects(List<ObjectId> pjIds, ObjectId _id, String domain) {
		if (pjIds.isEmpty())
			throw new ServiceException("û��ָ����Ŀ");
		if (_id == null)
			throw new ServiceException("û��ָ����Ŀ��");
		if (count(new BasicDBObject("_id", _id), domain) == 0)
			throw new ServiceException("ָ������Ŀ��������");
		UpdateResult ur = c("project", domain).updateMany(new Document("_id", new Document("$in", pjIds)),
				new Document("$set", new Document("program_id", _id)));
		if (ur.getModifiedCount() == 0)
			throw new ServiceException("û�и���");
	}

	@Override
	public void unsetProgram(ObjectId project_id, String domain) {
		UpdateResult ur = c("project", domain).updateOne(new Document("_id", project_id),
				new Document("$set", new Document("program_id", null)));
		if (ur.getModifiedCount() == 0)
			throw new ServiceException("û�и���");
	}

	// @Override
	// public List<Program> listFinishProgram(BasicDBObject condition,String
	// domain){
	// Integer skip = (Integer) condition.get("skip");
	// Integer limit = (Integer) condition.get("limit");
	// BasicDBObject filter = (BasicDBObject) condition.get("filter");
	// List<Bson> pipeline = new ArrayList<Bson>();
	// if (filter != null)
	// pipeline.add(new BasicDBObject("$match", filter));
	//
	// pipeline.addAll(Arrays.asList(
	// new BasicDBObject("$lookup", new BasicDBObject("from", "project")
	// .append("let", new BasicDBObject("program_id", "$_id"))
	// .append("pipeline", Arrays.asList(
	// new BasicDBObject("$match",
	// new BasicDBObject("$expr", new BasicDBObject("$and", Arrays.asList(
	// new BasicDBObject("$eq",
	// Arrays.asList("$program_id", "$$program_id")),
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
	// return c(Program.class,domain).aggregate(pipeline).into(new
	// ArrayList<Program>());
	// }

}
