package com.bizvisionsoft.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.News;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Stockholder;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.UpdateResult;

public class ProjectServiceImpl extends BasicServiceImpl implements ProjectService {

	@Override
	public Project insert(Project input) {
		// TODO 记录创建者
		Project project;
		if (input.getProjectTemplate_id() == null) {
			/////////////////////////////////////////////////////////////////////////////
			// 数据准备
			ObjectId obsRoot_id = new ObjectId();// 组织根
			ObjectId cbsRoot_id = new ObjectId();// 预算根

			ObjectId projectSet_id = input.getProjectSet_id();
			ObjectId obsParent_id = null;// 组织上级
			ObjectId cbsParent_id = null;// 成本上级
			if (projectSet_id != null) {
				// 获得上级obs_id
				Document doc = c("projectSet").find(new BasicDBObject("_id", projectSet_id))
						.projection(new BasicDBObject("obs_id", true).append("cbs_id", true)).first();
				obsParent_id = Optional.ofNullable(doc).map(d -> d.getObjectId("obs_id")).orElse(null);
				cbsParent_id = Optional.ofNullable(doc).map(d -> d.getObjectId("cbs_id")).orElse(null);

			}
			/////////////////////////////////////////////////////////////////////////////
			// 0. 创建项目
			project = insert(input.setOBS_id(obsRoot_id).setCBS_id(cbsRoot_id), Project.class);

			/////////////////////////////////////////////////////////////////////////////
			// 1. 项目团队初始化
			OBSItem obsRoot = new OBSItem()// 创建本项目的OBS根节点
					.set_id(obsRoot_id)// 设置_id与项目关联
					.setScope_id(project.get_id())// 设置scope_id表明该组织节点是该项目的组织
					.setParent_id(obsParent_id)// 设置上级的id
					.setName(project.getName() + "项目组")// 设置该组织节点的默认名称
					.setRoleId(OBSItem.ID_PM)// 设置该组织节点的角色id
					.setRoleName(OBSItem.NAME_PM)// 设置该组织节点的名称
					.setManagerId(project.getPmId()) // 设置该组织节点的角色对应的人
					.setScopeRoot(true);// 区分这个节点是范围内的根节点
			insert(obsRoot, OBSItem.class);

			/////////////////////////////////////////////////////////////////////////////
			// 2. 财务科目初始化
			// 创建根
			CBSItem cbsRoot = CBSItem.getInstance(project, true);//
			cbsRoot.set_id(cbsRoot_id);//
			cbsRoot.setParent_id(cbsParent_id);//
			cbsRoot.setId(project.getId());
			cbsRoot.setName(project.getName());
			insert(cbsRoot, CBSItem.class);

		} else {
			// TODO 根据模板创建

			project = insert(input, Project.class);
		}

		return get(project.get_id());
	}

	@Override
	public Project get(ObjectId _id) {
		List<Project> ds = createDataSet(new BasicDBObject("filter", new BasicDBObject("_id", _id)));
		if (ds.size() == 0) {
			throw new ServiceException("没有_id为" + _id + "的项目。");
		}
		return ds.get(0);
	}

	@Override
	public long count(BasicDBObject filter) {
		return count(filter, Project.class);
	}

