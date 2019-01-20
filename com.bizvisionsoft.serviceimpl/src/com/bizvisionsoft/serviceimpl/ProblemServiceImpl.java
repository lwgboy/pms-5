package com.bizvisionsoft.serviceimpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.Catalog;
import com.bizvisionsoft.service.model.CauseConsequence;
import com.bizvisionsoft.service.model.ClassifyCause;
import com.bizvisionsoft.service.model.ClassifyProblem;
import com.bizvisionsoft.service.model.ClassifyProblemLost;
import com.bizvisionsoft.service.model.FreqInd;
import com.bizvisionsoft.service.model.IncidenceInd;
import com.bizvisionsoft.service.model.LostInd;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.model.ProblemCostItem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.SeverityInd;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.bizvisionsoft.serviceimpl.renderer.ProblemCardRenderer;
import com.bizvisionsoft.serviceimpl.renderer.ProblemCostChartRender;
import com.mongodb.BasicDBObject;
import com.mongodb.Function;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;

public class ProblemServiceImpl extends BasicServiceImpl implements ProblemService {

	private Document appendRoleText(Document doc, String lang) {
		return doc.append("roleName", ProblemCardRenderer.cftRoleText[Integer.parseInt(doc.getString("role"))]);
	}

	private Document appendPriortyText(Document d, String lang) {
		return d.append("priorityText", ProblemCardRenderer.priorityText[Integer.parseInt(d.getString("priority"))]);
	}

	private Document appendActionText(Document d, String lang) {
		String act = d.getString("actionType");
		if ("make".equals(act)) {
			d.append("actionTypeText", "纠正问题产生");
		} else if ("out".equals(act)) {
			d.append("actionTypeText", "控制问题流出");
		}
		return d;
	}

	@Override
	public Problem get(ObjectId _id) {
		return get(_id, Problem.class);
	}

	public Problem info(ObjectId _id, String lang) {
		return get(_id);
	}

