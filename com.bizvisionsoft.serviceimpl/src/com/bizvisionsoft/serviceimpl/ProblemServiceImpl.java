package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.bizvisionsoft.serviceimpl.renderer.D1CFTRenderer;
import com.bizvisionsoft.serviceimpl.renderer.D2Renderer;
import com.bizvisionsoft.serviceimpl.renderer.D3Renderer;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;

public class ProblemServiceImpl extends BasicServiceImpl implements ProblemService {

	@Override
	public Problem get(ObjectId _id) {
		return get(_id, Problem.class);
	}
	
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

	private List<Bson> createDxPipeline(BasicDBObject condition, ObjectId problem_id) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject f = new BasicDBObject();
		Check.isAssigned(filter, s -> f.putAll(s));
		f.append("problem_id", problem_id);
		BasicDBObject sort = (BasicDBObject) condition.get("sort");

		List<Bson> pipeline = new ArrayList<>();
		pipeline.add(Aggregates.match(f));

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));
		return pipeline;
	}

	@Override
	public List<Document> listD1(BasicDBObject condition, ObjectId problem_id, String lang) {
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		if (sort == null) {
			sort = new BasicDBObject();
			condition.put("sort", sort);
		}
		sort.append("role", 1).append("_id", -1);

		List<Bson> pipeline = createDxPipeline(condition, problem_id);
		ArrayList<Document> result = c("d1CFT").aggregate(pipeline).map(d -> D1CFTRenderer.render(d, lang)).into(new ArrayList<>());
		return result;
	}

	@Override
	public Document insertD1Item(Document d1, String lang) {
		Document user = (Document) d1.get("userId_meta");

		ObjectId org_id = user.getObjectId("org_id");
		String userId = d1.getString("userId");
		String role = d1.getString("role");
		ObjectId problem_id = d1.getObjectId("problem_id");

		if ("0".equals(role) && c("d1CFT").countDocuments(new Document("problem_id", problem_id).append("role", "0")) > 0)
			throw new ServiceException("违反唯一性规则：一个CFT多功能小组只允许存在一位组长。");

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
		return D1CFTRenderer.render(doc, lang);
	}

	@Override
	public long deleteD1CFT(ObjectId _id) {
		return c("d1CFT").deleteOne(new BasicDBObject("_id", _id)).getDeletedCount();
	}

	@Override
	public List<Document> listD2(BasicDBObject condition, ObjectId problem_id, String lang) {
		List<Document> result = new ArrayList<>();
		// 5w2h
		Document doc = getD2ProblemDesc(problem_id);
		if (doc.get("what") != null)
			result.add(D2Renderer.renderPDCard(doc, lang));
		// photos
		listD2ProblemPhotos(problem_id).forEach(d -> result.add(D2Renderer.renderPhotoCard(d, lang)));
		// TODO 讨论 回复 及其他
		return result;
	}

	@Override
	public Document getD2ProblemDesc(ObjectId problem_id) {
		return Optional.ofNullable(c("d2ProblemDesc").find(new Document("_id", problem_id)).first())
				.orElse(new Document("_id", problem_id));
	}

	private List<Document> listD2ProblemPhotos(ObjectId problem_id) {
		return c("d2ProblemPhoto").find(new Document("problem_id", problem_id)).sort(new Document("_id", -1)).into(new ArrayList<>());
	}

	@SuppressWarnings("unchecked")
	@Override
	public long deleteD2ProblemPhotos(ObjectId _id) {
		Document d2ProblemPhoto = c("d2ProblemPhoto").findOneAndDelete(new BasicDBObject("_id", _id));
		Check.instanceThen(d2ProblemPhoto.get("problemImg"), List.class,
				l -> ((List<Document>) l).forEach(d -> deleteFile(d.getString("namepace"), d.getObjectId("_id").toString())));
		return 1l;
	}

	@Override
	public Document updateD2ProblemDesc(Document d, String lang) {
		Document filter = new Document("_id", d.get("_id"));
		Document set = new Document("$set", d);
		return D2Renderer.renderPDCard(c("d2ProblemDesc").findOneAndUpdate(filter, set,
				new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)), lang);
	}

	@Override
	public Document insertD2ProblemPhoto(Document t, String lang) {
		c("d2ProblemPhoto").insertOne(t);
		return D2Renderer.renderPhotoCard(t, lang);
	}

	@Override
	public List<Document> listD3(BasicDBObject condition, ObjectId problem_id, String lang) {
		List<Document> d3Result = new ArrayList<>();
		// ICA计划 d3ICAPlan，ICA计划条目
		// charger, finishDate, description, attachment
		// 谁负责，在何时，完成哪些工作（通常的ICA有哪些），附件是详细的计划，文件
		c("d3ICA").find(new Document("problem_id", problem_id)).sort(new Document("priority", 1)).map(d -> D3Renderer.renderICA(d, lang))
				.into(d3Result);

		// ICA验证 d3ICAVerify，验证结论，验证记录
		// 谁在什么时候，采用何种方式进行了验证，验证的结论是什么，验证的记录

		// ICA执行 d3ICAImpl，执行记录
		// 谁在何时完成了哪些工作，是否达到了效果，后续的跟进

		// ICA证实 d3ICAConfirm，单条记录，得到内部或外部客户的证实
		// 是否得到证实，证实人，时间
		return d3Result;
	}

	@Override
	public Document insertD3ICA(Document t, String lang) {
		c("d3ICA").insertOne(t);
		return D3Renderer.renderICA(t, lang);
	}

	@Override
	public Document getD3ICA(ObjectId _id) {
		return c("d3ICA").find(new Document("_id", _id)).first();
	}

	@Override
	public Document updateD3ICA(Document d, String lang) {
		Document filter = new Document("_id", d.get("_id"));
		Document set = new Document("$set", d);
		return D3Renderer.renderICA(
				c("d3ICA").findOneAndUpdate(filter, set, new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)),
				lang);
	}

	@Override
	public long deleteD3ICA(ObjectId _id) {
		return c("d3ICA").deleteOne(new BasicDBObject("_id", _id)).getDeletedCount();
	}

	@Override
	public List<Document> listD4(BasicDBObject condition, ObjectId problem_id, String lang) {
		// TODO Auto-generated method stub
		List<Document> result = new ArrayList<>();
		return result;
	}

	@Override
	public List<Document> listD5(BasicDBObject condition, ObjectId problem_id, String lang) {
		// TODO Auto-generated method stub
		List<Document> result = new ArrayList<>();
		return result;
	}

	@Override
	public List<Document> listD6(BasicDBObject condition, ObjectId problem_id, String lang) {
		// TODO Auto-generated method stub
		List<Document> result = new ArrayList<>();
		return result;
	}

	@Override
	public List<Document> listD7(BasicDBObject condition, ObjectId problem_id, String lang) {
		// TODO Auto-generated method stub
		List<Document> result = new ArrayList<>();
		return result;
	}

	@Override
	public List<Document> listD8(BasicDBObject condition, ObjectId problem_id, String lang) {
		// TODO Auto-generated method stub
		List<Document> result = new ArrayList<>();
		return result;
	}


}
