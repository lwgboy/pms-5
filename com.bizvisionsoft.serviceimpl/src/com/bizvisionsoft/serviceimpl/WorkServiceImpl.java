package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.mongocodex.codec.CodexProvider;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.ResourceActual;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkLink;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.service.model.WorkPackageProgress;
import com.bizvisionsoft.service.model.WorkResourcePlanDetail;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.UnwindOptions;
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

		appendOverdue(pipeline);

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

		AggregateIterable<Work> iterable = c(Work.class).aggregate(pipeline);
		return iterable;
	}
	public static void main(String[] args) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.lookup("aaa", "bbb", "ccc", "ddd"));
		System.out.println(new Document("q",pipeline).toBsonDocument(BsonDocument.class, CodexProvider.getRegistry()).toJson());
	}

	private void appendOverdue(List<Bson> pipeline) {
		int warningDay = (int) getSystemSetting(WARNING_DAY);
		warningDay = warningDay * 24 * 60 * 60 * 1000;
		List<Field<?>> fields = new ArrayList<Field<?>>();
		fields.add(new Field<Bson>("overdue", new BasicDBObject("$cond", new BasicDBObject("if", new BasicDBObject(
				"$ne", new Object[] { new BasicDBObject("$ifNull", new Object[] { "$actualFinish", true }), true }))
						.append("else",
								new BasicDBObject("$cond", new BasicDBObject("if",
										new BasicDBObject("$lt", new Object[] { "$planFinish", new Date() }))
												.append("then", "已超期")
												.append("else", new BasicDBObject("$cond",
														new BasicDBObject("if", new BasicDBObject("$lt",
																new Object[] { "$planFinish", new BasicDBObject("$add",
																		new Object[] { new Date(), warningDay }) }))
																				.append("then", "预警")
																				.append("else", "")))))
						.append("then", new BasicDBObject("$cond",
								new BasicDBObject("if",
										new BasicDBObject("$lt", new Object[] { "$planFinish", "$actualFinish" }))
												.append("then", "是").append("else", "否")))

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
	public List<Result> startStage(ObjectId _id, String executeBy) {
		List<Result> result = startStageCheck(_id, executeBy);
		if (!result.isEmpty()) {
			return result;
		}

		// 修改状态
		UpdateResult ur = c(Work.class).updateOne(new BasicDBObject("_id", _id),
				new BasicDBObject("$set", new BasicDBObject("status", ProjectStatus.Processing)
						.append("startOn", new Date()).append("startBy", executeBy)));

		// 根据ur构造下面的结果
		if (ur.getModifiedCount() == 0) {
			throw new ServiceException("没有满足启动条件的工作。");
		}

		ObjectId project_id = c("work").distinct("project_id", new BasicDBObject("_id", _id), ObjectId.class).first();
		ur = c(Project.class).updateOne(new BasicDBObject("_id", project_id),
				new BasicDBObject("$set", new BasicDBObject("stage_id", _id)));
		// 根据ur构造下面的结果
		if (ur.getModifiedCount() == 0) {
			throw new ServiceException("无法更新项目当前状态。");
		}

		// TODO 通知团队成员，工作已经启动

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

		return queryWork(skip, limit, new BasicDBObject("$or",
				new BasicDBObject[] { new BasicDBObject("chargerId", userid), new BasicDBObject("assignerId", userid) })
						.append("summary", false).append("actualFinish", null).append("distributed", true)
						.append("stage", new BasicDBObject("$ne", true)),
				filter, new BasicDBObject("planFinish", 1)).into(new ArrayList<Work>());
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
		pipeline.add(Aggregates.addFields(new Field<BasicDBObject>("deadline",
				new BasicDBObject("$arrayElemAt", new Object[] { "$work.planFinish", 0 }))));
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
						new Field<Bson>("qualifiedQty", new BasicDBObject("$max", "$progress.qualifiedQty")))));
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

	public List<Result> distributeWorkPlan(ObjectId _id, String distributeBy) {
		List<Result> result = distributeWorkPlanCheck(_id, distributeBy);
		if (!result.isEmpty()) {
			return result;
		}

		List<ObjectId> inputIds = Arrays.asList(_id);
		inputIds = getDesentItems(inputIds, "work", "parent_id");

		UpdateResult ur = c("work").updateMany(
				new BasicDBObject("_id", new BasicDBObject("$in", inputIds))
						.append("$or",
								new BasicDBObject[] { new BasicDBObject("chargerId", new Document("$ne", null)),
										new BasicDBObject("assignerId", new Document("$ne", null)) })
						.append("distributed", new BasicDBObject("$ne", true)),
				new BasicDBObject("$set", new BasicDBObject("distributed", true).append("distributeBy", distributeBy)
						.append("distributeOn", new Date())));

		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有需要下达的计划。"));
			return result;
		}

		return new ArrayList<Result>();
	}

	private List<Result> distributeWorkPlanCheck(ObjectId _id, String distributeBy) {
		// TODO 检查是否可以下达
		return new ArrayList<Result>();
	}

	@Override
	public List<Result> startWork(ObjectId _id) {
		List<Result> result = startWorkCheck(_id);

		// 工作没有开始时间或开始时间大于当前时间时，更新当前工作的开始时间为当前时间并获取更新的工作
		Date startDate = new Date();
		Document doc = c("work").findOneAndUpdate(
				new BasicDBObject("_id", _id).append("$or",
						new BasicDBObject[] { new BasicDBObject("actualStart", null),
								new BasicDBObject("actualStart", new BasicDBObject("$gt", startDate)) }),
				new BasicDBObject("$set", new BasicDBObject("actualStart", startDate)));

		// 如果没有获得工作则该工作已经启动
		if (doc == null) {
			result.add(Result.updateFailure("工作已经启动。"));
			return result;
		}

		// 更新上级工作的启动时间
		ObjectId parent_id = doc.getObjectId("parent_id");
		if (parent_id != null)
			startParentWork(parent_id, startDate);

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
	public List<Result> finishWork(ObjectId _id) {
		List<Result> result = finishWorkCheck(_id);
		// 工作没有完成时间或完成时间小于当前时间时，更新工作的完成时间为当前时间，并获取更新的工作
		Date finishDate = new Date();
		Document doc = c("work").findOneAndUpdate(
				new BasicDBObject("_id", _id).append("$or",
						new BasicDBObject[] { new BasicDBObject("actualFinish", null),
								new BasicDBObject("actualFinish", new BasicDBObject("$lt", finishDate)) }),
				new BasicDBObject("$set", new BasicDBObject("actualFinish", finishDate)));
		// 如果没有获得工作则该工作已经完工，并不需要继续循环完工上级工作
		if (doc == null) {
			result.add(Result.updateFailure("工作已经完成。"));
			return result;
		}

		// 更新上级工作的完工时间
		ObjectId parent_id = doc.getObjectId("parent_id");
		if (parent_id != null)
			finishParentWork(parent_id, finishDate);

		return result;
	}

	private void finishParentWork(ObjectId _id, Date finishDate) {
		// 判断该工作是否存在未完成的子工作，存在则不更新该工作实际完成时间
		long count = c("work").count(new BasicDBObject("parent_id", _id).append("actualFinish", null));
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
							new BasicDBObject("$set", new BasicDBObject("actualFinish", finishDate)));
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
	public List<Result> finishStage(ObjectId _id, String executeBy) {
		List<Result> result = finishStageCheck(_id, executeBy);
		if (!result.isEmpty()) {
			return result;
		}

		Document doc = c("work").find(new BasicDBObject("parent_id", _id))
				.projection(new BasicDBObject("actualFinish", true)).sort(new BasicDBObject("actualFinish", -1))
				.first();

		// 修改项目状态
		UpdateResult ur = c("work").updateOne(new BasicDBObject("_id", _id),
				new BasicDBObject("$set",
						new BasicDBObject("status", ProjectStatus.Closing).append("finishOn", new Date())
								.append("finishBy", executeBy).append("actualFinish", doc.get("actualFinish"))));

		// 根据ur构造下面的结果
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足完工条件的阶段。"));
			return result;
		}

		// TODO 处理阶段实际工期

		return result;
	}

	private List<Result> finishStageCheck(ObjectId _id, String executeBy) {
		//////////////////////////////////////////////////////////////////////
		// 须检查的信息
		// 1. 检查所属该阶段的工作是否全部完工，若没有，错误。根据工作完工后，自动向上级汇总实际完成的规则，只需要判断阶段下一级工作是否全部完工。
		ArrayList<Result> result = new ArrayList<Result>();
		long count = c("work").count(new BasicDBObject("parent_id", _id).append("actualFinish", null));
		if (count > 0) {
			result.add(Result.finishError("阶段存在没有完工的工作。"));
		}
		return result;
	}

	@Override
	public List<Result> closeStage(ObjectId _id, String executeBy) {
		List<Result> result = closeStageCheck(_id, executeBy);
		if (!result.isEmpty()) {
			return result;
		}

		// 修改状态
		UpdateResult ur = c(Work.class).updateOne(new BasicDBObject("_id", _id),
				new BasicDBObject("$set", new BasicDBObject("status", ProjectStatus.Closed)
						.append("closeOn", new Date()).append("closeBy", executeBy)));

		// 根据ur构造下面的结果
		if (ur.getModifiedCount() == 0) {
			throw new ServiceException("没有满足关闭条件的工作。");
		}

		// TODO 通知团队成员，工作已经关闭

		return result;
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
					res.setPlanBasicQty(works);

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
	public List<TrackView> listWorkPackageForScheduleInProject(ObjectId project_id, String catagory) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("project_id", project_id)));
		pipeline.add(new Document().append("$unwind", "$workPackageSetting"));
		pipeline.add(new Document().append("$match", new Document().append("workPackageSetting.catagory", catagory)));
		pipeline.add(new Document().append("$addFields",
				new Document().append("scheduleMonitoring", "$workPackageSetting")));
		pipeline.add(new Document().append("$project", new Document().append("workPackageSetting", false)));
		appendProject(pipeline);
		pipeline.add(new Document().append("$group", new Document().append("_id", "$scheduleMonitoring")
				.append("children", new Document().append("$push", "$$ROOT"))));
		pipeline.add(new Document().append("$replaceRoot", new Document().append("newRoot",
				new Document().append("$mergeObjects", Arrays.asList("$$ROOT", "$_id")))));

		return c("work", TrackView.class).aggregate(pipeline).into(new ArrayList<TrackView>());
	}

	@Override
	public long countWorkPackageForScheduleInProject(ObjectId project_id, String catagory) {
		return c("work")
				.count(new BasicDBObject("workPackageSetting.catagory", catagory).append("project_id", project_id));
	}

	@Override
	public List<TrackView> listWorkPackageForScheduleInStage(ObjectId stage_id, String catagory) {
		List<ObjectId> items = getDesentItems(Arrays.asList(stage_id), "work", "parent_id");
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("_id", new Document("$in", items))));
		pipeline.add(new Document().append("$unwind", "$workPackageSetting"));
		pipeline.add(new Document().append("$match", new Document().append("workPackageSetting.catagory", catagory)));
		pipeline.add(new Document().append("$addFields",
				new Document().append("scheduleMonitoring", "$workPackageSetting")));
		pipeline.add(new Document().append("$project", new Document().append("workPackageSetting", false)));
		appendProject(pipeline);
		pipeline.add(new Document().append("$group", new Document().append("_id", "$scheduleMonitoring")
				.append("children", new Document().append("$push", "$$ROOT"))));
		pipeline.add(new Document().append("$replaceRoot", new Document().append("newRoot",
				new Document().append("$mergeObjects", Arrays.asList("$$ROOT", "$_id")))));

		return c("work", TrackView.class).aggregate(pipeline).into(new ArrayList<TrackView>());
	}

	@Override
	public long countWorkPackageForScheduleInStage(ObjectId stage_id, String catagory) {
		List<ObjectId> items = getDesentItems(Arrays.asList(stage_id), "work", "parent_id");
		return c("work").count(new BasicDBObject("workPackageSetting.catagory", catagory).append("_id",
				new BasicDBObject("$in", items)));
	}

	@Override
	public List<Work> listWorkPackageForSchedule(BasicDBObject condition, String userid, String catagory) {
		List<ObjectId> items = getProject_id(userid);

		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("project_id", new Document("$in", items)).append("summary", false)
				.append("actualFinish", null).append("distributed", true)
				.append("stage", new BasicDBObject("$ne", true))));
		pipeline.add(new Document().append("$unwind", "$workPackageSetting"));
		pipeline.add(new Document().append("$match", new Document().append("workPackageSetting.catagory", catagory)));
		pipeline.add(new Document().append("$addFields",
				new Document().append("scheduleMonitoring", "$workPackageSetting")));
		pipeline.add(new Document().append("$project", new Document().append("workPackageSetting", false)));
		appendProject(pipeline);

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
		return c("work").count(filter.append("workPackageSetting.catagory", catagory).append("project_id",
				new BasicDBObject("$in", items)));
	}

	@Override
	public List<ResourceActual> addResourceActual(ResourceAssignment resa) {
		Set<ObjectId> workIds = new HashSet<ObjectId>();
		List<ResourceActual> documents = new ArrayList<ResourceActual>();

		Date planStart = resa.from;
		Date planFinish = resa.to;
		Calendar planStartCal = Calendar.getInstance();
		planStartCal.setTime(planStart);

		Calendar planFinishCal = Calendar.getInstance();
		planFinishCal.setTime(planFinish);
		planFinishCal.add(Calendar.DAY_OF_MONTH, 1);

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
		workIds.add(resa.work_id);

		double actualBasicQty = resa.actualBasicQty / documents.size();

		double actualOverTimeQty = resa.actualOverTimeQty / documents.size();
		for (ResourceActual resourceActual : documents) {
			resourceActual.setActualBasicQty(actualBasicQty);
			resourceActual.setActualOverTimeQty(actualOverTimeQty);
		}

		c(ResourceActual.class).insertMany(documents);

		return documents;
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
	public double getPlanWorks(String wbscode) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("wbsCode", Pattern.compile("1\\.")).append("summary", false)));
		pipeline.add(new Document("$group",
				new Document("_id", null).append("planWorks", new Document("$sum", "$planWorks"))));
		Document doc = c("work").aggregate(pipeline).first();
		if (doc != null) {
			return Optional.ofNullable(((Number) doc.get("planWorks")).doubleValue()).orElse(0d);
		}
		return 0;
	}

	@Override
	public double getActualWorks(String wbscode) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("wbsCode", Pattern.compile("1\\.")).append("summary", false)));
		pipeline.add(new Document("$group",
				new Document("_id", null).append("actualWorks", new Document("$sum", "$actualWorks"))));
		Document doc = c("work").aggregate(pipeline).first();
		if (doc != null) {
			return Optional.ofNullable(((Number) doc.get("actualWorks")).doubleValue()).orElse(0d);
		}
		return 0;
	}
}