	private List<Bson> appendBasicQueryPipeline(BasicDBObject condition, List<Bson> pipeline) {
		appendOrgFullName(pipeline, "dept_id", "deptName");
		Optional.ofNullable((BasicDBObject) condition.get("filter")).map(Aggregates::match).ifPresent(pipeline::add);
		Optional.ofNullable((BasicDBObject) condition.get("sort")).map(Aggregates::sort).ifPresent(pipeline::add);
		Optional.ofNullable((Integer) condition.get("skip")).map(Aggregates::skip).ifPresent(pipeline::add);
		Optional.ofNullable((Integer) condition.get("limit")).map(Aggregates::limit).ifPresent(pipeline::add);
		pipeline.add(Aggregates.lookup("d2ProblemPhoto", "_id", "problem_id", "d2ProblemPhoto"));
		pipeline.addAll(new JQ("追加-目标时间的临近性").set("dateField", "$latestTimeReq").set("targetDate", new Date())
				.set("urgencyIndField", "urgencyInd").array());
		pipeline.addAll(new JQ("追加-问题成本合计").array());
		return pipeline;
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
	public long deleteProblem(ObjectId _id) {
		return delete(_id, Problem.class);
	}

	@Override
	public List<Result> icaConfirm(ObjectId _id) {
		updateProblems(new FilterAndUpdate().filter(new BasicDBObject("_id", _id)).set(new BasicDBObject("confirmD3ICA", true)).bson());
		return new ArrayList<Result>();
	}

	@Override
	public Problem insertProblem(Problem p) {
		p.setStatus(Problem.StatusCreated);
		p = insert(p);
		List<Bson> pipe = appendBasicQueryPipeline(new Query().filter(new BasicDBObject("_id", p.get_id())).bson(), new ArrayList<>());
		return c(Problem.class).aggregate(pipe).first();
	}

	@Override
	public List<Problem> listProblems(BasicDBObject condition, String status, String userid) {
		ensureGet(condition, "filter").append("status", status);
		List<Bson> pipeline = appendBasicQueryPipeline(condition, new ArrayList<>());
		return c(Problem.class).aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public List<Document> listProblemsCard(BasicDBObject condition, String status, String userid, String lang) {
		ensureGet(condition, "filter").append("status", status);
		List<Bson> pipeline = appendBasicQueryPipeline(condition, new ArrayList<>());
		return c("problem").aggregate(pipeline).map(d -> ProblemCardRenderer.renderProblem(d, lang)).into(new ArrayList<>());
	}

	@Override
	public long updateProblems(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, Problem.class);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 通用行动
	//
	@Override
	public Document getAction(ObjectId _id) {
		return getDocument(_id, "problemAction");
	}

	@Override
	public long deleteAction(ObjectId _id) {
		return deleteOne(_id, "problemAction");
	}

	@Override
	public Document insertAction(Document t, ObjectId problem_id, String stage, String lang, String render) {
		c("problemAction").insertOne(t.append("problem_id", problem_id).append("stage", stage));
		if ("card".equals(render)) {
			t = ProblemCardRenderer.renderAction(t, lang);
		} else if ("gridrow".equals(render)) {
			appendPriortyText(t, lang);
			appendActionText(t, lang);
		}
		return t;
	}

	@Override
	public List<Document> listActions(BasicDBObject condition, ObjectId problem_id, String stage, String lang, String render) {
		Function<Document, Document> f;
		if ("card".equals(render)) {
			f = d -> ProblemCardRenderer.renderAction(d, lang);
		} else {
			f = d -> {
				appendPriortyText(d, lang);
				appendActionText(d, lang);
				return d;
			};
		}
		// TODO condition
		FindIterable<Document> iter = c("problemAction").find(new Document("problem_id", problem_id).append("stage", stage))
				.sort(new Document("priority", 1));
		return iter.map(f).into(new ArrayList<>());
	}

	@Override
	public Document updateAction(Document doc, String lang, String render) {
		BiFunction<Document, String, Document> func;
		if ("card".equals(render)) {
			func = ProblemCardRenderer::renderAction;
		} else {
			func = (d, l) -> {
				appendPriortyText(d, l);
				appendActionText(d, l);
				return d;
			};
		}
		return updateThen(doc, lang, "problemAction", func);
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
			return appendRoleText(doc, lang);
	}

	@Override
	public List<Document> listD1(BasicDBObject condition, ObjectId problem_id, String lang, String render) {
		ensureGet(condition, "sort").append("role", 1).append("_id", -1);
		List<Bson> pipeline = createDxPipeline(condition, problem_id);
		Function<Document, Document> f;
		if ("card".equals(render)) {
			f = d -> ProblemCardRenderer.renderD1CFTMember(d, lang);
		} else {
			f = d -> appendRoleText(d, lang);
		}
		return c("d1CFT").aggregate(pipeline).map(f).into(new ArrayList<>());
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// D2 问题描述，现场照片等
	//
	@Override
	public Document updateD2ProblemDesc(Document d, String lang) {
		Document filter = new Document("_id", d.get("_id"));
		Document set = new Document("$set", d);
		return ProblemCardRenderer.renderD25W2H(c("d2ProblemDesc").findOneAndUpdate(filter, set,
				new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)), lang);
	}

	@Override
	public Document getD2ProblemDesc(ObjectId problem_id) {
		return Optional.ofNullable(c("d2ProblemDesc").find(new Document("_id", problem_id)).first())
				.orElse(new Document("_id", problem_id));
	}

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
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// D4 根本原因
	//
	@Override
	public long countCauseConsequences(BasicDBObject filter) {
		return count(filter, CauseConsequence.class);
	}

	@Override
	public long deleteCauseConsequence(ObjectId _id) {
		return deleteOne(_id, "causeRelation");
	}

	@Override
	public Document getD4RootCauseDesc(ObjectId problem_id) {
		return getDocument(problem_id, "d4RootCauseDesc");
	}

	@Override
	public void insertD4RootCauseDesc(Document t, String lang) {
		c("d4RootCauseDesc").insertOne(t);
	}

	@Override
	public void updateD4RootCauseDesc(BasicDBObject fu, String lang) {
		update(fu, "d4RootCauseDesc");
	}

	@Override
	public CauseConsequence insertCauseConsequence(CauseConsequence cc) {
		return insert(cc);
	}

	@Override
	public long updateCauseConsequence(BasicDBObject fu) {
		return super.update(fu, CauseConsequence.class);
	}

	@Override
	public Document getCauseConsequence(ObjectId problem_id, String type) {
		Problem problem = get(problem_id);
		List<Document> data = new ArrayList<>();
		List<Document> links = new ArrayList<>();

		List<String> causeSubject = c("classifyCause").find(new Document("parent_id", null)).map(d -> d.getString("name"))
				.into(new ArrayList<>());
		// 根节点
		data.add(new Document("id", problem_id)//
				.append("name", problem.getName())//
				.append("draggable", true)//
				.append("category", "问题")//
				.append("desc", problem.getName())//
				.append("symbolSize", 50)//
				.append("value", 20));
		// TODO
		causeSubject.forEach(s -> {
			data.add(new Document("id", s)//
					.append("name", s)//
					.append("draggable", true)//
					.append("category", "类别")//
					.append("symbolSize", 50)//
					.append("desc", s)//
					.append("value", 20));
			links.add(new Document("source", problem_id).append("target", s).append("emphasis",
					new Document("label", new Document("show", false))));
		});

		c("causeRelation").find(new Document("problem_id", problem_id).append("type", type)).forEach((Document d) -> {
			String id = d.getObjectId("_id").toHexString();
			Document item = new Document("id", id)//
					.append("name", d.get("name"))//
					.append("desc", Optional.ofNullable(d.getString("description")).orElse(""))//
					.append("draggable", true)//
					.append("category", d.getString("subject"))//
					.append("symbolSize", 5 * d.getInteger("weight", 1))//
					.append("value", 100 * d.getDouble("probability"));
			data.add(item);
			String parentId = Optional.ofNullable(d.getObjectId("parent_id")).map(p -> p.toHexString()).orElse(d.getString("subject"));
			links.add(new Document("source", parentId).append("target", id).append("emphasis",
					new Document("label", new Document("show", false))));
		});

		List<Document> categories = new ArrayList<>();
		categories.add(new Document("name", "问题"));
		categories.add(new Document("name", "类别"));
		categories.addAll(causeSubject.stream().map(e -> new Document("name", e)).collect(Collectors.toList()));

		return new JQ("图表-因果关系图").set("data", data).set("links", links).set("categories", categories).doc();
	}

	@Override
	public List<CauseConsequence> listCauseConsequences(BasicDBObject filter) {
		return createDataSet(new BasicDBObject("filter", filter), CauseConsequence.class);
	}

	@Override
	public List<Document> listD4(BasicDBObject condition, ObjectId problem_id, String lang) {
		List<Document> result = new ArrayList<>();
		// 根本问题描述

		Document data = getD4RootCauseDesc(problem_id);
		Document rcdCard = null;
		Document epCard = null;
		if (data != null) {
			rcdCard = ProblemCardRenderer.renderD4(data, "make", data.getString("rootCauseDesc"), (Document) data.get("charger_meta"),
					data.getDate("date"), lang);
			epCard = ProblemCardRenderer.renderD4(data, "out", data.getString("escapePoint"), (Document) data.get("charger_meta"),
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
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// D5 PCA选择
	//
	@Override
	public long deleteD5PCA(ObjectId _id, String lang) {
		return deleteOne(_id, "d5PCA");
	}

	@Override
	public void insertD5DecisionCriteria(Document t, String language) {
		c("d5DecisionCriteria").insertOne(t);
	}

	@Override
	public Document getD5DecisionCriteria(ObjectId problem_id) {
		return c("d5DecisionCriteria").find(new Document("_id", problem_id)).first();
	}

	@Override
	public void insertD5PCA(Document t, String language) {
		c("d5PCA").insertOne(t);
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
	public List<Document> listD5PCA(ObjectId problem_id, String lang) {
		return c("d5PCA").find(new Document("problem_id", problem_id)).into(new ArrayList<>());
	}

	@Override
	public void updateD5DecisionCriteria(BasicDBObject fu, String lang) {
		update(fu, "d5DecisionCriteria");
	}

	@Override
	public void updateD5PCA(BasicDBObject fu, String lang) {
		update(fu, "d5PCA");
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// D7 预防
	//
	@Override
	public long deleteD7Similar(ObjectId _id) {
		return deleteOne(_id, "d7Similar");
	}

	@Override
	public Document getD7Similar(ObjectId _id) {
		return getDocument(_id, "d7Similar");
	}

	@Override
	public Document insertD7Similar(Document t, String lang, String render) {
		t.append("_id", new ObjectId());
		c("d7Similar").insertOne(t);
		if ("card".equals(render))
			return ProblemCardRenderer.renderD7Similar(t, lang);
		else
			return appendDegreeText(t, lang);
	}

	@Override
	public Document updateD7Similar(Document d, String lang, String render) {
		if ("card".equals(render))
			return updateThen(d, lang, "d7Similar", ProblemCardRenderer::renderD7Similar);
		else
			return updateThen(d, lang, "d7Similar", this::appendDegreeText);

	}

	@Override
	public List<Document> listD7Similar(BasicDBObject condition, ObjectId problem_id, String lang, String render) {
		return c("d7Similar").find(new Document("problem_id", problem_id)).sort(new Document("degree", 1))
				.map(d -> appendDegreeText(d, lang)).into(new ArrayList<>());
	}

	@Override
	public List<Document> listD7(BasicDBObject condition, ObjectId problem_id, String lang, String render) {
		List<Document> result = new ArrayList<>();
		c("d7Similar").find(new Document("problem_id", problem_id)).sort(new Document("degree", 1))
				.map(d -> ProblemCardRenderer.renderD7Similar(d, lang)).into(result);

		result.addAll(listActions(condition, problem_id, "spa", lang, render));

		return result;
	}

	private Document appendDegreeText(Document d, String lang) {
		return d.append("dgreeText", ProblemCardRenderer.similarDegreeText[Integer.parseInt(d.getString("degree"))]);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// D8 关闭
	//
	@Override
	public Document updateD8Exp(Document d, String lang, String render) {
		return updateThen(d, lang, "d8Exp", "card".equals(render) ? ProblemCardRenderer::renderD8Exp : null);
	}

	@Override
	public List<Document> listD8(BasicDBObject condition, ObjectId problem_id, String lang, String render) {
		if ("card".equals(render)) {
			return c("d8Exp").find(new Document("problem_id", problem_id)).map(d -> ProblemCardRenderer.renderD8Exp(d, lang))
					.into(new ArrayList<>());
		} else {
			return c("d8Exp").find(new Document("problem_id", problem_id)).into(new ArrayList<>());
		}
	}

	@Override
	public Document getD8Exp(ObjectId _id) {
		return getDocument(_id, "d8Exp");
	}

	@Override
	public long deleteD8Exp(ObjectId _id) {
		return deleteOne(_id, "d8Exp");
	}

	@Override
	public Document insertD8Experience(Document t, String lang, String render) {
		t.append("_id", new ObjectId());
		c("d8Exp").insertOne(t);
		if ("card".equals(render))
			return ProblemCardRenderer.renderD8Exp(t, lang);
		return t;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 问题严重性级别
	//
	@Override
	public List<SeverityInd> listSeverityInd() {
		return c(SeverityInd.class).find().sort(new BasicDBObject("_id", 1)).into(new ArrayList<SeverityInd>());
	}

	@Override
	public SeverityInd insertSeverityInd(SeverityInd item) {
		return insert(item);
	}

	@Override
	public long deleteSeverityInd(ObjectId _id) {
		return delete(_id, SeverityInd.class);
	}

	@Override
	public long updateSeverityInd(BasicDBObject fu) {
		return update(fu, SeverityInd.class);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 问题损失级别
	//
	@Override
	public List<LostInd> listLostInd() {
		return c(LostInd.class).find().sort(new BasicDBObject("_id", 1)).into(new ArrayList<>());
	}

	@Override
	public LostInd insertLostInd(LostInd item) {
		return insert(item);
	}

	@Override
	public long deleteLostInd(ObjectId _id) {
		return delete(_id, LostInd.class);
	}

	@Override
	public long updateLostInd(BasicDBObject fu) {
		return update(fu, LostInd.class);
	}

	@Override
	public List<FreqInd> listFreqInd() {
		return c(FreqInd.class).find().sort(new BasicDBObject("_id", 1)).into(new ArrayList<>());
	}

	@Override
	public FreqInd insertFreqInd(FreqInd item) {
		return insert(item);
	}

	@Override
	public long deleteFreqInd(ObjectId _id) {
		return delete(_id, FreqInd.class);
	}

	@Override
	public long updateFreqInd(BasicDBObject fu) {
		return update(fu, FreqInd.class);
	}

	@Override
	public List<IncidenceInd> listIncidenceInd() {
		return c(IncidenceInd.class).find().sort(new BasicDBObject("_id", 1)).into(new ArrayList<>());
	}

	@Override
	public IncidenceInd insertIncidenceInd(IncidenceInd item) {
		return insert(item);
	}

	@Override
	public long deleteIncidenceInd(ObjectId _id) {
		return delete(_id, IncidenceInd.class);
	}

	@Override
	public long updateIncidenceInd(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, IncidenceInd.class);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	private <T> List<T> listClassifyItems(BasicDBObject filter, Class<T> clazz) {
		List<Bson> pipeline = new ArrayList<Bson>();
		if (filter != null) {
			pipeline.add(Aggregates.match(filter));
		}
		pipeline.add(Aggregates.sort(new BasicDBObject("id", 1)));
		pipeline.addAll(new JQ("查询-通用-层次结构-增加isLeaf和Path").set("from", clazz.getAnnotation(PersistenceCollection.class).value()).array());

		return c(clazz).aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public List<ClassifyProblemLost> rootClassifyProblemLost() {
		return listClassifyProblemLost(new BasicDBObject("parent_id", null));
	}

	@Override
	public List<ClassifyProblemLost> listClassifyProblemLost(BasicDBObject filter) {
		return listClassifyItems(filter, ClassifyProblemLost.class);
	}

	@Override
	public long countClassifyProblemLost(ObjectId parent_id) {
		return count(new BasicDBObject("parent_id", parent_id), "classifyProblemLost");
	}

	@Override
	public ClassifyProblemLost insertClassifyProblemLost(ClassifyProblemLost ai) {
		return insert(ai);
	}

	@Override
	public long deleteClassifyProblemLost(ObjectId _id) {
		return deleteOne(_id, "classifyProblemLost");
	}

	@Override
	public long updateClassifyProblemLost(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, "classifyProblemLost");
	}

	@Override
	public List<ClassifyProblem> rootClassifyProblem() {
		return listClassifyProblem(new BasicDBObject("parent_id", null));
	}

	@Override
	public List<ClassifyProblem> listClassifyProblem(BasicDBObject filter) {
		return listClassifyItems(filter, ClassifyProblem.class);
	}

	@Override
	public long countClassifyProblem(ObjectId parent_id) {
		return count(new BasicDBObject("parent_id", parent_id), "classifyProblem");
	}

	@Override
	public ClassifyProblem insertClassifyProblem(ClassifyProblem ai) {
		return insert(ai);
	}

	@Override
	public long deleteClassifyProblem(ObjectId _id) {
		return deleteOne(_id, "classifyProblem");
	}

	@Override
	public long updateClassifyProblem(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, "classifyProblem");
	}

	@Override
	public List<ClassifyCause> rootClassifyCause() {
		return listClassifyCause(new BasicDBObject("parent_id", null));
	}

	@Override
	public List<ClassifyCause> listClassifyCause(BasicDBObject filter) {
		return listClassifyItems(filter, ClassifyCause.class);
	}

	@Override
	public List<ClassifyCause> rootClassifyCauseSelector(CauseConsequence cc) {
		if (cc == null)
			return new ArrayList<>();
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("name", cc.getSubject())));
		pipeline.add(Aggregates.lookup("classifyCause", "_id", "parent_id", "children1"));
		pipeline.add(Aggregates.unwind("$children1"));
		pipeline.add(Aggregates.replaceRoot("$children1"));
		pipeline.add(Aggregates.sort(new BasicDBObject("id", 1)));
		pipeline.addAll(new JQ("查询-通用-层次结构-增加isLeaf和Path").set("from", "classifyCause").array());
		debugPipeline(pipeline);
		return c(ClassifyCause.class).aggregate(pipeline).into(new ArrayList<>());

	}

	@Override
	public long countClassifyCause(ObjectId parent_id) {
		return count(new BasicDBObject("parent_id", parent_id), "classifyCause");
	}

	@Override
	public ClassifyCause insertClassifyCause(ClassifyCause ai) {
		return insert(ai);
	}

	@Override
	public long deleteClassifyCause(ObjectId _id) {
		return deleteOne(_id, "classifyCause");
	}

	@Override
	public long updateClassifyCause(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, "classifyCause");
	}

	@Override
	public long deleteCostItem(ObjectId _id) {
		return deleteOne(_id, "problemCostItem");
	}

	@Override
	public List<ProblemCostItem> listCostItems(BasicDBObject condition, ObjectId problem_id) {
		ensureGet(condition, "filter").append("problem_id", problem_id);
		ensureGet(condition, "sort").append("date", -1);
		return list(ProblemCostItem.class, condition,
				Aggregates.addFields(new Field<Document>("summary", new Document("$subtract", Arrays.asList("$drAmount", "$crAmount")))));
	}

	@Override
	public long countCostItems(BasicDBObject filter, ObjectId problem_id) {
		if (filter == null)
			filter = new BasicDBObject();
		filter.append("problem_id", problem_id);
		return count(filter, "problemCostItem");
	}

	@Override
	public long updateCostItems(BasicDBObject fu) {
		return update(fu, "problemCostItem");
	}

	@Override
	public ProblemCostItem insertCostItem(ProblemCostItem p, ObjectId problem_id) {
		p.setProblem_id(problem_id);
		insert(p);
		BasicDBObject cond = new Query().filter(new BasicDBObject("_id", p.get_id())).bson();
		return listCostItems(cond, problem_id).get(0);
	}

	@Override
	public Document getSummaryCost(ObjectId problem_id) {
		List<Document> pipe = Arrays.asList(new Document("$match", new Document("problem_id", problem_id)),
				new Document("$group",
						new Document("_id", "$problem_id").append("drAmount", new Document("$sum", "$drAmount")).append("crAmount",
								new Document("$sum", "$crAmount"))),
				new Document("$addFields", new Document("summary", new Document("$subtract", Arrays.asList("$drAmount", "$crAmount")))));
		debugPipeline(pipe);
		return c("problemCostItem").aggregate(pipe).first();
	}

	@Override
	public Document periodCostChart(ObjectId problem_id) {
		List<String> x = new ArrayList<>();
		Set<String> legend = new HashSet<>();
		Map<String, Document> ds = new HashMap<>();
		c("problemCostItem").aggregate(new JQ("查询-问题成本-根科目和年月汇总").set("match", new Document("problem_id", problem_id)).array())
				.forEach((Document d) -> {
					// 科目构建系列
					// 期间为x轴
					// 数值为y轴
					String account = d.getString("_id");
					legend.add(account);

					Document data = (Document) d.get("data");
					x.addAll(data.keySet());
					ds.put(account, data);
				});

		List<String> xAxis = new ArrayList<>();
		if (!x.isEmpty()) {
			Collections.sort(x);
			try {
				Calendar min = Calendar.getInstance();
				min.setTime(new SimpleDateFormat("yyyy-MM").parse(x.get(0)));
				Calendar max = Calendar.getInstance();
				max.setTime(new SimpleDateFormat("yyyy-MM").parse(x.get(x.size() - 1)));
				while (!min.after(max)) {
					xAxis.add(new SimpleDateFormat("yyyy-MM").format(min.getTime()));
					min.add(Calendar.MONTH, 1);
				}
			} catch (ParseException e) {
			}
		}

		List<Document> series = legend.stream().map(l -> {
			List<Double> data = xAxis.stream().map(s -> ds.get(l).get(s, 0d)).collect(Collectors.toList());
			return new Document("name", l).append("type", "bar").append("barGap", 0).append("data", data);
		}).collect(Collectors.toList());

		return new JQ("图表-通用-多维堆叠柱图").set("legend", legend).set("xAxis", xAxis).set("series", series).doc();
	}

	@Override
	public List<Document> listAdvisablePlan(BasicDBObject condition, ObjectId problem_id, String stage) {
		String[] stageNames = new String[] { "紧急反应", "临时控制", "永久纠正", "系统预防", "善后措施" };
		String stageName = stageNames[Arrays.asList("era", "ica", "pca", "spa", "lra").indexOf(stage)];
		List<Bson> pipeline = new ArrayList<>();
		pipeline.addAll(new JQ("查询-行动预案-匹配问题").set("problem_id", problem_id).set("stage", stageName).array());
		Optional.ofNullable((BasicDBObject) condition.get("filter")).ifPresent(f -> pipeline.add(Aggregates.match(f)));
		pipeline.add(Aggregates.sort(ensureGet(condition, "sort").append("_id", -1)));
		Optional.ofNullable((Integer) condition.get("skip")).ifPresent(f -> pipeline.add(Aggregates.skip(f)));
		Optional.ofNullable((Integer) condition.get("limit")).ifPresent(f -> pipeline.add(Aggregates.limit(f)));

		return c("problem").aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public long countAdvisablePlan(BasicDBObject filter, ObjectId problem_id, String stage) {
		String[] stageNames = new String[] { "紧急反应", "临时控制", "永久纠正", "系统预防", "善后措施" };
		String stageName = stageNames[Arrays.asList("era", "ica", "pca", "spa", "lra").indexOf(stage)];
		List<Bson> pipe = new ArrayList<>();
		pipe.addAll(new JQ("查询-行动预案-匹配问题").set("problem_id", problem_id).set("stage", stageName).array());
		if (filter != null)
			pipe.add(new BasicDBObject("$match", filter));
		pipe.add(Aggregates.count());
		return Optional.ofNullable(c("problem").aggregate(pipe).first()).map(d -> (Number) d.get("count")).map(d -> d.longValue())
				.orElse(0l);
	}

	@Override
	public List<Catalog> listClassifyRoot() {
		return Arrays.asList(CatalogMapper.classifyProblem(new Document("name", "所有类别")));
	}

	@Override
	public List<Catalog> listClassifyCostStructure(Catalog parent) {
		return c("classifyProblemLost").find(new Document("parent_id", parent._id)).sort(new Document("index", 1))
				.map(CatalogMapper::classifyProblem).into(new ArrayList<>());
	}

	@Override
	public long countClassifyCostStructure(Catalog parent) {
		return c("classifyProblemLost").countDocuments(new Document("parent_id", parent._id));
	}

	@Override
	public Document defaultClassifyCostOption() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		cal.set(year, 0, 1, 0, 0);
		Date start = cal.getTime();
		cal.add(Calendar.YEAR, 1);
		cal.add(Calendar.MINUTE, -1);
		Date end = cal.getTime();
		return new Document("dateRange", Arrays.asList(start, end)).append("xAxis", "month").append("stack", "分列");
	}

	@Override
	public Document createClassifyCostChart(Document condition) {
		return ProblemCostChartRender.renderClassifyCostChart(condition);
	}

	@Override
	public List<Catalog> listClassifyProblemStructure(Catalog parent) {
		return c("classifyProblem").find(new Document("parent_id", parent._id)).sort(new Document("index", 1))
				.map(CatalogMapper::classifyProblem).into(new ArrayList<>());
	}

	@Override
	public long countClassifyProblemStructure(Catalog parent) {
		return c("classifyProblem").countDocuments(new Document("parent_id", parent._id));

	}

	@Override
	public Document createClassifyProblemChart(Document condition) {
		return ProblemCostChartRender.renderClassifyProblemChart(condition);
	}

	@Override
	public Document createClassifyDeptChart(Document condition) {
		return ProblemCostChartRender.renderClassifyDeptChart(condition);
	}

	@Override
	public List<Catalog> listClassifyCauseStructure(Catalog parent) {
		return c("classifyCause").find(new Document("parent_id", parent._id)).sort(new Document("index", 1))
				.map(CatalogMapper::classifyProblem).into(new ArrayList<>());
	}

	@Override
	public long countClassifyCauseStructure(Catalog parent) {
		return c("classifyCause").countDocuments(new Document("parent_id", parent._id));

	}

	@Override
	public Document createClassifyCauseChart(Document condition) {
		return ProblemCostChartRender.renderClassifyCauseChart(condition);
	}

	@Override
	public List<Catalog> listOrgRoot() {
		return c("organization").find(new Document("parent_id", null)).map(CatalogMapper::org).into(new ArrayList<>());
	}

	@Override
	public List<Catalog> listOrgStructure(Catalog parent) {
		return c("organization").find(new Document("parent_id", parent._id)).sort(new Document("index", 1)).map(CatalogMapper::org)
				.into(new ArrayList<>());
	}

	@Override
	public long countOrgStructure(Catalog parent) {
		return c("organization").countDocuments(new Document("parent_id", parent._id));
	}

	@Override
	public Document createCostClassifyByProblemChart() {
		return ProblemCostChartRender.renderCostClassifyByProblemChart();
	}

	@Override
	public Document createCountClassifyByProblemChart() {
		return ProblemCostChartRender.renderCountClassifyByProblemChart();
	}

	@Override
	public Document createCostClassifyByCauseChart() {
		return ProblemCostChartRender.renderCostClassifyByCauseChart();
	}

	@Override
	public Document createCostClassifyByDeptChart() {
		return ProblemCostChartRender.renderCostClassifyByDeptChart();
	}

	@Override
	public List<Document> listSPA(BasicDBObject condition) {
		List<Bson> pipeline = new ArrayList<>();
		pipeline.add(Aggregates.match(new Document("stage", "spa")));
		pipeline.add(Aggregates.lookup("problem", "problem_id", "_id", "problem")); //
		pipeline.add(Aggregates.unwind("$problem"));
		pipeline.add(Aggregates.lookup("d7Similar", "problem._id", "problem_id", "similar"));//
		pipeline.add(Aggregates.addFields(new Field<Document>("similar", new Document("$reduce", new Document("input", "$similar.desc")
				.append("initialValue", "").append("in", new Document("$concat", Arrays.asList("$$value", "$$this", "; ")))))));
		Optional.ofNullable((BasicDBObject) condition.get("filter")).map(Aggregates::match).ifPresent(pipeline::add);
		Optional.ofNullable((BasicDBObject) condition.get("sort")).map(Aggregates::sort).ifPresent(pipeline::add);
		Optional.ofNullable((Integer) condition.get("skip")).map(Aggregates::skip).ifPresent(pipeline::add);
		Optional.ofNullable((Integer) condition.get("limit")).map(Aggregates::limit).ifPresent(pipeline::add);

		return c("problemAction").aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public long countSPA(BasicDBObject filter) {
		return count("problemAction", filter, Aggregates.match(new Document("stage", "spa")),
				Aggregates.lookup("problem", "problem_id", "_id", "problem"), //
				Aggregates.unwind("$problem"), Aggregates.lookup("d7Similar", "problem._id", "problem_id", "similar"), //
				Aggregates.addFields(new Field<Document>("similar", new Document("$reduce", new Document("input", "$similar.desc")
						.append("initialValue", "").append("in", new Document("$concat", Arrays.asList("$$value", "$$this", "; ")))))));
	}

}
