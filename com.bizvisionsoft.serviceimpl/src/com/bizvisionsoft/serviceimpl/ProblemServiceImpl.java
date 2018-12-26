package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.bson.conversions.Bson;

import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.tools.Check;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class ProblemServiceImpl extends BasicServiceImpl implements ProblemService {

	@Override
	public Problem insert(Problem p) {
		return super.insert(p);
	}

	@Override
	public List<Problem> list(BasicDBObject condition, String status, String userid, String lang) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		return query(skip, limit, filter, sort);
	}
	
	private List<Problem> query(Integer skip, Integer limit, BasicDBObject filter, BasicDBObject sort) {
		List<Bson> pipeline = appendQueryPipeline(skip, limit, filter, sort, new ArrayList<>());
		return c(Problem.class).aggregate(pipeline).into(new ArrayList<>());
	}

	private List<Bson> appendQueryPipeline(Integer skip, Integer limit, BasicDBObject filter, BasicDBObject sort, List<Bson> pipeline) {
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
	public long count(BasicDBObject filter, String status, String userid, String lang) {
		BasicDBObject f = new BasicDBObject();
		Check.isAssigned(filter,s->f.putAll(s));
		Check.isAssigned(status,s->f.append("status", status));
		return count(f,"problem");
	}

}
