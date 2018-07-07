package com.bizvisionsoft.serviceimpl;

import java.text.DecimalFormat;
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

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.Command;
import com.bizvisionsoft.service.model.DateMark;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.ResourceActual;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.ResourceTransfer;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkLink;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.service.model.WorkPackageProgress;
import com.bizvisionsoft.service.model.WorkResourcePlanDetail;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.service.tools.Util;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
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
	public List<Work> createTaskDataSet(BasicDBObject condition) {
		return queryWork(null, null, condition, null, new BasicDBObject("index", 1)).into(new ArrayList<Work>());
	}

	private AggregateIterable<Work> queryWork(Integer skip, Integer limit, BasicDBObject basicCondition,
			BasicDBObject filter, BasicDBObject sort) {
		List<Bson> pipeline = new ArrayList<Bson>();

		if (basicCondition != null)
			pipeline.add(Aggregates.match(basicCondition));

		appendProject(pipeline);

		appendUserInfo(pipeline, "chargerId", "chargerInfo");

		appendUserInfo(pipeline, "assignerId", "assignerInfo");

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		// pipeline.forEach(b->System.out.println(b.toBsonDocument(Document.class,
		// MongoClient.getDefaultCodecRegistry())));
		appendOverdue(pipeline);

		appendWorkTime(pipeline);

		AggregateIterable<Work> iterable = c(Work.class).aggregate(pipeline);
		return iterable;
	}

	private void appendWorkTime(List<Bson> pipeline) {
		pipeline.addAll(Arrays.asList(
				new Document("$lookup", new Document("from", "work")
						.append("let",
								new Document("wbsCode", "$wbsCode").append("project_id", "$project_id"))
						.append("pipeline",
								Arrays.asList(
										new Document("$match", new Document("$expr", new Document("$and", Arrays.asList(
												new Document("$eq", Arrays.asList("$project_id", "$$project_id")),
												new Document("$eq",
														Arrays.asList(new Document("$indexOfBytes",
																Arrays.asList("$wbsCode",
																		new Document("$concat",
																				Arrays.asList("$$wbsCode", ".")))),
																0)),
												new Document("$eq", Arrays.asList("$summary", false)))))),
										new Document().append("$addFields", new Document()
												.append("planDuration",
														new Document("$divide", Arrays.asList(
																new Document("$subtract",
																		Arrays.asList(new Document("$ifNull",
																				Arrays.asList("$estimatedFinish",
																						"$planFinish")),
																				"$planStart")),
																1000 * 3600 * 24)))
												.append("actualDuration",
														new Document("$divide", Arrays.asList(
																new Document("$subtract", Arrays.asList(
																		new Document("$ifNull",
																				Arrays.asList("$actualFinish",
																						new Date())),
																		new Document("$ifNull",
																				Arrays.asList("$actualStart",
																						new Date())))),
																1000 * 3600 * 24)))),
										new Document("$group", new Document("_id", null)
												.append("planDuration", new Document("$sum", "$planDuration"))
												.append("actualDuration", new Document("$sum", "$actualDuration"))
												.append("actualWorks", new Document("$sum", "$actualWorks"))
												.append("planWorks", new Document("$sum", "$planWorks")))))
						.append("as", "work")),
				new Document("$unwind", new Document("path", "$work").append("preserveNullAndEmptyArrays", true)),
				new Document("$addFields",
						new Document("summaryPlanDuration", "$work.planDuration")
								.append("summaryActualDuration", "$work.actualDuration")
								.append("summaryActualWorks", "$workTime.actualWorks")
								.append("summaryPlanWorks", "$workTime.planWorks")),
				new Document("$project", new Document("work", false))));

	}

	private void appendOverdue(List<Bson> pipeline) {
		int warningDay = (int) getSystemSetting(WARNING_DAY);
		warningDay = warningDay * 24 * 60 * 60 * 1000;
		List<Field<?>> fields = new ArrayList<Field<?>>();
		fields.add(new Field<Bson>("overdue", new BasicDBObject("$cond",
				new BasicDBObject("if", new BasicDBObject("$ifNull", new Object[] { "$actualFinish", true }))
						.append("else",
								new BasicDBObject("$cond", new BasicDBObject("if",
										new BasicDBObject("$lt", new Object[] { "$planFinish", new Date() }))
												.append("then", "超期")
												.append("else", new BasicDBObject("$cond",
														new BasicDBObject("if", new BasicDBObject("$lt",
																new Object[] { "$planFinish", new BasicDBObject("$add",
																		new Object[] { new Date(), warningDay }) }))
																				.append("then", "预警")
																				.append("else", "")))))
						.append("then", new BasicDBObject("$cond",
								new BasicDBObject("if",
										new BasicDBObject("$lt", new Object[] { "$planFinish", "$actualFinish" }))
												.append("then", "超期").append("else", "")))

		)));

		pipeline.add(Aggregates.addFields(fields));
	}

	@Override
	public List<WorkLink> createLinkDataSet(BasicDBObject condition) {
		return c(WorkLink.class).find(condition).into(new ArrayList<WorkLink>());
	}

	@Override
	public List<Work> listProjectRootTask(ObjectId project_id) {
		return createTaskDataSet(new BasicDBObject("project_id", project_id).append("parent_id", null));
	}

	@Override
	public long countProjectRootTask(ObjectId project_id) {
		return count(new BasicDBObject("project_id", project_id).append("parent_id", null), "work");
	}

	@Override
	public List<Work> listChildren(ObjectId parent_id) {
		return createTaskDataSet(new BasicDBObject("parent_id", parent_id));
	}

	@Override
	public long countChildren(ObjectId parent_id) {
		return count(new BasicDBObject("parent_id", parent_id), "work");
	}

	@Override
	public Work insertWork(Work work) {
		return insert(work, Work.class);
	}

	@Override
	public WorkLink insertLink(WorkLink link) {
		return insert(link, WorkLink.class);
	}

	@Override
	public long updateWork(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, Work.class);
	}

	@Override
	public long updateLink(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkLink.class);
	}

	@Override
	public long deleteWork(ObjectId _id) {
		return delete(_id, Work.class);
	}

	@Override
	public long deleteLink(ObjectId _id) {
		return delete(_id, WorkLink.class);
	}

	@Override
	public Work getWork(ObjectId _id) {
		return queryWork(null, null, new BasicDBObject("_id", _id), null, null).first();
	}

	@Override
	public WorkLink getLink(ObjectId _id) {
		return get(_id, WorkLink.class);
	}

	@Override
	public List<Result> startStage(Command com) {
		List<Result> result = startStageCheck(com._id, com.userId);
		if (!result.isEmpty()) {
			return result;
		}

		// 修改状态
		UpdateResult ur = c(Work.class).updateOne(new BasicDBObject("_id", com._id),
				new BasicDBObject("$set", new BasicDBObject("status", ProjectStatus.Processing)
						.append("startOn", com.date).append("startBy", com.userId)));

		// 根据ur构造下面的结果
		if (ur.getModifiedCount() == 0) {
			throw new ServiceException("没有满足启动条件的工作。");
		}

		ObjectId project_id = c("work").distinct("project_id", new BasicDBObject("_id", com._id), ObjectId.class)
				.first();
		ur = c(Project.class).updateOne(new BasicDBObject("_id", project_id),
				new BasicDBObject("$set", new BasicDBObject("stage_id", com._id)));
		// 根据ur构造下面的结果
		if (ur.getModifiedCount() == 0) {
			throw new ServiceException("无法更新项目当前状态。");
		}

		// 通知团队成员，工作已经启动
		List<String> memberIds = getStageMembers(com._id);
		String name = getName("work", com._id);
		String projectName = getName("project", project_id);
		sendMessage("阶段启动通知",
				"您参与的项目" + projectName + " 阶段" + name + "已于"
						+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(com.date) + "启动。",
				com.userId, memberIds, null);
		return result;
	}

	private List<Result> startStageCheck(ObjectId _id, String executeBy) {
		//////////////////////////////////////////////////////////////////////
		// 须检查的信息
		// 1. 检查是否创建了第一层的WBS，并有计划，如果没有，提示警告
		// 2. 检查组织结构是否完成，如果只有根，警告
		// 3. 检查第一层的WBS是否指定了必要的角色，如果没有负责人角色，提示警告。
		// 4. 缺少干系人，警告
		// 5. 没有做预算，警告
		// 6. 预算没做完，警告
		// 7. 预算没有分配，警告

		return new ArrayList<Result>();
	}

	@Override
	public Workspace getWorkspace(ObjectId _id) {
		BasicDBObject dbo = c("work").find(new BasicDBObject("_id", _id), BasicDBObject.class)
				.projection(new BasicDBObject("space_id", Boolean.TRUE).append("checkoutBy", Boolean.TRUE)
						.append("project_id", Boolean.TRUE))
				.first();
		return Workspace.newInstance(dbo.getObjectId("project_id"), _id, dbo.getObjectId("space_id"),
				dbo.getString("checkoutBy"));
	}

	@Override
	public List<WorkLink> createWorkLinkDataSet(ObjectId parent_id) {
		List<ObjectId> inputIds = new ArrayList<ObjectId>();
		inputIds.add(parent_id);
		inputIds = getDesentItems(inputIds, "work", "parent_id");
		return c(WorkLink.class).find(new BasicDBObject("source", new BasicDBObject("$in", inputIds)).append("target",
				new BasicDBObject("$in", inputIds))).into(new ArrayList<WorkLink>());
	}

	@Override
	public List<Work> createWorkTaskDataSet(ObjectId parent_id) {
		List<Work> result = new ArrayList<Work>();
		queryWork(null, null, new BasicDBObject("parent_id", parent_id), null, new BasicDBObject("index", 1))
				.forEach(new Block<Work>() {
					@Override
					public void apply(final Work work) {
						result.add(work);
						result.addAll(createWorkTaskDataSet(work.get_id()));
					}
				});
		return result;
	}

	@Override
	public List<WorkLink> createProjectLinkDataSet(ObjectId project_id) {
		return c(WorkLink.class).find(new BasicDBObject("project_id", project_id)).sort(new BasicDBObject("index", 1))
				.into(new ArrayList<WorkLink>());
	}

	@Override
	public List<Work> createProjectTaskDataSet(ObjectId project_id) {
		return queryWork(null, null, new BasicDBObject("project_id", project_id), null, new BasicDBObject("index", 1))
				.into(new ArrayList<Work>());
	}

	@Override
	public List<Work> createProcessingWorkDataSet(BasicDBObject condition, String userid) {
		Integer skip = Optional.ofNullable(condition).map(c -> (Integer) c.get("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> (Integer) c.get("limit")).orElse(null);
		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("filter")).orElse(null);
		BasicDBObject sort = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("sort")).orElse(null);

		int warningDay = (int) getSystemSetting(WARNING_DAY);

		List<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new BasicDBObject("$or",
				new BasicDBObject[] { new BasicDBObject("chargerId", userid), new BasicDBObject("assignerId", userid) })
						.append("summary", false).append("actualFinish", null).append("distributed", true)
						.append("stage", new BasicDBObject("$ne", true))));

		appendProject(pipeline);

		appendUserInfo(pipeline, "chargerId", "chargerInfo");

		appendUserInfo(pipeline, "assignerId", "assignerInfo");

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));
		else
			pipeline.add(Aggregates.sort(new BasicDBObject("planFinish", 1)));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		pipeline.add(Aggregates.addFields(new Field<Integer>("warningDay", warningDay)));

		return c(Work.class).aggregate(pipeline).into(new ArrayList<Work>());
	}

	@Override
	public long countProcessingWorkDataSet(BasicDBObject filter, String userid) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("summary", false);
		filter.put("actualFinish", null);
		filter.put("distributed", true);
		filter.put("$or", new BasicDBObject[] { new BasicDBObject("chargerId", userid),
				new BasicDBObject("assignerId", userid) });
		filter.put("stage", new BasicDBObject("$ne", true));
		return count(filter, Work.class);
	}

	@Override
	public List<Work> createFinishedWorkDataSet(BasicDBObject condition, String userid) {
		Integer skip = Optional.ofNullable(condition).map(c -> (Integer) c.get("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> (Integer) c.get("limit")).orElse(null);
		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("filter")).orElse(null);

		return queryWork(skip, limit, new BasicDBObject("$or",
				new BasicDBObject[] { new BasicDBObject("chargerId", userid), new BasicDBObject("assignerId", userid) })
						.append("summary", false).append("actualFinish", new BasicDBObject("$ne", null))
						.append("stage", new BasicDBObject("$ne", true)),
				filter, new BasicDBObject("actualFinish", -1)).into(new ArrayList<Work>());
	}

	@Override
	public long countFinishedWorkDataSet(BasicDBObject filter, String userid) {
		if (filter == null)
			filter = new BasicDBObject();

		if (filter.get("actualFinish") == null)
			filter.put("actualFinish", new BasicDBObject("$ne", null));

		filter.put("summary", false);
		filter.put("distributed", true);
		filter.put("$or", new BasicDBObject[] { new BasicDBObject("chargerId", userid),
				new BasicDBObject("assignerId", userid) });
		filter.put("stage", new BasicDBObject("$ne", true));

		return count(filter, Work.class);
	}

	@Override
	public List<Work> createDeptUserWorkDataSet(String userid) {
		ObjectId organization_id = c("user").distinct("org_id", new BasicDBObject("userId", userid), ObjectId.class)
				.first();
		if (organization_id != null) {
			List<String> users = c("user")
					.distinct("userId", new BasicDBObject().append("org_id", organization_id), String.class)
					.into(new ArrayList<String>());

			return queryWork(null, null,
					new BasicDBObject("$or",
							new BasicDBObject[] { new BasicDBObject("chargerId", new BasicDBObject("$in", users)),
									new BasicDBObject("assignerId", new BasicDBObject("$in", users)) })
											.append("summary", false).append("actualFinish", null)
											.append("distributed", true),
					null, new BasicDBObject("chargerId", 1).append("assignerId", 1).append("planFinish", 1))
							.into(new ArrayList<Work>());
		} else
			return new ArrayList<Work>();
	}

	@Override
	public List<WorkPackage> listWorkPackage(BasicDBObject condition) {
		return listWorkPackage(condition, "work");
	}

	public WorkPackage getWorkPackage(ObjectId _id) {
		return listWorkPackage(new Query().filter(new BasicDBObject("_id", _id)).bson(), "work").get(0);
	}

	@Override
	public List<WorkPackage> listWorkInTemplatePackage(BasicDBObject condition) {
		return listWorkPackage(condition, "workInTemplate");
	}

	private List<WorkPackage> listWorkPackage(BasicDBObject condition, String master) {
		List<Bson> pipeline = new ArrayList<Bson>();

		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		pipeline.add(Aggregates.lookup(master, "work_id", "_id", "work"));
		pipeline.add(Aggregates.addFields(
				new Field<BasicDBObject>("deadline",
						new BasicDBObject("$arrayElemAt", new Object[] { "$work.planFinish", 0 })),
				new Field<BasicDBObject>("actualFinish",
						new BasicDBObject("$arrayElemAt", new Object[] { "$work.actualFinish", 0 }))));
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

		appendUserInfo(pipeline, "chargerId", "chargerInfo");

		appendProgress(pipeline);

		ArrayList<WorkPackage> result = c(WorkPackage.class).aggregate(pipeline).into(new ArrayList<WorkPackage>());
		return result;
	}

	private void appendProgress(List<Bson> pipeline) {
		pipeline.add(Aggregates.lookup("workPackageProgress", "_id", "package_id", "progress"));
		pipeline.add(Aggregates.addFields(
				Arrays.asList(new Field<Bson>("updateTime", new BasicDBObject("$max", "$progress.updateTime")),
						new Field<Bson>("completeQty", new BasicDBObject("$sum", "$progress.completeQty")),
						new Field<Bson>("qualifiedQty", new BasicDBObject("$sum", "$progress.qualifiedQty")))));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));
		pipeline.add(
				new BasicDBObject("$lookup",
						new BasicDBObject("from", "workPackageProgress")
								.append("let",
										new BasicDBObject("pid", "$_id").append("pupdateTime", "$updateTime"))
								.append("pipeline",
										Arrays.asList(
												new BasicDBObject(
														"$match",
														new BasicDBObject("$expr",
																new BasicDBObject(
																		"$and",
																		Arrays.asList(
																				new BasicDBObject("$eq", Arrays.asList(
																						"$package_id", "$$pid")),
																				new BasicDBObject("$eq",
																						Arrays.asList("$updateTime",
																								"$$pupdateTime")))))),
												new BasicDBObject("$project",
														new BasicDBObject("completeStatus", true))))
								.append("as", "progress1")));
		pipeline.add(Aggregates.unwind("$progress1", new UnwindOptions().preserveNullAndEmptyArrays(true)));
		pipeline.add(Aggregates.addFields(new Field<String>("completeStatus", "$progress1.completeStatus")));
		pipeline.add(Aggregates.project(new BasicDBObject("progress1", false)));

	}

	@Override
	public long countWorkPackage(BasicDBObject filter) {
		return count(filter, WorkPackage.class);
	}

	@Override
	public WorkPackage insertWorkPackage(WorkPackage wp) {
		return insert(wp, WorkPackage.class);
	}

	@Override
	public long deleteWorkPackage(ObjectId _id) {
		return delete(_id, WorkPackage.class);
	}

	@Override
	public long updateWorkPackage(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkPackage.class);
	}

	public List<Result> distributeWorkPlan(Command com) {
		List<Result> result = distributeWorkPlanCheck(com._id, com.userId);
		if (!result.isEmpty()) {
			return result;
		}

		List<ObjectId> inputIds = getDesentItems(Arrays.asList(com._id), "work", "parent_id");

		BasicDBObject query = new BasicDBObject("_id", new BasicDBObject("$in", inputIds))
				.append("$or",
						new BasicDBObject[] { new BasicDBObject("chargerId", new Document("$ne", null)),
								new BasicDBObject("assignerId", new Document("$ne", null)) })
				.append("distributed", new BasicDBObject("$ne", true)).append("actualFinish", null);

		final List<ObjectId> ids = new ArrayList<>();
		final List<Message> messages = new ArrayList<>();

		Work work = get(com._id, Work.class);
		String pjName = getName("project", work.getProject_id());
		c("work").find(query).forEach((Document w) -> {
			ids.add(w.getObjectId("_id"));
			String chargerId = w.getString("chargerId");
			messages.add(Message.newInstance("工作计划下达通知",
					"您负责的项目 " + pjName + "，工作 " + w.getString("fullName") + "，预计从"
							+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(w.getDate("planStart")) + "开始到"
							+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(w.getDate("planFinish")) + "结束",
					com.userId, chargerId, null));
			String assignerId = w.getString("assignerId");
			messages.add(Message.newInstance("工作计划下达通知",
					"您指派的项目 " + pjName + "，工作 " + w.getString("fullName") + "，预计从"
							+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(w.getDate("planStart")) + "开始到"
							+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(w.getDate("planFinish")) + "结束",
					com.userId, assignerId, null));
		});

		if (ids.isEmpty()) {
			result.add(Result.updateFailure("没有需要下达的计划。"));
			return result;
		}

		c("work").updateMany(new BasicDBObject("_id", new BasicDBObject("$in", ids)),
				new BasicDBObject("$set", new BasicDBObject("distributed", true).append("distributeBy", com.userId)
						.append("distributeOn", com.date)));

		sendMessages(messages);

		return new ArrayList<Result>();
	}

	private List<Result> distributeWorkPlanCheck(ObjectId _id, String distributeBy) {
		// TODO 检查是否可以下达
		return new ArrayList<Result>();
	}

	@Override
	public List<Result> startWork(Command com) {
		List<Result> result = startWorkCheck(com._id);

		// 工作没有开始时间或开始时间大于当前时间时，更新当前工作的开始时间为当前时间并获取更新的工作
		Document doc = c("work").findOneAndUpdate(
				new BasicDBObject("_id", com._id).append("$or",
						new BasicDBObject[] { new BasicDBObject("actualStart", null),
								new BasicDBObject("actualStart", new BasicDBObject("$gt", com.date)) }),
				new BasicDBObject("$set", new BasicDBObject("actualStart", com.date)));

		// 如果没有获得工作则该工作已经启动
		if (doc == null) {
			result.add(Result.updateFailure("工作已经启动。"));
			return result;
		}

		// 更新上级工作的启动时间
		ObjectId parent_id = doc.getObjectId("parent_id");
		if (parent_id != null)
			startParentWork(parent_id, com.date);

		return new ArrayList<Result>();
	}

	private void startParentWork(ObjectId _id, Date startDate) {
		// 工作没有开始时间或开始时间大于启动时间时，更新工作的开始时间为当前时间，并获取更新的工作
		Document doc = c("work").findOneAndUpdate(
				new BasicDBObject("_id", _id).append("$or",
						new BasicDBObject[] { new BasicDBObject("actualStart", null),
								new BasicDBObject("actualStart", new BasicDBObject("$gt", startDate)) }),
				new BasicDBObject("$set", new BasicDBObject("actualStart", startDate)));
		// 如果没有获得工作则该工作已经启动，并不需要继续循环启动上级工作
		if (doc == null)
			return;
		// 更新上级工作的启动时间，如果没有上级工作，则更新项目启动时间
		ObjectId parent_id = doc.getObjectId("parent_id");
		if (parent_id != null)
			startParentWork(parent_id, startDate);
		else
			startProject(doc.getObjectId("project_id"), startDate);

	}

	private void startProject(ObjectId _id, Date startDate) {
		c("project").updateOne(
				new BasicDBObject("_id", _id).append("$or",
						new BasicDBObject[] { new BasicDBObject("actualStart", null),
								new BasicDBObject("actualStart", new BasicDBObject("$gt", startDate)) }),
				new BasicDBObject("$set", new BasicDBObject("actualStart", startDate)));
	}

	private List<Result> startWorkCheck(ObjectId _id) {
		// TODO 检查是否可以启动工作
		return new ArrayList<Result>();
	}

	@Override
	public List<Result> finishWork(Command com) {
		List<Result> result = finishWorkCheck(com._id);
		// 工作没有完成时间或完成时间小于当前时间时，更新工作的完成时间为当前时间，并获取更新的工作
		Document doc = c("work").findOneAndUpdate(new BasicDBObject("_id", com._id),
				new BasicDBObject("$set", new BasicDBObject("actualFinish", com.date).append("progress", 1d)));
		// 如果没有获得工作则该工作已经完工，并不需要继续循环完工上级工作
		if (doc == null) {
			result.add(Result.updateFailure("工作已经完成。"));
			return result;
		}

		// 更新上级工作的完工时间
		ObjectId parent_id = doc.getObjectId("parent_id");
		if (parent_id != null)
			finishParentWork(parent_id, com.date);

		ObjectId stage_id = getStageId(com._id);
		List<String> memberIds = new ArrayList<String>();
		String workName = getName("work", com._id);
		Work stage = c(Work.class).find(new Document("_id", stage_id)).first();
		Project project = c(Project.class).find(new Document("_id", doc.getObjectId("project_id"))).first();
		memberIds.add(stage.getChargerId());
		memberIds.add(project.getPmId());
		c("obs").distinct("managerId", new Document("scope_id", project.get_id()).append("project", "PPM"),
				String.class).forEach((String userId) -> {
					memberIds.add(userId);
				});
		if (doc.getBoolean("stage", false))
			sendMessage("工作完成通知",
					"您负责的项目" + project.getName() + " 阶段" + stage.getText() + " 工作" + workName + "已于"
							+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(com.date) + "完成。",
					com.userId, memberIds, null);
		else
			sendMessage("里程碑完成通知",
					"您负责的项目" + project.getName() + " 阶段" + stage.getText() + " 工作" + workName + "已于"
							+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(com.date) + "完成。",
					com.userId, memberIds, null);

		List<ObjectId> milestoneWorkId = new ArrayList<ObjectId>();

		c(WorkLink.class).find(new Document("source", com._id)).forEach((WorkLink workLink) -> {
			ObjectId targetId = workLink.getTargetId();
			Work work = getWork(targetId);
			String chargerId = work.getChargerId();
			if (chargerId != null)
				sendMessage("前序工作完成通知",
						"您负责的项目" + project.getName() + " 阶段" + stage.getText() + " 工作" + work.getText() + "的前序工作已于"
								+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(com.date) + "完成。",
						com.userId, chargerId, null);

			String assignerId = work.getAssignerId();
			if (assignerId != null)
				sendMessage("前序工作完成通知",
						"您指派的项目" + project.getName() + " 阶段" + stage.getText() + " 工作" + work.getText() + "的前序工作已于"
								+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(com.date) + "完成。",
						com.userId, assignerId, null);
			if (work.isMilestone()) {
				milestoneWorkId.add(work.get_id());
			}
		});

		milestoneWorkId.forEach(work_id -> {
			Command newComm = Command.newInstance(null, com.userId, com.date, work_id);
			List<Result> startWork = startWork(newComm);
			if (startWork.isEmpty()) {
				List<Result> finishWork = finishWork(newComm);
				result.addAll(finishWork);
			}
			result.addAll(startWork);
		});
		return result;
	}

	private ObjectId getStageId(ObjectId work_id) {
		ObjectId parent_id = c("work").distinct("parent_id", new Document("_id", work_id), ObjectId.class).first();
		if (parent_id != null) {
			return getStageId(parent_id);
		} else {
			return work_id;
		}
	}

	private void finishParentWork(ObjectId _id, Date finishDate) {
		// 判断该工作是否存在未完成的子工作，存在则不更新该工作实际完成时间
		long count = c("work").countDocuments(new BasicDBObject("parent_id", _id).append("actualFinish", null));
		if (count == 0) {
			// 工作没有完成时间或完成时间小于当前时间段且不为阶段时，更新工作的完成时间为当前时间，并获取更新的工作
			Document doc = c("work")
					.findOneAndUpdate(
							new BasicDBObject("_id", _id)
									.append("$or",
											new BasicDBObject[] { new BasicDBObject("actualFinish", null),
													new BasicDBObject("actualFinish",
															new BasicDBObject("$lt", finishDate)) })
									.append("stage", false),
							new BasicDBObject("$set",
									new BasicDBObject("actualFinish", finishDate).append("progress", 1d)));
			// 如果没有获得工作则该工作已经完工，并不需要继续循环完工上级工作
			if (doc == null)
				return;

			// TODO 处理摘要工作实际工期

			// 更新上级工作的完工时间
			ObjectId parent_id = doc.getObjectId("parent_id");
			if (parent_id != null)
				finishParentWork(parent_id, finishDate);
		}
	}

	private List<Result> finishWorkCheck(ObjectId _id) {
		// TODO 检查是否可以完成工作
		return new ArrayList<Result>();
	}

	@Override
	public List<Result> finishStage(Command com) {
		List<Result> result = finishStageCheck(com._id, com.userId);
		if (!result.isEmpty()) {
			return result;
		}

		Document doc = c("work").find(new BasicDBObject("parent_id", com._id))
				.projection(new BasicDBObject("actualFinish", true)).sort(new BasicDBObject("actualFinish", -1))
				.first();

		// 修改项目状态
		UpdateResult ur = c("work").updateOne(new BasicDBObject("_id", com._id),
				new BasicDBObject("$set",
						new BasicDBObject("status", ProjectStatus.Closing).append("finishOn", com.date)
								.append("finishBy", com.userId).append("actualFinish", doc.get("actualFinish"))
								.append("progress", 1d)));

		// 根据ur构造下面的结果
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足完工条件的阶段。"));
			return result;
		}

		// TODO 处理阶段实际工期

		// 阶段收尾通知
		ObjectId project_id = c("work").distinct("project_id", new BasicDBObject("_id", com._id), ObjectId.class)
				.first();
		List<String> memberIds = getStageMembers(com._id);
		String name = getName("work", com._id);
		String projectName = getName("project", project_id);
		sendMessage("阶段收尾通知",
				"您参与的项目" + projectName + " 阶段" + name + "已于"
						+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(com.date) + "进入收尾。",
				com.userId, memberIds, null);
		return result;
	}

	private List<Result> finishStageCheck(ObjectId _id, String executeBy) {
		//////////////////////////////////////////////////////////////////////
		// 须检查的信息
		// 1. 检查所属该阶段的工作是否全部完工，若没有，错误。根据工作完工后，自动向上级汇总实际完成的规则，只需要判断阶段下一级工作是否全部完工。
		ArrayList<Result> result = new ArrayList<Result>();
		long count = c("work").countDocuments(new BasicDBObject("parent_id", _id).append("actualFinish", null));
		if (count > 0) {
			result.add(Result.finishError("阶段存在没有完工的工作。"));
		}
		return result;
	}

	@Override
	public List<Result> closeStage(Command com) {
		List<Result> result = closeStageCheck(com._id, com.userId);
		if (!result.isEmpty()) {
			return result;
		}

		// 修改状态
		UpdateResult ur = c(Work.class).updateOne(new BasicDBObject("_id", com._id),
				new BasicDBObject("$set", new BasicDBObject("status", ProjectStatus.Closed).append("closeOn", com.date)
						.append("closeBy", com.userId)));

		// 根据ur构造下面的结果
		if (ur.getModifiedCount() == 0) {
			throw new ServiceException("没有满足关闭条件的工作。");
		}

		// 通知团队成员，工作已经关闭
		ObjectId project_id = c("work").distinct("project_id", new BasicDBObject("_id", com._id), ObjectId.class)
				.first();
		List<String> memberIds = getStageMembers(com._id);
		String name = getName("work", com._id);
		String projectName = getName("project", project_id);
		sendMessage("阶段关闭通知",
				"您参与的项目" + projectName + " 阶段" + name + "已于"
						+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(com.date) + "关闭。",
				com.userId, memberIds, null);

		return result;
	}

	private List<String> getStageMembers(ObjectId _id) {
		List<ObjectId> parentIds = c("obs").distinct("_id", new BasicDBObject("scope_id", _id), ObjectId.class)
				.into(new ArrayList<>());
		List<ObjectId> ids = getDesentItems(parentIds, "obs", "parent_id");
		ArrayList<String> memberIds = c("obs")
				.distinct("managerId", new BasicDBObject("_id", new BasicDBObject("$in", ids)).append("managerId",
						new BasicDBObject("$ne", null)), String.class)
				.into(new ArrayList<>());
		return memberIds;
	}

	private List<Result> closeStageCheck(ObjectId _id, String executeBy) {
		//////////////////////////////////////////////////////////////////////
		// 须检查的信息
		return new ArrayList<Result>();
	}

	@Override
	public long updateResourcePlan(BasicDBObject filterAndUpdate) {
		Bson bson = (Bson) filterAndUpdate.get("filter");
		ObjectId work_id = c("resourcePlan").distinct("work_id", bson, ObjectId.class).first();
		long update = update(filterAndUpdate, ResourcePlan.class);
		updateWorkPlanWorks(work_id);
		return update;
	}

	@Override
	public long deleteHumanResourcePlan(ObjectId work_id, String resId) {
		DeleteResult dr = c("resourcePlan")
				.deleteMany(new Document("work_id", work_id).append("usedHumanResId", resId));
		updateWorkPlanWorks(work_id);
		return dr.getDeletedCount();
	}

	@Override
	public long deleteEquipmentResourcePlan(ObjectId work_id, String resId) {
		DeleteResult dr = c("resourcePlan")
				.deleteMany(new Document("work_id", work_id).append("usedEquipResId", resId));
		updateWorkPlanWorks(work_id);
		return dr.getDeletedCount();
	}

	@Override
	public long deleteTypedResourcePlan(ObjectId work_id, String resId) {
		DeleteResult dr = c("resourcePlan")
				.deleteMany(new Document("work_id", work_id).append("usedTypedResId", resId));
		updateWorkPlanWorks(work_id);
		return dr.getDeletedCount();
	}

	@Override
	public long deleteHumanResourceActual(ObjectId work_id, String resId) {
		DeleteResult dr = c("resourceActual")
				.deleteMany(new Document("work_id", work_id).append("usedHumanResId", resId));
		updateWorkActualWorks(work_id);
		return dr.getDeletedCount();
	}

	@Override
	public long deleteEquipmentResourceActual(ObjectId work_id, String resId) {
		DeleteResult dr = c("resourceActual")
				.deleteMany(new Document("work_id", work_id).append("usedEquipResId", resId));
		updateWorkActualWorks(work_id);
		return dr.getDeletedCount();
	}

	@Override
	public long deleteTypedResourceActual(ObjectId work_id, String resId) {
		DeleteResult dr = c("resourceActual")
				.deleteMany(new Document("work_id", work_id).append("usedTypedResId", resId));
		updateWorkActualWorks(work_id);
		return dr.getDeletedCount();
	}

	@Override
	public List<ResourcePlan> addResourcePlan(List<ResourceAssignment> resas) {
		Set<ObjectId> workIds = new HashSet<ObjectId>();
		List<ResourcePlan> documents = new ArrayList<ResourcePlan>();
		resas.forEach(resa -> {
			double works = getWorkingHoursPerDay(resa.resTypeId);
			Document doc = c("work").find(new Document("_id", resa.work_id))
					.projection(new Document("planStart", 1).append("planFinish", 1)).first();
			Date planStart = doc.getDate("planStart");
			Date planFinish = doc.getDate("planFinish");
			Calendar planStartCal = Calendar.getInstance();
			planStartCal.setTime(planStart);

			Calendar planFinishCal = Calendar.getInstance();
			planFinishCal.setTime(planFinish);
			// planFinishCal.add(Calendar.DAY_OF_MONTH, 1);//与甘特图保持一致，不计算尾部日期

			while (planStartCal.getTime().before(planFinishCal.getTime())) {
				if (checkDayIsWorkingDay(planStartCal, resa.resTypeId)) {

					ResourcePlan res = resa.getResourcePlan();
					res.setId(planStartCal.getTime());
					res.setPlanBasicQty(works * resa.qty);
					res.setQty(resa.qty);

					documents.add(res);
				}
				planStartCal.add(Calendar.DAY_OF_MONTH, 1);
			}
			workIds.add(resa.work_id);
		});
		c(ResourcePlan.class).insertMany(documents);
		workIds.forEach(work_id -> {
			updateWorkPlanWorks(work_id);
		});

		// ResourcePlan r = insert(res, ResourcePlan.class);
		// queryResourceUsage(new Document("_id", r.get_id())).get(0);

		return documents;
	}

	@Override
	public ResourcePlan insertResourcePlan(ResourcePlan rp) {
		return insert(rp, ResourcePlan.class);
	}

	private void updateWorkPlanWorks(ObjectId work_id) {
		if (work_id != null) {
			// TODO 修改计算方式
			List<? extends Bson> pipeline = Arrays.asList(new Document("$match", new Document("work_id", work_id)),
					new Document("$addFields",
							new Document("planQty",
									new Document("$sum", Arrays.asList("$planBasicQty", "$planOverTimeQty")))),
					new Document("$group",
							new Document("_id", "$work_id").append("planWorks", new Document("$sum", "$planQty"))));

			double works = Optional.ofNullable(c("resourcePlan").aggregate(pipeline).first())
					.map(d -> d.getDouble("planWorks")).map(p -> p.doubleValue()).orElse(0d);

			c(Work.class).updateOne(new Document("_id", work_id),
					new Document("$set", new Document("planWorks", works)));
		}
	}

	@Override
	public List<ResourcePlan> listResourcePlan(ObjectId _id) {
		return c(ResourcePlan.class).aggregate(new JQ("查询资源计划用量").set("match", new Document("work_id", _id)).array())
				.into(new ArrayList<ResourcePlan>());
	}

	@Override
	public WorkPackageProgress insertWorkPackageProgress(WorkPackageProgress wpp) {
		return insert(wpp.setUpdateTime(new Date()), WorkPackageProgress.class);
	}

	@Override
	public long deleteWorkPackageProgress(ObjectId _id) {
		return delete(_id, WorkPackageProgress.class);
	}

	@Override
	public List<WorkPackageProgress> listWorkPackageProgress(BasicDBObject condition) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(new Document("$lookup", new Document().append("from", "workPackage")
				.append("localField", "package_id").append("foreignField", "_id").append("as", "workpackage")));
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

		return c(WorkPackageProgress.class).aggregate(pipeline).into(new ArrayList<WorkPackageProgress>());
	}

	@Override
	public long countWorkPackageProgress(BasicDBObject filter) {
		return count(filter, WorkPackageProgress.class);
	}

	@Override
	public long updateWorkPackageProgress(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkPackageProgress.class);
	}

	@Override
	public List<Work> listWorkPackageForScheduleInProject(ObjectId project_id, String catagory) {
		List<Bson> pipeline = (List<Bson>) new JQ("查询工作-工作包").set("match", new Document("project_id", project_id))
				.set("catagory", catagory).array();

		appendProject(pipeline);

		appendOverdue(pipeline);

		appendWorkTime(pipeline);

		appendUserInfo(pipeline, "chargerId", "chargerInfo");

		appendUserInfo(pipeline, "assignerId", "assignerInfo");

		return c(Work.class).aggregate(pipeline).into(new ArrayList<Work>());
	}

	@Override
	public long countWorkPackageForScheduleInProject(ObjectId project_id, String catagory) {
		return c("work").countDocuments(
				new BasicDBObject("workPackageSetting.catagory", catagory).append("project_id", project_id));
	}

	@Override
	public List<Work> listWorkPackageForScheduleInStage(ObjectId stage_id, String catagory) {
		List<ObjectId> items = getDesentItems(Arrays.asList(stage_id), "work", "parent_id");
		List<Bson> pipeline = (List<Bson>) new JQ("查询工作-工作包")
				.set("match", new Document("_id", new Document("$in", items))).set("catagory", catagory).array();

		appendProject(pipeline);

		appendOverdue(pipeline);

		appendWorkTime(pipeline);

		appendUserInfo(pipeline, "chargerId", "chargerInfo");

		appendUserInfo(pipeline, "assignerId", "assignerInfo");

		return c(Work.class).aggregate(pipeline).into(new ArrayList<Work>());
	}

	@Override
	public long countWorkPackageForScheduleInStage(ObjectId stage_id, String catagory) {
		List<ObjectId> items = getDesentItems(Arrays.asList(stage_id), "work", "parent_id");
		return c("work").countDocuments(new BasicDBObject("workPackageSetting.catagory", catagory).append("_id",
				new BasicDBObject("$in", items)));
	}

	@Override
	public List<Work> listWorkPackageForSchedule(BasicDBObject condition, String userid, String catagory) {
		// List<ObjectId> items = getProject_id(userid);

		List<Bson> pipeline = (List<Bson>) new JQ("查询工作-工作包").set("match", new Document("summary", false)
				.append("actualFinish", null).append("stage", new BasicDBObject("$ne", true))).set("catagory", catagory)
				.array();

		appendProject(pipeline);

		appendOverdue(pipeline);

		appendWorkTime(pipeline);

		appendUserInfo(pipeline, "chargerId", "chargerInfo");

		appendUserInfo(pipeline, "assignerId", "assignerInfo");

		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		if (sort != null)
			pipeline.add(Aggregates.sort(sort));
		else
			pipeline.add(Aggregates.sort(new Document("planFinish", 1)));

		Integer skip = (Integer) condition.get("skip");
		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		Integer limit = (Integer) condition.get("limit");
		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		return c(Work.class).aggregate(pipeline).into(new ArrayList<Work>());
	}

	private List<ObjectId> getProject_id(String userid) {
		// TODO 获取具有访问权限的项目，暂时使用的是作为项目经理的项目
		return c("project").distinct("_id", new BasicDBObject("pmId", userid), ObjectId.class)
				.into(new ArrayList<ObjectId>());
	}

	@Override
	public long countWorkPackageForSchedule(BasicDBObject filter, String userid, String catagory) {
		List<ObjectId> items = getProject_id(userid);
		if (filter == null)
			filter = new BasicDBObject();
		return c("work").countDocuments(filter.append("workPackageSetting.catagory", catagory).append("project_id",
				new BasicDBObject("$in", items)));
	}

	@Override
	public List<ResourceActual> addResourceActual(List<ResourceAssignment> resas) {
		List<ResourceActual> documents = new ArrayList<ResourceActual>();
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
				if (checkDayIsWorkingDay(planStartCal, resa.resTypeId)) {
					Date time = planStartCal.getTime();
					ResourceActual res = resa.getResourceActual();
					res.setId(time);
					documents.add(res);

					c(ResourceActual.class).deleteMany(new Document("id", time).append("work_id", res.getWork_id())
							.append("usedHumanResId", res.getUsedHumanResId())
							.append("usedEquipResId", res.getUsedEquipResId())
							.append("usedTypedResId", res.getUsedTypedResId()).append("resTypeId", res.getResTypeId()));
				}
				planStartCal.add(Calendar.DAY_OF_MONTH, 1);
			}

			double actualBasicQty = resa.actualBasicQty / documents.size();

			double actualOverTimeQty = resa.actualOverTimeQty / documents.size();
			for (ResourceActual resourceActual : documents) {
				resourceActual.setActualBasicQty(actualBasicQty);
				resourceActual.setActualOverTimeQty(actualOverTimeQty);
			}
		});

		c(ResourceActual.class).insertMany(documents);

		return documents;
	}

	@Override
	public ResourceActual insertResourceActual(ResourceActual ra) {
		return insert(ra, ResourceActual.class);
	}

	@Override
	public long updateResourceActual(BasicDBObject filterAndUpdate) {
		Bson bson = (Bson) filterAndUpdate.get("filter");
		ObjectId work_id = c("resourceActual").distinct("work_id", bson, ObjectId.class).first();
		long update = update(filterAndUpdate, ResourceActual.class);
		updateWorkActualWorks(work_id);
		return update;
	}

	private void updateWorkActualWorks(ObjectId work_id) {
		if (work_id != null) {
			// TODO 修改计算方式
			List<? extends Bson> pipeline = Arrays.asList(new Document("$match", new Document("work_id", work_id)),
					new Document("$addFields",
							new Document("actualQty",
									new Document("$sum", Arrays.asList("$actualBasicQty", "$actualOverTimeQty")))),
					new Document("$group",
							new Document("_id", "$work_id").append("actualWorks", new Document("$sum", "$actualQty"))));

			double works = Optional.ofNullable(c("resourceActual").aggregate(pipeline).first())
					.map(d -> d.getDouble("actualWorks")).map(p -> p.doubleValue()).orElse(0d);

			c(Work.class).updateOne(new Document("_id", work_id),
					new Document("$set", new Document("actualWorks", works)));
		}
	}

	@Override
	public List<ResourceActual> listResourceActual(ObjectId _id) {
		return c(ResourceActual.class).aggregate(new JQ("查询资源实际用量").set("match", new Document("work_id", _id)).array())
				.into(new ArrayList<>());
	}

	public List<WorkResourcePlanDetail> listConflictWorks(ResourcePlan resp) {
		Document eq = new Document();
		if (resp.getUsedHumanResId() != null)
			eq.put("$eq", Arrays.asList("$usedHumanResId", resp.getUsedHumanResId()));
		else if (resp.getUsedEquipResId() != null)
			eq.put("$eq", Arrays.asList("$usedEquipResId", resp.getUsedEquipResId()));

		List<? extends Bson> pipeline = Arrays.asList(new Document("$match", new Document("_id", resp.getWork_id())),
				new Document("$lookup", new Document("from", "resourcePlan")
						.append("let", new Document("planStart", "$planStart").append("planFinish", "$planFinish"))
						.append("pipeline",
								Arrays.asList(new Document("$match", new Document("$expr", new Document("$and",
										Arrays.asList(new Document("$gte", Arrays.asList("$id", "$$planStart")),
												new Document("$lte", Arrays.asList("$id", "$$planFinish")), eq))))))
						.append("as", "resourcePlan")),
				new Document("$unwind", "$resourcePlan"),
				new Document("$replaceRoot", new Document("newRoot", "$resourcePlan")),
				// new Document("$project",
				// new Document("_id", "$resourcePlan._id").append("work_id",
				// "$resourcePlan.work_id")
				// .append("usedHumanResId_id", "$resourcePlan.usedHumanResId_id")
				// .append("resTypeId", "$resourcePlan.resTypeId")
				// .append("planOverTimeQty", "$resourcePlan.planOverTimeQty")
				// .append("id", "$resourcePlan.id").append("planBasicQty",
				// "$resourcePlan.planBasicQty")),
				new Document("$group",
						new Document("_id", "$work_id").append("children", new Document("$push", "$$ROOT"))),
				new Document("$lookup",
						new Document("from", "work")
								.append("localField", "_id").append("foreignField", "_id").append("as", "work")),
				new Document("$unwind", "$work"),
				new Document("$addFields",
						new Document("project_id", "$work.project_id").append("planStart", "$work.planStart")
								.append("planFinish", "$work.planFinish").append("name", "$work.name")
								.append("actualStart", "$work.actualStart").append("actualFinish", "$work.actualFinish")
								.append("id", "$work.code")),
				new Document("$lookup",
						new Document("from", "project").append("localField", "project_id").append("foreignField", "_id")
								.append("as", "project")),
				new Document("$unwind", "$project"),
				new Document("$addFields",
						new Document("projectId", "$project.id").append("projectName", "$project.name")),
				new Document("$project", new Document("project", false).append("work", false)));
		return c("work", WorkResourcePlanDetail.class).aggregate(pipeline)
				.into(new ArrayList<WorkResourcePlanDetail>());
	}

	@Override
	public void assignRoleToProject(ObjectId project_id) {
		List<ObjectId> ids = getScopeOBS(project_id);
		Document condition = new Document("project_id", project_id);
		updateWorkRoleAssignment(c("work"), ids, condition);
		updateWorkRoleAssignment(c("workspace"), ids, condition);
	}

	private void updateWorkRoleAssignment(MongoCollection<Document> c, List<ObjectId> ids, Document condition) {
		c.find(condition.append("$or", Arrays.asList(new Document("chargerRoleId", new Document("$ne", null)),
				new Document("assignerRoleId", new Document("$ne", null))))).forEach((Document d) -> {
					String roleId = d.getString("chargerRoleId");
					if (!Util.isEmptyOrNull(roleId)) {
						String userId = getManagerIdOfRole(ids, roleId);
						if (!Util.isEmptyOrNull(userId)) {
							c.updateOne(new Document("_id", d.get("_id")),
									new Document("$set", new Document("chargerId", userId)));
						}
					}

					roleId = d.getString("assignerRoleId");
					if (!Util.isEmptyOrNull(roleId)) {
						String userId = getManagerIdOfRole(ids, roleId);
						if (!Util.isEmptyOrNull(userId)) {
							c.updateOne(new Document("_id", d.get("_id")),
									new Document("$set", new Document("assignerId", userId)));
						}
					}
				});
	}

	@Override
	public void assignRoleToStage(ObjectId work_id) {

	}

	private String getManagerIdOfRole(List<ObjectId> ids, String roleId) {
		return Optional
				.ofNullable(
						c("obs").find(new Document("_id", new Document("$in", ids)).append("roleId", roleId)).first())
				.map(d -> d.getString("managerId")).orElse(null);
	}

	private List<ObjectId> getScopeOBS(ObjectId scope_id) {
		List<ObjectId> parentIds = c("obs").distinct("_id", new Document("scope_id", scope_id), ObjectId.class)
				.into(new ArrayList<>());
		return getDesentItems(parentIds, "obs", "parent_id");
	}

	@Override
	public ObjectId getProjectId(ObjectId _id) {
		return c("work").distinct("project_id", new Document("_id", _id), ObjectId.class).first();
	}

	@Override
	public List<DateMark> listMyWorksDateMark(String userid) {
		List<? extends Bson> ls = new JQ("日期选择器-用户待办工作的时间标记").set("userId", userid).array();
		return c("work").aggregate(ls, DateMark.class).into(new ArrayList<>());
	}

	@Override
	public List<Document> getResource(ResourceTransfer rt) {
		String resourceCollection;
		if (ResourceTransfer.TYPE_PLAN == rt.getType()) {
			resourceCollection = "resourcePlan";
		} else {
			if (rt.isReport())
				resourceCollection = "workReportResourceActual";
			else
				resourceCollection = "resourceActual";
		}
		Document match;
		if (ResourceTransfer.SHOWTYPE_MULTIWORK_ONERESOURCE == rt.getShowType()) {
			match = new Document("usedEquipResId", rt.getUsedEquipResId())
					.append("usedHumanResId", rt.getUsedHumanResId()).append("usedTypedResId", rt.getUsedTypedResId())
					.append("resTypeId", rt.getResTypeId());
		} else {
			match = new Document("work_id", new Document("$in", rt.getWorkIds()));
		}
		return c(resourceCollection).aggregate(new JQ("编辑资源").set("match", match)
				.set("resourceCollection", resourceCollection).set("from", rt.getFrom()).set("to", rt.getTo()).array())
				.into(new ArrayList<Document>());
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
				return new DecimalFormat("0.0").format(d);
			}
		}
		return "";
	}

	@Override
	public Document getResourcePlanAnalysis(ObjectId project_id, String year) {
		List<Document> series = new ArrayList<Document>();
		Map<String, Double> worksMap = new TreeMap<String, Double>();
		Map<String, Double> amountMap = new TreeMap<String, Double>();
		for (int i = 1; i < 12; i++) {
			String key = year + String.format("%02d", i);
			worksMap.put(key, 0d);
			amountMap.put(key, 0d);
		}

		c("resourcePlan")
				.aggregate(new JQ("查询资源计划分析-Porject")
						.set("match", new Document("year", year).append("project_id", project_id)).array())
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

		option.append("legend",
				new Document("data", Arrays.asList("工时", "金额")).append("y", "bottom").append("x", "center"));
		option.append("grid",
				new Document("left", "2%").append("right", "2%").append("bottom", "3%").append("containLabel", true));

		option.append("yAxis", Arrays.asList(new Document("type", "category").append("data",
				Arrays.asList(" 1月", " 2月", " 3月", " 4月", " 5月", " 6月", " 7月", " 8月", " 9月", "10月", "11月", "12月"))));
		option.append("xAxis",
				Arrays.asList(
						new Document("type", "value").append("name", "工时").append("axisLabel",
								new Document("formatter", "{value} 小时")),
						new Document("type", "value").append("name", "金额").append("axisLabel",
								new Document("formatter", "{value} 万元"))));

		option.append("series", series);
		return option;

	}

	@Override
	public Document getResourceActualAnalysis(ObjectId project_id, String year) {
		List<Document> series = new ArrayList<Document>();
		Map<String, Double> worksMap = new TreeMap<String, Double>();
		Map<String, Double> amountMap = new TreeMap<String, Double>();
		for (int i = 1; i < 12; i++) {
			String key = year + String.format("%02d", i);
			worksMap.put(key, 0d);
			amountMap.put(key, 0d);
		}

		c("resourceActual")
				.aggregate(new JQ("查询资源实际分析-Porject")
						.set("match", new Document("year", year).append("project_id", project_id)).array())
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

		option.append("legend",
				new Document("data", Arrays.asList("工时", "金额")).append("y", "bottom").append("x", "center"));
		option.append("grid",
				new Document("left", "3%").append("right", "10%").append("bottom", "3%").append("containLabel", true));

		option.append("yAxis", Arrays.asList(new Document("type", "category").append("data",
				Arrays.asList(" 1月", " 2月", " 3月", " 4月", " 5月", " 6月", " 7月", " 8月", " 9月", "10月", "11月", "12月"))));
		option.append("xAxis",
				Arrays.asList(
						new Document("type", "value").append("name", "工时").append("axisLabel",
								new Document("formatter", "{value} 小时")),
						new Document("type", "value").append("name", "金额").append("axisLabel",
								new Document("formatter", "{value} 万元"))));

		option.append("series", series);
		return option;

	}

	@Override
	public Document getProjectResourcePlanAndUsageChart(ObjectId project_id) {
		Document project = c("project").find(new Document("_id", project_id)).first();
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
		c("resourcePlan").aggregate(new JQ("查询资源计划分析-Porject").set("match", match).array()).forEach((Document doc) -> {
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

		c("resourceActual").aggregate(new JQ("查询资源实际分析-Porject").set("match", match).array())
				.forEach((Document doc) -> {
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
		return new JQ("项目首页资源计划和用量状况").set("title", "项目资源计划和用量状况（小时）").set("xAxis", xAxisDate)
				.set("planWorks", planWorks).set("actualWorks", actualWorks).doc();
	}

	@Override
	public Document getResourceAllAnalysis(ObjectId project_id) {
		Document first = c("project").find(new Document("_id", project_id)).projection(new Document("actualStart", true)
				.append("actualFinish", true).append("planStart", true).append("planFinish", true).append("name", true))
				.first();
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
		c("resourcePlan").aggregate(new JQ("查询资源计划分析-Porject").set("match", match).array()).forEach((Document doc) -> {
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

		c("resourceActual").aggregate(new JQ("查询资源实际分析-Porject").set("match", match).array())
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

		Document option = createResourceAllAnalysis(start, end, "" + name + " 资源用量综合分析", actualWorksMap,
				actualAmountMap, planWorksMap, planAmountMap, xAxisDate);
		option.append("dataZoom",
				Arrays.asList(new Document("type", "inside").append("xAxisIndex", Arrays.asList(0, 1)),
						new Document("type", "slider").append("xAxisIndex", Arrays.asList(0, 1))));
		return option;

	}

	@Override
	public Document getResourceAllAnalysisByDept(String year, String userid) {
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

		Document match = new Document("year", year);
		List<ObjectId> orgids = c("user").distinct("org_id", new Document(), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		orgids = getDesentItems(orgids, "organization", "parent_id");
		c("resourcePlan")
				.aggregate(
						new JQ("查询资源计划分析-Dept").set("match", match).set("org_ids", new Document("$in", orgids)).array())
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

		c("resourceActual")
				.aggregate(
						new JQ("查询资源实际分析-Dept").set("match", match).set("org_ids", new Document("$in", orgids)).array())
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

		Document option = createResourceAllAnalysis(start, end, year + "年 资源用量综合分析", actualWorksMap, actualAmountMap,
				planWorksMap, planAmountMap, xAxisDate);
		return option;

	}

	private Document createResourceAllAnalysis(Calendar start, Calendar end, Object text,
			Map<String, Double> actualWorksMap, Map<String, Double> actualAmountMap, Map<String, Double> planWorksMap,
			Map<String, Double> planAmountMap, List<String> xAxisDate) {

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

		option.append("legend", new Document("data", Arrays.asList("计划工时", "实际工时", "计划金额", "实际金额")).append("y", "top")
				.append("x", "right"));
		option.append("grid",
				Arrays.asList(
						new Document("left", 10).append("right", 10).append("bottom", "50%").append("containLabel",
								true),
						new Document("left", 10).append("right", 10).append("bottom", 40).append("top", "55%")
								.append("containLabel", true)));

		option.append("xAxis", Arrays.asList(new Document("type", "category").append("data", xAxisDate),
				new Document("type", "category").append("data", xAxisDate).append("gridIndex", 1)));

		option.append("yAxis",
				Arrays.asList(
						new Document("type", "value").append("name", "工时").append("axisLabel",
								new Document("formatter", "{value} 小时")),
						new Document("type", "value").append("name", "金额")
								.append("axisLabel", new Document("formatter", "{value} 万元")).append("gridIndex", 1)));

		option.append("series", series);
		return option;
	}

	@Override
	public List<Document> getProjectResource(ObjectId project_id) {
		return c("work").aggregate(new JQ("查看资源汇总情况").set("match", new Document("project_id", project_id)).array())
				.into(new ArrayList<Document>());
	}

	@Override
	public List<Document> getProjectResourceByDept(String userid, long start, long end) {
		List<ObjectId> orgids = c("user").distinct("org_id", new Document(), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		orgids = getDesentItems(orgids, "organization", "parent_id");

		return c("work").aggregate(
				new JQ("查看资源汇总情况-Dept").set("workMatch", new Document()).set("org_ids", new Document("$in", orgids))
						.set("start", new Date(start)).set("end", new Date(end)).array())
				.into(new ArrayList<Document>());
	}

	@Override
	public List<Document> addWorkReportResourceActual(List<ResourceAssignment> resas, ObjectId workReportItemId) {
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
				if (checkDayIsWorkingDay(planStartCal, resa.resTypeId)) {
					Date time = planStartCal.getTime();
					Document res = resa.getResourceActualDocument();
					res.append("id", time);
					res.append("workReportItemId", workReportItemId);
					documents.add(res);

					c(ResourceActual.class).deleteMany(new Document("id", time).append("work_id", res.get("work_id"))
							.append("usedHumanResId", res.get("usedHumanResId"))
							.append("usedEquipResId", res.get("usedEquipResId"))
							.append("usedTypedResId", res.get("usedTypedResId"))
							.append("resTypeId", res.get("resTypeId")).append("workReportItemId", workReportItemId));
				}
				planStartCal.add(Calendar.DAY_OF_MONTH, 1);
			}

			double actualBasicQty = resa.actualBasicQty / documents.size();

			double actualOverTimeQty = resa.actualOverTimeQty / documents.size();
			for (Document resourceActual : documents) {
				resourceActual.append("actualBasicQty", actualBasicQty);
				resourceActual.append("actualOverTimeQty", actualOverTimeQty);
			}
		});

		c("workReportResourceActual").insertMany(documents);

		return documents;
	}

	@Override
	public Document insertWorkReportResourceActual(ResourceActual ra, ObjectId workReportItemId) {
		Document doc = new Document("id", ra.getId()).append("work_id", ra.getWork_id())
				.append("usedHumanResId", ra.getUsedHumanResId()).append("usedEquipResId", ra.getUsedEquipResId())
				.append("usedTypedResId", ra.getUsedTypedResId()).append("resTypeId", ra.getResTypeId())
				.append("workReportItemId", workReportItemId).append("actualBasicQty", ra.getActualBasicQty())
				.append("actualOverTimeQty", ra.getActualOverTimeQty());

		c("workReportResourceActual").insertOne(doc);
		return doc;
	}

	@Override
	public long updateWorkReportResourceActual(BasicDBObject filterAndUpdate) {
		BasicDBObject filter = (BasicDBObject) filterAndUpdate.get("filter");
		BasicDBObject update = (BasicDBObject) filterAndUpdate.get("update");
		update.remove("_id");
		UpdateOptions option = new UpdateOptions();
		option.upsert(false);
		UpdateResult updateMany = c("workReportResourceActual").updateMany(filter, update, option);
		long cnt = updateMany.getModifiedCount();
		return cnt;
	}

	@Override
	public List<Work> createBaselineTaskDataSet(ObjectId baseline_id) {

		List<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new BasicDBObject("baseline_id", baseline_id)));

		pipeline.add(Aggregates.sort(new BasicDBObject("index", 1)));

		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		pipeline.add(Aggregates.addFields(Arrays.asList(new Field<String>("projectName", "$project.name"),
				new Field<String>("projectNumber", "$project.id"))));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));

		appendUserInfo(pipeline, "chargerId", "chargerInfo");

		appendUserInfo(pipeline, "assignerId", "assignerInfo");

		// appendOverdue(pipeline);
		//
		// appendWorkTime(pipeline);

		ArrayList<Work> into = c("baselineWork", Work.class).aggregate(pipeline).into(new ArrayList<Work>());
		return into;
	}

	@Override
	public List<WorkLink> createBaselineLinkDataSet(ObjectId baseline_id) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("baseline_id", baseline_id)));

		pipeline.add(Aggregates.sort(new BasicDBObject("index", 1)));

		pipeline.add(Aggregates.lookup("baselineWork", "source", "_id", "sourceWork"));
		pipeline.add(Aggregates.unwind("$sourceWork"));

		pipeline.add(Aggregates.lookup("baselineWork", "target", "_id", "targetWork"));
		pipeline.add(Aggregates.unwind("$targetWork"));

		ArrayList<WorkLink> into = c("baselineWorkLinks", WorkLink.class).aggregate(pipeline)
				.into(new ArrayList<WorkLink>());
		return into;
	}

	@Override
	public Document getProjectWorkScoreChart(ObjectId project_id) {
		Document workFilter = new Document("project_id", project_id).append("actualStart", new Document("$ne", null));
		return getWorkScoreChart(workFilter);
	}

	@Override
	public Document getAdministratedProjectWorkScoreChart(String managerId) {
		List<ObjectId> pjIdList = getAdministratedProjects(managerId);
		Document workFilter = new Document("project_id", new Document("$in", pjIdList)).append("actualStart",
				new Document("$ne", null));
		return getWorkScoreChart(workFilter);
	}

	private Document getWorkScoreChart(Document workFilter) {
		List<Document> indicator = new ArrayList<>();
		List<Double> avg = new ArrayList<>();

		c("work").aggregate(new JQ("项目工作如期评分").set("match", new Document("actualStart", new Document("$ne", null)))
				.set("now", new Date()).array()).forEach((Document d) -> {
					indicator.add(new Document("name", d.getString("_id")).append("max", 100));
					avg.add((double) Math.round(1000 * ((Number) d.get("score")).doubleValue()) / 10);
				});

		Double[] value = new Double[avg.size()];

		c("work").aggregate(new JQ("项目工作如期评分").set("match", workFilter).set("now", new Date()).array())
				.forEach((Document d) -> {
					for (int i = 0; i < indicator.size(); i++) {
						if (indicator.get(i).getString("name").equals(d.getString("_id"))) {
							value[i] = (double) Math.round(1000 * ((Number) d.get("score")).doubleValue()) / 10;
							break;
						}
					}
				});

		return new JQ("项目首页图表工作如期评分").set("indicator", indicator).set("avg", avg).set("value", Arrays.asList(value))
				.doc();
	}

	@Override
	public Work getOpenStage(ObjectId _id, String userId) {
		Work work = getWork(_id);
		if (userId.equals(work.getChargerId())) {
			return work;
		}
		ObjectId project_id = work.getProject_id();
		long l = c(OBSItem.class)
				.countDocuments(new Document("scope_id", new Document("$in", Arrays.asList(_id, project_id)))
						.append("managerId", userId)
						.append("roleId", new Document("$in", Arrays.asList("PPM", "PFM"))));
		if (l > 0) {
			return work;
		}
		return null;
	}

	@Override
	public List<Work> createChargerProcessingWorkDataSet(BasicDBObject condition, String userid) {

		Integer skip = Optional.ofNullable(condition).map(c -> (Integer) c.get("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> (Integer) c.get("limit")).orElse(null);
		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("filter")).orElse(null);
		BasicDBObject sort = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("sort")).orElse(null);

		int warningDay = (int) getSystemSetting(WARNING_DAY);

		List<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates
				.match(new BasicDBObject("chargerId", userid).append("summary", false).append("actualFinish", null)
						.append("distributed", true).append("stage", new BasicDBObject("$ne", true))));

		appendProject(pipeline);

		appendUserInfo(pipeline, "chargerId", "chargerInfo");

		appendUserInfo(pipeline, "assignerId", "assignerInfo");

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));
		else
			pipeline.add(Aggregates.sort(new BasicDBObject("planFinish", 1)));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		pipeline.add(Aggregates.addFields(new Field<Integer>("warningDay", warningDay)));

		return c(Work.class).aggregate(pipeline).into(new ArrayList<Work>());
	}

	@Override
	public long countChargerProcessingWorkDataSet(BasicDBObject filter, String userid) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("summary", false);
		filter.put("actualFinish", null);
		filter.put("distributed", true);
		filter.put("chargerId", userid);
		filter.put("stage", new BasicDBObject("$ne", true));
		return count(filter, Work.class);
	}

	@Override
	public List<Work> createAssignerProcessingWorkDataSet(BasicDBObject condition, String userid) {

		Integer skip = Optional.ofNullable(condition).map(c -> (Integer) c.get("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> (Integer) c.get("limit")).orElse(null);
		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("filter")).orElse(null);
		BasicDBObject sort = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("sort")).orElse(null);

		int warningDay = (int) getSystemSetting(WARNING_DAY);

		List<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates
				.match(new BasicDBObject("assignerId", userid).append("summary", false).append("actualFinish", null)
						.append("distributed", true).append("stage", new BasicDBObject("$ne", true))));

		appendProject(pipeline);

		appendUserInfo(pipeline, "chargerId", "chargerInfo");

		appendUserInfo(pipeline, "assignerId", "assignerInfo");

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));
		else
			pipeline.add(Aggregates.sort(new BasicDBObject("planFinish", 1)));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		pipeline.add(Aggregates.addFields(new Field<Integer>("warningDay", warningDay)));

		return c(Work.class).aggregate(pipeline).into(new ArrayList<Work>());
	}

	@Override
	public long countAssignerProcessingWorkDataSet(BasicDBObject filter, String userid) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("summary", false);
		filter.put("actualFinish", null);
		filter.put("distributed", true);
		filter.put("assignerId", userid);
		filter.put("stage", new BasicDBObject("$ne", true));
		return count(filter, Work.class);
	}

}
