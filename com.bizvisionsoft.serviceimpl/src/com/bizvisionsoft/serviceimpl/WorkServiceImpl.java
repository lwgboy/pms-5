package com.bizvisionsoft.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.CheckItem;
import com.bizvisionsoft.service.model.Command;
import com.bizvisionsoft.service.model.DateMark;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Period;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.ResourceActual;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.ResourceTransfer;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Role;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.UpdateWorkPackages;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkLink;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.service.model.WorkPackageProgress;
import com.bizvisionsoft.service.model.WorkResourcePlanDetail;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.renderer.WorkRenderer;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.UnwindOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class WorkServiceImpl extends BasicServiceImpl implements WorkService {

	@Override
	public List<Work> createTaskDataSet(BasicDBObject condition, String domain) {
		return queryWork(null, null, condition, null, new BasicDBObject("index", 1), domain).into(new ArrayList<Work>());
	}

	private void appendWorkTime(List<Bson> pipeline) {
		pipeline.addAll(Arrays.asList(
				new Document("$lookup",
						new Document("from", "work").append("let", new Document("wbsCode", "$wbsCode").append("project_id", "$project_id"))
								.append("pipeline", Arrays.asList(
										new Document("$match",
												new Document("$expr", new Document("$and",
														Arrays.asList(
																new Document("$eq", Arrays.asList("$project_id",
																		"$$project_id")),
																new Document("$eq",
																		Arrays.asList(
																				new Document("$indexOfBytes",
																						Arrays.asList("$wbsCode",
																								new Document("$concat",
																										Arrays.asList("$$wbsCode", ".")))),
																				0)),
																new Document("$eq", Arrays.asList("$summary", false)))))),
										new Document()
												.append("$addFields",
														new Document()
																.append("planDuration",
																		new Document("$divide",
																				Arrays.asList(new Document("$subtract",
																						Arrays.asList(
																								new Document("$ifNull",
																										Arrays.asList("$estimatedFinish",
																												"$planFinish")),
																								"$planStart")),
																						1000 * 3600 * 24)))
																.append("actualDuration",
																		new Document("$divide", Arrays.asList(
																				new Document("$subtract",
																						Arrays.asList(
																								new Document("$ifNull",
																										Arrays.asList("$actualFinish",
																												new Date())),
																								new Document("$ifNull",
																										Arrays.asList("$actualStart",
																												new Date())))),
																				1000 * 3600 * 24)))),
										new Document("$group",
												new Document("_id", null).append("planDuration", new Document("$sum", "$planDuration"))
														.append("actualDuration", new Document("$sum", "$actualDuration"))
														.append("actualWorks", new Document("$sum", "$actualWorks"))
														.append("planWorks", new Document("$sum", "$planWorks")))))
								.append("as", "work")),
				new Document("$unwind", new Document("path", "$work").append("preserveNullAndEmptyArrays", true)),
				new Document("$addFields",
						new Document("summaryPlanDuration", "$work.planDuration").append("summaryActualDuration", "$work.actualDuration")
								.append("summaryActualWorks", "$workTime.actualWorks").append("summaryPlanWorks", "$workTime.planWorks")),
				new Document("$project", new Document("work", false))));

	}

	private void appendOverdue(List<Bson> pipeline) {
		List<Field<?>> fields = new ArrayList<Field<?>>();
		fields.add(new Field<Bson>("overdue", new BasicDBObject("$cond", Arrays.asList(
				// 判断当前工作是否完成
				"$actualFinish",
				// 完成时。依据计划完成时间小于实际完成时间，返回true，否则为false
				new BasicDBObject("$cond",
						Arrays.asList(new BasicDBObject("$lt", Arrays.asList("$planFinish", "$actualFinish")), true, false)),
				// 未完成时。依据计划完成时间小于当前时间，返回true，否则为false
				new BasicDBObject("$cond", Arrays.asList(new BasicDBObject("$lt", Arrays.asList("$planFinish", new Date())), true, false))

		))));

		// fields.add(new Field<Bson>("overdue",
		// new BasicDBObject("$cond",
		// new BasicDBObject("if", new BasicDBObject("$ifNull", new Object[] {
		// "$actualFinish", true }))
		// .append("else", new BasicDBObject("$cond", new BasicDBObject("if",
		// new BasicDBObject("$lt", new Object[] { "$planFinish", new Date() }))
		// .append("then", "超期").append("else",
		// new BasicDBObject("$cond", new BasicDBObject("if",
		// new BasicDBObject("$lt", new Object[] { "$planFinish",
		// new BasicDBObject("$add",
		// new Object[] { new Date(), 5 }) }))
		// .append("then", "预警")
		// .append("else", "")))))
		// .append("then",
		// new BasicDBObject("$cond",
		// new BasicDBObject("if",
		// new BasicDBObject("$lt",
		// new Object[] { "$planFinish", "$actualFinish" }))
		// .append("then", "超期").append("else", "")))
		//
		// )));

		pipeline.add(Aggregates.addFields(fields));
	}

	@Override
	public List<WorkLink> createLinkDataSet(BasicDBObject condition, String domain) {
		return c(WorkLink.class, domain).find(condition).into(new ArrayList<WorkLink>());
	}

	@Override
	public List<Work> listProjectRootTask(ObjectId project_id, String domain) {
		return createTaskDataSet(new BasicDBObject("project_id", project_id).append("parent_id", null), domain);
	}

	@Override
	public long countProjectRootTask(ObjectId project_id, String domain) {
		return count(new BasicDBObject("project_id", project_id).append("parent_id", null), "work", domain);
	}

	@Override
	public List<Work> listChildren(ObjectId parent_id, String domain) {
		return createTaskDataSet(new BasicDBObject("parent_id", parent_id), domain);
	}

	@Override
	public long countChildren(ObjectId parent_id, String domain) {
		return count(new BasicDBObject("parent_id", parent_id), "work", domain);
	}

	@Override
	public Work insertWork(Work work, String domain) {
		return insert(work, Work.class, domain);
	}

	@Override
	public WorkLink insertLink(WorkLink link, String domain) {
		return insert(link, WorkLink.class, domain);
	}

	@Override
	public long updateWork(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, Work.class, domain);
	}

	@Override
	public long updateLink(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, WorkLink.class, domain);
	}

	@Override
	public long deleteWork(ObjectId _id, String domain) {
		Work work = getWork(_id, domain);
		if (work.isStage()) {
			ObjectId obs_id = work.getOBS_id();
			if (obs_id != null)
				new OBSServiceImpl().delete(obs_id, domain);

			ObjectId cbs_id = work.getCBS_id();
			if (cbs_id != null)
				new CBSServiceImpl().delete(cbs_id, domain);
		}
		List<ObjectId> workIds = getDesentItems(Arrays.asList(_id), "work", "parent_id", domain);

		// 删除风险
		c("riskEffect", domain).deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", workIds)));
		// 删除资源计划
		c("resourcePlan", domain).deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", workIds)));

		return c("work", domain).deleteMany(new BasicDBObject("_id", new BasicDBObject("$in", workIds))).getDeletedCount();
	}

	@Override
	public long deleteLink(ObjectId _id, String domain) {
		return delete(_id, WorkLink.class, domain);
	}

	@Override
	public Work getWork(ObjectId _id, String domain) {
		return queryWork(null, null, new BasicDBObject("_id", _id), null, null, domain).first();
	}

	@Override
	public WorkLink getLink(ObjectId _id, String domain) {
		return get(_id, WorkLink.class, domain);
	}

	@Override
	public List<Result> startStage(Command com, String domain) {
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 状态检查
		Document stage = c("work", domain).find(new Document("_id", com._id)).first();
		if (!ProjectStatus.Created.equals(stage.getString("status"))) {
			return Arrays.asList(Result.error("阶段当前的状态不允许执行阶段启动操作"));
		}
		ObjectId pj_id = stage.getObjectId("project_id");
		Project pj = get(pj_id, Project.class, domain);
		if (!ProjectStatus.Processing.equals(pj.getStatus())) {
			return Arrays.asList(Result.error("项目当前的状态不允许执行阶段启动操作"));
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 更新阶段状态
		c("work", domain).updateOne(new Document("_id", com._id), new Document("$set", //
				new Document("status", ProjectStatus.Processing)//
						.append("distributed", true)// 需设为已下达
						.append("startInfo", com.info())));

		List<ObjectId> ids = new ArrayList<ObjectId>();
		c("work", domain).aggregate(
				Domain.getJQ(domain, "查询-工作-阶段需下达的工作计划").set("project_id", pj_id).set("match", new Document("_stage._id", com._id)).array())
				.forEach((Document w) -> ids.add(w.getObjectId("_id")));
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 如果没有可下达的计划，提示
		if (!ids.isEmpty()) {
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 更新下达计划的和项目，记录下达信息
			Document distributeInfo = com.info();
			c("work", domain).updateMany(new Document("_id", new Document("$in", ids)), //
					new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 更新项目阶段
		c("project", domain).updateOne(new Document("_id", pj_id), new Document("$set", new Document("stage_id", com._id)));

		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 发送消息
		sendStageMessage(stage, "启动", com.date, com.userId, domain);

		return new ArrayList<>();
	}

	@Override
	public List<Result> finishStage(Command com, String domain) {
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 状态检查
		Document stage = c("work", domain).find(new Document("_id", com._id)).first();
		if (!ProjectStatus.Processing.equals(stage.getString("status"))) {// 阶段必须是进行中才能收尾
			return Arrays.asList(Result.error("阶段当前的状态不允许执行阶段收尾操作"));
		}
		ObjectId pj_id = stage.getObjectId("project_id");
		Project pj = get(pj_id, Project.class, domain);
		if (ProjectStatus.Created.equals(pj.getStatus()) || ProjectStatus.Closed.equals(pj.getStatus())) {
			return Arrays.asList(Result.error("项目当前的状态不允许执行阶段收尾操作"));
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 如果存在未完成的工作，警告
		if (ICommand.Finish_Stage.equals(com.name)) {
			Number count = (Number) c("work", domain).aggregate(Domain.getJQ(domain, "查询-工作-阶段未完成工作数").set("stage_id", com._id).array())
					.first().get("count");
			if (count.intValue() > 0) {
				return Arrays.asList(Result.warning("阶段存在一些尚未完成的工作。"));
			}
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 获得时间
		Document latest = c("work", domain).find(new Document("parent_id", com._id)).projection(new Document("actualFinish", true))
				.sort(new Document("actualFinish", -1)).first();
		Date actualFinish = Optional.ofNullable(latest).map(l -> l.getDate("actualFinish")).orElse(new Date());

		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 更新
		c("work", domain).updateOne(new Document("_id", com._id), new Document("$set", new Document("status", ProjectStatus.Closing)//
				.append("actualFinish", actualFinish)//
				.append("progress", 1d)//
				.append("finishInfo", com.info())));

		sendStageMessage(stage, "收尾", com.date, com.userId, domain);
		return new ArrayList<Result>();
	}

	@Override
	public List<Result> closeStage(Command com, String domain) {
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 状态检查
		Document stage = c("work", domain).find(new Document("_id", com._id)).first();
		if (ProjectStatus.Closed.equals(stage.getString("status"))) {
			return Arrays.asList(Result.error("阶段当前的状态不允许执行阶段关闭操作"));
		}
		ObjectId pj_id = stage.getObjectId("project_id");
		Project pj = get(pj_id, Project.class, domain);
		if (ProjectStatus.Created.equals(pj.getStatus()) || ProjectStatus.Closed.equals(pj.getStatus())) {
			return Arrays.asList(Result.error("项目当前的状态不允许执行阶段关闭操作"));
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 如果阶段下存在未完成的工作，警告
		if (ICommand.Close_Stage.equals(com.name)) {
			Number count = (Number) c("work", domain).aggregate(Domain.getJQ(domain, "查询-工作-阶段未完成工作数").set("stage_id", com._id).array())
					.first().get("count");
			if (count.intValue() > 0) {
				return Arrays.asList(Result.warning("阶段存在一些尚未完成的工作。"));
			}
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 更新
		c("work", domain).updateOne(new Document("_id", com._id),
				new Document("$set", new Document("status", ProjectStatus.Closed).append("closeInfo", com.info())));
		// TODO 关闭未完成的工作
		sendStageMessage(stage, "关闭", com.date, com.userId, domain);
		return new ArrayList<>();
	}

	@Override
	public Workspace getWorkspace(ObjectId _id, String domain) {
		BasicDBObject dbo = c("work", domain).find(new BasicDBObject("_id", _id), BasicDBObject.class)
				.projection(
						new BasicDBObject("space_id", Boolean.TRUE).append("checkoutBy", Boolean.TRUE).append("project_id", Boolean.TRUE))
				.first();
		return Workspace.newInstance(dbo.getObjectId("project_id"), _id, dbo.getObjectId("space_id"), dbo.getString("checkoutBy"));
	}

	@Override
	public List<WorkLink> createWorkLinkDataSet(ObjectId parent_id, String domain) {
		List<ObjectId> inputIds = new ArrayList<ObjectId>();
		inputIds.add(parent_id);
		inputIds = getDesentItems(inputIds, "work", "parent_id", domain);
		return c(WorkLink.class, domain)
				.find(new BasicDBObject("source", new BasicDBObject("$in", inputIds)).append("target", new BasicDBObject("$in", inputIds)))
				.into(new ArrayList<WorkLink>());
	}

	@Override
	public List<Work> createWorkTaskDataSet(ObjectId parent_id, String domain) {
		List<Work> result = new ArrayList<Work>();
		queryWork(null, null, new BasicDBObject("parent_id", parent_id), null, new BasicDBObject("index", 1), domain)
				.forEach(new Block<Work>() {
					@Override
					public void apply(final Work work) {
						result.add(work);
						result.addAll(createWorkTaskDataSet(work.get_id(), domain));
					}
				});
		return result;
	}

	@Override
	public List<WorkLink> createProjectLinkDataSet(ObjectId project_id, String domain) {
		// yangjun 2018/10/31
		return c(WorkLink.class, domain).find(new BasicDBObject("project_id", project_id))
				.sort(new BasicDBObject("index", 1).append("_id", -1)).into(new ArrayList<WorkLink>());
	}

	@Override
	public List<Work> createProjectTaskDataSet(ObjectId project_id, String domain) {
		// yangjun 2018/10/31
		return queryWork(null, null, new BasicDBObject("project_id", project_id), null, new BasicDBObject("index", 1).append("_id", -1),
				domain).into(new ArrayList<Work>());
	}

	private AggregateIterable<Work> queryWork(Integer skip, Integer limit, BasicDBObject basicCondition, BasicDBObject filter,
			BasicDBObject sort, String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();

		if (basicCondition != null)
			pipeline.add(Aggregates.match(basicCondition));

		appendProject(pipeline);

		appendUserInfo(pipeline, "chargerId", "chargerInfo", domain);

		appendUserInfo(pipeline, "assignerId", "assignerInfo", domain);

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendOverdue(pipeline);

		appendWorkTime(pipeline);

		pipeline.addAll(Domain.getJQ(domain, "追加-工作-阶段名称").array());

		return c(Work.class, domain).aggregate(pipeline);
	}

	private BasicDBObject appendPlanWorkCondition(BasicDBObject condition) {
		condition.append("summary", false)//
				.append("actualStart", null)//
				.append("distributed", true)//
				.append("stage", new BasicDBObject("$ne", true));//
		return condition;
	}

	private BasicDBObject appendExecWorkCondition(BasicDBObject condition) {
		condition.append("summary", false)//
				.append("actualStart", new BasicDBObject("$ne", null))//
				.append("actualFinish", null)//
				.append("distributed", true)//
				.append("stage", new BasicDBObject("$ne", true));//
		return condition;
	}

	private BasicDBObject appendFinishedWorkCondition(BasicDBObject condition) {
		if (condition.get("actualFinish") == null)
			condition.put("actualFinish", new BasicDBObject("$ne", null));

		condition.append("summary", false).append("distributed", true).append("stage", new BasicDBObject("$ne", true));
		return condition;
	}

	private BasicDBObject appendTodoWorkCondition(BasicDBObject condition) {
		condition.append("summary", false)//
				.append("actualFinish", null)//
				.append("distributed", true)//
				.append("stage", new BasicDBObject("$ne", true));//
		return condition;
	}

	@Override
	public List<Work> listMyProcessingWork(BasicDBObject condition, String userid, String domain) {
		BasicDBObject basicCondition = new BasicDBObject("$or",
				new BasicDBObject[] { new BasicDBObject("chargerId", userid), new BasicDBObject("assignerId", userid) })
						.append("summary", false).append("actualFinish", null).append("distributed", true)
						.append("stage", new BasicDBObject("$ne", true));

		Integer skip = Optional.ofNullable(condition).map(c -> (Integer) c.get("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> (Integer) c.get("limit")).orElse(null);
		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("filter")).orElse(null);
		BasicDBObject sort = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("sort"))
				.orElse(new BasicDBObject("planStart", 1).append("_id", -1));

		AggregateIterable<Work> iter = queryWork(skip, limit, basicCondition, filter, sort, domain);
		return iter.into(new ArrayList<Work>());
	}

	@Override
	public long countMyProcessingWork(BasicDBObject filter, String userid, String domain) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("summary", false);
		filter.put("actualFinish", null);
		filter.put("distributed", true);
		filter.put("$or", new BasicDBObject[] { new BasicDBObject("chargerId", userid), new BasicDBObject("assignerId", userid) });
		filter.put("stage", new BasicDBObject("$ne", true));
		return count(filter, Work.class, domain);
	}

	@Override
	public List<Work> createDeptUserWorkDataSet(String userid, String domain) {
		// 修改部门工作无法显示下级部门人员的工作问题
		// TODO 503行到511行可以通过JQ获取。
		// 获取当前用户所在部门
		List<ObjectId> orgIds = c("organization", domain).distinct("_id", new Document("managerId", userid), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		if (orgIds.size() > 0) {
			// 获取用户所在部门的下级部门
			orgIds = getDesentItems(orgIds, "organization", "parent_id", domain);
			// 获取部门中的成员
			List<String> users = c("user", domain)
					.distinct("userId", new BasicDBObject().append("org_id", new BasicDBObject("$in", orgIds)), String.class)
					.into(new ArrayList<String>());

			return queryWork(null, null,
					new BasicDBObject("$or",
							new BasicDBObject[] { new BasicDBObject("chargerId", new BasicDBObject("$in", users)),
									new BasicDBObject("assignerId", new BasicDBObject("$in", users)) }).append("summary", false)
											.append("actualFinish", null).append("distributed", true),
					null, new BasicDBObject("chargerId", 1).append("assignerId", 1).append("planFinish", 1), domain)
							.into(new ArrayList<Work>());
		} else
			return new ArrayList<Work>();
	}

	@Override
	public List<WorkPackage> listWorkPackage(BasicDBObject condition, String domain) {
		return listPackage(condition, "work", domain);
	}

	public WorkPackage getWorkPackage(ObjectId _id, String domain) {
		return listPackage(new Query().filter(new BasicDBObject("_id", _id)).bson(), "work", domain).get(0);
	}

	@Override
	public List<WorkPackage> listWorkInTemplatePackage(BasicDBObject condition, String domain) {
		return listPackage(condition, "workInTemplate", domain);
	}

	private List<WorkPackage> listPackage(BasicDBObject condition, String master, String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();

		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		pipeline.add(Aggregates.lookup(master, "work_id", "_id", "work"));
		pipeline.add(Aggregates.addFields(
				new Field<BasicDBObject>("deadline", new BasicDBObject("$arrayElemAt", new Object[] { "$work.planFinish", 0 })),
				new Field<BasicDBObject>("actualFinish", new BasicDBObject("$arrayElemAt", new Object[] { "$work.actualFinish", 0 }))));
		pipeline.add(Aggregates.project(new BasicDBObject("work", false)));

		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		Integer skip = (Integer) condition.get("skip");
		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		Integer limit = (Integer) condition.get("limit");
		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendUserInfo(pipeline, "chargerId", "chargerInfo", domain);

		appendProgress(pipeline);

		ArrayList<WorkPackage> result = c(WorkPackage.class, domain).aggregate(pipeline).into(new ArrayList<WorkPackage>());
		return result;
	}

	private void appendProgress(List<Bson> pipeline) {
		pipeline.add(Aggregates.lookup("workPackageProgress", "_id", "package_id", "progress"));
		pipeline.add(Aggregates.addFields(Arrays.asList(new Field<Bson>("updateTime", new BasicDBObject("$max", "$progress.updateTime")),
				new Field<Bson>("completeQty", new BasicDBObject("$sum", "$progress.completeQty")),
				new Field<Bson>("qualifiedQty", new BasicDBObject("$sum", "$progress.qualifiedQty")))));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));
		pipeline.add(
				new BasicDBObject("$lookup",
						new BasicDBObject("from", "workPackageProgress")
								.append("let", new BasicDBObject("pid",
										"$_id").append("pupdateTime",
												"$updateTime"))
								.append("pipeline", Arrays.asList(
										new BasicDBObject("$match",
												new BasicDBObject("$expr", new BasicDBObject("$and",
														Arrays.asList(new BasicDBObject("$eq", Arrays.asList("$package_id", "$$pid")),
																new BasicDBObject("$eq", Arrays.asList("$updateTime", "$$pupdateTime")))))),
										new BasicDBObject("$project", new BasicDBObject("completeStatus", true))))
								.append("as", "progress1")));
		pipeline.add(Aggregates.unwind("$progress1", new UnwindOptions().preserveNullAndEmptyArrays(true)));
		pipeline.add(Aggregates.addFields(new Field<String>("completeStatus", "$progress1.completeStatus")));
		pipeline.add(Aggregates.project(new BasicDBObject("progress1", false)));

	}

	@Override
	public long countWorkPackage(BasicDBObject filter, String domain) {
		return count(filter, WorkPackage.class, domain);
	}

	@Override
	public WorkPackage insertWorkPackage(WorkPackage wp, String domain) {
		return insert(wp, WorkPackage.class, domain);
	}

	@Override
	public long deleteWorkPackage(ObjectId _id, String domain) {
		return delete(_id, WorkPackage.class, domain);
	}

	@Override
	public long updateWorkPackage(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, WorkPackage.class, domain);
	}

	private List<String> getWBSBranch(String wbs, String domain) {
		List<String> parentWbs = new ArrayList<>();
		int index = 0;
		int endIndex = wbs.indexOf(".", index);
		while (endIndex != -1) {
			String code = wbs.substring(0, endIndex);
			parentWbs.add(code);
			index = endIndex + 1;
			endIndex = wbs.indexOf(".", index);
		}
		parentWbs.add(wbs);
		return parentWbs;
	}

	@Override
	public List<Result> startWork(Command com, String domain) {
		////////////////////////////////////////////////////////////////////////////////////////////////
		// 工作启动检查
		Work work = get(com._id, Work.class, domain);
		if (work == null)
			return Arrays.asList(Result.notFoundError("id:" + com._id + "，该工作不存在"));
		if (work.isSummary() || work.isStage() || work.isMilestone() || work.getActualStart() != null)
			return Arrays.asList(Result.notAllowedError(work + "，工作不允许执行启动操作"));

		////////////////////////////////////////////////////////////////////////////////////////////////
		// 查找需要更新时间的工作
		Document query = new Document("project_id", work.getProject_id())// 项目下的
				.append("wbsCode", new Document("$in", getWBSBranch(work.getWBSCode(), domain)))// 本工作及上级工作
				.append("actualStart", null);// 没有开始的工作
		List<ObjectId> toUpd = c("work", domain).distinct("_id", query, ObjectId.class).into(new ArrayList<>());// 需要更新的工作

		////////////////////////////////////////////////////////////////////////////////////////////////
		// 更新工作和上级工作的实际开始时间
		c("work", domain).updateMany(new Document("_id", new Document("$in", toUpd)),
				new Document("$set", new Document("actualStart", com.date)));// 更新为当前时间

		////////////////////////////////////////////////////////////////////////////////////////////////
		// 更新项目的实际开始时间
		c("project", domain).updateOne(new Document("_id", work.getProject_id()).append("actualStart", null), //
				new Document("$set", new Document("actualStart", com.date)));// 更新为当前时间

		////////////////////////////////////////////////////////////////////////////////////////////////
		// 根据工作搭接关系处理搭接工作，包括里程碑，并发通知
		String projectName = getName("project", work.getProject_id(), domain);
		List<Message> msg = handlePostPreced(projectName, toUpd, Arrays.asList("SS", "SF"), true, com.date, com.userId, new ArrayList<>(),
				domain);

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 发出消息通知
		sendMessages(msg, projectName);

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 项目进度计划排程
		schedule(work.getProject_id(), domain);

		return new ArrayList<Result>();
	}

	private List<Message> handlePostPreced(String projectName, List<ObjectId> toUpd, List<String> types, boolean isStart, Date date,
			String sender, List<Message> msg, String domain) {
		////////////////////////////////////////////////////////////////////////////////////////////////
		// 查找工作搭接关系处理搭接工作
		List<? extends Bson> pipeline = Arrays.asList(//
				new Document("$match", new Document("type", new Document("$in", types))// 匹配搭接类型
						.append("source", new Document("$in", toUpd))), // 源Work
				new Document("$lookup", new Document("from", "work")// 找出匹配的目标Work
						.append("localField", "target").append("foreignField", "_id").append("as", "targetWork")), //
				new Document("$unwind", new Document("path", "$targetWork")), //
				new Document("$lookup", new Document("from", "work")// 找出匹配的源Work
						.append("localField", "source").append("foreignField", "_id").append("as", "sourceWork")), //
				new Document("$unwind", new Document("path", "$sourceWork")));

		List<ObjectId> milestones = new ArrayList<>();

		c("worklinks", domain).aggregate(pipeline).forEach((Document d) -> {
			Document src = (Document) d.get("sourceWork");
			Document tgt = (Document) d.get("targetWork");
			String type = d.getString("type");
			if (tgt.getBoolean("milestone", false)) {
				if ((tgt.getDate("actualFinish") == null)) {
					if (isStart && ("SS".equals(type) || "SF".equals(type))) {
						milestones.add(tgt.getObjectId("_id"));
					} else if (!isStart && ("FS".equals(type) || "FF".equals(type))) {
						milestones.add(tgt.getObjectId("_id"));
					}
				}
			} else {
				if (!tgt.getBoolean("summary", false) && !tgt.getBoolean("stage", false)) {
					Check.isAssigned(tgt.getString("chargerId"),
							c -> msg.add(Message.precedenceEventMsg(projectName, src, tgt, type, isStart, c, sender)));
				}
			}
		});

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 处理里程碑
		if (!milestones.isEmpty()) {
			c("work", domain).updateMany(new Document("_id", new Document("$in", milestones)),
					new Document("$set", new Document("actualStart", date).append("actualFinish", date).append("progress", 1d)));// 更新为当前时间
			generateWorkNotice(domain, milestones, msg, "完成", date, sender, "PM", "PPM");
			handlePostPreced(projectName, milestones, Arrays.asList("SS", "SF"), true, date, sender, msg, domain);
			handlePostPreced(projectName, milestones, Arrays.asList("FS", "FF"), false, date, sender, msg, domain);
		}
		return msg;
	}

	@SuppressWarnings("unchecked")
	private void generateWorkNotice(String domain, List<ObjectId> workIds, List<Message> msg, String eventName, Date eventDate,
			String sender, String... receiverRole) {
		Consumer<Document> action = (Document d) -> {
			String projectName = ((Document) d.get("project")).getString("name");
			new HashSet<>((List<String>) d.get("receiver")).forEach(receiver -> {
				msg.add(Message.workEventMsg(projectName, d, eventName, eventDate, sender, receiver));
			});
		};
		searchProjectOBSMember(workIds, Arrays.asList(receiverRole), action, domain);
	}

	private void searchProjectOBSMember(List<ObjectId> workIds, List<String> receiverRole, Consumer<Document> action, String domain) {
		c("work", domain).aggregate(Domain.getJQ(domain, "查询-成员-OBS-工作所属项目")//
				.set("match", new Document("_id", new Document("$in", workIds)))//
				.set("roleIdArray", receiverRole).array())//
				.forEach(action);
	}

	@Override
	public List<Result> finishWork(Command com, String domain) {
		////////////////////////////////////////////////////////////////////////////////////////////////
		// 工作完成检查
		Work work = get(com._id, Work.class, domain);
		if (work == null)
			return Arrays.asList(Result.notFoundError("id:" + com._id + "工作不存在"));
		if (work.isSummary() || work.isStage() || work.isMilestone() || work.getActualFinish() != null)
			return Arrays.asList(Result.notAllowedError(work + "，工作不允许执行完成操作"));
		List<CheckItem> checklist = work.getChecklist();
		if (checklist != null && checklist.stream().filter(c -> !"通过".equals(c.getChoise())).count() > 0)
			return Arrays.asList(Result.notAllowedError(work + "，检查项尚未完全通过"));

		////////////////////////////////////////////////////////////////////////////////////////////////
		// 准备更新时间的工作
		List<ObjectId> toUpdate = new ArrayList<>();
		toUpdate.add(com._id);

		////////////////////////////////////////////////////////////////////////////////////////////////
		// 需通知完成的工作
		List<ObjectId> noticeWorks = new ArrayList<>();
		String lvl = work.getManageLevel();
		if ("1".equals(lvl) || ("2".equals(lvl)))
			noticeWorks.add(com._id);

		////////////////////////////////////////////////////////////////////////////////////////////////
		// 更新上级工作的实际完成时间和进展
		ObjectId parent_id = work.getParent_id();
		while (parent_id != null) {
			List<? extends Bson> pip = Arrays.asList(//
					new Document("$match", new Document("_id", parent_id)), // 找到父工作
					new Document("$lookup", new Document("from", "work").append("pipeline", //
							Arrays.asList(new Document("$match", new Document("$expr", //
									new Document("$and", Arrays.asList(new Document("$not", "$actualFinish"), // 完成时间为空，就是没有完成的
											new Document("$not", "$milestone"), // 排除里程碑
											new Document("$eq", Arrays.asList("$parent_id", parent_id))))))))// 下级的parent_id是父id
							.append("as", "bros")));
			Document d = c("work", domain).aggregate(pip).first();
			if (d != null && ((List<?>) d.get("bros")).size() <= 1) {// 所有的下级工作均已完成，本工作需要完成。size==1是需要完成的子工作
				toUpdate.add(parent_id);
				lvl = d.getString("manageLevel");
				if ("1".equals(lvl) || ("2".equals(lvl)))
					noticeWorks.add(com._id);
				parent_id = d.getObjectId("parent_id");
			} else {
				parent_id = null;
			}
		}

		////////////////////////////////////////////////////////////////////////////////////////////////
		// 更新时间和进展
		Document setFinish = new Document("$set", new Document("actualFinish", com.date).append("progress", 1d));
		c("work", domain).updateMany(new Document("_id", new Document("$in", toUpdate)), setFinish);// 更新为当前时间

		////////////////////////////////////////////////////////////////////////////////////////////////
		// 根据工作搭接关系处理搭接工作，包括里程碑，并发通知
		String projectName = getName("project", work.getProject_id(), domain);
		List<Message> msg = handlePostPreced(projectName, toUpdate, Arrays.asList("FS", "FF"), false, com.date, com.userId,
				new ArrayList<>(), domain);

		generateWorkNotice(domain, noticeWorks, msg, "完成", com.date, com.userId, "PM", "PPM");
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 发出消息通知
		sendMessages(msg, domain);

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 项目进度计划排程
		schedule(work.getProject_id(), domain);
		return new ArrayList<Result>();
	}

	@SuppressWarnings("unchecked")
	private void sendStageMessage(Document stage, String event, Date eventDate, String sender, String domain) {
		//////////////////////////////////////////////////////////////////////
		// 通知阶段团队成员，和项目经理，计划经理工作
		final List<String> receivers = getStageMembers(stage.getObjectId("_id"), domain);
		searchProjectOBSMember(Arrays.asList(stage.getObjectId("_id")), Arrays.asList("PM", "PPM"), d -> {
			receivers.addAll((List<String>) d.get("receiver"));
		}, domain);
		final String projectName = getName("project", stage.getObjectId("project_id"), domain);
		final List<Message> msg = new ArrayList<>();
		new HashSet<String>(receivers).forEach(r -> msg.add(Message.workEventMsg(projectName, stage, event, eventDate, sender, r)));
		sendMessages(msg, domain);
	}

	private List<String> getStageMembers(ObjectId _id, String domain) {
		List<ObjectId> parentIds = c("obs", domain).distinct("_id", new BasicDBObject("scope_id", _id), ObjectId.class)
				.into(new ArrayList<>());
		List<ObjectId> ids = getDesentItems(parentIds, "obs", "parent_id", domain);
		ArrayList<String> memberIds = c("obs", domain).distinct("managerId",
				new BasicDBObject("_id", new BasicDBObject("$in", ids)).append("managerId", new BasicDBObject("$ne", null)), String.class)
				.into(new ArrayList<>());
		String charger = c("work", domain).find(new Document("_id", _id)).first().getString("chargerId");
		if (!memberIds.contains(charger))
			memberIds.add(charger);
		return memberIds;
	}

	@Override
	public long updateResourcePlan(BasicDBObject filterAndUpdate, String domain) {
		Bson bson = (Bson) filterAndUpdate.get("filter");
		ObjectId work_id = c("resourcePlan", domain).distinct("work_id", bson, ObjectId.class).first();
		long update = update(filterAndUpdate, "resourcePlan", domain);
		updateWorkPlanWorks(work_id, domain);
		return update;
	}

	@Override
	public long deleteHumanResourcePlan(ObjectId work_id, String resId, String domain) {
		DeleteResult dr = c("resourcePlan", domain).deleteMany(new Document("work_id", work_id).append("usedHumanResId", resId));
		updateWorkPlanWorks(work_id, domain);
		return dr.getDeletedCount();
	}

	@Override
	public long deleteEquipmentResourcePlan(ObjectId work_id, String resId, String domain) {
		DeleteResult dr = c("resourcePlan", domain).deleteMany(new Document("work_id", work_id).append("usedEquipResId", resId));
		updateWorkPlanWorks(work_id, domain);
		return dr.getDeletedCount();
	}

	@Override
	public long deleteTypedResourcePlan(ObjectId work_id, String resId, String domain) {
		DeleteResult dr = c("resourcePlan", domain).deleteMany(new Document("work_id", work_id).append("usedTypedResId", resId));
		updateWorkPlanWorks(work_id, domain);
		return dr.getDeletedCount();
	}

	@Override
	public long deleteHumanResourceActual(ObjectId work_id, String resId, String domain) {
		DeleteResult dr = c("resourceActual", domain).deleteMany(new Document("work_id", work_id).append("usedHumanResId", resId));
		updateWorkActualWorks(work_id, domain);
		return dr.getDeletedCount();
	}

	@Override
	public long deleteEquipmentResourceActual(ObjectId work_id, String resId, String domain) {
		DeleteResult dr = c("resourceActual", domain).deleteMany(new Document("work_id", work_id).append("usedEquipResId", resId));
		updateWorkActualWorks(work_id, domain);
		return dr.getDeletedCount();
	}

	@Override
	public long deleteTypedResourceActual(ObjectId work_id, String resId, String domain) {
		DeleteResult dr = c("resourceActual", domain).deleteMany(new Document("work_id", work_id).append("usedTypedResId", resId));
		updateWorkActualWorks(work_id, domain);
		return dr.getDeletedCount();
	}

	@Override
	public ResourcePlan insertResourcePlan(ResourcePlan rp, String domain) {
		return insert(rp, ResourcePlan.class, domain);
	}

	private void updateWorkPlanWorks(ObjectId work_id, String domain) {
		if (work_id != null) {
			// TODO 修改计算方式
			List<? extends Bson> pipeline = Arrays.asList(new Document("$match", new Document("work_id", work_id)),
					new Document("$addFields",
							new Document("planQty", new Document("$sum", Arrays.asList("$planBasicQty", "$planOverTimeQty")))),
					new Document("$group", new Document("_id", "$work_id").append("planWorks", new Document("$sum", "$planQty"))));

			double works = Optional.ofNullable(c("resourcePlan", domain).aggregate(pipeline).first()).map(d -> d.getDouble("planWorks"))
					.map(p -> p.doubleValue()).orElse(0d);

			c(Work.class, domain).updateOne(new Document("_id", work_id), new Document("$set", new Document("planWorks", works)));
		}
	}

	@Override
	public WorkPackageProgress insertWorkPackageProgress(WorkPackageProgress wpp, String domain) {
		return insert(wpp.setUpdateTime(new Date()), WorkPackageProgress.class, domain);
	}

	@Override
	public long deleteWorkPackageProgress(ObjectId _id, String domain) {
		return delete(_id, WorkPackageProgress.class, domain);
	}

	@Override
	public List<WorkPackageProgress> listWorkPackageProgress(BasicDBObject condition, String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(new Document("$lookup", new Document().append("from", "workPackage").append("localField", "package_id")
				.append("foreignField", "_id").append("as", "workpackage")));
		pipeline.add(new Document("$unwind", "$workpackage"));
		pipeline.add(new Document("$addFields", new Document().append("unit", "$workpackage.unit")));
		pipeline.add(new Document("$project", new Document().append("workpackage", false)));

		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		Integer skip = (Integer) condition.get("skip");
		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		Integer limit = (Integer) condition.get("limit");
		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		return c(WorkPackageProgress.class, domain).aggregate(pipeline).into(new ArrayList<WorkPackageProgress>());
	}

	@Override
	public long countWorkPackageProgress(BasicDBObject filter, String domain) {
		return count(filter, WorkPackageProgress.class, domain);
	}

	@Override
	public long updateWorkPackageProgress(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, WorkPackageProgress.class, domain);
	}

	@Override
	public List<Work> listWorkPackageForScheduleInProject(ObjectId project_id, String catagory, String domain) {
		// TODO 检查是否可以合并到queryWork中
		List<Bson> pipeline = (List<Bson>) Domain.getJQ(domain, "追加-工作-工作包").set("match", new Document("project_id", project_id))
				.set("catagory", catagory).array();

		pipeline.addAll(Domain.getJQ(domain, "追加-工作-阶段名称").array());

		appendProject(pipeline);

		appendOverdue(pipeline);

		appendWorkTime(pipeline);

		appendUserInfo(pipeline, "chargerId", "chargerInfo", domain);

		// appendUserInfo(pipeline, "assignerId", "assignerInfo");
		// yangjun 2018/10/31
		pipeline.add(Aggregates.sort(new Document("index", 1).append("_id", -1)));

		// List<Work> result = new ArrayList<Work>();
		//
		// c(Work.class,domain).aggregate(pipeline).forEach((Work work) -> {
		// if (work.getParent_id() != null)
		// work.setStageName(getStageName(work.getParent_id()));
		// else
		// work.setStageName(work.getText());
		// result.add(work);
		// });
		//
		// return result;

		return c(Work.class, domain).aggregate(pipeline).into(new ArrayList<>());
	}

	// private String getStageName(ObjectId _id,String domain){
	// Document first = c("work",domain).find(new Document("_id", _id))
	// .projection(new Document("name", 1).append("parent_id", 1)).first();
	// ObjectId parent_id = first.get("parent_id", ObjectId.class);
	// if (parent_id != null){
	// return getStageName(parent_id);
	// }
	// return first.get("name", String.class);
	// }

	@Override
	public long countWorkPackageForScheduleInProject(ObjectId project_id, String catagory, String domain) {
		return c("work", domain)
				.countDocuments(new BasicDBObject("workPackageSetting.catagory", catagory).append("project_id", project_id));
	}

	@Override
	public List<Work> listWorkPackageForScheduleInStage(ObjectId stage_id, String catagory, String domain) {
		List<ObjectId> items = getDesentItems(Arrays.asList(stage_id), "work", "parent_id", domain);
		List<Bson> pipeline = (List<Bson>) Domain.getJQ(domain, "追加-工作-工作包").set("match", new Document("_id", new Document("$in", items)))
				.set("catagory", catagory).array();

		appendProject(pipeline);

		appendOverdue(pipeline);

		appendWorkTime(pipeline);

		appendUserInfo(pipeline, "chargerId", "chargerInfo", domain);

		// appendUserInfo(pipeline, "assignerId", "assignerInfo");
		// yangjun 2018/10/31
		pipeline.add(Aggregates.sort(new Document("index", 1).append("_id", -1)));

		return c(Work.class, domain).aggregate(pipeline).into(new ArrayList<Work>());
	}

	@Override
	public long countWorkPackageForScheduleInStage(ObjectId stage_id, String catagory, String domain) {
		List<ObjectId> items = getDesentItems(Arrays.asList(stage_id), "work", "parent_id", domain);
		return c("work", domain)
				.countDocuments(new BasicDBObject("workPackageSetting.catagory", catagory).append("_id", new BasicDBObject("$in", items)));
	}

	// TODO 检查是否可以与listWorkPackageForScheduleInProject进行合并
	@Override
	public List<Work> listWorkPackageForSchedule(BasicDBObject condition, String userid, String catagory, String domain) {
		List<Bson> pipeline = (List<Bson>) Domain.getJQ(domain, "追加-工作-工作包")
				.set("match", new Document("summary", false).append("actualFinish", null).append("stage", new BasicDBObject("$ne", true)))
				.set("catagory", catagory).array();
		// TODO 检查是否可以合并到queryWork中
		pipeline.addAll(Domain.getJQ(domain, "追加-工作-阶段名称").array());

		// 获取生产工作情况时，如果用户具有制造管理权限时，只显示全部项目的的生产工作；获取采购工作情况时，如果用户具有供应链管理权限时，只显示全部项目的的采购工作；其它情况时显示当前用户在项目PMO团队中的项目的工作
		if (!((TrackView.CATAGORY_PRODUCTION.equals(catagory)
				&& checkUserRoles(userid, Arrays.asList(Role.SYS_ROLE_MM_ID, Role.SYS_ROLE_PD_ID), domain))
				|| (TrackView.CATAGORY_PURCHASE.equals(catagory)
						&& checkUserRoles(userid, Arrays.asList(Role.SYS_ROLE_SCM_ID, Role.SYS_ROLE_PD_ID), domain))))
			appendQueryUserInProjectPMO(pipeline, userid, "$project_id", domain);

		appendProject(pipeline);

		appendOverdue(pipeline);

		appendWorkTime(pipeline);

		appendUserInfo(pipeline, "chargerId", "chargerInfo", domain);

		appendUserInfo(pipeline, "assignerId", "assignerInfo", domain);

		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		if (sort != null)
			pipeline.add(Aggregates.sort(sort));
		else// yangjun 2018/10/31
			pipeline.add(Aggregates.sort(new Document("project_id", 1).append("planFinish", 1).append("_id", -1)));

		Integer skip = (Integer) condition.get("skip");
		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		Integer limit = (Integer) condition.get("limit");
		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		// List<Work> result = new ArrayList<Work>();
		//
		// c(Work.class,domain).aggregate(pipeline).forEach((Work work) -> {
		// if (work.getParent_id() != null)
		// work.setStageName(getStageName(work.getParent_id()));
		// else
		// work.setStageName(work.getText());
		// result.add(work);
		// });

		return c(Work.class, domain).aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public long countWorkPackageForSchedule(BasicDBObject filter, String userid, String catagory, String domain) {
		// 获取生产工作情况时，如果用户具有制造管理权限时，只显示全部项目的的生产工作；获取采购工作情况时，如果用户具有供应链管理权限时，只显示全部项目的的采购工作；其它情况时显示当前用户在项目PMO团队中的项目的工作
		if ((TrackView.CATAGORY_PRODUCTION.equals(catagory)
				&& checkUserRoles(userid, Arrays.asList(Role.SYS_ROLE_MM_ID, Role.SYS_ROLE_PD_ID), domain))
				|| (TrackView.CATAGORY_PURCHASE.equals(catagory)
						&& checkUserRoles(userid, Arrays.asList(Role.SYS_ROLE_SCM_ID, Role.SYS_ROLE_PD_ID), domain))) {
			if (filter == null)
				filter = new BasicDBObject();

			filter.append("workPackageSetting.catagory", catagory);
			return c("work", domain).countDocuments(filter);
		} else {
			List<Bson> pipeline = new ArrayList<Bson>();
			// 根据传入的filter构造查询
			if (filter != null)
				pipeline.add(Aggregates.match(filter));

			// 构造默认类型查询
			pipeline.add(Aggregates.match(new BasicDBObject("workPackageSetting.catagory", catagory)));

			appendQueryUserInProjectPMO(pipeline, userid, "$project_id", domain);
			return c("work", domain).aggregate(pipeline).into(new ArrayList<>()).size();
		}
	}

	@Override
	public void addResourcePlan(List<ResourceAssignment> resas, String domain) {
		Set<ObjectId> workIds = new HashSet<ObjectId>();
		List<ResourcePlan> documents = new ArrayList<ResourcePlan>();
		resas.forEach(resa -> {
			double works = getWorkingHoursPerDay(resa.resTypeId, domain);// 取出默认的每天工作时间

			Calendar from = Calendar.getInstance();
			from.setTime(resa.from);
			Calendar to = Calendar.getInstance();
			to.setTime(resa.to);

			while (from.before(to)) {
				Date id = from.getTime();
				if (checkDayIsWorkingDay(from, resa.resTypeId, domain) && hasResource("resourcePlan", id, resa.work_id, resa.usedHumanResId,
						resa.usedEquipResId, resa.usedTypedResId, resa.resTypeId, domain)) {

					ResourcePlan res = resa.getResourcePlan();
					res.setId(id);
					res.setPlanBasicQty(works);// 设置默认的每天工作时间
					res.setQty(resa.qty);

					documents.add(res);
				}
				from.add(Calendar.DAY_OF_MONTH, 1);
			}
			workIds.add(resa.work_id);
		});

		if (documents.size() > 0)
			c(ResourcePlan.class, domain).insertMany(documents);

		workIds.forEach(d -> updateWorkPlanWorks(d, domain));
	}

	@Override
	public void addResourceActual(List<ResourceAssignment> resas, String domain) {
		Set<ObjectId> workIds = new HashSet<ObjectId>();

		List<ResourceActual> documents = new ArrayList<ResourceActual>();
		resas.forEach(resa -> {
			double works = getWorkingHoursPerDay(resa.resTypeId, domain);// 取出默认的每天工作时间

			Calendar from = Calendar.getInstance();
			from.setTime(Formatter.getStartOfDay(resa.from));
			Calendar to = Calendar.getInstance();
			to.setTime(Formatter.getStartOfDay(resa.to));

			while (from.before(to)) {
				Date id = from.getTime();
				if (checkDayIsWorkingDay(from, resa.resTypeId, domain) && hasResource("resourceActual", id, resa.work_id,
						resa.usedHumanResId, resa.usedEquipResId, resa.usedTypedResId, resa.resTypeId, domain)) {

					ResourceActual res = resa.getResourceActual();
					res.setId(id);
					res.setQty(resa.qty);
					res.setActualBasicQty(works);// 设置默认的每天工作时间
					documents.add(res);
				}
				from.add(Calendar.DAY_OF_MONTH, 1);
			}
			workIds.add(resa.work_id);
		});

		// 增加判断，如果是重复添加，会造成documents没有插入数据
		if (documents.size() > 0)
			c(ResourceActual.class, domain).insertMany(documents);

		workIds.forEach(d -> updateWorkActualWorks(d, domain));
	}

	private boolean hasResource(String col, Date time, ObjectId work_id, String usedHumanResId, String usedEquipResId,
			String usedTypedResId, ObjectId resTypeId, String domain) {
		return c(col, domain).countDocuments(new Document("id", time).append("work_id", work_id).append("usedHumanResId", usedHumanResId)
				.append("usedEquipResId", usedEquipResId).append("usedTypedResId", usedTypedResId).append("resTypeId", resTypeId)) == 0;
	}

	@Override
	public ResourceActual insertResourceActual(ResourceActual ra, String domain) {
		return insert(ra, ResourceActual.class, domain);
	}

	@Override
	public long updateResourceActual(BasicDBObject filterAndUpdate, String domain) {
		Bson bson = (Bson) filterAndUpdate.get("filter");
		ObjectId work_id = c("resourceActual", domain).distinct("work_id", bson, ObjectId.class).first();
		long update = update(filterAndUpdate, "resourceActual", domain);
		updateWorkActualWorks(work_id, domain);
		return update;
	}

	private void updateWorkActualWorks(ObjectId work_id, String domain) {
		if (work_id != null) {
			// TODO 修改计算方式
			List<? extends Bson> pipeline = Arrays.asList(new Document("$match", new Document("work_id", work_id)),
					new Document("$addFields",
							new Document("actualQty", new Document("$sum", Arrays.asList("$actualBasicQty", "$actualOverTimeQty")))),
					new Document("$group", new Document("_id", "$work_id").append("actualWorks", new Document("$sum", "$actualQty"))));

			double works = Optional.ofNullable(c("resourceActual", domain).aggregate(pipeline).first()).map(d -> d.getDouble("actualWorks"))
					.map(p -> p.doubleValue()).orElse(0d);

			c(Work.class, domain).updateOne(new Document("_id", work_id), new Document("$set", new Document("actualWorks", works)));
		}
	}

	public List<WorkResourcePlanDetail> listConflictWorks(ResourcePlan resp, String domain) {
		Document eq = new Document();
		if (resp.getUsedHumanResId() != null)
			eq.put("$eq", Arrays.asList("$usedHumanResId", resp.getUsedHumanResId()));
		else if (resp.getUsedEquipResId() != null)
			eq.put("$eq", Arrays.asList("$usedEquipResId", resp.getUsedEquipResId()));

		List<? extends Bson> pipeline = Arrays.asList(
				new Document("$match", new Document("_id",
						resp.getWork_id())),
				new Document("$lookup",
						new Document("from", "resourcePlan")
								.append("let", new Document("planStart", "$planStart").append("planFinish", "$planFinish"))
								.append("pipeline",
										Arrays.asList(new Document("$match",
												new Document("$expr",
														new Document("$and",
																Arrays.asList(new Document("$gte", Arrays.asList("$id", "$$planStart")),
																		new Document("$lte", Arrays.asList("$id", "$$planFinish")), eq))))))
								.append("as", "resourcePlan")),
				new Document("$unwind", "$resourcePlan"), new Document("$replaceRoot", new Document("newRoot", "$resourcePlan")),
				// new Document("$project",
				// new Document("_id", "$resourcePlan._id").append("work_id",
				// "$resourcePlan.work_id")
				// .append("usedHumanResId_id", "$resourcePlan.usedHumanResId_id")
				// .append("resTypeId", "$resourcePlan.resTypeId")
				// .append("planOverTimeQty", "$resourcePlan.planOverTimeQty")
				// .append("id", "$resourcePlan.id").append("planBasicQty",
				// "$resourcePlan.planBasicQty")),
				new Document("$group", new Document("_id", "$work_id").append("children", new Document("$push", "$$ROOT"))),
				new Document("$lookup",
						new Document("from", "work").append("localField", "_id").append("foreignField", "_id").append("as", "work")),
				new Document("$unwind", "$work"),
				new Document("$addFields", new Document("project_id", "$work.project_id").append("planStart", "$work.planStart")
						.append("planFinish", "$work.planFinish").append("name", "$work.name").append("actualStart", "$work.actualStart")
						.append("actualFinish", "$work.actualFinish").append("id", "$work.code")),
				new Document("$lookup",
						new Document("from", "project").append("localField", "project_id").append("foreignField", "_id").append("as",
								"project")),
				new Document("$unwind", "$project"),
				new Document("$addFields", new Document("projectId", "$project.id").append("projectName", "$project.name")),
				new Document("$project", new Document("project", false).append("work", false)));
		return c("work", WorkResourcePlanDetail.class, domain).aggregate(pipeline).into(new ArrayList<WorkResourcePlanDetail>());
	}

	@Override
	public void assignRoleToProject(ObjectId project_id, boolean cover, String domain) {
		List<ObjectId> ids = getScopeOBS(project_id, domain);
		Document condition = new Document("project_id", project_id).append("actualFinish", null);
		// 为工作分配人员
		updateWorkRoleAssignment("work", ids, condition, cover, domain);
		// 为工作区中的工作分配人员
		updateWorkRoleAssignment("workspace", ids, condition, cover, domain);
	}

	/**
	 * 依据工作设定的负责人和指派者角色,为工作分配负责人和指派者
	 * 
	 * @param c
	 *            待分配的集合
	 * @param ids
	 *            OBS节点ID
	 * @param condition
	 *            待分配的工作范围
	 * @param cover
	 *            是否覆盖已分配的工作
	 */
	private void updateWorkRoleAssignment(String cName, List<ObjectId> ids, Document condition, boolean cover, String domain) {
		MongoCollection<Document> c = c(cName, domain);
		c.find(condition.append("$or", Arrays.asList(new Document("chargerRoleId", new Document("$ne", null)),
				new Document("assignerRoleId", new Document("$ne", null))))).forEach((Document d) -> {
					// 如需要覆盖或者负责人为空时,更新工作的负责人
					if (cover || d.get("chargerId") == null) {
						// 负责人角色不为空时,根据该角色的_id找到该角色的管理者,如果管理者不为空,则更新为工作的负责人.
						Check.isAssigned(d.getString("chargerRoleId"), rId -> {
							Check.isAssigned(getManagerIdOfRole(ids, rId, domain), uId -> c.updateOne(new Document("_id", d.get("_id")),
									new Document("$set", new Document("chargerId", uId))));
						});
					}
					// 如需要覆盖或者指派者为空时,更新工作的指派者
					if (cover || d.get("assignerId") == null) {
						// 指派者角色不为空时,根据该角色的_id找到该角色的管理者,如果管理者不为空,则更新为工作的指派者.
						Check.isAssigned(d.getString("assignerRoleId"), rId -> {
							Check.isAssigned(getManagerIdOfRole(ids, rId, domain), uId -> c.updateOne(new Document("_id", d.get("_id")),
									new Document("$set", new Document("assignerId", uId))));
						});
					}
				});
	}

	/**
	 * 检查是否存在需要覆盖负责人和指派者的工作
	 */
	@Override
	public boolean checkCoverWork(ObjectId project_id, String domain) {
		// 当工作未完成，并且存在负责人角色且负责人不为空或指派者角色且指派者不为空时，存在需要覆盖负责人和指派者的工作
		Document condition = new Document("project_id", project_id).append("actualFinish", null).append("$or",
				Arrays.asList(new Document("chargerRoleId", new Document("$ne", null)).append("chargerId", new Document("$ne", null)),
						new Document("assignerRoleId", new Document("$ne", null)).append("assignerId", new Document("$ne", null))));

		// 对工作和工作区进行检查。
		return c("work", domain).countDocuments(condition) > 0 ? true : (c("workspace", domain).countDocuments(condition) > 0);
	}

	@Override
	public void assignRoleToStage(ObjectId work_id, String domain) {
		List<ObjectId> ids = getScopeOBS(work_id, domain);
		List<ObjectId> workIds = getDesentItems(Arrays.asList(work_id), "work", "parent_id", domain);
		Document condition = new Document("_id", new Document("$in", workIds)).append("actualFinish", null);
		updateWorkRoleAssignment("work", ids, condition, false, domain);
		updateWorkRoleAssignment("workspace", ids, condition, false, domain);
	}

	private String getManagerIdOfRole(List<ObjectId> ids, String roleId, String domain) {
		return Optional.ofNullable(c("obs", domain).find(new Document("_id", new Document("$in", ids)).append("roleId", roleId)).first())
				.map(d -> d.getString("managerId")).orElse(null);
	}

	private List<ObjectId> getScopeOBS(ObjectId scope_id, String domain) {
		List<ObjectId> parentIds = c("obs", domain).distinct("_id", new Document("scope_id", scope_id), ObjectId.class)
				.into(new ArrayList<>());
		return getDesentItems(parentIds, "obs", "parent_id", domain);
	}

	@Override
	public ObjectId getProjectId(ObjectId _id, String domain) {
		return c("work", domain).distinct("project_id", new Document("_id", _id), ObjectId.class).first();
	}

	@Override
	@Deprecated
	public List<DateMark> listMyWorksDateMark(String userid, String domain) {
		List<? extends Bson> ls = Domain.getJQ(domain, "查询-时间标记-待处理工作").set("userId", userid).array();
		return c("work", domain).aggregate(ls, DateMark.class).into(new ArrayList<>());
	}

	@Override
	public List<Document> getResource(ResourceTransfer rt, String domain) {
		String col;
		if (ResourceTransfer.TYPE_PLAN == rt.getType()) {
			col = "resourcePlan";
		} else {
			if (rt.isReport())
				col = "workReportResourceActual";
			else
				col = "resourceActual";
		}
		Document match;
		if (ResourceTransfer.SHOWTYPE_MULTIWORK_ONERESOURCE == rt.getShowType()) {
			match = new Document("usedEquipResId", rt.getUsedEquipResId()).append("usedHumanResId", rt.getUsedHumanResId())
					.append("usedTypedResId", rt.getUsedTypedResId()).append("resTypeId", rt.getResTypeId());
		} else {
			match = new Document("work_id", new Document("$in", rt.getWorkIds()));
			if (rt.isReport())
				match.append("workReportItemId", rt.getWorkReportItemId());
		}
		return c(col, domain).aggregate(Domain.getJQ(domain, "查询-资源").set("match", match).set("resourceCollection", col)
				.set("from", rt.getFrom()).set("to", rt.getTo()).array()).into(new ArrayList<Document>());
	}

	/**
	 * 垃圾
	 * 
	 * @param value
	 * @return
	 */
	private String getStringValue(Object value) {
		if (value instanceof Number) {
			double d = ((Number) value).doubleValue();
			if (d != 0d) {
				return Formatter.getString(d);
			}
		}
		return "";
	}

	@Override
	public Document getResourcePlanAnalysis(ObjectId project_id, String year, String domain) {
		List<Document> series = new ArrayList<Document>();
		Map<String, Double> worksMap = new TreeMap<String, Double>();
		Map<String, Double> amountMap = new TreeMap<String, Double>();
		for (int i = 1; i < 12; i++) {
			String key = year + String.format("%02d", i);
			worksMap.put(key, 0d);
			amountMap.put(key, 0d);
		}

		c("resourcePlan", domain).aggregate(
				Domain.getJQ(domain, "查询-资源-计划用量-项目").set("match", new Document("year", year).append("project_id", project_id)).array())
				.forEach((Document doc) -> {
					String id = doc.getString("_id");
					Double worksD = worksMap.get(id);
					if (worksD != null) {
						Object works = doc.get("planQty");
						if (works != null) {
							worksD += ((Number) works).doubleValue();
							worksMap.put(id, worksD);
						}
					}

					Double amountD = amountMap.get(id);
					if (amountD != null) {
						Object amount = doc.get("planAmount");
						if (amount != null) {
							amountD += (((Number) amount).doubleValue() / 10000d);
							amountMap.put(id, amountD);
						}
					}
				});

		Document worksData = new Document();
		worksData.append("name", "工时");
		worksData.append("type", "bar");
		worksData.append("label", new Document("normal", new Document("show", true).append("position", "inside")));

		List<Object> worksdata = new ArrayList<Object>();
		for (Double d : worksMap.values()) {
			worksdata.add(getStringValue(d));
		}
		worksData.append("data", worksdata);
		series.add(worksData);

		Document amountData = new Document();
		amountData.append("name", "金额");
		amountData.append("type", "bar");
		amountData.append("xAxisIndex", 1);
		amountData.append("label", new Document("normal", new Document("show", true).append("position", "inside")));
		List<Object> amountdata = new ArrayList<Object>();
		for (Double d : amountMap.values()) {
			amountdata.add(getStringValue(d));
		}
		amountData.append("data", amountdata);

		series.add(amountData);

		Document option = new Document();
		option.append("title", new Document("text", year + "年  项目各月资源计划用量分析").append("x", "center"));
		// option.append("tooltip", new Document("trigger",
		// "axis").append("axisPointer", new Document("type", "shadow")));

		option.append("legend", new Document("data", Arrays.asList("工时", "金额")).append("y", "bottom").append("x", "center"));
		option.append("grid", new Document("left", "2%").append("right", "2%").append("bottom", "3%").append("containLabel", true));

		option.append("yAxis", Arrays.asList(new Document("type", "category").append("data",
				Arrays.asList(" 1月", " 2月", " 3月", " 4月", " 5月", " 6月", " 7月", " 8月", " 9月", "10月", "11月", "12月"))));
		option.append("xAxis",
				Arrays.asList(
						new Document("type", "value").append("name", "工时").append("axisLabel", new Document("formatter", "{value} 小时")),
						new Document("type", "value").append("name", "金额").append("axisLabel", new Document("formatter", "{value} 万元"))));

		option.append("series", series);
		return option;

	}

	@Override
	public Document getResourceActualAnalysis(ObjectId project_id, String year, String domain) {
		List<Document> series = new ArrayList<Document>();
		Map<String, Double> worksMap = new TreeMap<String, Double>();
		Map<String, Double> amountMap = new TreeMap<String, Double>();
		for (int i = 1; i < 12; i++) {
			String key = year + String.format("%02d", i);
			worksMap.put(key, 0d);
			amountMap.put(key, 0d);
		}

		c("resourceActual", domain).aggregate(
				Domain.getJQ(domain, "查询-资源-实际用量-项目").set("match", new Document("year", year).append("project_id", project_id)).array())
				.forEach((Document doc) -> {
					String id = doc.getString("_id");
					Double worksD = worksMap.get(id);
					if (worksD != null) {
						Object works = doc.get("actualQty");
						if (works != null) {
							worksD += ((Number) works).doubleValue();
							worksMap.put(id, worksD);
						}
					}

					Double amountD = amountMap.get(id);
					if (amountD != null) {
						Object amount = doc.get("actualAmount");
						if (amount != null) {
							amountD += (((Number) amount).doubleValue() / 10000d);
							amountMap.put(id, amountD);
						}
					}
				});

		Document worksData = new Document();
		worksData.append("name", "工时");
		worksData.append("type", "bar");
		worksData.append("label", new Document("normal", new Document("show", true).append("position", "inside")));

		List<Object> worksdata = new ArrayList<Object>();
		for (Double d : worksMap.values()) {
			worksdata.add(getStringValue(d));
		}
		worksData.append("data", worksdata);
		series.add(worksData);

		Document amountData = new Document();
		amountData.append("name", "金额");
		amountData.append("type", "bar");
		amountData.append("xAxisIndex", 1);
		amountData.append("label", new Document("normal", new Document("show", true).append("position", "inside")));
		List<Object> amountdata = new ArrayList<Object>();
		for (Double d : amountMap.values()) {
			amountdata.add(getStringValue(d));
		}
		amountData.append("data", amountdata);

		series.add(amountData);

		Document option = new Document();
		option.append("title", new Document("text", year + "年  项目各月资源实际用量分析").append("x", "center"));
		// option.append("tooltip", new Document("trigger",
		// "axis").append("axisPointer", new Document("type", "shadow")));

		option.append("legend", new Document("data", Arrays.asList("工时", "金额")).append("y", "bottom").append("x", "center"));
		option.append("grid", new Document("left", "3%").append("right", "10%").append("bottom", "3%").append("containLabel", true));

		option.append("yAxis", Arrays.asList(new Document("type", "category").append("data",
				Arrays.asList(" 1月", " 2月", " 3月", " 4月", " 5月", " 6月", " 7月", " 8月", " 9月", "10月", "11月", "12月"))));
		option.append("xAxis",
				Arrays.asList(
						new Document("type", "value").append("name", "工时").append("axisLabel", new Document("formatter", "{value} 小时")),
						new Document("type", "value").append("name", "金额").append("axisLabel", new Document("formatter", "{value} 万元"))));

		option.append("series", series);
		return option;

	}

	@Override
	public Document getProjectResourcePlanAndUsageChart(ObjectId project_id, String domain) {
		Document project = c("project", domain).find(new Document("_id", project_id)).first();
		Date actualStart = project.getDate("actualStart");
		Date actualFinish = project.getDate("actualFinish");
		Date planStart = project.getDate("planStart");
		Date planFinish = project.getDate("planFinish");

		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		if (actualFinish != null && actualFinish.after(planFinish))
			end.setTime(actualFinish);
		else
			end.setTime(planFinish);

		if (actualStart != null && actualStart.before(planStart))
			start.setTime(actualStart);
		else
			start.setTime(planStart);

		List<String> xAxisDate = new ArrayList<String>();
		Map<String, Double> actualWorksMap = new LinkedHashMap<String, Double>();
		Map<String, Double> planWorksMap = new LinkedHashMap<String, Double>();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMM");
		while (start.before(end) || start.equals(end)) {
			String key = sdf1.format(start.getTime());
			xAxisDate.add(sdf.format(start.getTime()));
			actualWorksMap.put(key, 0d);
			planWorksMap.put(key, 0d);
			start.add(Calendar.MONTH, 1);
		}

		Document match = new Document("project_id", project_id);
		c("resourcePlan", domain).aggregate(Domain.getJQ(domain, "查询-资源-计划用量-项目").set("match", match).array()).forEach((Document doc) -> {
			String id = doc.getString("_id");
			Double worksD = planWorksMap.get(id);
			if (worksD != null) {
				Object works = doc.get("planQty");
				if (works != null) {
					worksD += ((Number) works).doubleValue();
					planWorksMap.put(id, (double) Math.round(worksD * 10) / 10);
				}
			}
		});

		c("resourceActual", domain).aggregate(Domain.getJQ(domain, "查询-资源-实际用量-项目").set("match", match).array()).forEach((Document doc) -> {
			String id = doc.getString("_id");
			Double worksD = actualWorksMap.get(id);
			if (worksD != null) {
				Object works = doc.get("actualQty");
				if (works != null) {
					worksD += ((Number) works).doubleValue();
					actualWorksMap.put(id, (double) Math.round(worksD * 10) / 10);
				}
			}
		});

		List<Double> planWorks = Arrays.asList(planWorksMap.values().toArray(new Double[0]));
		List<Double> actualWorks = Arrays.asList(actualWorksMap.values().toArray(new Double[0]));
		return Domain.getJQ(domain, "图表-资源计划和实际-项目").set("title", "项目资源计划和用量状况（小时）").set("xAxis", xAxisDate).set("planWorks", planWorks)
				.set("actualWorks", actualWorks).doc();
	}

	@Override
	public Document getResourceAllAnalysis(ObjectId project_id, String domain) {
		Document first = c("project", domain).find(new Document("_id", project_id)).projection(new Document("actualStart", true)
				.append("actualFinish", true).append("planStart", true).append("planFinish", true).append("name", true)).first();
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		Object actualStart = first.get("actualStart");
		Object actualFinish = first.get("actualFinish");
		Object planStart = first.get("planStart");
		Object planFinish = first.get("planFinish");
		Object name = first.get("name");
		if (actualFinish != null && ((Date) actualFinish).after(((Date) planFinish)))
			end.setTime((Date) actualFinish);
		else
			end.setTime((Date) planFinish);

		if (actualStart != null && ((Date) actualStart).before(((Date) planStart)))
			start.setTime((Date) actualStart);
		else
			start.setTime((Date) planStart);

		start.set(Calendar.DAY_OF_MONTH, 1);
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);

		end.set(Calendar.DAY_OF_MONTH, 1);
		end.set(Calendar.HOUR_OF_DAY, 0);
		end.set(Calendar.MINUTE, 0);
		end.set(Calendar.SECOND, 0);
		end.set(Calendar.MILLISECOND, 0);

		List<String> xAxisDate = new ArrayList<String>();
		Map<String, Double> actualWorksMap = new TreeMap<String, Double>();
		Map<String, Double> actualAmountMap = new TreeMap<String, Double>();
		Map<String, Double> planWorksMap = new TreeMap<String, Double>();
		Map<String, Double> planAmountMap = new TreeMap<String, Double>();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMM");
		while (start.before(end) || start.equals(end)) {
			String key = sdf1.format(start.getTime());
			xAxisDate.add(sdf.format(start.getTime()));
			actualWorksMap.put(key, 0d);
			actualAmountMap.put(key, 0d);
			planWorksMap.put(key, 0d);
			planAmountMap.put(key, 0d);
			start.add(Calendar.MONTH, 1);
		}

		Document match = new Document("project_id", project_id);
		c("resourcePlan", domain).aggregate(Domain.getJQ(domain, "查询-资源-计划用量-项目").set("match", match).array()).forEach((Document doc) -> {
			String id = doc.getString("_id");
			Double worksD = planWorksMap.get(id);
			if (worksD != null) {
				Object works = doc.get("planQty");
				if (works != null) {
					worksD += ((Number) works).doubleValue();
					planWorksMap.put(id, worksD);
				}
			}

			Double amountD = planAmountMap.get(id);
			if (amountD != null) {
				Object amount = doc.get("planAmount");
				if (amount != null) {
					amountD += (((Number) amount).doubleValue() / 10000d);
					planAmountMap.put(id, amountD);
				}
			}
		});

		c("resourceActual", domain).aggregate(Domain.getJQ(domain, "查询-资源-实际用量-项目").set("match", match).array()).forEach((Document doc) -> {
			String id = doc.getString("_id");
			Double worksD = actualWorksMap.get(id);
			if (worksD != null) {
				Object works = doc.get("actualQty");
				if (works != null) {
					worksD += ((Number) works).doubleValue();
					actualWorksMap.put(id, worksD);
				}
			}

			Double amountD = actualAmountMap.get(id);
			if (amountD != null) {
				Object amount = doc.get("actualAmount");
				if (amount != null) {
					amountD += (((Number) amount).doubleValue() / 10000d);
					actualAmountMap.put(id, amountD);
				}
			}
		});

		Document option = createResourceAllAnalysis(start, end, "" + name + " 资源用量综合分析", actualWorksMap, actualAmountMap, planWorksMap,
				planAmountMap, xAxisDate);
		option.append("dataZoom", Arrays.asList(new Document("type", "inside").append("xAxisIndex", Arrays.asList(0, 1)),
				new Document("type", "slider").append("xAxisIndex", Arrays.asList(0, 1))));
		return option;

	}

	@Override
	public Document getResourceAllAnalysisByDept(String year, String userid, String domain) {
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();

		start.set(Integer.parseInt(year), 0, 1, 0, 0, 0);
		start.set(Calendar.MILLISECOND, 0);

		end.set(Integer.parseInt(year), 11, 1, 0, 0, 0);
		end.set(Calendar.MILLISECOND, 0);

		List<String> xAxisDate = new ArrayList<String>();
		Map<String, Double> actualWorksMap = new TreeMap<String, Double>();
		Map<String, Double> actualAmountMap = new TreeMap<String, Double>();
		Map<String, Double> planWorksMap = new TreeMap<String, Double>();
		Map<String, Double> planAmountMap = new TreeMap<String, Double>();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMM");
		while (start.before(end) || start.equals(end)) {
			String key = sdf1.format(start.getTime());
			xAxisDate.add(sdf.format(start.getTime()));
			actualWorksMap.put(key, 0d);
			actualAmountMap.put(key, 0d);
			planWorksMap.put(key, 0d);
			planAmountMap.put(key, 0d);
			start.add(Calendar.MONTH, 1);
		}
		List<ObjectId> orgids = new ArrayList<ObjectId>();
		List<String> deptName = new ArrayList<String>();
		Document match = new Document("year", year);
		c("organization", domain).find(new Document("managerId", userid)).forEach((Document doc) -> {
			orgids.add(doc.getObjectId("_id"));
			deptName.add(doc.getString("name"));
		});
		orgids.addAll(getDesentItems(orgids, "organization", "parent_id", domain));
		c("resourcePlan", domain)
				.aggregate(Domain.getJQ(domain, "查询-资源-计划用量-部门").set("match", match).set("org_ids", new Document("$in", orgids)).array())
				.forEach((Document doc) -> {
					String id = doc.getString("_id");
					Double worksD = planWorksMap.get(id);
					if (worksD != null) {
						Object works = doc.get("planQty");
						if (works != null) {
							worksD += ((Number) works).doubleValue();
							planWorksMap.put(id, worksD);
						}
					}

					Double amountD = planAmountMap.get(id);
					if (amountD != null) {
						Object amount = doc.get("planAmount");
						if (amount != null) {
							amountD += (((Number) amount).doubleValue() / 10000d);
							planAmountMap.put(id, amountD);
						}
					}
				});

		c("resourceActual", domain)
				.aggregate(Domain.getJQ(domain, "查询-资源-实际用量-部门").set("match", match).set("org_ids", new Document("$in", orgids)).array())
				.forEach((Document doc) -> {
					String id = doc.getString("_id");
					Double worksD = actualWorksMap.get(id);
					if (worksD != null) {
						Object works = doc.get("actualQty");
						if (works != null) {
							worksD += ((Number) works).doubleValue();
							actualWorksMap.put(id, worksD);
						}
					}

					Double amountD = actualAmountMap.get(id);
					if (amountD != null) {
						Object amount = doc.get("actualAmount");
						if (amount != null) {
							amountD += (((Number) amount).doubleValue() / 10000d);
							actualAmountMap.put(id, amountD);
						}
					}
				});

		Document option = createResourceAllAnalysis(start, end, year + "年  " + Formatter.getString(deptName) + " 资源用量综合分析", actualWorksMap,
				actualAmountMap, planWorksMap, planAmountMap, xAxisDate);
		return option;

	}

	private Document createResourceAllAnalysis(Calendar start, Calendar end, Object text, Map<String, Double> actualWorksMap,
			Map<String, Double> actualAmountMap, Map<String, Double> planWorksMap, Map<String, Double> planAmountMap,
			List<String> xAxisDate) {

		List<Document> series = new ArrayList<Document>();

		Document planWorksData = new Document();
		planWorksData.append("name", "计划工时");
		planWorksData.append("type", "bar");
		planWorksData.append("label", new Document("normal", new Document("show", true).append("position", "top")));

		List<Object> palnWorksdata = new ArrayList<Object>();
		for (Double d : planWorksMap.values()) {
			palnWorksdata.add(getStringValue(d));
		}
		planWorksData.append("data", palnWorksdata);
		series.add(planWorksData);

		Document actualWorksData = new Document();
		actualWorksData.append("name", "实际工时");
		actualWorksData.append("type", "bar");
		actualWorksData.append("label", new Document("normal", new Document("show", true).append("position", "top")));

		List<Object> actualWorksdata = new ArrayList<Object>();
		for (Double d : actualWorksMap.values()) {
			actualWorksdata.add(getStringValue(d));
		}
		actualWorksData.append("data", actualWorksdata);
		series.add(actualWorksData);

		Document planAmountData = new Document();
		planAmountData.append("name", "计划金额");
		planAmountData.append("type", "bar");
		planAmountData.append("yAxisIndex", 1);
		planAmountData.append("xAxisIndex", 1);
		planAmountData.append("label", new Document("normal", new Document("show", true).append("position", "top")));
		List<Object> planAmountdata = new ArrayList<Object>();
		for (Double d : planAmountMap.values()) {
			planAmountdata.add(getStringValue(d));
		}
		planAmountData.append("data", planAmountdata);

		series.add(planAmountData);

		Document actualAmountData = new Document();
		actualAmountData.append("name", "实际金额");
		actualAmountData.append("type", "bar");
		actualAmountData.append("yAxisIndex", 1);
		actualAmountData.append("xAxisIndex", 1);
		actualAmountData.append("label", new Document("normal", new Document("show", true).append("position", "top")));
		List<Object> actualAmountdata = new ArrayList<Object>();
		for (Double d : actualAmountMap.values()) {
			actualAmountdata.add(getStringValue(d));
		}
		actualAmountData.append("data", actualAmountdata);

		series.add(actualAmountData);

		Document option = new Document();
		option.append("title", new Document("text", text).append("x", "center"));
		option.append("tooltip", new Document("trigger", "axis").append("axisPointer", new Document("type", "shadow")));

		option.append("legend",
				new Document("data", Arrays.asList("计划工时", "实际工时", "计划金额", "实际金额")).append("y", "top").append("x", "right"));
		option.append("grid", Arrays.asList(
				new Document("left", 10).append("right", 10).append("bottom", "50%").append("containLabel", true),
				new Document("left", 10).append("right", 10).append("bottom", 40).append("top", "55%").append("containLabel", true)));

		option.append("xAxis", Arrays.asList(new Document("type", "category").append("data", xAxisDate),
				new Document("type", "category").append("data", xAxisDate).append("gridIndex", 1)));

		option.append("yAxis",
				Arrays.asList(
						new Document("type", "value").append("name", "工时").append("axisLabel", new Document("formatter", "{value} 小时")),
						new Document("type", "value").append("name", "金额").append("axisLabel", new Document("formatter", "{value} 万元"))
								.append("gridIndex", 1)));

		option.append("series", series);
		return option;
	}

	@Override
	public List<Document> getProjectResource(ObjectId project_id, String domain) {
		return c("work", domain)
				.aggregate(Domain.getJQ(domain, "查询-资源-计划和实际用量-项目").set("match", new Document("project_id", project_id)).array())
				.into(new ArrayList<Document>());
	}

	@Override
	public List<Document> getResourceOfChargedDept(Period period, String chargerId, String domain) {
		List<ObjectId> orgids = c("user", domain).distinct("org_id", new Document(), ObjectId.class).into(new ArrayList<ObjectId>());
		orgids = getDesentItems(orgids, "organization", "parent_id", domain);

		return c("work", domain)
				.aggregate(Domain.getJQ(domain, "查询-资源-计划和实际用量-负责人所在部门").set("workMatch", new Document())
						.set("org_ids", new Document("$in", orgids)).set("start", period.from).set("end", period.to).array())
				.into(new ArrayList<Document>());
	}

	@Override
	public List<Document> addWorkReportResourceActual(List<ResourceAssignment> resas, ObjectId workReportItemId, String domain) {
		List<Document> documents = new ArrayList<Document>();
		resas.forEach(resa -> {
			Date planStart = resa.from;
			Date planFinish = resa.to;
			Calendar planStartCal = Calendar.getInstance();
			planStartCal.setTime(planStart);
			planStartCal.set(Calendar.HOUR_OF_DAY, 0);
			planStartCal.set(Calendar.MINUTE, 0);
			planStartCal.set(Calendar.SECOND, 0);
			planStartCal.set(Calendar.MILLISECOND, 0);

			Calendar planFinishCal = Calendar.getInstance();
			planFinishCal.setTime(planFinish);
			planFinishCal.add(Calendar.DAY_OF_MONTH, 1);
			planFinishCal.set(Calendar.HOUR_OF_DAY, 0);
			planFinishCal.set(Calendar.MINUTE, 0);
			planFinishCal.set(Calendar.SECOND, 0);
			planFinishCal.set(Calendar.MILLISECOND, 0);

			while (planStartCal.getTime().before(planFinishCal.getTime())) {
				Date time = planStartCal.getTime();
				Document res = resa.getResourceActualDocument();
				res.append("id", time);
				res.append("workReportItemId", workReportItemId);
				documents.add(res);

				c("workReportResourceActual", domain).deleteMany(
						new Document("id", time).append("work_id", res.get("work_id")).append("usedHumanResId", res.get("usedHumanResId"))
								.append("usedEquipResId", res.get("usedEquipResId")).append("usedTypedResId", res.get("usedTypedResId"))
								.append("resTypeId", res.get("resTypeId")).append("workReportItemId", workReportItemId));
				planStartCal.add(Calendar.DAY_OF_MONTH, 1);
			}

			double actualBasicQty = resa.actualBasicQty / documents.size();

			double actualOverTimeQty = resa.actualOverTimeQty / documents.size();
			for (Document resourceActual : documents) {
				resourceActual.append("actualBasicQty", actualBasicQty);
				resourceActual.append("actualOverTimeQty", actualOverTimeQty);
			}
		});
		if (documents.size() > 0)
			c("workReportResourceActual", domain).insertMany(documents);

		return documents;
	}

	@Override
	public Document insertWorkReportResourceActual(ResourceActual ra, ObjectId workReportItemId, String domain) {
		Document doc = new Document("id", ra.getId()).append("work_id", ra.getWork_id()).append("usedHumanResId", ra.getUsedHumanResId())
				.append("usedEquipResId", ra.getUsedEquipResId()).append("usedTypedResId", ra.getUsedTypedResId())
				.append("resTypeId", ra.getResTypeId()).append("workReportItemId", workReportItemId)
				.append("actualBasicQty", ra.getActualBasicQty()).append("actualOverTimeQty", ra.getActualOverTimeQty());

		c("workReportResourceActual", domain).insertOne(doc);
		return doc;
	}

	@Override
	public long updateWorkReportResourceActual(BasicDBObject filterAndUpdate, String domain) {
		BasicDBObject filter = (BasicDBObject) filterAndUpdate.get("filter");
		BasicDBObject update = (BasicDBObject) filterAndUpdate.get("update");
		update.remove("_id");
		UpdateOptions option = new UpdateOptions();
		option.upsert(false);
		UpdateResult updateMany = c("workReportResourceActual", domain).updateMany(filter, update, option);
		long cnt = updateMany.getModifiedCount();
		return cnt;
	}

	@Override
	public List<Work> createBaselineTaskDataSet(ObjectId baseline_id, String domain) {

		List<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new BasicDBObject("baseline_id", baseline_id)));
		// yangjun 2018/10/31
		pipeline.add(Aggregates.sort(new BasicDBObject("index", 1).append("_id", -1)));

		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		pipeline.add(Aggregates.addFields(
				Arrays.asList(new Field<String>("projectName", "$project.name"), new Field<String>("projectNumber", "$project.id"))));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));

		appendUserInfo(pipeline, "chargerId", "chargerInfo", domain);

		appendUserInfo(pipeline, "assignerId", "assignerInfo", domain);

		// appendOverdue(pipeline);
		//
		// appendWorkTime(pipeline);

		ArrayList<Work> into = c("baselineWork", Work.class, domain).aggregate(pipeline).into(new ArrayList<Work>());
		return into;
	}

	@Override
	public List<WorkLink> createBaselineLinkDataSet(ObjectId baseline_id, String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("baseline_id", baseline_id)));
		// yangjun 2018/10/31
		pipeline.add(Aggregates.sort(new BasicDBObject("index", 1).append("_id", -1)));

		pipeline.add(Aggregates.lookup("baselineWork", "source", "_id", "sourceWork"));
		pipeline.add(Aggregates.unwind("$sourceWork"));

		pipeline.add(Aggregates.lookup("baselineWork", "target", "_id", "targetWork"));
		pipeline.add(Aggregates.unwind("$targetWork"));

		ArrayList<WorkLink> into = c("baselineWorkLinks", WorkLink.class, domain).aggregate(pipeline).into(new ArrayList<WorkLink>());
		return into;
	}

	@Override
	public Document getProjectWorkScoreChart(ObjectId project_id, String domain) {
		Document workFilter = new Document("project_id", project_id).append("actualStart", new Document("$ne", null));
		return getWorkScoreChart(workFilter, domain);
	}

	@Override
	public Document getAdministratedProjectWorkScoreChart(String managerId, String domain) {
		List<ObjectId> pjIdList = getAdministratedProjects(managerId, domain);
		Document workFilter = new Document("project_id", new Document("$in", pjIdList)).append("actualStart", new Document("$ne", null));
		return getWorkScoreChart(workFilter, domain);
	}

	private Document getWorkScoreChart(Document workFilter, String domain) {
		List<Document> indicator = new ArrayList<>();
		List<Double> avg = new ArrayList<>();

		List<Bson> pipe = Domain.getJQ(domain, "查询-评分-工作按期率").set("match", new Document("actualStart", new Document("$ne", null)))
				.set("now", new Date()).array();
		debugPipeline(pipe);
		c("work", domain).aggregate(pipe).forEach((Document d) -> {
			indicator.add(new Document("name", d.getString("_id")).append("max", 100));
			avg.add((double) Math.round(1000 * ((Number) d.get("score")).doubleValue()) / 10);
		});

		Double[] value = new Double[avg.size()];

		c("work", domain).aggregate(Domain.getJQ(domain, "查询-评分-工作按期率").set("match", workFilter).set("now", new Date()).array())
				.forEach((Document d) -> {
					for (int i = 0; i < indicator.size(); i++) {
						if (indicator.get(i).getString("name").equals(d.getString("_id"))) {
							value[i] = (double) Math.round(1000 * ((Number) d.get("score")).doubleValue()) / 10;
							break;
						}
					}
				});

		if (indicator.size() == 0)
			indicator.add(new Document("name", "").append("max", 100));

		return Domain.getJQ(domain, "图表-评分-工作按期率").set("indicator", indicator).set("avg", avg).set("value", Arrays.asList(value)).doc();
	}

	@Override
	public List<Work> createChargerProcessingWorkDataSet(BasicDBObject condition, String userid, String domain) {

		Integer skip = Optional.ofNullable(condition).map(c -> (Integer) c.get("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> (Integer) c.get("limit")).orElse(null);
		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("filter")).orElse(null);
		BasicDBObject sort = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("sort")).orElse(null);

		List<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new BasicDBObject("chargerId", userid).append("summary", false).append("actualFinish", null)
				.append("distributed", true).append("stage", new BasicDBObject("$ne", true))));

		appendProject(pipeline);

		appendUserInfo(pipeline, "chargerId", "chargerInfo", domain);

		appendUserInfo(pipeline, "assignerId", "assignerInfo", domain);

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));
		else// yangjun 2018/10/31
			pipeline.add(Aggregates.sort(new BasicDBObject("planFinish", 1).append("_id", -1)));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		return c(Work.class, domain).aggregate(pipeline).into(new ArrayList<Work>());
	}

	@Override
	public long countChargerProcessingWorkDataSet(BasicDBObject filter, String userid, String domain) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("summary", false);
		filter.put("actualFinish", null);
		filter.put("distributed", true);
		filter.put("chargerId", userid);
		filter.put("stage", new BasicDBObject("$ne", true));
		return count(filter, Work.class, domain);
	}

	@Override
	public List<Work> listMyAssignmentWork(BasicDBObject condition, String userid, String domain) {
		BasicDBObject basicCondition = appendAssignmentWorkCondition(new BasicDBObject().append("assignerId", userid));

		Integer skip = Optional.ofNullable(condition).map(c -> (Integer) c.get("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> (Integer) c.get("limit")).orElse(null);
		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("filter")).orElse(null);
		BasicDBObject sort = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("sort"))
				.orElse(new BasicDBObject("planFinish", 1).append("_id", -1));

		return queryWork(skip, limit, basicCondition, filter, sort, domain).into(new ArrayList<>());
	}

	private BasicDBObject appendAssignmentWorkCondition(BasicDBObject condition) {
		if (condition.get("assignerId") == null)
			condition.put("assignerId", new BasicDBObject("$ne", null));

		condition.append("summary", false).append("actualFinish", null).append("distributed", true).append("stage",
				new BasicDBObject("$ne", true));
		return condition;
	}

	@Override
	public long countMyAssignmentWork(BasicDBObject filter, String userid, String domain) {
		if (filter == null)
			filter = new BasicDBObject();

		appendAssignmentWorkCondition(filter.append("assignerId", userid));
		return count(filter, Work.class, domain);
	}

	@Override
	public List<WorkPackage> updatePurchaseWorkPackage(UpdateWorkPackages uwp, String domain) {
		BasicDBObject filter = new BasicDBObject("work_id", uwp.getWork_id()).append("catagory", uwp.getCatagory()).append("name",
				uwp.getName());
		List<ObjectId> ids = c("workPackage", domain).distinct("_id", filter, ObjectId.class).into(new ArrayList<ObjectId>());
		c("workPackageProgress", domain).deleteMany(new BasicDBObject("", new BasicDBObject("$in", ids)));
		List<WorkPackage> workPackages = uwp.getWorkPackages();
		if (workPackages.size() > 0)
			c(WorkPackage.class, domain).insertMany(workPackages);
		return listWorkPackage(new Query()
				.filter(new BasicDBObject("work_id", uwp.getWork_id()).append("catagory", uwp.getCatagory()).append("name", uwp.getName()))
				.bson(), domain);
	}

	@Override
	public void removeWorkPackage(List<UpdateWorkPackages> uwps, String domain) {
		for (UpdateWorkPackages uwp : uwps) {
			BasicDBObject filter = new BasicDBObject("work_id", uwp.getWork_id()).append("catagory", uwp.getCatagory()).append("name",
					uwp.getName());
			List<ObjectId> ids = c("workPackage", domain).distinct("_id", filter, ObjectId.class).into(new ArrayList<ObjectId>());
			c("workPackageProgress", domain).deleteMany(new BasicDBObject("", new BasicDBObject("$in", ids)));
			c("workPackage", domain).deleteMany(filter).getDeletedCount();
		}
	}

	@Override
	public List<WorkPackage> updateProductionWorkPackage(UpdateWorkPackages uwp, String domain) {
		BasicDBObject dbo = new BasicDBObject("work_id", uwp.getWork_id()).append("catagory", uwp.getCatagory()).append("name",
				uwp.getName());
		List<ObjectId> ids = c(WorkPackage.class, domain).distinct("_id", dbo, ObjectId.class).into(new ArrayList<ObjectId>());
		c(WorkPackage.class, domain).deleteMany(dbo);
		c(WorkPackageProgress.class, domain).deleteMany(new Document("package_id", new Document("$in", ids)));

		List<WorkPackage> workPackages = uwp.getWorkPackages();
		if (workPackages.size() > 0) {
			c(WorkPackage.class, domain).insertMany(workPackages);

			List<WorkPackageProgress> workPackageProgresss = uwp.getWorkPackageProgresss();
			if (workPackageProgresss.size() > 0)
				c(WorkPackageProgress.class, domain).insertMany(workPackageProgresss);
		}

		return listWorkPackage(new Query().filter(dbo).bson(), domain);
	}

	@Override
	public List<WorkPackage> updateDevelopmentWorkPackage(UpdateWorkPackages uwp, String domain) {
		List<WorkPackage> workPackages = uwp.getWorkPackages();

		if (workPackages.size() > 0) {
			List<ObjectId> workPackageIds = new ArrayList<ObjectId>();
			workPackages.forEach(workPackage -> {
				ObjectId _id = workPackage.get_id();
				c(WorkPackage.class, domain).updateOne(new BasicDBObject("_id", _id),
						new BasicDBObject("$set", new BasicDBObject("id", workPackage.id).append("description", workPackage.description)
								.append("verNo", workPackage.verNo).append("planStatus", workPackage.planStatus)
								.append("documentType", workPackage.documentType).append("completeStatus", workPackage.completeStatus)));

				workPackageIds.add(_id);
			});
			c(WorkPackageProgress.class, domain).deleteMany(new BasicDBObject("package_id", new BasicDBObject("$in", workPackageIds)));

			List<WorkPackageProgress> workPackageProgresss = uwp.getWorkPackageProgresss();
			if (workPackageProgresss.size() > 0)
				c(WorkPackageProgress.class, domain).insertMany(workPackageProgresss);
		}

		return listWorkPackage(new Query()
				.filter(new BasicDBObject("work_id", uwp.getWork_id()).append("catagory", uwp.getCatagory()).append("name", uwp.getName()))
				.bson(), domain);
	}

	@Override
	public ObjectId updateWorkPackageInfo(Document info, String domain) {
		ObjectId _id = info.getObjectId("_id");
		ObjectId work_id = info.getObjectId("work_id");
		String catagory = info.getString("catagory");
		String name = info.getString("name");
		Double completeQty = info.getDouble("completeQty");
		info.remove("_id");
		info.remove("work_id");
		info.remove("catagory");
		info.remove("name");
		if (_id == null) {
			_id = new ObjectId();
			c("workPackage", domain).insertOne(new Document("_id", _id).append("work_id", work_id).append("name", name)
					.append("catagory", catagory).append("completeQty", completeQty).append("info", info));
		} else {
			c("workPackage", domain).updateOne(new Document("_id", _id),
					new Document("$set", new Document("info", info).append("completeQty", completeQty)));
		}
		return _id;
	}

	@Override
	public List<Work> listProjectPlannedWork(BasicDBObject condition, ObjectId project_id, String domain) {
		return iterateWorks(condition, new BasicDBObject("project_id", project_id).append("chargerId", new BasicDBObject("$ne", null)),
				new BasicDBObject("planStart", 1).append("_id", -1), this::appendPlanWorkCondition, domain).into(new ArrayList<Work>());
	}

	@Override
	public List<Document> listProjectPlannedWorkCard(BasicDBObject condition, ObjectId project_id, String userid, String lang,
			String domain) {
		ArrayList<Document> into = iterateWorks(condition,
				new BasicDBObject("project_id", project_id).append("chargerId", new BasicDBObject("$ne", null)),
				new BasicDBObject("planStart", 1).append("_id", -1), this::appendPlanWorkCondition, domain)
						.map((Work w) -> WorkRenderer.render(w, Check.equals(userid, w.getChargerId()), false, lang))
						.into(new ArrayList<>());
		return into;
	}

	@Override
	public List<Document> listMyTodoWorkCard(BasicDBObject condition, String userid, String lang, String domain) {
		BasicDBObject bc = new BasicDBObject("$or",
				Arrays.asList(new BasicDBObject("chargerId", userid), new BasicDBObject("assignerId", userid)));
		return iterateWorks(condition, bc, new BasicDBObject("planStart", 1).append("_id", -1), this::appendTodoWorkCondition, domain)
				.map(w -> WorkRenderer.render(w, lang)).into(new ArrayList<>());
	}

	@Override
	public long countMyTodoWork(BasicDBObject filter, String userid, String domain) {
		if (filter == null)
			filter = new BasicDBObject();
		appendTodoWorkCondition(
				filter.append("$or", Arrays.asList(new BasicDBObject("chargerId", userid), new BasicDBObject("assignerId", userid))));
		return count(filter, Work.class, domain);
	}

	@Override
	public List<Work> listMyPlannedWork(BasicDBObject condition, String userid, String domain) {
		return iterateWorks(condition, new BasicDBObject("chargerId", userid), new BasicDBObject("planStart", 1).append("_id", -1),
				this::appendPlanWorkCondition, domain).into(new ArrayList<Work>());
	}

	@Override
	public List<Document> listMyPlannedWorkCard(BasicDBObject condition, String userid, String lang, String domain) {
		return iterateWorks(condition, new BasicDBObject("chargerId", userid), new BasicDBObject("planStart", 1).append("_id", -1),
				this::appendPlanWorkCondition, domain).map(w -> WorkRenderer.render(w, lang)).into(new ArrayList<>());
	}

	@Override
	public long countMyPlannedWork(BasicDBObject filter, String userid, String domain) {
		if (filter == null)
			filter = new BasicDBObject();
		appendPlanWorkCondition(filter.append("chargerId", userid));
		return count(filter, Work.class, domain);
	}

	@Override
	public long countProjectPlannedWork(BasicDBObject filter, ObjectId project_id, String domain) {
		if (filter == null)
			filter = new BasicDBObject();
		appendPlanWorkCondition(filter.append("project_id", project_id).append("chargerId", new BasicDBObject("$ne", null)));
		return count(filter, Work.class, domain);
	}

	@Override
	public List<Work> listProjectExecutingWork(BasicDBObject condition, ObjectId project_id, String domain) {
		return iterateWorks(condition,
				new BasicDBObject().append("project_id", project_id).append("chargerId", new BasicDBObject("$ne", null)),
				new BasicDBObject("planStart", 1).append("_id", -1), this::appendExecWorkCondition, domain).into(new ArrayList<Work>());
	}

	@Override
	public List<Document> listProjectExecutingWorkCard(BasicDBObject condition, ObjectId project_id, String userid, String lang,
			String domain) {
		return iterateWorks(condition,
				new BasicDBObject().append("project_id", project_id).append("chargerId", new BasicDBObject("$ne", null)),
				new BasicDBObject("planStart", 1).append("_id", -1), this::appendExecWorkCondition, domain)
						.map((Work w) -> WorkRenderer.render(w, Check.equals(userid, w.getChargerId()), false, lang))
						.into(new ArrayList<>());
	}

	@Override
	public List<Work> listMyExecutingWork(BasicDBObject condition, String userid, String domain) {
		return iterateWorks(condition, new BasicDBObject("chargerId", userid), new BasicDBObject("planStart", 1).append("_id", -1),
				this::appendExecWorkCondition, domain).into(new ArrayList<Work>());
	}

	@Override
	public List<Document> listMyExecutingWorkCard(BasicDBObject condition, String userid, String lang, String domain) {
		return iterateWorks(condition, new BasicDBObject("chargerId", userid), new BasicDBObject("planStart", 1).append("_id", -1),
				this::appendExecWorkCondition, domain).map(w -> WorkRenderer.render(w, lang)).into(new ArrayList<>());
	}

	private AggregateIterable<Work> iterateWorks(BasicDBObject condition, BasicDBObject basicCondition, BasicDBObject defaultSort,
			Consumer<BasicDBObject> appendCondition, String domain) {

		Integer skip = Optional.ofNullable(condition).map(c -> (Integer) c.get("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> (Integer) c.get("limit")).orElse(null);
		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("filter")).orElse(null);
		BasicDBObject sort = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("sort")).orElse(defaultSort);

		appendCondition.accept(basicCondition);

		return queryWork(skip, limit, basicCondition, filter, sort, domain);
	}

	@Override
	public long countMyExecutingWork(BasicDBObject filter, String userid, String domain) {
		if (filter == null)
			filter = new BasicDBObject();
		appendExecWorkCondition(filter.append("chargerId", userid));
		return count(filter, Work.class, domain);
	}

	@Override
	public long countProjectExecutingWork(BasicDBObject filter, ObjectId project_id, String domain) {
		if (filter == null)
			filter = new BasicDBObject();
		appendExecWorkCondition(filter.append("project_id", project_id).append("chargerId", new BasicDBObject("$ne", null)));
		return count(filter, Work.class, domain);
	}

	@Override
	public List<Work> listProjectFinishedWork(BasicDBObject condition, ObjectId project_id, String domain) {
		return iterateWorks(condition, new BasicDBObject("project_id", project_id), new BasicDBObject("actualFinish", -1).append("_id", -1),
				this::appendFinishedWorkCondition, domain).into(new ArrayList<Work>());
	}

	@Override
	public List<Document> listProjectFinishedWorkCard(BasicDBObject condition, ObjectId project_id, String userid, String lang,
			String domain) {
		return iterateWorks(condition, new BasicDBObject("project_id", project_id), new BasicDBObject("actualFinish", -1).append("_id", -1),
				this::appendFinishedWorkCondition, domain)
						.map((Work w) -> WorkRenderer.render(w, Check.equals(userid, w.getChargerId()), false, lang))
						.into(new ArrayList<>());
	}

	@Override
	public List<Work> listMyFinishedWork(BasicDBObject condition, String userid, String domain) {
		return iterateWorks(condition,
				new BasicDBObject("$or",
						new BasicDBObject[] { new BasicDBObject("chargerId", userid), new BasicDBObject("assignerId", userid) }),
				new BasicDBObject("actualFinish", -1).append("_id", -1), this::appendFinishedWorkCondition, domain)
						.into(new ArrayList<Work>());
	}

	@Override
	public List<Document> listMyFinishedWorkCard(BasicDBObject condition, String userid, String lang, String domain) {
		return iterateWorks(condition,
				new BasicDBObject("$or",
						new BasicDBObject[] { new BasicDBObject("chargerId", userid), new BasicDBObject("assignerId", userid) }),
				new BasicDBObject("actualFinish", -1).append("_id", -1), this::appendFinishedWorkCondition, domain)
						.map(w -> WorkRenderer.render(w, lang)).into(new ArrayList<>());
	}

	@Override
	public long countMyFinishedWork(BasicDBObject filter, String userid, String domain) {
		if (filter == null)
			filter = new BasicDBObject();

		appendFinishedWorkCondition(filter.append("$or",
				new BasicDBObject[] { new BasicDBObject("chargerId", userid), new BasicDBObject("assignerId", userid) }));

		return count(filter, Work.class, domain);
	}

	@Override
	public long countProjectFinishedWork(BasicDBObject filter, ObjectId project_id, String domain) {
		if (filter == null)
			filter = new BasicDBObject();
		appendFinishedWorkCondition(filter.append("project_id", project_id));
		return count(filter, Work.class, domain);
	}

	@Override
	public List<Work> listProjectUnAssignmentWork(BasicDBObject condition, ObjectId project_id, String domain) {
		return iterateWorks(condition, new BasicDBObject("chargerId", null).append("project_id", project_id),
				new BasicDBObject("planFinish", 1).append("_id", -1), this::appendAssignmentWorkCondition, domain)
						.into(new ArrayList<Work>());
	}

	@Override
	public List<Document> listProjectUnAssignmentWorkCard(BasicDBObject condition, ObjectId project_id, String userid, String lang,
			String domain) {
		return iterateWorks(condition, new BasicDBObject("chargerId", null).append("project_id", project_id),
				new BasicDBObject("planFinish", 1).append("_id", -1), this::appendAssignmentWorkCondition, domain)
						.map((Work w) -> WorkRenderer.render(w, Check.equals(userid, w.getAssignerId()), false, lang))
						.into(new ArrayList<>());
	}

	@Override
	public List<Work> listMyUnAssignmentWork(BasicDBObject condition, String userid, String domain) {
		return iterateWorks(condition, new BasicDBObject("chargerId", null).append("assignerId", userid),
				new BasicDBObject("planFinish", 1).append("_id", -1), this::appendAssignmentWorkCondition, domain).into(new ArrayList<>());
	}

	@Override
	public List<Document> listMyUnAssignmentWorkCard(BasicDBObject condition, String userid, String lang, String domain) {
		return iterateWorks(condition, new BasicDBObject("chargerId", null).append("assignerId", userid),
				new BasicDBObject("planFinish", 1).append("_id", -1), this::appendAssignmentWorkCondition, domain)
						.map(w -> WorkRenderer.render(w, lang)).into(new ArrayList<>());
	}

	@Override
	public long countMyUnAssignmentWork(BasicDBObject filter, String userid, String domain) {
		if (filter == null)
			filter = new BasicDBObject();
		appendAssignmentWorkCondition(filter.append("assignerId", userid).append("chargerId", null));
		return count(filter, Work.class, domain);
	}

	@Override
	public long countProjectUnAssignmentWork(BasicDBObject filter, ObjectId project_id, String domain) {
		if (filter == null)
			filter = new BasicDBObject();
		appendAssignmentWorkCondition(filter.append("chargerId", null).append("project_id", project_id));
		return count(filter, Work.class, domain);
	}

	/**
	 * 清除指定用户未开始工作的负责人和参与者
	 */
	@Override
	public void removeUnStartWorkUser(List<String> userId, ObjectId project_id, String currentUserId, String domain) {
		String msgSubject = "工作移除通知";
		Project project = c(Project.class, domain).find(new Document("_id", project_id)).first();
		List<Message> messages = new ArrayList<>();
		c("work", domain).find(new Document("project_id", project_id)//
				.append("actualFinish", null)//
				.append("distributed", true)//
				.append("$or", Arrays.asList(new Document("chargerId", new Document("$in", userId)),
						new Document("assignerId", new Document("$in", userId)))))
				.forEach((Document d) -> {
					String content = "项目：" + project.getName() + " ，工作：" + d.getString("fullName");
					if (userId.contains(d.getString("chargerId"))) {
						messages.add(Message.newInstance(msgSubject, content + "，您已不再担任工作负责人。", currentUserId,
								(String) d.getString("chargerId"), null));
					}
					if (userId.contains(d.getString("assignerId"))) {
						messages.add(Message.newInstance(msgSubject, content + "，您已不再担任工作指派者。", currentUserId,
								(String) d.getString("assignerId"), null));
					}
				});

		// 清除工作负责人
		c("work", domain).updateMany(
				new Document("project_id", project_id).append("actualStart", null).append("chargerId", new Document("$in", userId)),
				new Document("$set", new Document("chargerId", null)));

		// 清除工作指派者
		c("work", domain).updateMany(
				new Document("project_id", project_id).append("actualStart", null).append("assignerId", new Document("$in", userId)),
				new Document("$set", new Document("assignerId", null)));

		// 清除工作区工作负责人
		c("workspace", domain).updateMany(
				new Document("project_id", project_id).append("actualStart", null).append("chargerId", new Document("$in", userId)),
				new Document("$set", new Document("chargerId", null)));

		// 清除工作区工作参与者
		c("workspace", domain).updateMany(
				new Document("project_id", project_id).append("actualStart", null).append("assignerId", new Document("$in", userId)),
				new Document("$set", new Document("assignerId", null)));

		// 消息
		String projectStatus = project.getStatus();
		if (messages.size() > 0 && !ProjectStatus.Created.equals(projectStatus))
			sendMessages(messages, projectStatus);
	}

	@Override
	public Work assignUserToWorkChager(ObjectId work_id, String userId, String domain) {
		// 获得当前指派人员的工作
		Work work = getWork(work_id, domain);
		ObjectId project_id = work.getProject_id();
		// 检查当前用户是否在项目团队中（即该用户是否在项目团队中具有角色）
		OBSServiceImpl obsServiceImpl = new OBSServiceImpl();
		List<String> scopeRoleofUser = obsServiceImpl.getScopeRoleofUser(project_id, userId, domain);
		// 如果当前用户不在项目团队中，将其添加到项目团队中
		if (scopeRoleofUser.size() == 0) {
			// 获取当前项目的项目参与者团队,如果没有就进行添加，存在就增加成员
			List<OBSItem> roles = obsServiceImpl.getScopeTeamofRoleId(project_id, OBSItem.ID_PARTICIPATES, domain);
			if (roles.size() == 0) {
				ObjectId parent_id = obsServiceImpl.getScopeRootOBS(project_id, domain).get(0).get_id();
				OBSItem obsItem = new OBSItem().setDomain(domain).setParent_id(parent_id).setScope_id(project_id).setIsRole(false)
						.generateSeq(domain).setRoleId(OBSItem.ID_PARTICIPATES)// .setRoleName(OBSItem.NAME_PARTICIPATES)
						.setName("项目" + OBSItem.NAME_PARTICIPATES);
				Document doc = obsItem.encodeDocument();
				doc.put("member", Arrays.asList(userId));
				doc.remove("_id");
				c("obs", domain).insertOne(doc);
			} else {
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", roles.get(0).get_id()))
						.update(new BasicDBObject("$addToSet", new BasicDBObject("member", userId))).bson();
				update(fu, OBSItem.class, domain);
			}
		}
		// 修改工作负责人
		updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", work_id)).set(new BasicDBObject("chargerId", userId)).bson(),
				domain);
		// 消息
		Project project = c(Project.class, domain).find(new Document("_id", project_id)).first();
		sendMessage("工作指派通知", "项目：" + project.getName() + " ，工作：" + work.getFullName() + "，已指定您担任工作指派者。", work.getAssignerId(), userId,
				null, domain);
		return getWork(work_id, domain);
	}

	@Override
	public void transferWorkUser(ObjectId project_id, String sourceId, String targetId, String currentUserId, String domain) {
		String msgSubject = "工作移交通知";
		Project project = c(Project.class, domain).find(new Document("_id", project_id)).first();
		List<Message> messages = new ArrayList<>();
		c("work", domain).find(new Document("project_id", project_id)//
				.append("actualFinish", null)//
				.append("distributed", true)//
				.append("$or", Arrays.asList(new Document("chargerId", sourceId), new Document("assignerId", sourceId))))
				.forEach((Document d) -> {
					String content = "项目：" + project.getName() + " ，工作：" + d.getString("fullName");
					if (sourceId.equals(d.getString("chargerId"))) {
						messages.add(Message.newInstance(msgSubject, content + "，您已不再担任工作负责人。", currentUserId, (String) sourceId, null));
						messages.add(Message.newInstance(msgSubject, content + "，已指定您担任工作指派者。", currentUserId, (String) targetId, null));
					}
					if (sourceId.equals(d.getString("assignerId"))) {
						messages.add(Message.newInstance(msgSubject, content + "，您已不再担任工作指派者。", currentUserId, (String) sourceId, null));
						messages.add(Message.newInstance(msgSubject, content + "，已指定您担任工作负责人。", currentUserId, (String) targetId, null));
					}
				});

		// 移交未完成工作的负责人，包括：进行中和未开始的工作
		c("work", domain).updateMany(new Document("project_id", project_id)//
				.append("chargerId", sourceId)//
				.append("actualFinish", null), //
				new Document("$set", new Document("chargerId", targetId)));
		// 移交未完成工作的指派者，包括：进行中和未开始的工作
		c("work", domain).updateMany(new Document("project_id", project_id)//
				.append("assignerId", sourceId)//
				.append("actualFinish", null), //
				new Document("$set", new Document("assignerId", targetId)));

		// 移交工作区中未完成工作的负责人，包括：进行中和未开始的工作
		c("workspace", domain).updateMany(new Document("project_id", project_id)//
				.append("chargerId", sourceId)//
				.append("actualFinish", null), //
				new Document("$set", new Document("chargerId", targetId)));
		// 移交工作区中未完成工作的指派者，包括：进行中和未开始的工作
		c("workspace", domain).updateMany(new Document("project_id", project_id)//
				.append("assignerId", sourceId)//
				.append("actualFinish", null), //
				new Document("$set", new Document("assignerId", targetId)));

		// 消息
		String projectStatus = project.getStatus();
		if (messages.size() > 0 && !ProjectStatus.Created.equals(projectStatus))
			sendMessages(messages, domain);
	}
}
