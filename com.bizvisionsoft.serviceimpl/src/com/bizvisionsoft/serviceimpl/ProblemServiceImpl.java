package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class ProblemServiceImpl extends BasicServiceImpl implements ProblemService {

	@Override
	public Problem insertProblem(Problem p) {
		p.setStatus(Problem.StatusCreated);
		p = super.insert(p);
		return queryProblems(0, 1, new BasicDBObject("_id", p.get_id()), null).get(0);
	}

	@Override
	public List<Problem> listProblems(BasicDBObject condition, String status, String userid, String lang) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");

		BasicDBObject f = new BasicDBObject();
		Check.isAssigned(filter, s -> f.putAll(s));
		Check.isAssigned(status, s -> f.append("status", status));

		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		return queryProblems(skip, limit, f, sort);
	}

	private List<Problem> queryProblems(Integer skip, Integer limit, BasicDBObject filter, BasicDBObject sort) {
		List<Bson> pipeline = appendProblemQueryPipeline(skip, limit, filter, sort, new ArrayList<>());
		return c(Problem.class).aggregate(pipeline).into(new ArrayList<>());
	}

	private List<Bson> appendProblemQueryPipeline(Integer skip, Integer limit, BasicDBObject filter, BasicDBObject sort,
			List<Bson> pipeline) {
		appendOrgFullName(pipeline, "dept_id", "deptName");

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		return pipeline;
	}

	@Override
	public long countProblems(BasicDBObject filter, String status, String userid, String lang) {
		BasicDBObject f = new BasicDBObject();
		Check.isAssigned(filter, s -> f.putAll(s));
		Check.isAssigned(status, s -> f.append("status", status));
		return count(f, "problem");
	}

	@Override
	public long deleteProblem(ObjectId _id) {
		return delete(_id, Problem.class);
	}

	@Override
	public long updateProblems(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, Problem.class);
	}

	@Override
	public List<Document> listD1(BasicDBObject condition, ObjectId problem_id) {
		return null;
	}

	@Override
	public long countD1(BasicDBObject filter, ObjectId problem_id) {
		return 0;
	}

	@Override
	public Document insertD1(Document d1) {
		Document user = (Document) d1.get("userId_meta");

		ObjectId org_id = user.getObjectId("org_id");
		String userId = d1.getString("userId");
		String role = d1.getString("role");
		ObjectId problem_id = d1.getObjectId("problem_id");
		Document doc = new Document("_id", new ObjectId()).append("problem_id", problem_id).append("userId", userId).append("role", role)
				.append("name", user.get("name")).append("mobile", user.get("mobile")).append("position", user.get("position"))
				.append("email", user.get("email")).append("headPics", user.get("headPics"));

		List<Bson> pipe = new JQ("查询-用户所在组织-根据组织类型").set("match", new Document("userId", userId)).set("orgType", "部门").array();
		pipe.add(Aggregates.sort(new Document("idx", -1)));
		user = c("user").aggregate(pipe).first();

		String dept;
		if (user != null) {
			dept = ((Document) user.get("org")).getString("fullName");
		} else if (org_id != null) {
			dept = getString("organization", "fullName", org_id);
		} else {
			dept = "";
		}
		// TODO 唯一索引，多国语言提示传lang参数
		doc.append("dept", dept);
		c("d1CFT").insertOne(doc);
		// 渲染卡片
		return doc;
	}

	@Override
	public List<Document> listD2(BasicDBObject condition, ObjectId problem_id) {
		return listD1(condition, problem_id);
	}

	@Override
	public long countD2(BasicDBObject filter, ObjectId problem_id) {
		// TODO Auto-generated method stub
		return countD1(filter, problem_id);
	}

	@Override
	public List<Document> listD3(BasicDBObject condition, ObjectId problem_id) {
		// TODO Auto-generated method stub
		return listD1(condition, problem_id);
	}

	@Override
	public long countD3(BasicDBObject filter, ObjectId problem_id) {
		// TODO Auto-generated method stub
		return countD1(filter, problem_id);
	}

	@Override
	public List<Document> listD4(BasicDBObject condition, ObjectId problem_id) {
		// TODO Auto-generated method stub
		return listD1(condition, problem_id);
	}

	@Override
	public long countD4(BasicDBObject filter, ObjectId problem_id) {
		// TODO Auto-generated method stub
		return countD1(filter, problem_id);
	}

	@Override
	public List<Document> listD5(BasicDBObject condition, ObjectId problem_id) {
		// TODO Auto-generated method stub
		return listD1(condition, problem_id);
	}

	@Override
	public long countD5(BasicDBObject filter, ObjectId problem_id) {
		// TODO Auto-generated method stub
		return countD1(filter, problem_id);
	}

	@Override
	public List<Document> listD6(BasicDBObject condition, ObjectId problem_id) {
		// TODO Auto-generated method stub
		return listD1(condition, problem_id);
	}

	@Override
	public long countD6(BasicDBObject filter, ObjectId problem_id) {
		// TODO Auto-generated method stub
		return countD1(filter, problem_id);
	}

	@Override
	public List<Document> listD7(BasicDBObject condition, ObjectId problem_id) {
		// TODO Auto-generated method stub
		return listD1(condition, problem_id);
	}

	@Override
	public long countD7(BasicDBObject filter, ObjectId problem_id) {
		// TODO Auto-generated method stub
		return countD1(filter, problem_id);
	}

	@Override
	public List<Document> listD8(BasicDBObject condition, ObjectId problem_id) {
		// TODO Auto-generated method stub
		return listD1(condition, problem_id);
	}

	@Override
	public long countD8(BasicDBObject filter, ObjectId problem_id) {
		// TODO Auto-generated method stub
		return countD1(filter, problem_id);
	}

}