	@Override
	public List<Project> createDataSet(BasicDBObject condition) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		return query(skip, limit, filter);
	}

	private List<Project> query(Integer skip, Integer limit, BasicDBObject filter) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		appendQueryPipeline(skip, limit, filter, pipeline);

		List<Project> result = new ArrayList<Project>();
		c(Project.class).aggregate(pipeline).into(result);
		return result;

	}

	private void appendQueryPipeline(Integer skip, Integer limit, BasicDBObject filter, List<Bson> pipeline) {
		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		// TODO 补充pipeline
		// 1. 承担组织
		appendOrgFullName(pipeline, "impUnit_id", "impUnitOrgFullName");

		appendUserInfo(pipeline, "pmId", "pmInfo");

		appendStage(pipeline, "stage_id", "stage");

		appendLookupAndUnwind(pipeline, "projectSet", "projectSet_id", "projectSet");

		appendLookupAndUnwind(pipeline, "eps", "eps_id", "eps");

		appendLookupAndUnwind(pipeline, "project", "parentProject_id", "parentProject");

		appendWorkTime(pipeline);

		// TODO 增加超期判断，
		appendOverdue(pipeline);
	}

	private void appendOverdue(List<Bson> pipeline) {
		pipeline.addAll(
				Arrays.asList(
						new Document("$lookup", new Document("from", "work")
								.append("let",
										new Document("project_id", "$_id"))
								.append("pipeline",
										Arrays.asList(
												new Document("$match",
														new Document("$expr",
																new Document("$and", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document("$in",
																				Arrays.asList("$manageLevel",
																						Arrays.asList("1", "2"))),
																		new Document("$or", Arrays.asList(
																				new Document("$lt",
																						Arrays.asList("$planFinish",
																								new Date())),
																				new Document("$lt",
																						Arrays.asList("$planFinish",
																								"$actualFinish")))))))),
												new Document("$group",
														new Document("_id", null).append("count",
																new Document("$sum", 1.0)))))
								.append("as", "work")),
						new Document("$unwind",
								new Document("path", "$work").append("preserveNullAndEmptyArrays", true)),
						new Document("$addFields",
								new Document("overdue", new Document("$gt", Arrays.asList("$work.count", 0.0)))),
						new Document("$project", new Document("work", false))));
	}

	private void appendWorkTime(List<Bson> pipeline) {
		pipeline.addAll(Arrays.asList(
				new Document("$lookup", new Document()
						.append("from", "work").append("let", new Document("project_id", "$_id")).append("pipeline",
								Arrays.asList(
										new Document("$match",
												new Document("$expr",
														new Document("$and",
																Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document("$eq",
																				Arrays.asList("$summary", false)))))),
										new Document("$group",
												new Document("_id", null)
														.append("actualWorks", new Document("$sum", "$actualWorks"))
														.append("planWorks", new Document("$sum", "$planWorks")))))
						.append("as", "worktime")),
				new Document("$unwind", new Document("path", "$worktime").append("preserveNullAndEmptyArrays", true)),
				new Document("$addFields", new Document("summaryActualWorks", "$worktime.actualWorks")
						.append("summaryPlanWorks", "$worktime.planWorks")),
				new Document("$project", new Document("worktime", false))));

		pipeline.addAll(Arrays.asList(
				new Document("$lookup",
						new Document("from", "work")
								.append("let",
										new Document("project_id", "_id"))
								.append("pipeline",
										Arrays.asList(
												new Document("$match",
														new Document("$expr",
																new Document("$and",
																		Arrays.asList(
																				new Document("$eq",
																						Arrays.asList("$project_id",
																								"$$project_id")),
																				new Document("$eq",
																						Arrays.asList("$summary",
																								false)))))),
												new Document().append("$addFields", new Document()
														.append("planDuration", new Document("$divide",
																Arrays.asList(
																		new Document("$subtract",
																				Arrays.asList("$planFinish",
																						"$planStart")),
																		1000 * 3600 * 24)))
														.append("actualDuration", new Document("$divide", Arrays.asList(
																new Document("$subtract", Arrays.asList(
																		new Document("$ifNull",
																				Arrays.asList("$actualFinish",
																						new Date())),
																		new Document("$ifNull",
																				Arrays.asList("$actualStart",
																						new Date())))),
																1000 * 3600 * 24)))),
												new Document("$group",
														new Document("_id", null)
																.append("planDuration",
																		new Document("$sum", "$planDuration"))
																.append("actualDuration",
																		new Document("$sum", "$actualDuration")))))
								.append("as", "work")),
				new Document("$unwind", new Document("path", "$work").append("preserveNullAndEmptyArrays", true)),
				new Document("$addFields", new Document("summaryPlanDuration", "$work.planDuration")
						.append("summaryActualDuration", "$work.actualDuration")),
				new Document("$project", new Document("work", false))));
	}

	@Override
	public List<Date> getPlanDateRange(ObjectId _id) {
		Project data = c(Project.class).find(new BasicDBObject("_id", _id))
				.projection(new BasicDBObject().append("planStart", 1).append("planFinish", 1)).first();
		ArrayList<Date> result = new ArrayList<Date>();
		result.add(data.getPlanStart());
		result.add(data.getPlanFinish());
		return result;
	}

	@Override
	public long update(BasicDBObject fu) {
		return update(fu, Project.class);
	}

	@Override
	public List<Result> startProject(ObjectId _id, String executeBy) {
		List<Result> result = startProjectCheck(_id, executeBy);
		if (!result.isEmpty()) {
			return result;
		}

		// 修改项目状态
		UpdateResult ur = c(Project.class).updateOne(new BasicDBObject("_id", _id),
				new BasicDBObject("$set", new BasicDBObject("status", ProjectStatus.Processing)
						.append("startOn", new Date()).append("startBy", executeBy)));

		// 根据ur构造下面的结果
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足启动条件的项目。"));
			return result;
		}

		// TODO CBS金额汇总

		// 通知项目团队成员，项目已经启动
		List<String> memberIds = getProjectMembers(_id);
		String name = getName("project", _id);
		sendMessage("项目启动通知", "项目" + name + "已于" + new SimpleDateFormat("yyyy-M-d") + "启动。", executeBy, memberIds,
				null);
		return result;
	}

	private List<String> getProjectMembers(ObjectId _id) {
		List<ObjectId> parentIds = c("obs").distinct("_id", new BasicDBObject("scope_id", _id), ObjectId.class)
				.into(new ArrayList<>());
		List<ObjectId> ids = getDesentItems(parentIds, "obs", "parent_id");
		ArrayList<String> memberIds = c("obs").distinct("managerId",
				new BasicDBObject("_id",
						new BasicDBObject("$in", ids)).append("managerId", new BasicDBObject("$ne", null)),
				String.class).into(new ArrayList<>());
		return memberIds;
	}

	private List<Result> startProjectCheck(ObjectId _id, String executeBy) {
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
	public List<Result> distributeProjectPlan(ObjectId _id, String distributeBy) {
		List<Result> result = distributeProjectPlanCheck(_id, distributeBy);
		if (!result.isEmpty()) {
			return result;
		}

		Document query = new Document("project_id", _id).append("parent_id", null)
				.append("chargerId", new Document("$ne", null)).append("distributed", new Document("$ne", true));

		final List<ObjectId> ids = new ArrayList<>();
		final List<Message> messages = new ArrayList<>();

		String pjName = getName("project", _id);
		c("work").find(query).forEach((Document w) -> {
			ids.add(w.getObjectId("_id"));
			messages.add(Message.newInstance("新下达的工作计划",
					"项目 " + pjName + "，工作 " + w.getString("fullName") + "，计划 "
							+ new SimpleDateFormat("yyyy/M/d").format(w.getDate("planStart")) + " - "
							+ new SimpleDateFormat("yyyy/M/d").format(w.getDate("planFinish")),
					distributeBy, w.getString("chargerId"), null));
		});

		if (ids.isEmpty()) {
			result.add(Result.updateFailure("没有需要下达的计划。"));
			return result;
		}

		c(Work.class).updateMany(new Document("_id", new Document("$in", ids)),
				new Document("$set", new Document("distributed", true).append("distributeBy", distributeBy)
						.append("distributeOn", new Date())));

		sendMessages(messages);

		return new ArrayList<Result>();

	}

	private List<Result> distributeProjectPlanCheck(ObjectId _id, String distributeBy) {
		// TODO 检查是否可以下达
		return new ArrayList<Result>();
	}

	@Override
	public List<Work> listStage(ObjectId _id) {
		// TODO 排序
		return new WorkServiceImpl().query(null, null, new BasicDBObject("project_id", _id).append("stage", true),
				Work.class);
	}

	@Override
	public long countStage(ObjectId _id) {
		return c("work").count(new BasicDBObject("project_id", _id).append("stage", true));
	}

	@Override
	public List<Stockholder> getStockholders(BasicDBObject condition, ObjectId _id) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("project_id", _id);
		return createDataSet(condition, Stockholder.class);
	}

	@Override
	public long countStockholders(BasicDBObject filter, ObjectId _id) {
		filter.append("project_id", _id);
		return count(filter, Stockholder.class);
	}

	@Override
	public Stockholder insertStockholder(Stockholder c) {
		return insert(c, Stockholder.class);
	}

	@Override
	public List<Project> listManagedProjects(BasicDBObject condition, String userid) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.put("pmId", userid);
		return query(skip, limit, filter);
	}

	@Override
	public long countManagedProjects(BasicDBObject filter, String userid) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.put("pmId", userid);
		return count(filter, Project.class);
	}

	@Override
	public List<Project> listParticipatedProjects(BasicDBObject condition, String userId) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");

		List<Bson> pipeline = new ArrayList<Bson>();

		appendParticipatedProjectQuery(userId, pipeline);

		appendQueryPipeline(skip, limit, filter, pipeline);

		return c("obs").aggregate(pipeline, Project.class).into(new ArrayList<Project>());
	}

	private void appendParticipatedProjectQuery(String userId, List<Bson> pipeline) {
		pipeline.add(new Document("$match",
				new Document("$or", Arrays.asList(new Document("member", userId), new Document("managerId", userId)))));

		pipeline.add(new Document("$lookup", new Document("from", "work").append("localField", "scope_id")
				.append("foreignField", "_id").append("as", "work")));

		pipeline.add(new Document("$lookup", new Document("from", "project").append("localField", "scope_id")
				.append("foreignField", "_id").append("as", "project")));

		pipeline.add(new Document("$lookup", new Document("from", "project").append("localField", "work.project_id")
				.append("foreignField", "_id").append("as", "project2")));

		pipeline.add(
				new Document("$unwind", new Document("path", "$project").append("preserveNullAndEmptyArrays", true)));

		pipeline.add(
				new Document("$unwind", new Document("path", "$project2").append("preserveNullAndEmptyArrays", true)));

		pipeline.add(
				new Document("$addFields", new Document("project_id", new Document("$cond", Arrays.asList("$project",
						"$project._id", new Document("$cond", Arrays.asList("$project2", "$project2._id", null)))))));

		pipeline.add(new Document("$group", new Document("_id", "$project_id")));

		pipeline.add(new Document("$lookup", new Document("from", "project").append("localField", "_id")
				.append("foreignField", "_id").append("as", "project")));

		pipeline.add(new Document("$replaceRoot",
				new Document("newRoot", new Document("$arrayElemAt", Arrays.asList("$project", 0)))));
	}

	@Override
	public long countParticipatedProjects(BasicDBObject filter, String userId) {
		List<Bson> pipeline = new ArrayList<Bson>();

		appendParticipatedProjectQuery(userId, pipeline);

		if (filter != null)
			pipeline.add(new Document("$match", filter));

		return c("obs").aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public long delete(ObjectId _id) {
		// TODO 删除检查
		return delete(_id, Project.class);
	}

	@Override
	public Workspace getWorkspace(ObjectId _id) {
		BasicDBObject dbo = c("project").find(new BasicDBObject("_id", _id), BasicDBObject.class)
				.projection(new BasicDBObject("space_id", Boolean.TRUE).append("checkoutBy", Boolean.TRUE)).first();
		return Workspace.newInstance(_id, dbo.getObjectId("space_id"), dbo.getString("checkoutBy"));
	}

	@Override
	public List<Result> finishProject(ObjectId _id, String executeBy) {
		List<Result> result = finishProjectCheck(_id, executeBy);
		if (!result.isEmpty()) {
			return result;
		}

		Document doc = c("work").find(new BasicDBObject("project_id", _id))
				.projection(new BasicDBObject("actualFinish", true)).sort(new BasicDBObject("actualFinish", -1))
				.first();
		
		// 修改项目状态
		UpdateResult ur = c("project").updateOne(new BasicDBObject("_id", _id),
				new BasicDBObject("$set",
						new BasicDBObject("status", ProjectStatus.Closing).append("progress", 1d)
								.append("finishOn", new Date()).append("finishBy", executeBy)
								.append("actualFinish", doc.get("actualFinish"))));

		// 根据ur构造下面的结果
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足完工条件的项目。"));
			return result;
		}

		// 通知项目团队成员，项目已经启动
		List<String> memberIds = getProjectMembers(_id);
		String name = getName("project", _id);
		sendMessage("项目完工通知", "项目" + name + "已于" + new SimpleDateFormat("yyyy-M-d") + "完工。", executeBy, memberIds,
				null);

		return result;
	}

	private List<Result> finishProjectCheck(ObjectId _id, String executeBy) {
		//////////////////////////////////////////////////////////////////////
		// 须检查的信息
		// 1. 检查所属该项目的工作是否全部完工，若没有，错误。
		ArrayList<Result> result = new ArrayList<Result>();
		long count = c("work").count(new BasicDBObject("project_id", _id).append("actualFinish", null));
		if (count > 0) {
			result.add(Result.finishError("项目存在没有完工的工作。"));
		}
		return result;
	}

	@Override
	public List<Result> closeProject(ObjectId _id, String executeBy) {
		List<Result> result = closeProjectCheck(_id, executeBy);
		if (!result.isEmpty()) {
			return result;
		}

		// 修改项目状态
		UpdateResult ur = c(Project.class).updateOne(new BasicDBObject("_id", _id),
				new BasicDBObject("$set", new BasicDBObject("status", ProjectStatus.Closed)
						.append("closeOn", new Date()).append("closeBy", executeBy)));

		// 根据ur构造下面的结果
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足关闭条件的项目。"));
			return result;
		}

		// TODO 通知项目团队成员，项目已经关闭

		return result;
	}

	private List<Result> closeProjectCheck(ObjectId _id, String executeBy) {
		//////////////////////////////////////////////////////////////////////
		// 须检查的信息

		return new ArrayList<Result>();
	}

	@Override
	public String generateWorkOrder(String catalog, ObjectId parentproject_id, ObjectId impunit_id) {
		/**
		 * TODO 需要根据九洲定制
		 * 
		 * KG×-××-××-××××
		 * 
		 * 作号采用四级编码。
		 *
		 * 第1位至第2位为类型码：KG代表科研类项目；YG代表预研类项目；CG代表CBB项目。
		 *
		 * 第3位为承研部门：1代表识别事业部；2代表探测事业部；3代表空管公司；5代表通信与对抗事业部；6代表工程部；7代表预研部；8代表共性部。
		 *
		 * 第4位至第9位为立项顺序号，分为两部分：
		 * 1.第4位至第6位按流水号排序，每年从“-01”开始往后编排，如项目为子项目，则第4位至第6位为父项目立项顺序号；
		 * 2.第7位至第9位为子项目流水号，从项目中获取的子项目流水号，如不是子项目，则第7位至第9位为空。
		 * 
		 * 第10位至第14位为立项年份。
		 **/
		String workOrder;
		if ("预研".equals(catalog)) {
			workOrder = "YG";
		} else if ("CBB".equals(catalog)) {
			workOrder = "CG";
		} else {
			workOrder = "KG";
		}
		int year = Calendar.getInstance().get(Calendar.YEAR);

		String orgNo = c("organization").distinct("id", new Document("_id", impunit_id), String.class).first();
		workOrder += orgNo;

		if (parentproject_id != null) {
			String parentWorkOrder = c("project")
					.distinct("workOrder", new Document("_id", parentproject_id), String.class).first();
			String[] workorders = parentWorkOrder.split("-");
			workOrder += "-" + workorders[1];
			int index = generateCode(Generator.DEFAULT_NAME, "projectno" + workorders[1]);
			workOrder += "-" + String.format("%02d", index);

		} else {
			int index = generateCode(Generator.DEFAULT_NAME, "projectno" + year);
			workOrder += "-" + String.format("%02d", index);
		}

		workOrder += "-" + String.format("%04d", year);

		return workOrder;
	}

	@Override
	public List<News> getRecentNews(ObjectId _id, int count) {
		ArrayList<News> result = new ArrayList<News>();
		c("work")
				.aggregate(
						new JQ("查询时间线").set("match",
								new Document("manageLevel", new Document("$in", Arrays.asList("1", "2")))
										.append("project_id", _id))
								.set("limit", count).array())
				.forEach((Document doc) -> {
					if (doc.get("date").equals(doc.get("actualStart"))) {
						result.add(new News().setDate(doc.getDate("date"))
								.setContent("" + doc.get("userInfo") + " " + doc.get("name") + " 启动 "));
					} else if (doc.get("date").equals(doc.get("actualFinish"))) {
						result.add(new News().setDate(doc.getDate("date"))
								.setContent("" + doc.get("userInfo") + " " + doc.get("name") + " 完成"));
					}
				});
		return result;
	}

}
