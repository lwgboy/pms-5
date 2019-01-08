package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.CauseConsequence;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.bizvisionsoft.serviceimpl.renderer.ProblemCardRenderer;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;

public class ProblemServiceImpl extends BasicServiceImpl implements ProblemService {

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
	public long countCauseConsequences(BasicDBObject filter) {
		return count(filter, CauseConsequence.class);
	}

	@Override
	public long countProblems(BasicDBObject filter, String status, String userid, String lang) {
		BasicDBObject f = new BasicDBObject();
		Check.isAssigned(filter, s -> f.putAll(s));
		Check.isAssigned(status, s -> f.append("status", status));
		return count(f, "problem");
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
	public long deleteCauseConsequence(ObjectId _id) {
		return deleteOne(_id, "causeRelation");
	}

	@Override
	public long deleteD3ICA(ObjectId _id) {
		return deleteOne(_id, "d3ICA");
	}

	@Override
	public long deleteD5PCA(ObjectId _id, String lang) {
		return deleteOne(_id, "d5PCA");
	}

	@Override
	public long deleteD6IVPCA(ObjectId _id) {
		return deleteOne(_id, "d6IVPCA");
	}

	@Override
	public long deleteD7Similar(ObjectId _id) {
		return deleteOne(_id, "d7Similar");
	}

	@Override
	public long deleteD7SPA(ObjectId _id) {
		return deleteOne(_id, "d7SPA");
	}

	@Override
	public long deleteD8Exp(ObjectId _id) {
		return deleteOne(_id, "d8Exp");
	}

	@Override
	public long deleteProblem(ObjectId _id) {
		return delete(_id, Problem.class);
	}

	@Override
	public Problem get(ObjectId _id) {
		return get(_id, Problem.class);
	}

	public Problem info(ObjectId _id, String lang) {
		return get(_id);
	}

	@Override
	public Document getCauseConsequence(ObjectId problem_id, String type) {
		Problem problem = get(problem_id);
		List<Document> data = new ArrayList<>();
		List<Document> links = new ArrayList<>();
		// 根节点
		data.add(new Document("id", problem_id)//
				.append("name", problem.getName())//
				.append("draggable", true)//
				.append("category", "问题")//
				.append("desc", problem.getName())//
				.append("symbolSize", 50)//
				.append("value", 20));
		Arrays.asList(CauseSubject).forEach(s -> {
			data.add(new Document("id", s)//
					.append("name", s)//
					.append("draggable", true)//
					.append("category", "类别")//
					.append("symbolSize", 50)//
					.append("desc", s)//
					.append("value", 20));
			links.add(new Document("source", problem_id).append("target", s));
		});

		c("causeRelation").find(new Document("problem_id", problem_id).append("type", type)).forEach((Document d) -> {
			String id = d.getObjectId("_id").toHexString();
			Document item = new Document("id", id)//
					.append("name", d.get("name"))//
					.append("desc", Optional.ofNullable(d.getString("description")).orElse(""))//
					.append("draggable", true)//
					.append("category", d.getString("subject"))//
					.append("symbolSize", 20 * d.getInteger("weight", 1))//
					.append("value", 100 * d.getDouble("probability"));
			data.add(item);
			String parentId = Optional.ofNullable(d.getObjectId("parent_id")).map(p -> p.toHexString()).orElse(d.getString("subject"));
			links.add(new Document("source", parentId).append("target", id));
		});

		List<Document> categories = new ArrayList<>();
		categories.add(new Document("name", "问题"));
		categories.add(new Document("name", "类别"));
		categories.addAll(Arrays.asList(CauseSubject).stream().map(e -> new Document("name", e)).collect(Collectors.toList()));

		return new JQ("图表-因果关系图").set("data", data).set("links", links).set("categories", categories).doc();

	}

	@Override
	public Document getD2ProblemDesc(ObjectId problem_id) {
		return Optional.ofNullable(c("d2ProblemDesc").find(new Document("_id", problem_id)).first())
				.orElse(new Document("_id", problem_id));
	}

	@Override
	public Document getD3ICA(ObjectId _id) {
		return c("d3ICA").find(new Document("_id", _id)).first();
	}

	@Override
	public Document getD4RootCauseDesc(ObjectId problem_id) {
		return c("d4RootCauseDesc").find(new Document("_id", problem_id)).first();
	}

	@Override
	public Document getD5DecisionCriteria(ObjectId problem_id) {
		return c("d5DecisionCriteria").find(new Document("_id", problem_id)).first();
	}

	@Override
	public Document getD6IVPCA(ObjectId _id) {
		return c("d6IVPCA").find(new Document("_id", _id)).first();
	}

	@Override
	public Document getD7Similar(ObjectId _id) {
		return c("d7Similar").find(new Document("_id", _id)).first();
	}

	@Override
	public Document getD7SPA(ObjectId _id) {
		return c("d7SPA").find(new Document("_id", _id)).first();
	}

	@Override
	public Document getD8Exp(ObjectId _id) {
		return c("d8Exp").find(new Document("_id", _id)).first();
	}

	@Override
	public List<Result> icaConfirm(ObjectId _id) {
		updateProblems(new FilterAndUpdate().filter(new BasicDBObject("_id", _id)).set(new BasicDBObject("confirmD3ICA", true)).bson());
		return new ArrayList<Result>();
	}

	@Override
	public CauseConsequence insertCauseConsequence(CauseConsequence cc) {
		return insert(cc);
	}

	@Override
	public Document insertD3ICA(Document t, String lang) {
		c("d3ICA").insertOne(t);
		return ProblemCardRenderer.renderD3ICA(t, lang);
	}

	@Override
	public void insertD4RootCauseDesc(Document t, String lang) {
		c("d4RootCauseDesc").insertOne(t);
	}

	@Override
	public void insertD5DecisionCriteria(Document t, String language) {
		c("d5DecisionCriteria").insertOne(t);
	}

	@Override
	public void insertD5PCA(Document t, String language) {
		c("d5PCA").insertOne(t);
	}

	@Override
	public Document insertD6IVPCA(Document t, String lang) {
		t.append("_id", new ObjectId());
		c("d6IVPCA").insertOne(t);
		return ProblemCardRenderer.renderD6IVPCA(t, lang);
	}

	@Override
	public Document insertD7PreventAction(Document t, String lang) {
		t.append("_id", new ObjectId());
		c("d7SPA").insertOne(t);
		return ProblemCardRenderer.renderD7SPA(t, lang);
	}

	@Override
	public Document insertD7SimilarSituation(Document t, String lang) {
		t.append("_id", new ObjectId());
		c("d7Similar").insertOne(t);
		return ProblemCardRenderer.renderD7Similar(t, lang);
	}

	@Override
	public Document insertD8Experience(Document t, String lang) {
		t.append("_id", new ObjectId());
		c("d8Exp").insertOne(t);
		return ProblemCardRenderer.renderD8Exp(t, lang);
	}

	@Override
	public Problem insertProblem(Problem p) {
		p.setStatus(Problem.StatusCreated);
		p = insert(p);
		return queryProblems(0, 1, new BasicDBObject("_id", p.get_id()), null).get(0);
	}

	@Override
	public List<CauseConsequence> listCauseConsequences(BasicDBObject filter) {
		return createDataSet(new BasicDBObject("filter", filter), CauseConsequence.class);
	}

	@Override
	public List<Document> listD2(BasicDBObject condition, ObjectId problem_id, String lang) {
		List<Document> result = new ArrayList<>();
		// photos
		listD2ProblemPhotos(problem_id).forEach(d -> result.add(ProblemCardRenderer.renderD2PhotoCard(d, lang)));
		// 5w2h
		Document doc = getD2ProblemDesc(problem_id);
		if (doc.get("what") != null)
			result.add(ProblemCardRenderer.renderD25W2H(doc, lang));
		return result;
	}

	@Override
	public List<Document> listD3(BasicDBObject condition, ObjectId problem_id, String lang) {
		List<Document> d3Result = new ArrayList<>();
		c("d3ICA").find(new Document("problem_id", problem_id)).sort(new Document("priority", 1).append("_id", 1))
				.forEach((Document d) -> d3Result.add(ProblemCardRenderer.renderD3ICA(d, lang)));
		return d3Result;
	}

	@Override
	public List<Document> listD4(BasicDBObject condition, ObjectId problem_id, String lang) {
		List<Document> result = new ArrayList<>();
		// 根本问题描述

		Document data = getD4RootCauseDesc(problem_id);
		Document rcdCard = null;
		Document epCard = null;
		if (data != null) {
			rcdCard = ProblemCardRenderer.renderD4(data, "问题产生的根本原因", data.getString("rootCauseDesc"), (Document) data.get("charger_meta"),
					data.getDate("date"), lang);
			epCard = ProblemCardRenderer.renderD4(data, "问题流出的逃出点", data.getString("escapePoint"), (Document) data.get("charger_meta"),
					data.getDate("date"), lang);
		}

		// 问题产生的原因
		if (rcdCard != null) {
			result.add(rcdCard);
		}
		List<Bson> pipe = new ArrayList<>();
		pipe.add(new Document("$match", new Document("problem_id", problem_id).append("type", "因果分析-制造")));
		pipe.add(new Document("$lookup", new Document("from", "causeRelation").append("localField", "_id")
				.append("foreignField", "parent_id").append("as", "parent")));
		pipe.add(new Document("$match", new Document("parent", new Document("$size", 0))));
		c("causeRelation").aggregate(pipe).map(d -> ProblemCardRenderer.renderD4CauseConsequence(d, lang)).into(result);
		// 问题流出的原因
		if (epCard != null) {
			result.add(epCard);
		}
		pipe.clear();
		pipe.add(new Document("$match", new Document("problem_id", problem_id).append("type", "因果分析-流出")));
		pipe.add(new Document("$lookup", new Document("from", "causeRelation").append("localField", "_id")
				.append("foreignField", "parent_id").append("as", "parent")));
		pipe.add(new Document("$match", new Document("parent", new Document("$size", 0))));
		c("causeRelation").aggregate(pipe).map(d -> ProblemCardRenderer.renderD4CauseConsequence(d, lang)).into(result);

		return result;
	}

	@Override
	public List<Document> listD5(BasicDBObject condition, ObjectId problem_id, String lang) {
		List<Document> result = new ArrayList<>();
		Document d = c("d5PCA").find(new Document("problem_id", problem_id).append("selected", true)).first();
		if (d != null) {
			Date planStart = d.getDate("planStart1");
			Date planFinish = d.getDate("planFinish1");

			result.add(ProblemCardRenderer.renderD5PCA((List<?>) d.get("pca1"), "问题产生纠正措施", (Document) d.get("charger1_meta"), planStart,
					planFinish, lang));

			planStart = d.getDate("planStart2");
			planFinish = d.getDate("planFinish2");

			result.add(ProblemCardRenderer.renderD5PCA((List<?>) d.get("pca2"), "问题流出纠正措施", (Document) d.get("charger2_meta"), planStart,
					planFinish, lang));
		}

		return result;
	}

	@Override
	public List<Document> listD6(BasicDBObject condition, ObjectId problem_id, String lang) {
		return c("d6IVPCA").find(new Document("problem_id", problem_id)).map(d -> ProblemCardRenderer.renderD6IVPCA(d, lang))
				.into(new ArrayList<>());
	}

	@Override
	public List<Document> listD7(BasicDBObject condition, ObjectId problem_id, String lang) {
		List<Document> result = new ArrayList<>();
		c("d7Similar").find(new Document("problem_id", problem_id)).sort(new Document("degree", 1))
				.map(d -> ProblemCardRenderer.renderD7Similar(d, lang)).into(result);

		c("d7SPA").find(new Document("problem_id", problem_id)).map(d -> ProblemCardRenderer.renderD7SPA(d, lang)).into(result);

		return result;
	}

	@Override
	public List<Document> listD8(BasicDBObject condition, ObjectId problem_id, String lang) {
		List<Document> result = new ArrayList<>();
		c("d8Exp").find(new Document("problem_id", problem_id)).map(d -> ProblemCardRenderer.renderD8Exp(d, lang)).into(result);
		return result;
	}

	@Override
	public List<Document> listD5PCA(ObjectId problem_id, String lang) {
		return c("d5PCA").find(new Document("problem_id", problem_id)).into(new ArrayList<>());
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

	@Override
	public long updateCauseConsequence(BasicDBObject fu) {
		return super.update(fu, CauseConsequence.class);
	}

	@Override
	public Document updateD2ProblemDesc(Document d, String lang) {
		Document filter = new Document("_id", d.get("_id"));
		Document set = new Document("$set", d);
		return ProblemCardRenderer.renderD25W2H(c("d2ProblemDesc").findOneAndUpdate(filter, set,
				new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)), lang);
	}

	@Override
	public Document updateD3ICA(Document d, String lang) {
		return updateThen(d, lang, "d3ICA", ProblemCardRenderer::renderD3ICA);
	}

	@Override
	public void updateD4RootCauseDesc(BasicDBObject fu, String lang) {
		update(fu, "d4RootCauseDesc");
	}

	@Override
	public void updateD5DecisionCriteria(BasicDBObject fu, String lang) {
		update(fu, "d5DecisionCriteria");
	}

	@Override
	public void updateD5PCA(BasicDBObject fu, String lang) {
		update(fu, "d5PCA");
	}

	@Override
	public Document updateD6IVPCA(Document d, String lang) {
		return updateThen(d, lang, "d6IVPCA", ProblemCardRenderer::renderD6IVPCA);
	}

	@Override
	public Document updateD7Similar(Document d, String lang) {
		return updateThen(d, lang, "d7Similar", ProblemCardRenderer::renderD7Similar);
	}

	@Override
	public Document updateD7SPA(Document d, String lang) {
		return updateThen(d, lang, "d7SPA", ProblemCardRenderer::renderD7SPA);
	}

	@Override
	public Document updateD8Exp(Document d, String lang) {
		return updateThen(d, lang, "d8Exp", ProblemCardRenderer::renderD8Exp);
	}

	@Override
	public long updateProblems(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, Problem.class);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// D0 紧急应变措施
	//
	@Override
	public Document getD0ERA(ObjectId _id) {
		return getDocument(_id, "d0ERA");
	}

	@Override
	public long deleteD0ERA(ObjectId _id) {
		return deleteOne(_id, "d0ERA");
	}

	@Override
	public Document insertD0ERA(Document t, String lang, String render) {
		c("d0ERA").insertOne(t);
		if ("card".equals(render)) {
			return ProblemCardRenderer.renderD0ERA(t, lang);
		} else if ("gridrow".equals(render)) {
			return listD0DS(new BasicDBObject("filter", new BasicDBObject("_id", t.get("_id"))), t.getObjectId("problem_id"), lang).get(0);
		}
		return t;
	}

	@Override
	public List<Document> listD0(BasicDBObject condition, ObjectId problem_id, String lang) {
		return c("d0ERA").find(new Document("problem_id", problem_id)).map((Document d) -> ProblemCardRenderer.renderD0ERA(d, lang))
				.into(new ArrayList<>());
	}

	@Override
	public List<Document> listD0DS(BasicDBObject condition, ObjectId problem_id, String lang) {
		List<Bson> pipe = createDxPipeline(condition, problem_id);
		pipe.add(new Document("$addFields",
				(new Document("chargerInfo", "$charger_meta.name").append("content",
						new Document("$reduce", new Document("input", "$era.name").append("initialValue", "").append("in",
								new Document("$concat", Arrays.asList("$$value", "$$this", "; "))))))));
		return c("d0ERA").aggregate(pipe).into(new ArrayList<>());
	}

	@Override
	public Document updateD0ERA(Document d, String lang, String render) {
		BiFunction<Document, String, Document> func;
		if ("gridrow".equals(render)) {
			func = (t,
					l) -> listD0DS(new BasicDBObject("filter", new BasicDBObject("_id", t.get("_id"))), t.getObjectId("problem_id"), lang)
							.get(0);
		} else if ("card".equals(render)) {
			func = ProblemCardRenderer::renderD0ERA;
		} else {
			func = (t, l) -> t;
		}
		return updateThen(d, lang, "d0ERA", func);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// D1 CFT团队
	//
	@Override
	public long deleteD1CFT(ObjectId _id) {
		return deleteOne(_id, "d1CFT");
	}

	@Override
	public Document insertD1Item(Document d1, String lang, String render) {
		Document user = (Document) d1.get("member_meta");

		ObjectId org_id = user.getObjectId("org_id");
		String userId = d1.getString("member");
		String role = d1.getString("role");
		ObjectId problem_id = d1.getObjectId("problem_id");

		if ("0".equals(role) && c("d1CFT").countDocuments(new Document("problem_id", problem_id).append("role", "0")) > 0)
			throw new ServiceException("违反唯一性规则：一个CFT多功能小组只允许存在一位组长。");

		Document doc = new Document("_id", new ObjectId()).append("problem_id", problem_id).append("member", userId).append("role", role)
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
		if ("card".equals(render))
			return ProblemCardRenderer.renderD1CFTMember(doc, lang);
		else
			return doc.append("roleName", ProblemCardRenderer.cftRoleText[Integer.parseInt(doc.getString("role"))]);
	}

	@Override
	public List<Document> listD1(BasicDBObject condition, ObjectId problem_id, String lang) {
		ensureGet(condition, "sort").append("role", 1).append("_id", -1);

		List<Bson> pipeline = createDxPipeline(condition, problem_id);
		return c("d1CFT").aggregate(pipeline).map(d -> ProblemCardRenderer.renderD1CFTMember(d, lang)).into(new ArrayList<>());
	}

	@Override
	public List<Document> listD1DS(BasicDBObject condition, ObjectId problem_id, String lang) {
		ensureGet(condition, "sort").append("role", 1).append("_id", -1);

		List<Bson> pipeline = createDxPipeline(condition, problem_id);
		return c("d1CFT").aggregate(pipeline)
				.map(d -> d.append("roleName", ProblemCardRenderer.cftRoleText[Integer.parseInt(d.getString("role"))]))
				.into(new ArrayList<>());
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// D2 现场照片
	//
	@Override
	public long deleteD2ProblemPhotos(ObjectId _id) {
		Document delete = c("d2ProblemPhoto").findOneAndDelete(new BasicDBObject("_id", _id));
		if (delete != null) {
			deleteFileInField(delete, "problemImg");
			return 1;
		}
		return 0;
	}

	@Override
	public Document insertD2ProblemPhoto(Document t, String lang, String render) {
		c("d2ProblemPhoto").insertOne(t);
		if ("card".equals(render))
			return ProblemCardRenderer.renderD2PhotoCard(t, lang);
		return t;
	}

	public List<Document> listD2ProblemPhotos(ObjectId problem_id) {
		return c("d2ProblemPhoto").find(new Document("problem_id", problem_id)).sort(new Document("_id", -1)).into(new ArrayList<>());
	}

}
