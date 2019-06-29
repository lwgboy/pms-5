package com.bizvisionsoft.serviceimpl;

import java.io.FileInputStream;
import java.io.InputStream;
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
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.common.Service;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.dps.Dispatcher;
import com.bizvisionsoft.service.model.Catalog;
import com.bizvisionsoft.service.model.CauseConsequence;
import com.bizvisionsoft.service.model.ClassifyCause;
import com.bizvisionsoft.service.model.ClassifyProblem;
import com.bizvisionsoft.service.model.ClassifyProblemLost;
import com.bizvisionsoft.service.model.FreqInd;
import com.bizvisionsoft.service.model.IncidenceInd;
import com.bizvisionsoft.service.model.LostInd;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.model.ProblemActionInfo;
import com.bizvisionsoft.service.model.ProblemActionLinkInfo;
import com.bizvisionsoft.service.model.ProblemCostItem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.SeverityInd;
import com.bizvisionsoft.service.provider.BasicDBObjectAdapter;
import com.bizvisionsoft.service.provider.DateAdapter;
import com.bizvisionsoft.service.provider.DocumentAdapter;
import com.bizvisionsoft.service.provider.ObjectIdAdapter;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.renderer.ProblemCardRenderer;
import com.bizvisionsoft.serviceimpl.renderer.ProblemChartRender;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.Function;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;

public class ProblemServiceImpl extends BasicServiceImpl implements ProblemService {

	private Document appendRoleText(Document doc, String lang) {
		return doc.append("roleName", cftRoleText[Integer.parseInt(doc.getString("role"))]);
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
	public Problem get(ObjectId _id, String domain) {
		return get(_id, Problem.class, domain);
	}

	public Problem info(ObjectId _id, String lang, String domain) {
		return get(_id, domain);
	}

	private List<Bson> appendBasicQueryPipeline(BasicDBObject condition, List<Bson> pipeline, String domain) {
		appendOrgFullName(pipeline, "dept_id", "deptName");
		pipeline.add(Aggregates.lookup("organization", "dept_id", "_id", "orgs"));
		pipeline.add(Aggregates.addFields(Arrays.asList(new Field<>("deptName", "$orgs.fullName"))));
		pipeline.add(Aggregates.project(new Document("orgs", false)));
		appendConditionToPipeline(pipeline, condition);
		pipeline.add(Aggregates.lookup("d2ProblemPhoto", "_id", "problem_id", "d2ProblemPhoto"));
		pipeline.addAll(Domain.getJQ(domain, "追加-目标时间的临近性").set("dateField", "$latestTimeReq")
				.set("targetDate", new Date()).set("urgencyIndField", "urgencyInd").array());
		pipeline.addAll(Domain.getJQ(domain, "追加-问题成本合计").array());
		return pipeline;
	}

	@Override
	public long countProblems(BasicDBObject filter, String status, String userid, String lang, String domain) {
		BasicDBObject f = new BasicDBObject();
		Check.isAssigned(filter, s -> f.putAll(s));
		Check.isAssigned(status, s -> f.append("status", status));
		return count(f, "problem", domain);
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
	public long deleteProblem(ObjectId _id, String domain) {
		return delete(_id, Problem.class, domain);
	}

	@Override
	public List<Result> icaConfirm(ObjectId _id, String domain) {
		updateProblems(new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
				.set(new BasicDBObject("confirmD3ICA", true)).bson(), domain);
		return new ArrayList<Result>();
	}

	@Override
	public Problem insertProblem(Problem p, String domain) {
		p.setStatus(Problem.StatusCreated);

		p = insert(p, domain);
		List<Bson> pipe = appendBasicQueryPipeline(new Query().filter(new BasicDBObject("_id", p.get_id())).bson(),
				new ArrayList<>(), domain);
		return c(Problem.class, domain).aggregate(pipe).first();
	}

	@Override
	public List<Problem> listProblems(BasicDBObject condition, String status, String userid, String domain) {
		ensureGet(condition, "filter").append("status", status);
		List<Bson> pipeline = appendBasicQueryPipeline(condition, new ArrayList<>(), domain);
		pipeline.addAll(Domain.getJQ(domain, "追加-问题查询权限").set("userId", userid).array());
		ArrayList<Problem> result = c(Problem.class, domain).aggregate(pipeline).into(new ArrayList<>());
		return result;
	}

	@Override
	public List<Document> listProblemsCard(BasicDBObject condition, String status, String userid, String lang,
			String domain) {
		ensureGet(condition, "filter").append("status", status);
		List<Bson> pipeline = appendBasicQueryPipeline(condition, new ArrayList<>(), domain);
		pipeline.addAll(Domain.getJQ(domain, "追加-问题查询权限").set("userId", userid).array());
		ArrayList<Document> result = c("problem", domain).aggregate(pipeline)
				.map(d -> new ProblemCardRenderer(lang, domain).renderProblem(d, true)).into(new ArrayList<>());
		if ("已创建".equals(status) && result.isEmpty()) {
			result.add(new ProblemCardRenderer(lang, domain)
					.renderActionPlaceHoder(new Document("_action", "create").append("_text", "+")));
		}
		return result;
	}

	@Override
	public long updateProblems(BasicDBObject filterAndUpdate, String domain) {
		long result = update(filterAndUpdate, Problem.class, domain);
		sendMessageOfProblemUpdate(filterAndUpdate, "updated", domain);
		return result;
	}

	@Override
	public long updateProblemsLifecycle(BasicDBObject filterAndUpdate, String msgCode, String domain) {
		long result = update(filterAndUpdate, Problem.class, domain);
		sendMessageOfProblemUpdate(filterAndUpdate, msgCode, domain);
		return result;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 通用行动
	//
	@Override
	public Document getAction(ObjectId _id, String domain) {
		return getDocument(_id, "problemAction", domain);
	}

	@Override
	public long deleteAction(ObjectId _id, String domain) {
		Document doc = findAndDeleteOne(_id, "problemAction", domain);
		if (doc != null) {
			// 通知团队 已创建Action
			sendMessageOfProblemAction(doc, "deleted", domain);
			return 1;
		}
		return 0;
	}

	@Override
	public Document insertAction(Document t, ObjectId problem_id, String stage, String lang, String render,
			String domain) {
		c("problemAction", domain).insertOne(t.append("problem_id", problem_id).append("stage", stage));
		// 判断该成员是否在项目团队中，如果不在，自动加入
		String charger = t.getString("charger");
		long cnt = c("d1CFT", domain).countDocuments(new Document("member", charger).append("problem_id", problem_id));
		if (cnt == 0) {
			int roleId = Arrays.asList(cftRoleText).indexOf(stage.toUpperCase());
			if (roleId != -1) {
				Document charger_meta = (Document) t.get("charger_meta");
				Document d1 = new Document("member_meta", charger_meta).append("member", charger)
						.append("role", "" + roleId).append("problem_id", problem_id);
				insertD1Item(d1, lang, null, domain);
			}
		}
		// 通知团队 已创建Action
		sendMessageOfProblemAction(t, "created", domain);
		if ("card".equals(render)) {
			t = new ProblemCardRenderer(lang, domain).renderAction(t, true);
		} else if ("gridrow".equals(render)) {
			appendActionText(t, lang);
		}
		return t;
	}

	private void sendMessageOfProblemAction(Document action, String msgCode, String domain) {
		String stage = action.getString("stage");
		String subject = getStageName(stage);
		if ("created".equals(msgCode)) {
			subject = subject + "已创建";
		} else if ("deleted".equals(msgCode)) {
			subject = subject + "已删除";
		} else if ("updated".equals(msgCode)) {
			subject = subject + "已更新";
			return;// 更新不必提示
		} else if ("verified".equals(msgCode)) {
			subject = subject + "验证结果已提交";
		} else if ("finished".equals(msgCode)) {
			subject = subject + "已完成";
		} else {
			return;
		}

		ObjectId problem_id = action.getObjectId("problem_id");
		Problem problem = get(problem_id, domain);
		String sender = problem.getCreationInfo().userId;
		String actionName = action.getString("action");
		String content = "问题：" + problem + "<br>" + "行动：" + actionName;
		ArrayList<String> receivers = c("d1CFT", domain).find(new Document("problem_id", problem_id))
				.map(d -> d.getString("member")).into(new ArrayList<>());
		receivers.add(sender);
		sendMessage(subject, content, null, receivers, null, domain);
	}

	private void sendMessageOfProblemTeamUpdate(Document cftData, String msgCode, String domain) {
		String member = cftData.getString("name");
		String content;
		if ("deleted".equals(msgCode)) {
			content = member + "已从团队中移去";
		} else if ("created".equals(msgCode)) {
			content = member + "已加入到团队";
		} else {
			return;
		}

		ObjectId problem_id = cftData.getObjectId("problem_id");
		Problem problem = get(problem_id, domain);
		content = "问题：" + problem + "<br>" + content;
		String sender = problem.getCreationInfo().userId;
		String subject = "问题解决团队更新";
		ArrayList<String> receivers = c("d1CFT", domain).find(new Document("problem_id", problem_id))
				.map(d -> d.getString("member")).into(new ArrayList<>());
		receivers.add(sender);
		sendMessage(subject, content, null, receivers, null, domain);
	}

	private void sendMessageOfProblemUpdate(BasicDBObject filterAndUpdate, String msgCode, String domain) {
		String subject;
		if ("updated".equals(msgCode)) {
			subject = "问题已更新";
			return;// 更新不必提示
		} else if ("canceled".equals(msgCode)) {
			subject = "问题已取消";
		} else if ("closed".equals(msgCode)) {
			subject = "问题已关闭";
		} else if ("started".equals(msgCode)) {
			subject = "问题已启动";
		} else if ("icaConfirmed".equals(msgCode)) {
			subject = "问题临时控制行动的有效性已确认";
		} else if ("pcaApproved".equals(msgCode)) {
			subject = "问题永久纠正措施已批准";
		} else if ("pcaValidated".equals(msgCode)) {
			subject = "问题永久纠正措施的实施效果已通过验证";
		} else if ("pcaConfirmed".equals(msgCode)) {
			subject = "问题永久纠正措施的有效性已确认";
		} else {
			return;
		}

		Problem problem = c(Problem.class, domain).find((BasicDBObject) filterAndUpdate.get("filter")).first();
		if (problem == null) {
			logger.warn("无法获取更新的问题记录");
		}
		String sender = problem.getCreationInfo().userId;
		String content = "问题：" + problem;
		ArrayList<String> receivers = c("d1CFT", domain).find(new Document("problem_id", problem.get_id()))
				.map(d -> d.getString("member")).into(new ArrayList<>());
		receivers.add(sender);
		sendMessage(subject, content, null, receivers, null, domain);

	}

	@Override
	public List<Document> listActions(BasicDBObject condition, ObjectId problem_id, String stage, String lang,
			String render, String domain) {
		boolean editable = isProblemEditable(problem_id, domain);

		Function<Document, Document> f;
		if ("card".equals(render)) {
			f = d -> new ProblemCardRenderer(lang, domain).renderAction(d, editable);
		} else {
			f = d -> {
				appendActionText(d, lang);
				return d;
			};
		}
		// TODO condition
		FindIterable<Document> iter = c("problemAction", domain)
				.find(new Document("problem_id", problem_id).append("stage", stage))
				.sort(new Document("actionType", 1).append("index", 1));
		ArrayList<Document> result = iter.map(f).into(new ArrayList<>());
		if (editable && "card".equals(render) && result.isEmpty())
			result.add(new ProblemCardRenderer(lang, domain)
					.renderActionPlaceHoder(new Document("_action", "create").append("_text", "+")));
		return result;
	}

	private boolean isProblemEditable(ObjectId problem_id, String domain) {
		String status = c("problem", domain).distinct("status", new Document("_id", problem_id), String.class).first();
		boolean editable = "解决中".equals(status);
		return editable;
	}

	@Override
	public Document updateAction(Document doc, String lang, String render, String msgCode, String domain) {
		Document updated = update(doc, "problemAction", domain);
		if (updated != null) {
			Document result;
			if ("card".equals(render)) {
				result = new ProblemCardRenderer(lang, domain).renderAction(updated, true);
			} else {
				result = this.appendActionText(updated, lang);
			}
			sendMessageOfProblemAction(updated, msgCode, domain);
			return result;
		} else {
			return null;
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public List<Document> listD0Init(BasicDBObject condition, ObjectId problem_id, String lang, String render,
			String domain) {
		return c("problem", domain).find(new Document("_id", problem_id))
				.map(d -> new ProblemCardRenderer(lang, domain).renderProblem(d, false)).into(new ArrayList<>());
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// D1 CFT团队
	//
	@Override
	public long deleteD1CFT(ObjectId _id, String domain) {
		Document doc = findAndDeleteOne(_id, "d1CFT", domain);
		if (doc != null) {
			sendMessageOfProblemTeamUpdate(doc, "deleted", domain);
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public Document insertD1Item(Document d1, String lang, String render, String domain) {
		Document user = (Document) d1.get("member_meta");

		ObjectId org_id = user.getObjectId("org_id");
		String userId = d1.getString("member");
		String role = d1.getString("role");
		ObjectId problem_id = d1.getObjectId("problem_id");

		if ("0".equals(role)
				&& c("d1CFT", domain).countDocuments(new Document("problem_id", problem_id).append("role", "0")) > 0)
			throw new ServiceException("违反唯一性规则：一个CFT多功能小组只允许存在一位组长。");

		Document doc = new Document("_id", new ObjectId()).append("problem_id", problem_id).append("member", userId)
				.append("role", role).append("name", user.get("name")).append("mobile", user.get("mobile"))
				.append("position", user.get("position")).append("email", user.get("email"))
				.append("headPics", user.get("headPics"));

		List<Bson> pipe = Domain.getJQ(domain, "查询-用户所在组织-根据组织类型").set("match", new Document("userId", userId))
				.set("orgType", "部门").array();
		pipe.add(Aggregates.sort(new Document("idx", -1)));
		user = c("user", domain).aggregate(pipe).first();

		String dept;
		if (user != null) {
			dept = ((Document) user.get("org")).getString("fullName");
		} else if (org_id != null) {
			dept = getString("organization", "fullName", org_id, domain);
		} else {
			dept = "";
		}
		// TODO 唯一索引，多国语言提示传lang参数
		doc.append("dept", dept);
		c("d1CFT", domain).insertOne(doc);

		// 发送消息
		sendMessageOfProblemTeamUpdate(doc, "created", domain);

		// 渲染卡片
		if ("card".equals(render))
			return new ProblemCardRenderer(lang, domain).renderD1CFTMember(doc, lang, true);
		else if ("gridrow".equals(render))
			return appendRoleText(doc, lang);
		else
			return null;
	}

	@Override
	public void insertD1Items(List<Document> d1s, String domain) {
		List<Document> inserts = new ArrayList<Document>();
		d1s.forEach(d1 -> {
			Document user = (Document) d1.get("member_meta");
			ObjectId org_id = user.getObjectId("org_id");
			String userId = d1.getString("member");
			String role = d1.getString("role");
			ObjectId problem_id = d1.getObjectId("problem_id");

			if ("0".equals(role) && c("d1CFT", domain)
					.countDocuments(new Document("problem_id", problem_id).append("role", "0")) > 0)
				throw new ServiceException("违反唯一性规则：一个CFT多功能小组只允许存在一位组长。");

			Document doc = new Document("_id", new ObjectId()).append("problem_id", problem_id).append("member", userId)
					.append("role", role).append("name", user.get("name")).append("mobile", user.get("mobile"))
					.append("position", user.get("position")).append("email", user.get("email"))
					.append("headPics", user.get("headPics"));

			List<Bson> pipe = Domain.getJQ(domain, "查询-用户所在组织-根据组织类型").set("match", new Document("userId", userId))
					.set("orgType", "部门").array();
			pipe.add(Aggregates.sort(new Document("idx", -1)));
			user = c("user", domain).aggregate(pipe).first();

			String dept;
			if (user != null) {
				dept = ((Document) user.get("org")).getString("fullName");
			} else if (org_id != null) {
				dept = getString("organization", "fullName", org_id, domain);
			} else {
				dept = "";
			}
			doc.append("dept", dept);
			inserts.add(doc);
		});
		c("d1CFT", domain).insertMany(inserts);
		inserts.forEach(doc -> {
			sendMessageOfProblemTeamUpdate(doc, "created", domain);
		});
	}

	@Override
	public List<Document> listD1(BasicDBObject condition, ObjectId problem_id, String userId, String lang,
			String render, String domain) {
		ensureGet(condition, "sort").append("role", 1).append("_id", -1);
		List<Bson> pipeline = createDxPipeline(condition, problem_id);
		Function<Document, Document> f;
		if ("card".equals(render)) {
			boolean editable = isProblemEditable(problem_id, domain)
					&& new ProblemActionControl().hasPrivate(problem_id, ACTION_EDIT_TEAM, userId, domain);
			f = d -> new ProblemCardRenderer(lang, domain).renderD1CFTMember(d, lang, editable);
		} else {
			f = d -> appendRoleText(d, lang);
		}
		ArrayList<Document> result = c("d1CFT", domain).aggregate(pipeline).map(f).into(new ArrayList<>());
		if ("card".equals(render) && result.isEmpty())
			result.add(new ProblemCardRenderer(lang, domain)
					.renderActionPlaceHoder(new Document("_action", "create").append("_text", "+")));
		return result;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// D2 问题描述，现场照片等
	//
	@Override
	public Document updateD2ProblemDesc(Document d, String lang, String domain) {
		Document filter = new Document("_id", d.get("_id"));
		Document set = new Document("$set", d);
		return new ProblemCardRenderer(lang, domain).renderD25W2H(c("d2ProblemDesc", domain).findOneAndUpdate(filter,
				set, new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)));
	}

	@Override
	public Document getD2ProblemDesc(ObjectId problem_id, String domain) {
		return Optional.ofNullable(c("d2ProblemDesc", domain).find(new Document("_id", problem_id)).first())
				.orElse(new Document("_id", problem_id));
	}

	@Override
	public long deleteD2ProblemPhotos(ObjectId _id, String domain) {
		Document delete = c("d2ProblemPhoto", domain).findOneAndDelete(new BasicDBObject("_id", _id));
		if (delete != null) {
			deleteFileInField(delete, "problemImg", domain);
			return 1;
		}
		return 0;
	}

	@Override
	public Document insertD2ProblemPhoto(Document t, String lang, String render, String domain) {
		c("d2ProblemPhoto", domain).insertOne(t);
		if ("card".equals(render))
			return new ProblemCardRenderer(lang, domain).renderD2PhotoCard(t);
		return t;
	}

	@Override
	public void insertD2ProblemPhotos(List<Document> t, String domain) {
		c("d2ProblemPhoto", domain).insertMany(t);
	}

	public List<Document> listD2ProblemPhotos(ObjectId problem_id, String domain) {
		return c("d2ProblemPhoto", domain).find(new Document("problem_id", problem_id)).sort(new Document("_id", -1))
				.into(new ArrayList<>());
	}

	@Override
	public List<Document> listD2(BasicDBObject condition, ObjectId problem_id, String lang, String domain) {
		List<Document> result = new ArrayList<>();
		// photos
		listD2ProblemPhotos(problem_id, domain)
				.forEach(d -> result.add(new ProblemCardRenderer(lang, domain).renderD2PhotoCard(d)));
		// 5w2h
		Document doc = getD2ProblemDesc(problem_id, domain);
		if (doc.get("what") != null)
			result.add(new ProblemCardRenderer(lang, domain).renderD25W2H(doc));

		if (result.isEmpty()) {
			String text = "<img src='rwt-resources/extres/img/photo_w.svg' style='width='36px' height='36px'>";
			result.add(new ProblemCardRenderer(lang, domain)
					.renderActionPlaceHoder(new Document("_action", "createPhoto").append("_text", text)));
		}
		return result;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// D4 根本原因
	//
	@Override
	public long countCauseConsequences(BasicDBObject filter, String domain) {
		return count(filter, CauseConsequence.class, domain);
	}

	@Override
	public long deleteCauseConsequence(ObjectId _id, String domain) {
		return delete(_id, "causeRelation", domain);
	}

	@Override
	public Document getD4RootCauseDesc(ObjectId problem_id, String domain) {
		return getDocument(problem_id, "d4RootCauseDesc", domain);
	}

	@Override
	public void insertD4RootCauseDesc(Document t, String lang, String domain) {
		c("d4RootCauseDesc", domain).insertOne(t);
	}

	@Override
	public void updateD4RootCauseDesc(BasicDBObject fu, String lang, String domain) {
		update(fu, "d4RootCauseDesc", domain);
	}

	@Override
	public CauseConsequence insertCauseConsequence(CauseConsequence cc, String domain) {
		return insert(cc, domain);
	}

	@Override
	public void insertCauseConsequences(List<CauseConsequence> cc, String domain) {
		c(CauseConsequence.class, domain).insertMany(cc);
	}

	@Override
	public long updateCauseConsequence(BasicDBObject fu, String domain) {
		return super.update(fu, CauseConsequence.class, domain);
	}

	@Override
	public Document getCauseConsequence(ObjectId problem_id, String type, String domain) {
		Problem problem = get(problem_id, domain);
		List<Document> data = new ArrayList<>();
		List<Document> links = new ArrayList<>();

		List<String> causeSubject = c("classifyCause", domain).find(new Document("parent_id", null))
				.map(d -> d.getString("name")).into(new ArrayList<>());
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

		c("causeRelation", domain).find(new Document("problem_id", problem_id).append("type", type))
				.forEach((Document d) -> {
					String id = d.getObjectId("_id").toHexString();
					Document item = new Document("id", id)//
							.append("name", d.get("name"))//
							.append("desc", Optional.ofNullable(d.getString("description")).orElse(""))//
							.append("draggable", true)//
							.append("category", d.getString("subject"))//
							.append("symbolSize", 5 * d.getInteger("weight", 1))//
							.append("value", 100 * d.getDouble("probability"));
					data.add(item);
					String parentId = Optional.ofNullable(d.getObjectId("parent_id")).map(p -> p.toHexString())
							.orElse(d.getString("subject"));
					links.add(new Document("source", parentId).append("target", id).append("emphasis",
							new Document("label", new Document("show", false))));
				});

		List<Document> categories = new ArrayList<>();
		categories.add(new Document("name", "问题"));
		categories.add(new Document("name", "类别"));
		categories.addAll(causeSubject.stream().map(e -> new Document("name", e)).collect(Collectors.toList()));

		return Domain.getJQ(domain, "图表-因果关系图").set("data", data).set("links", links).set("categories", categories)
				.doc();
	}

	@Override
	public List<CauseConsequence> listCauseConsequences(BasicDBObject filter, String domain) {
		return createDataSet(new BasicDBObject("filter", filter), CauseConsequence.class, domain);
	}

	@Override
	public List<Document> listD4(BasicDBObject condition, ObjectId problem_id, String lang, String domain) {
		List<Document> result = new ArrayList<>();
		// 根本问题描述

		Document data = getD4RootCauseDesc(problem_id, domain);
		Document rcdCard = null;
		Document epCard = null;
		if (data != null) {
			rcdCard = new ProblemCardRenderer(lang, domain).renderD4(data, "make", data.getString("rootCauseDesc"),
					(Document) data.get("charger_meta"), data.getDate("date"));
			epCard = new ProblemCardRenderer(lang, domain).renderD4(data, "out", data.getString("escapePoint"),
					(Document) data.get("charger_meta"), data.getDate("date"));
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
		c("causeRelation", domain).aggregate(pipe)
				.map(d -> new ProblemCardRenderer(lang, domain).renderD4CauseConsequence(d)).into(result);
		// 问题流出的原因
		if (epCard != null) {
			result.add(epCard);
		}
		pipe.clear();
		pipe.add(new Document("$match", new Document("problem_id", problem_id).append("type", "因果分析-流出")));
		pipe.add(new Document("$lookup", new Document("from", "causeRelation").append("localField", "_id")
				.append("foreignField", "parent_id").append("as", "parent")));
		pipe.add(new Document("$match", new Document("parent", new Document("$size", 0))));
		c("causeRelation", domain).aggregate(pipe)
				.map(d -> new ProblemCardRenderer(lang, domain).renderD4CauseConsequence(d)).into(result);

		return result;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// D5 PCA选择
	//
	@Override
	public long deleteD5PCA(ObjectId _id, String lang, String domain) {
		return delete(_id, "d5PCA", domain);
	}

	@Override
	public void insertD5DecisionCriteria(Document t, String language, String domain) {
		c("d5DecisionCriteria", domain).insertOne(t);
	}

	@Override
	public Document getD5DecisionCriteria(ObjectId problem_id, String domain) {
		return c("d5DecisionCriteria", domain).find(new Document("_id", problem_id)).first();
	}

	@Override
	public void insertD5PCA(Document t, String language, String domain) {
		c("d5PCA", domain).insertOne(t);
	}

	@Override
	public List<Document> listD5(BasicDBObject condition, ObjectId problem_id, String lang, String domain) {
		List<Document> result = new ArrayList<>();
		Document d = c("d5PCA", domain).find(new Document("problem_id", problem_id).append("selected", true)).first();
		if (d != null) {
			Date planStart = d.getDate("planStart1");
			Date planFinish = d.getDate("planFinish1");

			result.add(new ProblemCardRenderer(lang, domain).renderD5PCA((List<?>) d.get("pca1"), "问题产生纠正措施",
					(Document) d.get("charger1_meta"), planStart, planFinish));

			planStart = d.getDate("planStart2");
			planFinish = d.getDate("planFinish2");

			result.add(new ProblemCardRenderer(lang, domain).renderD5PCA((List<?>) d.get("pca2"), "问题流出纠正措施",
					(Document) d.get("charger2_meta"), planStart, planFinish));
		}
		return result;
	}

	@Override
	public List<Document> listD5PCA(ObjectId problem_id, String lang, String domain) {
		return c("d5PCA", domain).find(new Document("problem_id", problem_id)).into(new ArrayList<>());
	}

	@Override
	public void updateD5DecisionCriteria(BasicDBObject fu, String lang, String domain) {
		update(fu, "d5DecisionCriteria", domain);
	}

	@Override
	public void updateD5PCA(BasicDBObject fu, String lang, String domain) {
		update(fu, "d5PCA", domain);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// D7 预防
	//
	@Override
	public long deleteD7Similar(ObjectId _id, String domain) {
		return delete(_id, "d7Similar", domain);
	}

	@Override
	public Document getD7Similar(ObjectId _id, String domain) {
		return getDocument(_id, "d7Similar", domain);
	}

	@Override
	public Document insertD7Similar(Document t, String lang, String render, String domain) {
		t.append("_id", new ObjectId());
		c("d7Similar", domain).insertOne(t);
		if ("card".equals(render))
			return new ProblemCardRenderer(lang, domain).renderD7Similar(t);
		else
			return appendDegreeText(t, lang);
	}

	@Override
	public void insertD7Similars(List<Document> t, String domain) {
		c("d7Similar", domain).insertMany(t);
	}

	@Override
	public Document updateD7Similar(Document d, String lang, String render, String domain) {
		if ("card".equals(render))
			return updateThen(d, lang, "d7Similar", domain,
					(doc, l) -> new ProblemCardRenderer(lang, domain).renderD7Similar(doc));
		else
			return updateThen(d, lang, "d7Similar", domain, this::appendDegreeText);

	}

	@Override
	public List<Document> listD7Similar(BasicDBObject condition, ObjectId problem_id, String lang, String render,
			String domain) {
		return c("d7Similar", domain).find(new Document("problem_id", problem_id)).sort(new Document("degree", 1))
				.map(d -> appendDegreeText(d, lang)).into(new ArrayList<>());
	}

	@Override
	public List<Document> listD7(BasicDBObject condition, ObjectId problem_id, String lang, String render,
			String domain) {
		List<Document> result = new ArrayList<>();
		c("d7Similar", domain).find(new Document("problem_id", problem_id)).sort(new Document("degree", 1))
				.map(d -> new ProblemCardRenderer(lang, domain).renderD7Similar(d)).into(result);

		result.addAll(listActions(condition, problem_id, "spa", lang, render, domain));

		return result;
	}

	private Document appendDegreeText(Document d, String lang) {
		return d.append("dgreeText", similarDegreeText[Integer.parseInt(d.getString("degree"))]);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// D8 关闭
	//
	@Override
	public Document updateD8Exp(Document t, String lang, String render, String domain) {
		List<String> result = extractKeywords(t, 10, "name");// 确保
		result.addAll(extractKeywords(t, 20, "advantage", "weakness"));// 优先
		result.addAll(extractKeywords(t, 100, "method", "advantage", "weakness"));
		if (!result.isEmpty()) {
			t.append("keyword", result);
		}
		if ("card".equals(render)) {
			return updateThen(t, lang, "d8Exp", domain,
					(doc, l) -> new ProblemCardRenderer(lang, domain).renderD8Exp(doc));
		} else {
			return updateThen(t, lang, "d8Exp", domain, null);
		}
	}

	@Override
	public List<Document> listD8(BasicDBObject condition, ObjectId problem_id, String lang, String render,
			String domain) {
		if ("card".equals(render)) {
			return c("d8Exp", domain).find(new Document("problem_id", problem_id))
					.map(d -> new ProblemCardRenderer(lang, domain).renderD8Exp(d)).into(new ArrayList<>());
		} else {
			return c("d8Exp", domain).find(new Document("problem_id", problem_id)).into(new ArrayList<>());
		}
	}

	@Override
	public Document getD8Exp(ObjectId _id, String domain) {
		return getDocument(_id, "d8Exp", domain);
	}

	@Override
	public long deleteD8Exp(ObjectId _id, String domain) {
		return delete(_id, "d8Exp", domain);
	}

	@Override
	public Document insertD8Experience(Document t, String lang, String render, String domain) {
		t.append("_id", new ObjectId());
		List<String> result = extractKeywords(t, 10, "name");// 确保
		result.addAll(extractKeywords(t, 20, "advantage", "weakness"));// 优先
		result.addAll(extractKeywords(t, 100, "method", "advantage", "weakness"));
		if (!result.isEmpty()) {
			t.append("keyword", result);
		}
		c("d8Exp", domain).insertOne(t);
		if ("card".equals(render))
			return new ProblemCardRenderer(lang, domain).renderD8Exp(t);
		return t;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 问题严重性级别
	//
	@Override
	public List<SeverityInd> listSeverityInd(String domain) {
		return c(SeverityInd.class, domain).find().sort(new BasicDBObject("_id", 1)).into(new ArrayList<SeverityInd>());
	}

	@Override
	public SeverityInd insertSeverityInd(SeverityInd item, String domain) {
		return insert(item, domain);
	}

	@Override
	public long deleteSeverityInd(ObjectId _id, String domain) {
		return delete(_id, SeverityInd.class, domain);
	}

	@Override
	public long updateSeverityInd(BasicDBObject fu, String domain) {
		return update(fu, SeverityInd.class, domain);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 问题损失级别
	//
	@Override
	public List<LostInd> listLostInd(String domain) {
		return c(LostInd.class, domain).find().sort(new BasicDBObject("_id", 1)).into(new ArrayList<>());
	}

	@Override
	public LostInd insertLostInd(LostInd item, String domain) {
		return insert(item, domain);
	}

	@Override
	public long deleteLostInd(ObjectId _id, String domain) {
		return delete(_id, LostInd.class, domain);
	}

	@Override
	public long updateLostInd(BasicDBObject fu, String domain) {
		return update(fu, LostInd.class, domain);
	}

	@Override
	public List<FreqInd> listFreqInd(String domain) {
		return c(FreqInd.class, domain).find().sort(new BasicDBObject("_id", 1)).into(new ArrayList<>());
	}

	@Override
	public FreqInd insertFreqInd(FreqInd item, String domain) {
		return insert(item, domain);
	}

	@Override
	public long deleteFreqInd(ObjectId _id, String domain) {
		return delete(_id, FreqInd.class, domain);
	}

	@Override
	public long updateFreqInd(BasicDBObject fu, String domain) {
		return update(fu, FreqInd.class, domain);
	}

	@Override
	public List<IncidenceInd> listIncidenceInd(String domain) {
		return c(IncidenceInd.class, domain).find().sort(new BasicDBObject("_id", 1)).into(new ArrayList<>());
	}

	@Override
	public IncidenceInd insertIncidenceInd(IncidenceInd item, String domain) {
		return insert(item, domain);
	}

	@Override
	public long deleteIncidenceInd(ObjectId _id, String domain) {
		return delete(_id, IncidenceInd.class, domain);
	}

	@Override
	public long updateIncidenceInd(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, IncidenceInd.class, domain);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	private <T> List<T> listClassifyItems(BasicDBObject filter, Class<T> clazz, String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();
		if (filter != null) {
			pipeline.add(Aggregates.match(filter));
		}
		pipeline.add(Aggregates.sort(new BasicDBObject("id", 1)));
		pipeline.addAll(Domain.getJQ(domain, "查询-通用-层次结构-增加isLeaf和Path")
				.set("from", clazz.getAnnotation(PersistenceCollection.class).value()).array());

		return c(clazz, domain).aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public List<ClassifyProblemLost> rootClassifyProblemLost(String domain) {
		return listClassifyProblemLost(new BasicDBObject("parent_id", null), domain);
	}

	@Override
	public List<ClassifyProblemLost> listClassifyProblemLost(BasicDBObject filter, String domain) {
		return listClassifyItems(filter, ClassifyProblemLost.class, domain);
	}

	@Override
	public long countClassifyProblemLost(ObjectId parent_id, String domain) {
		return count(new BasicDBObject("parent_id", parent_id), "classifyProblemLost", domain);
	}

	@Override
	public ClassifyProblemLost insertClassifyProblemLost(ClassifyProblemLost ai, String domain) {
		return insert(ai, domain);
	}

	@Override
	public long deleteClassifyProblemLost(ObjectId _id, String domain) {
		return delete(_id, "classifyProblemLost", domain);
	}

	@Override
	public long updateClassifyProblemLost(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, "classifyProblemLost", domain);
	}

	@Override
	public List<ClassifyProblem> rootClassifyProblem(String domain) {
		return listClassifyProblem(new BasicDBObject("parent_id", null), domain);
	}

	@Override
	public List<ClassifyProblem> listClassifyProblem(BasicDBObject filter, String domain) {
		return listClassifyItems(filter, ClassifyProblem.class, domain);
	}

	@Override
	public long countClassifyProblem(ObjectId parent_id, String domain) {
		return count(new BasicDBObject("parent_id", parent_id), "classifyProblem", domain);
	}

	@Override
	public ClassifyProblem insertClassifyProblem(ClassifyProblem ai, String domain) {
		return insert(ai, domain);
	}

	@Override
	public long deleteClassifyProblem(ObjectId _id, String domain) {
		return delete(_id, "classifyProblem", domain);
	}

	@Override
	public long updateClassifyProblem(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, "classifyProblem", domain);
	}

	@Override
	public List<ClassifyCause> rootClassifyCause(String domain) {
		return listClassifyCause(new BasicDBObject("parent_id", null), domain);
	}

	@Override
	public List<ClassifyCause> listClassifyCause(BasicDBObject filter, String domain) {
		return listClassifyItems(filter, ClassifyCause.class, domain);
	}

	@Override
	public List<ClassifyCause> rootClassifyCauseSelector(CauseConsequence cc, String domain) {
		if (cc == null)
			return new ArrayList<>();
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("name", cc.getSubject())));
		pipeline.add(Aggregates.lookup("classifyCause", "_id", "parent_id", "children1"));
		pipeline.add(Aggregates.unwind("$children1"));
		pipeline.add(Aggregates.replaceRoot("$children1"));
		pipeline.add(Aggregates.sort(new BasicDBObject("id", 1)));
		pipeline.addAll(Domain.getJQ(domain, "查询-通用-层次结构-增加isLeaf和Path").set("from", "classifyCause").array());
		debugPipeline(pipeline);
		return c(ClassifyCause.class, domain).aggregate(pipeline).into(new ArrayList<>());

	}

	@Override
	public List<ClassifyCause> rootClassifyCauseSelector(Document doc, String domain) {
		if (doc == null)
			return new ArrayList<>();
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("name", doc.get("subject"))));
		pipeline.add(Aggregates.lookup("classifyCause", "_id", "parent_id", "children1"));
		pipeline.add(Aggregates.unwind("$children1"));
		pipeline.add(Aggregates.replaceRoot("$children1"));
		pipeline.add(Aggregates.sort(new BasicDBObject("id", 1)));
		pipeline.addAll(Domain.getJQ(domain, "查询-通用-层次结构-增加isLeaf和Path").set("from", "classifyCause").array());
		debugPipeline(pipeline);
		return c(ClassifyCause.class, domain).aggregate(pipeline).into(new ArrayList<>());

	}

	@Override
	public long countClassifyCause(ObjectId parent_id, String domain) {
		return count(new BasicDBObject("parent_id", parent_id), "classifyCause", domain);
	}

	@Override
	public ClassifyCause insertClassifyCause(ClassifyCause ai, String domain) {
		return insert(ai, domain);
	}

	@Override
	public long deleteClassifyCause(ObjectId _id, String domain) {
		return delete(_id, "classifyCause", domain);
	}

	@Override
	public long updateClassifyCause(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, "classifyCause", domain);
	}

	@Override
	public long deleteCostItem(ObjectId _id, String domain) {
		return delete(_id, "problemCostItem", domain);
	}

	@Override
	public List<ProblemCostItem> listCostItems(BasicDBObject condition, ObjectId problem_id, String domain) {
		ensureGet(condition, "filter").append("problem_id", problem_id);
		ensureGet(condition, "sort").append("date", -1);
		return list(ProblemCostItem.class, domain, condition, Aggregates.addFields(
				new Field<Document>("summary", new Document("$subtract", Arrays.asList("$drAmount", "$crAmount")))));
	}

	@Override
	public long countCostItems(BasicDBObject filter, ObjectId problem_id, String domain) {
		if (filter == null)
			filter = new BasicDBObject();
		filter.append("problem_id", problem_id);
		return count(filter, "problemCostItem", domain);
	}

	@Override
	public long updateCostItems(BasicDBObject fu, String domain) {
		return update(fu, "problemCostItem", domain);
	}

	@Override
	public ProblemCostItem insertCostItem(ProblemCostItem p, ObjectId problem_id, String domain) {
		p.setProblem_id(problem_id);
		insert(p, domain);
		BasicDBObject cond = new Query().filter(new BasicDBObject("_id", p.get_id())).bson();
		return listCostItems(cond, problem_id, domain).get(0);
	}

	@Override
	public Document getSummaryCost(ObjectId problem_id, String domain) {
		List<Document> pipe = Arrays.asList(new Document("$match", new Document("problem_id", problem_id)),
				new Document("$group",
						new Document("_id", "$problem_id").append("drAmount", new Document("$sum", "$drAmount"))
								.append("crAmount", new Document("$sum", "$crAmount"))),
				new Document("$addFields",
						new Document("summary", new Document("$subtract", Arrays.asList("$drAmount", "$crAmount")))));
		debugPipeline(pipe);
		return c("problemCostItem", domain).aggregate(pipe).first();
	}

	@Override
	public Document periodCostChart(ObjectId problem_id, String domain) {
		List<String> x = new ArrayList<>();
		Set<String> legend = new HashSet<>();
		Map<String, Document> ds = new HashMap<>();
		c("problemCostItem", domain).aggregate(
				Domain.getJQ(domain, "查询-问题成本-根科目和年月汇总").set("match", new Document("problem_id", problem_id)).array())
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
		if (x.isEmpty())
			return blankChart(domain);

		List<String> xAxis = new ArrayList<>();
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

		List<Document> series = legend.stream().map(l -> {
			List<Double> data = xAxis.stream().map(s -> ds.get(l).get(s, 0d)).collect(Collectors.toList());
			return new Document("name", l).append("type", "bar").append("barGap", 0).append("data", data);
		}).collect(Collectors.toList());

		return Domain.getJQ(domain, "图表-通用-多维堆叠柱图").set("legend", legend).set("xAxis", xAxis).set("series", series)
				.doc();
	}

	@Override
	public List<Document> listAdvisablePlan(BasicDBObject condition, ObjectId problem_id, String stage, String domain) {
		List<Bson> pipeline = createAdvisePipeline(problem_id, stage, domain);
		Optional.ofNullable((BasicDBObject) condition.get("filter")).ifPresent(f -> pipeline.add(Aggregates.match(f)));
		pipeline.add(Aggregates.sort(ensureGet(condition, "sort").append("_id", -1)));
		Optional.ofNullable((Integer) condition.get("skip")).ifPresent(f -> pipeline.add(Aggregates.skip(f)));
		Optional.ofNullable((Integer) condition.get("limit")).ifPresent(f -> pipeline.add(Aggregates.limit(f)));
		return c("problem", domain).aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public long countAdvisablePlan(BasicDBObject filter, ObjectId problem_id, String stage, String domain) {
		List<Bson> pipeline = createAdvisePipeline(problem_id, stage, domain);
		if (filter != null)
			pipeline.add(new BasicDBObject("$match", filter));
		pipeline.add(Aggregates.count());
		return Optional.ofNullable(c("problem", domain).aggregate(pipeline).first()).map(d -> (Number) d.get("count"))
				.map(d -> d.longValue()).orElse(0l);
	}

	private List<Bson> createAdvisePipeline(ObjectId problem_id, String stage, String domain) {
		String stageName = getStageName(stage);
		return Domain.getJQ(domain, "查询-行动预案-匹配问题").set("problem_id", problem_id)
				.set("in", Arrays.asList(stageName, "$stage")).array();
	}

	private String getStageName(String stage) {
		return actionName[Arrays.asList(actionType).indexOf(stage)];
	}

	@Override
	public List<Document> listCriteriaTemplate(BasicDBObject condition, ObjectId problem_id, String domain) {
		List<Bson> pipeline = Domain.getJQ(domain, "查询-目标和决策模板-匹配问题").set("problem_id", problem_id).array();
		Optional.ofNullable((BasicDBObject) condition.get("filter")).ifPresent(f -> pipeline.add(Aggregates.match(f)));
		pipeline.add(Aggregates.sort(ensureGet(condition, "sort").append("_id", -1)));
		Optional.ofNullable((Integer) condition.get("skip")).ifPresent(f -> pipeline.add(Aggregates.skip(f)));
		Optional.ofNullable((Integer) condition.get("limit")).ifPresent(f -> pipeline.add(Aggregates.limit(f)));
		return c("problem", domain).aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public long countCriteriaTemplate(BasicDBObject filter, ObjectId problem_id, String domain) {
		List<Bson> pipeline = Domain.getJQ(domain, "查询-目标和决策模板-匹配问题").set("problem_id", problem_id).array();
		if (filter != null)
			pipeline.add(new BasicDBObject("$match", filter));
		pipeline.add(Aggregates.count());
		return Optional.ofNullable(c("problem", domain).aggregate(pipeline).first()).map(d -> (Number) d.get("count"))
				.map(d -> d.longValue()).orElse(0l);
	}

	@Override
	public List<Catalog> listClassifyRoot(String domain) {
		return Arrays.asList(CatalogMapper.classifyProblem(new Document("name", "所有类别")));
	}

	@Override
	public List<Catalog> listClassifyCostStructure(Catalog parent, String domain) {
		return c("classifyProblemLost", domain).find(new Document("parent_id", parent._id))
				.sort(new Document("index", 1)).map(CatalogMapper::classifyProblem).into(new ArrayList<>());
	}

	@Override
	public long countClassifyCostStructure(Catalog parent, String domain) {
		return c("classifyProblemLost", domain).countDocuments(new Document("parent_id", parent._id));
	}

	@Override
	public Document defaultClassifyCostOption(String domain) {
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
	public Document createClassifyCostChart(Document condition, String domain) {
		return ProblemChartRender.renderClassifyCostChart(condition, domain);
	}

	@Override
	public List<Catalog> listClassifyProblemStructure(Catalog parent, String domain) {
		return c("classifyProblem", domain)
				.find(new Document("parent_id", Optional.ofNullable(parent).map(p -> p._id).orElse(null)))
				.sort(new Document("index", 1)).map(CatalogMapper::classifyProblem).into(new ArrayList<>());
	}

	@Override
	public long countClassifyProblemStructure(Catalog parent, String domain) {
		return c("classifyProblem", domain).countDocuments(new Document("parent_id", parent._id));

	}

	@Override
	public Document createClassifyProblemChart(Document condition, String domain) {
		return ProblemChartRender.renderClassifyProblemChart(condition, domain);
	}

	@Override
	public Document createClassifyDeptChart(Document condition, String domain) {
		return ProblemChartRender.renderClassifyDeptChart(condition, domain);
	}

	@Override
	public List<Catalog> listClassifyCauseStructure(Catalog parent, String domain) {
		return c("classifyCause", domain).find(new Document("parent_id", parent._id)).sort(new Document("index", 1))
				.map(CatalogMapper::classifyProblem).into(new ArrayList<>());
	}

	@Override
	public long countClassifyCauseStructure(Catalog parent, String domain) {
		return c("classifyCause", domain).countDocuments(new Document("parent_id", parent._id));

	}

	@Override
	public Document createClassifyCauseChart(Document condition, String domain) {
		return ProblemChartRender.renderClassifyCauseChart(condition, domain);
	}

	@Override
	public List<Catalog> listOrgRoot(String domain) {
		return c("organization", domain).find(new Document("parent_id", null)).map(CatalogMapper::org)
				.into(new ArrayList<>());
	}

	@Override
	public List<Catalog> listOrgStructure(Catalog parent, String domain) {
		return c("organization", domain).find(new Document("parent_id", parent._id)).sort(new Document("index", 1))
				.map(CatalogMapper::org).into(new ArrayList<>());
	}

	@Override
	public long countOrgStructure(Catalog parent, String domain) {
		return c("organization", domain).countDocuments(new Document("parent_id", parent._id));
	}

	@Override
	public Document createCostClassifyByProblemChart(String domain) {
		return ProblemChartRender.renderCostClassifyByProblemChart(domain);
	}

	@Override
	public Document createCountClassifyByProblemChart(String domain) {
		return ProblemChartRender.renderCountClassifyByProblemChart(domain);
	}

	@Override
	public Document createCostClassifyByCauseChart(String domain) {
		return ProblemChartRender.renderCostClassifyByCauseChart(domain);
	}

	@Override
	public Document createCostClassifyByDeptChart(String domain) {
		return ProblemChartRender.renderCostClassifyByDeptChart(domain);
	}

	@Override
	public Document createCauseProblemChart(String domain) {
		return ProblemChartRender.renderCauseProblemChart(domain);
	}

	@Override
	public List<Document> listActions(BasicDBObject condition, String stage, String domain) {
		List<Bson> pipeline = new ArrayList<>();
		pipeline.add(Aggregates.match(new Document("stage", stage)));
		pipeline.add(Aggregates.lookup("problem", "problem_id", "_id", "problem")); //
		pipeline.add(Aggregates.unwind("$problem"));
		pipeline.add(Aggregates.lookup("d7Similar", "problem._id", "problem_id", "similar"));//
		pipeline.add(Aggregates.addFields(new Field<Document>("similar",
				new Document("$reduce", new Document("input", "$similar.desc").append("initialValue", "").append("in",
						new Document("$concat", Arrays.asList("$$value", "$$this", "; ")))))));
		appendConditionToPipeline(pipeline, condition);

		return c("problemAction", domain).aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public long countActions(BasicDBObject filter, String stage, String domain) {
		return count("problemAction", domain, filter, Aggregates.match(new Document("stage", stage)),
				Aggregates.lookup("problem", "problem_id", "_id", "problem"), //
				Aggregates.unwind("$problem"), Aggregates.lookup("d7Similar", "problem._id", "problem_id", "similar"), //
				Aggregates.addFields(new Field<Document>("similar",
						new Document("$reduce", new Document("input", "$similar.desc").append("initialValue", "")
								.append("in", new Document("$concat", Arrays.asList("$$value", "$$this", "; ")))))));
	}

	@Override
	public List<Document> listExp(BasicDBObject condition, String domain) {
		List<Bson> pipe = new ArrayList<>();
		pipe.add(new Document("$lookup", new Document("from", "problem").append("localField", "problem_id")
				.append("foreignField", "_id").append("as", "problem")));
		pipe.add(new Document("$unwind", new Document("path", "$problem").append("preserveNullAndEmptyArrays", true)));
		appendConditionToPipeline(pipe, condition);
		return c("d8Exp", domain).aggregate(pipe).into(new ArrayList<>());
	}

	@Override
	public long countExp(BasicDBObject filter, String domain) {
		List<Bson> prefixPipelines = new ArrayList<>();
		prefixPipelines.add(new Document("$lookup", new Document("from", "problem").append("localField", "problem_id")
				.append("foreignField", "_id").append("as", "problem")));
		prefixPipelines.add(
				new Document("$unwind", new Document("path", "$problem").append("preserveNullAndEmptyArrays", true)));
		return count("d8Exp", domain, filter, prefixPipelines);
	}

	@Override
	public List<ProblemActionInfo> listGanttActions(ObjectId problem_id, String domain) {
		List<ProblemActionInfo> result = new ArrayList<>();

		String[][] stages = new String[][] { { "era", "紧急应对行动" }, { "ica", "临时控制行动" }, { "pca", "永久纠正措施" },
				{ "spa", "系统性预防措施" }, { "lra", "挽回和善后措施" } };

		for (int i = 0; i < stages.length; i++) {
			List<ProblemActionInfo> actions = listGanttStageActions(problem_id, stages[i][0], domain);
			if (!actions.isEmpty()) {
				ProblemActionInfo stage = new ProblemActionInfo();
				stage._id = new ObjectId();
				stage.stage = stages[i][0];
				stage.text = stages[i][1];
				stage.chargerInfo = "";
				stage.index = i;
				stage.type = "project";
				actions.forEach(p -> {
					if (stage.start_date == null || stage.start_date.after(p.start_date))
						stage.start_date = p.start_date;
					if (stage.end_date == null || stage.end_date.before(p.end_date)) {
						stage.end_date = p.end_date;
					}
					p.type = "task";
					p.parent_id = stage._id;
				});
				result.add(stage);
				result.addAll(actions);
			}
		}
		return result;
	}

	private List<ProblemActionInfo> listGanttStageActions(ObjectId problem_id, String stage, String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(new Document("$match", new Document("problem_id", problem_id).append("stage", stage)));
		pipeline.add(new Document("$project", //
				new Document("text", "$action")//
						.append("start_date", "$planStart")//
						.append("end_date", "$planFinish")//
						.append("index", true)//
						.append("chargerInfo", "$charger_meta.name")//
						.append("finished", "$finish")//
						.append("verification", true)//
		));
		pipeline.add(new Document("$sort", new Document("index", 1).append("_id", 1)));
		return c("problemAction", domain).aggregate(pipeline, ProblemActionInfo.class).into(new ArrayList<>());
	}

	@Override
	public List<ProblemActionLinkInfo> listGanttActionLinks(ObjectId _id, String domain) {
		// TODO
		return new ArrayList<>();
	}

	@Override
	public void insertActions(List<Document> actions, String domain) {
		c("problemAction", domain).insertMany(actions);
	}

	@Override
	public boolean hasPrivate(ObjectId problem_id, String action, String userId, String domain) {
		return new ProblemActionControl().hasPrivate(problem_id, action, userId, domain);
	}

	@Override
	public List<Catalog> listProblemAnlysisRoot(ObjectId problem_id, String domain) {
		return c("problem", domain).aggregate(Domain.getJQ(domain, "查询-问题-根分类").set("problem_id", problem_id).array())
				.map(CatalogMapper::classifyProblem).into(new ArrayList<>());
	}

	@Override
	public Document defaultProblemAnlysisOption(ObjectId problem_id, String domain) {
		return new Document("keyword", getString("problem", "name", problem_id, domain)).append("problem_id",
				problem_id);
	}

	@Override
	public Document createProblemAnlysisChart(Document condition, String domain) {
		return ProblemChartRender.renderAnlysisChart(condition, domain);
	}

	@Override
	public List<Problem> listAllProblems(BasicDBObject condition, String status, String domain) {
		BasicDBObject filter = ensureGet(condition, "filter");
		if (filter.get("status") == null) {
			filter.append("status", status);
		}
		List<Bson> pipeline = appendBasicQueryPipeline(condition, new ArrayList<>(), domain);
		ArrayList<Problem> result = c(Problem.class, domain).aggregate(pipeline).into(new ArrayList<>());
		return result;
	}

	@Override
	public long countAllProblems(BasicDBObject filter, String status, String domain) {
		BasicDBObject f = new BasicDBObject();
		Check.isAssigned(filter, s -> f.putAll(s));
		if (filter == null || filter.get("status") == null) {
			f.append("status", status);
		}
		return count(f, "problem", domain);
	}

	@Override
	public Document loadCauseAnalysis(ObjectId problem_id, String domain) {
		Document result = new Document();

		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(new Document("$match", new Document("problem_id", problem_id)));
		pipeline.add(new Document("$lookup", new Document("from", "causeRelation").append("localField", "_id")
				.append("foreignField", "parent_id").append("as", "child")));
		pipeline.add(new Document("$match",
				new Document("$expr", new Document("$eq", Arrays.asList(new Document("$size", "$child"), 0)))));
		pipeline.add(new Document("$project", new Document("child", false)));

		c("causeRelation", domain).aggregate(pipeline).forEach((Document d) -> {
			if ("因果分析-制造".equals(d.get("type"))) {
				Object r = result.get("rootCauseDesc");
				if (r != null) {
					result.append("rootCauseDesc", r + "\n" + d.getString("name"));
				} else {
					result.append("rootCauseDesc", d.getString("name"));
				}
			} else if ("因果分析-流出".equals(d.get("type"))) {
				Object r = result.get("escapePoint");
				if (r != null) {
					result.append("escapePoint", r + "\n" + d.getString("name"));
				} else {
					result.append("escapePoint", d.getString("name"));
				}
			}
		});
		return result;
	}

	@Override
	public List<Problem> listUnCancelProblems(BasicDBObject condition, String domain) {
		BasicDBObject filter = ensureGet(condition, "filter");
		if (filter.get("status") == null) {
			filter.append("status", new BasicDBObject("$ne", "已取消"));
		}
		List<Bson> pipeline = appendBasicQueryPipeline(condition, new ArrayList<>(), domain);
		ArrayList<Problem> result = c(Problem.class, domain).aggregate(pipeline).into(new ArrayList<>());
		return result;
	}

	@Override
	public long countUnCancelProblems(BasicDBObject filter, String domain) {
		BasicDBObject f = new BasicDBObject();
		Check.isAssigned(filter, s -> f.putAll(s));
		if (filter == null || filter.get("status") == null) {
			f.append("status", new BasicDBObject("$ne", "已取消"));
		}
		return count(f, "problem", domain);
	}

	@Override
	public InputStream createReportAndGetDownloadPath(Document rptParam, ObjectId _id, String template, String fileName,
			String serverName, int serverPath, String domain) {// TODO domain传递
		try {
			// 获取JQ中的查询语句

			Map<String, String> param = new HashMap<String, String>();
			for (String key : rptParam.keySet()) {
				String queryName = (String) rptParam.get(key);
				String json = new GsonBuilder()//
						.serializeNulls().registerTypeAdapter(ObjectId.class, new ObjectIdAdapter())//
						.registerTypeAdapter(Date.class, new DateAdapter())//
						.registerTypeAdapter(BasicDBObject.class, new BasicDBObjectAdapter())//
						.registerTypeAdapter(Document.class, new DocumentAdapter())//
						.create().toJson(Domain.getJQ(domain, queryName).set("id", _id).set("domain", domain).list());
				param.put(key, json);
			}
			if (param.get("dbConnect") == null) {
				Domain dom = Domain.get(domain);
				param.put("dbConnect", dom.getDatabaseUrl());
			}
			List<Map<String, Object>> processors = new ArrayList<Map<String, Object>>();

			// 构建第一个处理器及其参数
			Map<String, Object> processor = new HashMap<String, Object>();
			processor.put("processorTypeId", "com.bizvpm.dps.processor.report:birtreport");
			Map<String, Object> parameter = new HashMap<String, Object>();
			String filePath = Domain.getReportTemplateFile(domain, template);
			FileInputStream fis = new FileInputStream(filePath);
			parameter.put("design", fis);
			parameter.put("output", "html");
			parameter.put("output_html_string", false);
			parameter.put("task_parameter", param);
			processor.put("parameter", parameter);
			processors.add(processor);

			// 构建第二个处理器及其参数
			processor = new HashMap<String, Object>();
			processor.put("processorTypeId", "com.bizvpm.dps.processor.topsreport:topsreport");
			parameter = new HashMap<String, Object>();
			parameter.put("serverPath", "http://" + serverName + ":" + serverPath);
			List<Object> medList = new ArrayList<Object>();
			List<Object> medItem = new ArrayList<Object>();
			medItem.add(0);
			medItem.add("result");
			medItem.add("file");
			medList.add(medItem);
			processor.put("intermediate", medList);
			processor.put("parameter", parameter);
			processors.add(processor);

			// 构建第三个处理器及其参数
			processor = new HashMap<String, Object>();
			processor.put("processorTypeId", "com.bizvpm.dps.processor.msoffice:msoffice.msofficeconverter");
			parameter = new HashMap<String, Object>();
			parameter.put("sourceType", "html");
			parameter.put("targetType", "docx");
			parameter.put("targetName", fileName);
			parameter.put("returnZIP", true);
			parameter.put("hasAtt", true);
			parameter.put("hasImage", true);
			medList = new ArrayList<Object>();
			medItem = new ArrayList<Object>();
			medItem.add(1);
			medItem.add("result");
			medItem.add("file");
			medList.add(medItem);
			medItem = new ArrayList<Object>();
			medItem.add(1);
			medItem.add("template");
			medItem.add("template");
			medList.add(medItem);
			medItem = new ArrayList<Object>();
			medItem.add(1);
			medItem.add("serverPath");
			medItem.add("serverPath");
			medList.add(medItem);
			processor.put("intermediate", medList);
			processor.put("parameter", parameter);
			processors.add(processor);

			Dispatcher dispatcher = Service.get(Dispatcher.class);
			try {
				Map<String, Object> result = dispatcher.run(processors,
						"com.bizvpm.dps.processor.dispatch:dispatch.sequential");
				return (InputStream) result.get("file");
			} catch (Exception e) {
				logger.error("DPS 转换出错。", e);
			}
		} catch (Exception e) {
			logger.error("转换参数有误。", e);
		}
		return null;
	}

	@Override
	public void updateCauseConsequences(List<CauseConsequence> cc, String domain) {
		// TODO 批量编辑编辑
	}

	@Override
	public void updateD1Items(List<Document> d1, String domain) {
		// TODO 批量编辑编辑
	}

	@Override
	public void updateD2ProblemPhotos(List<Document> t, String domain) {
		// TODO 批量编辑编辑
	}

	@Override
	public void updateD7Similars(List<Document> t, String domain) {
		// TODO 批量编辑编辑
	}

	@Override
	public void updateActions(List<Document> actions, String domain) {
		// TODO 批量编辑编辑
	}

}
