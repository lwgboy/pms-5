package com.bizvisionsoft.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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

import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Baseline;
import com.bizvisionsoft.service.model.BaselineComparable;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.ChangeProcess;
import com.bizvisionsoft.service.model.Command;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.News;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.model.ProjectChangeTask;
import com.bizvisionsoft.service.model.ProjectScheduleInfo;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Role;
import com.bizvisionsoft.service.model.SalesItem;
import com.bizvisionsoft.service.model.Stockholder;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.bizvisionsoft.serviceimpl.renderer.ProjectChangeRenderer;
import com.bizvisionsoft.serviceimpl.renderer.ProjectRenderer;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.result.UpdateResult;

/**
 * @author hua
 *
 */
public class ProjectServiceImpl extends BasicServiceImpl implements ProjectService {

	@Override
	public Project insert(Project input) {
		// TODO 记录创建者
		Project project = null;
		if (input.getProjectTemplate_id() == null) {
			/////////////////////////////////////////////////////////////////////////////
			// 数据准备
			ObjectId obsRoot_id = new ObjectId();// 组织根
			ObjectId cbsRoot_id = new ObjectId();// 预算根

			ObjectId program_id = input.getProgram_id();
			ObjectId obsParent_id = null;// 组织上级
			ObjectId cbsParent_id = null;// 成本上级
			if (program_id != null) {
				// 获得上级obs_id
				Document doc = c("program").find(new BasicDBObject("_id", program_id))
						.projection(new BasicDBObject("obs_id", true).append("cbs_id", true)).first();
				obsParent_id = Optional.ofNullable(doc).map(d -> d.getObjectId("obs_id")).orElse(null);
				cbsParent_id = Optional.ofNullable(doc).map(d -> d.getObjectId("cbs_id")).orElse(null);

			}
			/////////////////////////////////////////////////////////////////////////////
			// 0. 创建项目
			try {
				project = insert(input.setOBS_id(obsRoot_id).setCBS_id(cbsRoot_id), Project.class);
			} catch (Exception e) {
				if (e instanceof MongoBulkWriteException) {
					throw new ServiceException(e.getMessage());
				}
			}
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
			cbsRoot.setName(project.getName());
			insert(cbsRoot, CBSItem.class);

		} else {
			try {
				project = insert(input, Project.class);
			} catch (Exception e) {
				if (e instanceof MongoBulkWriteException) {
					throw new ServiceException(e.getMessage());
				}
			}
		}

		return get(project.get_id());
	}

	@Override
	public Project get(ObjectId _id) {
		List<Project> ds = list(new BasicDBObject("filter", new BasicDBObject("_id", _id)));
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
	public List<Project> list(BasicDBObject condition) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		return query(skip, limit, filter, sort);
	}

	private List<Project> query(Integer skip, Integer limit, BasicDBObject filter, BasicDBObject sort) {
		List<Bson> pipeline = appendQueryPipeline(skip, limit, filter, sort, new ArrayList<>());
		return c(Project.class).aggregate(pipeline).into(new ArrayList<Project>());
	}

	private List<Bson> appendQueryPipeline(Integer skip, Integer limit, BasicDBObject filter, BasicDBObject sort, List<Bson> pipeline) {
		// 1. 承担组织
		appendOrgFullName(pipeline, "impUnit_id", "impUnitOrgFullName");

		appendUserInfo(pipeline, "pmId", "pmInfo");

		appendStage(pipeline, "stage_id", "stage");

		appendLookupAndUnwind(pipeline, "program", "program_id", "program");

		appendLookupAndUnwind(pipeline, "eps", "eps_id", "eps");

		appendLookupAndUnwind(pipeline, "project", "parentProject_id", "parentProject");

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));
		else
			// yangjun 2018/10/31
			pipeline.add(Aggregates.sort(new BasicDBObject("planFinish", 1).append("_id", -1)));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		// yangjun 2018/11/1
		pipeline.addAll(new JQ("追加-项目-工期与工时").array());

		pipeline.addAll(new JQ("追加-项目-SAR").array());

		return pipeline;
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
		try {
			return update(fu, Project.class);
		} catch (Exception e) {
			handleMongoException(e, "项目" + fu);
		}
		return 0;
	}

	/**
	 * 使用update来修改项目
	 */
	@Override
	@Deprecated
	public void updateProjectId(ObjectId _id, String id) {
		Document cond = new Document("_id", _id);
		Project project = c(Project.class).find(cond).first();

		if (!ProjectStatus.Created.equals(project.getStatus())) {
			throw new ServiceException("只能在项目启动前设置编号");
		}

		try {
			String workOrder = generateWorkOrder(_id);
			c("project").updateOne(cond, new Document("$set", new Document("id", id).append("workOrder", workOrder)));
		} catch (Exception e) {
			throw handleMongoException(e, "项目编号" + id);
		}
	}

	@Override
	public void approveProject(Command com) {
		Document cond = new Document("_id", com._id);
		Project project = c(Project.class).find(cond).first();

		if (!ProjectStatus.Created.equals(project.getStatus())) {
			throw new ServiceException("已启动的项目无需再次批准");
		}

		if (Boolean.TRUE.equals(project.getStartApproved())) {
			throw new ServiceException("已批准的项目无需再次批准");
		}

		c("project").updateOne(cond, new Document("$set", new Document("startApproved", true).append("approveInfo", com.info())));
	}

	@Override
	public List<Result> startProject(Command com) {
		/////////////////////////////////////////////////////////////////////////////
		// 检查项目启动
		List<Result> result = startProjectCheck(com._id, com.userId);
		if (result.stream().anyMatch(r -> Result.TYPE_ERROR == r.type// 错误的必须返回
				|| (Result.TYPE_WARNING == r.type && ICommand.Start_Project.equals(com.name)))) {// 不忽略警告的必须返回
			return result;
		}
		/////////////////////////////////////////////////////////////////////////////
		// 修改项目状态
		c("project").updateOne(new Document("_id", com._id),
				new Document("$set", new Document("status", ProjectStatus.Processing).append("startInfo", com.info())));

		List<ObjectId> ids;

		boolean stageEnabled = getValue("project", "stageEnable", com._id, Boolean.class);

		if (stageEnabled) {
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 下达阶段计划
			ids = c("work").distinct("_id", new Document("project_id", com._id)// 本项目中的所有阶段
					.append("stage", true), ObjectId.class).into(new ArrayList<ObjectId>());
		} else {
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 下达非阶段管理的项目的工作计划
			ids = c("work").distinct("_id", new Document("project_id", com._id)// 本项目中的所有工作
					, ObjectId.class).into(new ArrayList<ObjectId>());
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 更新下达计划的工作和项目，记录下达信息
		if (!ids.isEmpty()) {
			Document distributeInfo = com.info();
			c("work").updateMany(new Document("_id", new Document("$in", ids)), //
					new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
			c("project").updateOne(new Document("_id", com._id), //
					new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
		}

		/////////////////////////////////////////////////////////////////////////////
		// 通知项目团队成员，项目已经启动
		List<String> memberIds = getProjectMembers(com._id);
		sendMessage("项目启动通知", "项目：" + getName("project", com._id) + " 已于 " + Message.format(com.date) + " 启动。", com.userId, memberIds,
				null);
		return new ArrayList<>();
	}

	private List<String> getProjectMembers(ObjectId _id) {
		List<ObjectId> parentIds = c("obs").distinct("_id", new BasicDBObject("scope_id", _id), ObjectId.class).into(new ArrayList<>());
		List<ObjectId> ids = getDesentItems(parentIds, "obs", "parent_id");
		ArrayList<String> memberIds = c("obs").distinct("managerId",
				new BasicDBObject("_id", new BasicDBObject("$in", ids)).append("managerId", new BasicDBObject("$ne", null)), String.class)
				.into(new ArrayList<>());
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
		List<Result> result = new ArrayList<Result>();
		
		Object checkPlanSetting = getSystemSetting("项目启动检查", "plan");
		//TODO 按照启动检查来处理
		long l = c(Work.class).countDocuments(new Document("project_id", _id).append("parent_id", null));
		if (l == 0)
			result.add(Result.warning("项目尚未创建进度计划"));

		// l = c(Work.class).countDocuments(new Document("project_id",
		// _id).append("manageLevel", "1").append("$or",
		// Arrays.asList(new Document("assignerId", null), new Document("chargerId",
		// null))));
		// if (l == 0)
		// result.add(Result.startProjectError("项目沒有创建进度计划.",
		// Result.CODE_PROJECT_NOWORK));
		//
		// l = c(Work.class).countDocuments(new Document("project_id",
		// _id).append("parent_id", null)
		// .append("chargerId", null).append("assignerId", null));
		// if (l == 0)
		// result.add(Result.startProjectWarning("项目进度计划没有指定必要角色.",
		// Result.CODE_PROJECT_NOWORKROLE));
		//
		// l = c(OBSItem.class).countDocuments(new Document("scope_id", _id));
		// if (l > 1)
		// result.add(Result.startProjectWarning("项目沒有创建组织结构.",
		// Result.CODE_PROJECT_NOOBS));
		//
		// l = c(Stockholder.class).countDocuments(new Document("project_id", _id));
		// if (l > 1)
		// result.add(Result.startProjectWarning("项目沒有创建干系人.",
		// Result.CODE_PROJECT_NOSTOCKHOLDER));

		Project project = get(_id);

		// ObjectId cbs_id = project.getCBS_id();
		// List<ObjectId> cbsIds = getDesentItems(Arrays.asList(cbs_id), "cbs",
		// "parent_id");
		// l = c(CBSPeriod.class).countDocuments(new Document("cbsItem_id", new
		// Document("$in", cbsIds)));
		// if (l == 0) {
		// l = c(CBSSubject.class).countDocuments(new Document("cbsItem_id", new
		// Document("$in", cbsIds)));
		// if (l == 0)
		// result.add(Result.startProjectWarning("项目尚未编制预算",
		// Result.CODE_PROJECT_NOCBS));
		// }

		if (!Boolean.TRUE.equals(project.getStartApproved())) {
			result.add(Result.error("项目尚未获得启动批准"));
		}

		return result;
	}

	public List<Result> distributeProjectPlan(Command com) {
		final List<Message> msg = new ArrayList<>();
		final Set<ObjectId> ids = new HashSet<>();

		Project project = get(com._id, Project.class);
		final String projectName = project.getName();
		boolean stageEnabled = project.isStageEnable();

		if (stageEnabled) {
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 下达阶段计划(阶段 已创建)
			c("work").find(//
					new Document("project_id", com._id)// 本项目中
							.append("chargerId", new Document("$ne", null))// 负责人不为空
							// .append("distributed", new Document("$ne", true))// 没有下达的
							.append("status", ProjectStatus.Created) // 已创建的阶段
							.append("stage", true))// 阶段
					.forEach((Document w) -> {
						ids.add(w.getObjectId("_id"));
						msg.add(Message.distributeStageMsg(projectName, w, com.userId, w.getString("chargerId")));
					});
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 下达工作计划，阶段是进行中的，不是总成型工作的，没有下达计划的工作
			c("work").aggregate(new JQ("查询-工作-阶段需下达的工作计划").set("project_id", com._id).set("match", new Document()).array())
					.forEach((Document w) -> {
						ids.add(w.getObjectId("_id"));
						Check.isAssigned(w.getString("chargerId"),
								c -> msg.add(Message.distributeWorkMsg("工作计划下达通知", projectName, w, true, com.userId, c)));
						Check.isAssigned(w.getString("assignerId"),
								c -> msg.add(Message.distributeWorkMsg("工作计划下达通知", projectName, w, false, com.userId, c)));
					});
		} else {
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 下达非阶段管理的项目的工作计划
			c("work").find(//
					new Document("project_id", com._id)// 本项目中
							.append("chargerId", new Document("$ne", null))// 负责人不为空
			// .append("distributed", new Document("$ne", true))// 没有下达的
			).forEach((Document w) -> {
				ids.add(w.getObjectId("_id"));
				msg.add(Message.distributeStageMsg(projectName, w, com.userId, w.getString("chargerId")));
			});
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 如果没有可下达的计划，提示
		if (ids.isEmpty()) {
			return Arrays.asList(Result.info("没有需要下达的计划。"));
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 更新下达计划的工作和项目，记录下达信息
		Document distributeInfo = com.info();
		c("work").updateMany(new Document("_id", new Document("$in", new ArrayList<>(ids))), //
				new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
		c("project").updateOne(new Document("_id", com._id), //
				new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 发出消息通知
		sendMessages(msg);

		return new ArrayList<>();
	}

	@Override
	public List<Work> listStage(ObjectId _id) {
		return new WorkServiceImpl().query(null, null, new BasicDBObject("project_id", _id).append("stage", true),
				new BasicDBObject("wbsCode", 1), Work.class);
	}

	@Override
	public long countStage(ObjectId _id) {
		return c("work").countDocuments(new BasicDBObject("project_id", _id).append("stage", true));
	}

	@Override
	public List<Work> listMyStage(String userId) {
		return new WorkServiceImpl().createTaskDataSet(
				new BasicDBObject("$or", Arrays.asList(new BasicDBObject("chargerId", userId), new BasicDBObject("assignerId", userId)))
						.append("stage", true).append("status", new BasicDBObject("$in",
								Arrays.asList(ProjectStatus.Created, ProjectStatus.Processing, ProjectStatus.Closing))));
	}

	@Override
	public long countMyStage(String userId) {
		return count(
				new BasicDBObject("$or", Arrays.asList(new BasicDBObject("chargerId", userId), new BasicDBObject("assignerId", userId)))
						.append("stage", true),
				"work");
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
	public long updateStockholder(BasicDBObject fu) {
		return update(fu, Stockholder.class);
	}

	@Override
	public List<Project> listManagedProjects(BasicDBObject condition, String userid) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		// 杨骏 2018/10/30
		if (filter == null) {
			filter = new BasicDBObject();
			// condition.put("filter", filter);
		}
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		// if (sort == null) {
		// sort = new BasicDBObject();
		// condition.put("sort", sort);
		// }

		filter.put("pmId", userid);
		return query(skip, limit, filter, sort);
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
		return iterateParticipatedProject(condition, userId).into(new ArrayList<>());
	}

	@Override
	public List<Document> listParticipatedProjectsCard(BasicDBObject condition, String userId) {
		return iterateParticipatedProject(condition, userId).map(ProjectRenderer::render).into(new ArrayList<>());
	}

	private AggregateIterable<Project> iterateParticipatedProject(BasicDBObject condition, String userId) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");

		List<Bson> pipeline = new ArrayList<Bson>();
		// 杨骏 2018/11/1
		pipeline.addAll(new JQ("查询-项目-参与者").set("userId", userId).array());

		appendQueryPipeline(skip, limit, filter, sort, pipeline);

		AggregateIterable<Project> iterable = c("obs").aggregate(pipeline, Project.class);
		return iterable;
	}

	@Override
	public long countParticipatedProjects(BasicDBObject filter, String userId) {
		List<Bson> pipeline = new ArrayList<Bson>();
		// 杨骏 2018/11/1
		pipeline.addAll(new JQ("查询-项目-参与者").set("userId", userId).array());
		if (filter != null)
			pipeline.add(new Document("$match", filter));

		return c("obs").aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public List<Project> listParticipatedProjectsInDaily(BasicDBObject condition, String userId) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return listParticipatedProjects(condition, userId, cal.getTime());
	}

	@Override
	public long countParticipatedProjectsInDaily(BasicDBObject filter, String userId) {
		Date startWorkFinish = new Date();
		return countParticipatedProjects(filter, userId, startWorkFinish);
	}

	@Override
	public List<Project> listParticipatedProjectsInWeekly(BasicDBObject condition, String userId) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.setFirstDayOfWeek(Calendar.SUNDAY);
		int i = cal.get(Calendar.DAY_OF_WEEK);

		cal.add(Calendar.DAY_OF_MONTH, cal.getFirstDayOfWeek() - i);
		return listParticipatedProjects(condition, userId, cal.getTime());
	}

	@Override
	public long countParticipatedProjectsInWeekly(BasicDBObject filter, String userId) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.setFirstDayOfWeek(Calendar.SUNDAY);
		int i = cal.get(Calendar.DAY_OF_WEEK);

		cal.add(Calendar.DAY_OF_MONTH, cal.getFirstDayOfWeek() - i);
		return countParticipatedProjects(filter, userId, cal.getTime());
	}

	@Override
	public List<Project> listParticipatedProjectsInMonthly(BasicDBObject condition, String userId) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return listParticipatedProjects(condition, userId, cal.getTime());
	}

	@Override
	public long countParticipatedProjectsInMonthly(BasicDBObject filter, String userId) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return countParticipatedProjects(filter, userId, cal.getTime());
	}

	private List<Project> listParticipatedProjects(BasicDBObject condition, String userId, Date startWorkFinish) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");

		List<ObjectId> project_ids = c("work")
				.distinct("project_id",
						new Document("summary", false)
								.append("actualStart",
										new Document("$ne", null))
								.append("$and", Arrays.asList(
										new Document("$or",
												Arrays.asList(new Document("actualFinish", null),
														new Document("actualFinish", new Document("$gte", startWorkFinish)))),
										new Document("$or",
												Arrays.asList(new Document("chargerId", userId), new Document("assignerId", userId))))),
						ObjectId.class)
				.into(new ArrayList<ObjectId>());

		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("_id", new Document("$in", project_ids)).append("status",
				new Document("$in", Arrays.asList(ProjectStatus.Processing, ProjectStatus.Closing)))));

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendOrgFullName(pipeline, "impUnit_id", "impUnitOrgFullName");

		appendUserInfo(pipeline, "pmId", "pmInfo");

		ArrayList<Project> into = c("project").aggregate(pipeline, Project.class).into(new ArrayList<Project>());
		return into;
	}

	private long countParticipatedProjects(BasicDBObject filter, String userId, Date startWorkFinish) {
		List<Bson> pipeline = new ArrayList<Bson>();
		List<ObjectId> project_ids = c("work")
				.distinct("project_id",
						new Document("summary", false)
								.append("actualStart",
										new Document("$ne", null))
								.append("$and", Arrays.asList(
										new Document("$or",
												Arrays.asList(new Document("actualFinish", null),
														new Document("actualFinish", new Document("$gte", startWorkFinish)))),
										new Document("$or",
												Arrays.asList(new Document("chargerId", userId), new Document("assignerId", userId))))),
						ObjectId.class)
				.into(new ArrayList<ObjectId>());

		pipeline.add(Aggregates.match(new Document("_id", new Document("$in", project_ids))));
		if (filter != null)
			pipeline.add(new Document("$match", filter));

		return c("project").aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public long delete(ObjectId _id) {
		Project project = get(_id);
		if (!ProjectStatus.Created.equals(project.getStatus())) {
			throw new ServiceException("当前项目不允许删除，只能删除已创建状态的项目。");
		}
		// 获得所有的work
		List<ObjectId> workIds = c("work").distinct("_id", new Document("project_id", _id), ObjectId.class).into(new ArrayList<>());
		// 清除关联的resourcePlan
		c("resourcePlan").deleteMany(new Document("work_id", new Document("$in", workIds)));
		// 清除关联的resourceActual
		c("resourceActual").deleteMany(new Document("work_id", new Document("$in", workIds)));

		// 清除关联的workPackage
		c("workPackage").deleteMany(new Document("work_id", new Document("$in", workIds)));
		// 清除worklinks
		c("worklinks").deleteMany(new Document("project_id", _id));
		// 清除work
		c("work").deleteMany(new Document("project_id", _id));
		// 清除workspace
		c("workspace").deleteMany(new Document("project_id", _id));
		// 清除worklinksspace
		c("worklinksspace").deleteMany(new Document("project_id", _id));

		// 清除obs
		c("obs").deleteMany(
				new Document("$or", Arrays.asList(new Document("scope_id", new Document("$in", workIds)), new Document("scope_id", _id))));

		// 清除cbs
		List<ObjectId> cbsIds = c("cbs")
				.distinct("_id", new Document("$or", Arrays.asList(new Document("scope_id", new Document("$in", workIds)),
						new Document("scopeRoot", false).append("scope_id", _id))), ObjectId.class)
				.into(new ArrayList<>());
		c("cbs").deleteMany(new Document("_id", new Document("$in", cbsIds)));
		c("cbsPeriod").deleteMany(new Document("cbsItem_id", new Document("$in", cbsIds)));
		c("cbsSubject").deleteMany(new Document("cbsItem_id", new Document("$in", cbsIds)));

		// 清除folder
		c("folder").deleteMany(new Document("project_id", _id));

		// 清除baseline
		c("baseline").deleteMany(new Document("project_id", _id));
		c("baselineWork").deleteMany(new Document("project_id", _id));
		c("baselineWorkLinks").deleteMany(new Document("project_id", _id));

		// 清除projectChange
		c("projectChange").deleteMany(new Document("project_id", _id));

		// 清除rbs
		List<ObjectId> rbsIds = c("rbsItem").distinct("_id", new Document("project_id", _id), ObjectId.class).into(new ArrayList<>());
		c("rbsItem").deleteMany(new Document("project_id", _id));
		c("riskEffect").deleteMany(new Document("project_id", _id));
		c("riskResponse").deleteMany(new Document("rbsItem_id", new Document("$in", rbsIds)));

		// 清除salesItem
		c("salesItem").deleteMany(new Document("project_id", _id));

		// 清除stockholder
		c("stockholder").deleteMany(new Document("project_id", _id));

		// 清除workReport
		c("workReport").deleteMany(new Document("project_id", _id));
		c("workReportItem").deleteMany(new Document("work_id", new Document("$in", workIds)));
		c("workReportResourceActual").deleteMany(new Document("work_id", new Document("$in", workIds)));

		return delete(_id, Project.class);
	}

	@Override
	public Workspace getWorkspace(ObjectId _id) {
		BasicDBObject dbo = c("project").find(new BasicDBObject("_id", _id), BasicDBObject.class)
				.projection(new BasicDBObject("space_id", Boolean.TRUE).append("checkoutBy", Boolean.TRUE)).first();
		return Workspace.newInstance(_id, dbo.getObjectId("space_id"), dbo.getString("checkoutBy"));
	}

	@Override
	public List<Result> finishProject(Command com) {
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 状态检查
		Document project = c("project").find(new Document("_id", com._id)).first();
		if (!ProjectStatus.Processing.equals(project.getString("status"))) {// 必须是进行中才能收尾
			return Arrays.asList(Result.error("项目当前的状态不允许执行收尾操作"));
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 如果存在未完成的工作，警告
		if (ICommand.Finish_Project.equals(com.name)) {
			long count = c("work").countDocuments(new BasicDBObject("project_id", com._id).append("actualFinish", null));
			if (count > 0) {
				return Arrays.asList(Result.warning("项目存在一些尚未完成的工作。"));
			}
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 获得时间
		// yangjun 2018/10/31
		Document latest = c("work").find(new Document("parent_id", com._id)).projection(new Document("actualFinish", true))
				.sort(new Document("actualFinish", -1).append("_id", -1)).first();
		Date actualFinish = Optional.ofNullable(latest).map(l -> l.getDate("actualFinish")).orElse(new Date());

		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 修改项目状态
		c("project").updateOne(new Document("_id", com._id), new Document("$set", new Document("status", ProjectStatus.Closing)
				.append("progress", 1d).append("finishInfo", com.info()).append("actualFinish", actualFinish)));

		// 通知项目团队成员，项目收尾
		List<String> memberIds = getProjectMembers(com._id);
		sendMessage("项目收尾通知", "项目：" + project.getString("name") + " 已于 " + Formatter.getString(com.date) + " 进入收尾。", com.userId, memberIds,
				null);

		return new ArrayList<>();
	}

	@Override
	public List<Result> closeProject(Command com) {
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 状态检查
		Document project = c("project").find(new Document("_id", com._id)).first();
		if (ProjectStatus.Closed.equals(project.getString("status"))) {
			return Arrays.asList(Result.error("项目当前的状态不允许执行关闭操作"));
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 如果存在未完成的工作，警告
		if (ICommand.Finish_Project.equals(com.name)) {
			long count = c("work").countDocuments(new BasicDBObject("project_id", com._id).append("actualFinish", null));
			if (count > 0) {
				return Arrays.asList(Result.warning("项目存在一些尚未完成的工作，这些工作也将同时关闭。"));
			}
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 修改项目状态
		c("project").updateOne(new Document("_id", com._id),
				new Document("$set", new Document("status", ProjectStatus.Closed).append("closeInfo", com.info())));
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 修改工作状态
		// 未开始的工作，skipStart
		c("work").updateMany(new Document("project_id", com._id).append("actualStart", null), //
				new Document("$set", new Document("skipStart", true).append("actualStart", com.date)));
		// 未完成的工作，skipFinish
		c("work").updateMany(new Document("project_id", com._id).append("actualFinish", null), //
				new Document("$set", new Document("skipFinish", true).append("actualFinish", com.date)));

		// 通知项目团队成员，项目已经关闭
		List<String> memberIds = getProjectMembers(com._id);
		sendMessage("项目关闭通知",
				"项目：" + project.getString("name") + " 已于 " + new SimpleDateFormat(Formatter.DATE_FORMAT_DATE).format(com.date) + " 关闭。",
				com.userId, memberIds, null);
		return new ArrayList<>();
	}

	/**
	 * TODO 迁移到前台进行
	 * 
	 * @param _id
	 * @return
	 */
	@Deprecated
	private String generateWorkOrder(ObjectId _id) {
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
		Project project = get(_id);
		String catalog = project.getCatalog();
		ObjectId parentproject_id = project.getParentProject_id();
		ObjectId program_id = project.getProgram_id();
		ObjectId impunit_id = project.getImpUnit_id();

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
			String parentWorkOrder = c("project").distinct("workOrder", new Document("_id", parentproject_id), String.class).first();
			String[] workorders = parentWorkOrder.split("-");
			workOrder += "-" + workorders[1];
			int index = generateCode(Generator.DEFAULT_NAME, "projectno" + parentWorkOrder);
			workOrder += "-" + String.format("%02d", index);

		} else if (program_id != null) {
			String programWorkOrder = c("program").distinct("workOrder", new Document("_id", program_id), String.class).first();
			String[] workorders = programWorkOrder.split("-");
			workOrder += "-" + workorders[1];
			int index = generateCode(Generator.DEFAULT_NAME, "projectno" + programWorkOrder);
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
		List<Bson> pipeline = new JQ("查询-时间线")
				.set("match", new Document("manageLevel", new Document("$in", Arrays.asList("1", "2"))).append("project_id", _id))
				.set("limit", count).array();
		c("work").aggregate(pipeline).forEach((Document doc) -> {
			Object user = Optional.ofNullable(doc.get("userInfo")).orElse("");
			if (doc.get("date").equals(doc.get("actualFinish"))) {
				result.add(new News().setDate(doc.getDate("date")).setContent(user + " " + doc.get("name") + " 完成 "));
			} else if (doc.get("date").equals(doc.get("actualStart"))) {
				result.add(new News().setDate(doc.getDate("date")).setContent(user + " " + doc.get("name") + " 启动"));
			}
		});
		return result;
	}

	@Override
	public SalesItem insertSalesItem(SalesItem salesItem) {
		return insert(salesItem);
	}

	@Override
	public long deleteStockholder(ObjectId _id) {
		return delete(_id, Stockholder.class);
	}

	@Override
	public List<Baseline> listBaseline(BasicDBObject condition, ObjectId _id) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		if (sort == null) {
			sort = new BasicDBObject("_id", -1);
		}
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("project_id", _id);
		return query(skip, limit, filter, sort, Baseline.class);
	}

	@Override
	public long countBaseline(BasicDBObject filter, ObjectId _id) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("project_id", _id);

		return count(filter, Baseline.class);
	}

	@Override
	public Baseline createBaseline(Baseline baseline) {
		Baseline newBaseline = insert(baseline, Baseline.class);
		ObjectId project_id = baseline.getProject_id();
		// 获取要存储到基线的项目
		Document projectDoc = c("project").find(new Document("_id", project_id)).first();
		ObjectId newBaseline_id = newBaseline.get_id();
		projectDoc.append("projectName", projectDoc.get("name"));
		projectDoc.append("projectDescription", projectDoc.get("description"));
		projectDoc.remove("description");
		projectDoc.remove("name");
		projectDoc.remove("_id");
		projectDoc.remove("checkoutBy");
		projectDoc.remove("space_id");

		// 获取要存储到基线的工作
		Map<ObjectId, ObjectId> workIds = new HashMap<ObjectId, ObjectId>();
		List<Document> workDocs = new ArrayList<Document>();
		c("work").find(new Document("project_id", project_id)).sort(new Document("parent_id", 1)).forEach((Document doc) -> {
			ObjectId work_id = doc.getObjectId("_id");
			ObjectId newWork_id = new ObjectId();
			workIds.put(work_id, newWork_id);
			doc.append("old_id", work_id);
			doc.append("_id", newWork_id);

			ObjectId parent_id = doc.getObjectId("parent_id");
			if (parent_id != null) {
				ObjectId newParent_id = workIds.get(parent_id);
				doc.append("parent_id", newParent_id);
			}

			doc.append("baseline_id", newBaseline_id);

			doc.remove("checkoutBy");
			doc.remove("space_id");

			workDocs.add(doc);
		});

		// 获取要存储到基线的工作关联关系
		List<Document> worklinkDocs = new ArrayList<Document>();
		c("worklinks").find(new Document("project_id", project_id)).forEach((Document doc) -> {
			ObjectId target = doc.getObjectId("target");
			doc.append("target", workIds.get(target));
			ObjectId source = doc.getObjectId("source");
			doc.append("source", workIds.get(source));

			doc.append("baseline_id", newBaseline_id);

			doc.remove("_id");

			worklinkDocs.add(doc);
		});

		// 插入项目和工作数据
		c(Baseline.class).updateMany(new Document("_id", baseline.get_id()), new Document("$set", projectDoc));

		if (workDocs.size() > 0)
			c("baselineWork").insertMany(workDocs);

		if (worklinkDocs.size() > 0)
			c("baselineWorkLinks").insertMany(worklinkDocs);

		return get(newBaseline_id, Baseline.class);
	}

	@Override
	public long deleteBaseline(ObjectId _id) {
		c("baselineWork").deleteMany(new Document("baseline_id", _id));

		c("baselineWorkLinks").deleteMany(new Document("baseline_id", _id));

		return delete(_id, Baseline.class);
	}

	@Override
	public long updateBaseline(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, Baseline.class);
	}

	@Override
	public List<BaselineComparable> getBaselineComparable(List<ObjectId> projectIds) {
		// TODO JQ
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("project_id", new Document("$in", projectIds))));
		pipeline.add(Aggregates.sort(new Document("project_id", 1).append("index", 1)));
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		pipeline.add(Aggregates.addFields(
				Arrays.asList(new Field<String>("projectName", "$project.name"), new Field<String>("projectNumber", "$project.id"))));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));
		appendUserInfo(pipeline, "chargerId", "chargerInfo");
		appendUserInfo(pipeline, "assignerId", "assignerInfo");
		List<Work> works = c("work", Work.class).aggregate(pipeline).into(new ArrayList<Work>());

		pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("baseline_id", new Document("$in", projectIds))));
		pipeline.add(Aggregates.sort(new Document("baseline_id", 1).append("index", 1)));
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		pipeline.add(Aggregates.addFields(
				Arrays.asList(new Field<String>("projectName", "$project.name"), new Field<String>("projectNumber", "$project.id"))));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));
		appendUserInfo(pipeline, "chargerId", "chargerInfo");
		appendUserInfo(pipeline, "assignerId", "assignerInfo");
		List<Work> baselineWorks = c("baselineWork", Work.class).aggregate(pipeline).into(new ArrayList<Work>());

		List<BaselineComparable> result = new ArrayList<BaselineComparable>();
		ObjectId p = null;
		if (works.size() > 0) {
			for (Work work : works) {
				if (p == null) {
					p = work.getProject_id();
				}
				result.add(new BaselineComparable().setWork1(work));
			}
		}
		for (Work work : baselineWorks) {
			if (p == null) {
				p = work.getBaseline_id();
			}
			if (p.equals(work.getBaseline_id())) {
				result.add(new BaselineComparable().setWork1(work));
			} else {
				boolean add = true;
				for (BaselineComparable bc : result) {
					Work work1 = bc.getWork1();
					if (work1 != null) {
						ObjectId work1Old_id = work1.getOld_id();
						if (work1Old_id != null && work1Old_id.equals(work.getOld_id())) {
							bc.setWork2(work);
							add = false;
							break;
						} else if (work1.get_id().equals(work.getOld_id())) {
							bc.setWork2(work);
							add = false;
							break;
						} else if (work1.getFullName().equals(work.getFullName())) {
							bc.setWork2(work);
							add = false;
							break;
						}
					} else {
						Work work2 = bc.getWork2();
						if (work2 != null) {
							ObjectId work2Old_id = work2.getOld_id();
							if (work2Old_id != null && work2Old_id.equals(work.getOld_id())) {
								bc.setWork1(work);
								add = false;
								break;
							} else if (work2.get_id().equals(work.getOld_id())) {
								bc.setWork1(work);
								add = false;
								break;
							} else if (work2.getFullName().equals(work.getFullName())) {
								bc.setWork1(work);
								add = false;
								break;
							}
						}
					}
				}
				if (add) {
					result.add(new BaselineComparable().setWork2(work));
				}
			}
		}

		return result;
	}

	@Override
	public List<ProjectChange> listProjectChange(BasicDBObject condition, ObjectId _id) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("project_id", _id);

		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(filter));

		pipeline.add(Aggregates.sort(new BasicDBObject("_id", -1)));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo");
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");
		// yangjun 2018/10/31
		return c(ProjectChange.class).aggregate(pipeline).into(new ArrayList<ProjectChange>());
	}

	@Override
	public long countProjectChange(BasicDBObject filter, ObjectId _id) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("project_id", _id);

		return count(filter, ProjectChange.class);
	}

	@Override
	public ProjectChange createProjectChange(ProjectChange pc) {
		List<ProjectChangeTask> reviewer = pc.getReviewer();
		List<OBSItem> obsItems = new OBSServiceImpl().getScopeOBS(pc.getProject_id());
		List<ChangeProcess> changeProcesss = new CommonServiceImpl().createChangeProcessDataSet();
		for (ChangeProcess changeProcess : changeProcesss) {
			if (changeProcess.getProjectOBSId() != null) {
				for (OBSItem obsItem : obsItems) {
					// yangjun 2018/10/31
					if (Check.equals(obsItem.getRoleId(), changeProcess.getProjectOBSId())) {
						ProjectChangeTask pct = new ProjectChangeTask();
						pct.user = obsItem.getManagerId();
						pct.name = changeProcess.getTaskName();
						reviewer.add(pct);
					}
				}
			} else {
				ProjectChangeTask pct = new ProjectChangeTask();
				pct.name = changeProcess.getTaskName();
				reviewer.add(pct);
			}
		}

		ProjectChange newPC = insert(pc, ProjectChange.class);
		return listProjectChangeInfo(newPC.get_id()).get(0);
	}

	@Override
	public long deleteProjectChange(ObjectId _id) {
		return delete(_id, ProjectChange.class);
	}

	@Override
	public long updateProjectChange(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, ProjectChange.class);
	}

	@Override
	public ProjectChange getProjectChange(ObjectId _id) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new BasicDBObject("_id", _id)));

		pipeline.add(Aggregates.sort(new BasicDBObject("_id", -1)));

		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo");
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		return c(ProjectChange.class).aggregate(pipeline).first();
	}

	@Override
	public List<ProjectChange> listProjectChangeInfo(ObjectId _id) {
		List<Bson> pipeline = new ArrayList<Bson>();

		pipeline.addAll(new JQ("查询-项目变更").set("match", new Document("_id", _id)).array());

		appendUserInfo(pipeline, "applicant", "applicantInfo");
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		return c(ProjectChange.class).aggregate(pipeline).into(new ArrayList<ProjectChange>());
	}

	@Override
	public List<Result> submitProjectChange(List<ObjectId> projectChangeIds) {
		List<Result> result = submitProjectChangeCheck(projectChangeIds);
		if (!result.isEmpty()) {
			return result;
		}

		UpdateResult ur = c(ProjectChange.class).updateMany(new Document("_id", new Document("$in", projectChangeIds)),
				new Document("$set", new Document("submitDate", new Date()).append("status", ProjectChange.STATUS_SUBMIT)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足提交条件的变更申请。"));
			return result;
		}

		// 发送通知
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new Document("_id", new Document("$in", projectChangeIds))));
		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo");
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		c(ProjectChange.class).aggregate(pipeline).forEach((ProjectChange projectChange) -> {
			List<String> receivers = new ArrayList<String>();
			projectChange.getReviewer().forEach((ProjectChangeTask receiver) -> {
				receivers.add(receiver.user);
			});
			sendMessage("项目变更申请", "" + projectChange.getApplicantInfo() + " 发起了项目：" + projectChange.getProjectName() + " 的变更申请，请您进行审核。",
					projectChange.getApplicantId(), receivers, null);

			String pmId = c("project").distinct("pmId", new Document("_id", projectChange.getProject_id()), String.class).first();
			if (!receivers.contains(pmId))
				sendMessage("项目变更申请", "" + projectChange.getApplicantInfo() + " 发起了项目：" + projectChange.getProjectName() + " 的变更申请，",
						projectChange.getApplicantId(), receivers, null);

		});

		return result;
	}

	private List<Result> submitProjectChangeCheck(List<ObjectId> projectChangeIds) {
		List<Result> result = new ArrayList<Result>();
		long count = c(ProjectChange.class)
				.countDocuments(new Document("_id", new Document("$in", projectChangeIds)).append("reviewer.user", null));
		if (count > 0) {
			result.add(Result.submitProjectChangeError("缺少审核人员"));
		}
		return result;
	}

	@Override
	public List<Result> passProjectChange(ProjectChangeTask projectChangeTask) {
		List<Result> result = passProjectChangeCheck(projectChangeTask);
		if (!result.isEmpty()) {
			return result;
		}
		// yangjun 2018/10/31
		ProjectChange pc = getProjectChange(projectChangeTask.projectChange_id);
		List<ProjectChangeTask> reviewers = pc.getReviewer();
		List<BasicDBObject> reviewer = new ArrayList<BasicDBObject>();
		String status = ProjectChange.STATUS_PASS;
		for (ProjectChangeTask re : reviewers) {
			if (re.choice == null && !re.name.equals(projectChangeTask.name)) {
				status = ProjectChange.STATUS_SUBMIT;
			} else if (re.name.equals(projectChangeTask.name)) {
				re.user = projectChangeTask.user;
				re.choice = projectChangeTask.choice;
				re.date = projectChangeTask.date;
				re.comment = projectChangeTask.comment;
			}
			reviewer.add(getBson(re));
		}

		UpdateResult ur = c(ProjectChange.class).updateOne(new BasicDBObject("_id", projectChangeTask.projectChange_id),
				new BasicDBObject("$set", new BasicDBObject("reviewer", reviewer).append("status", status)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足确认条件的变更申请。"));
			return result;
		}

		// 发送变更批准通知
		sendMessage("项目变更申请已批准", "" + projectChangeTask.getUser() + " 批准了项目：" + pc.getProjectName() + " 的变更申请，", projectChangeTask.user,
				pc.getApplicantId(), null);
		if (ProjectChange.STATUS_PASS.equals(status)) {
			String pmId = c("project").distinct("pmId", new Document("_id", pc.getProject_id()), String.class).first();
			sendMessage("项目变更申请已通过", "项目：" + pc.getProjectName() + " 的变更申请已审核通过，", pc.getApplicantId(), pmId, null);
		}

		return result;
	}

	private List<Result> passProjectChangeCheck(ProjectChangeTask projectChangeTask) {
		return new ArrayList<Result>();
	}

	@Override
	public List<Result> confirmProjectChange(List<ObjectId> projectChangeIds, String userId) {
		List<Result> result = confirmProjectChangeCheck(projectChangeIds);
		if (!result.isEmpty()) {
			return result;
		}

		UpdateResult ur = c(ProjectChange.class).updateMany(new Document("_id", new Document("$in", projectChangeIds)), new Document("$set",
				new Document("verifyDate", new Date()).append("verify", userId).append("status", ProjectChange.STATUS_CONFIRM)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足确认条件的变更申请。"));
			return result;
		}
		// 发送通知
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new Document("_id", new Document("$in", projectChangeIds))));
		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo");
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		c(ProjectChange.class).aggregate(pipeline).forEach((ProjectChange pc) -> {
			sendMessage("项目变更申请已关闭", "项目：" + pc.getProjectName() + " 的变更已关闭，", userId, pc.getApplicantId(), null);
		});
		return result;
	}

	private List<Result> confirmProjectChangeCheck(List<ObjectId> projectChangeIds) {
		return new ArrayList<Result>();
	}

	public List<Result> cancelProjectChange(ProjectChangeTask projectChangeTask) {
		List<Result> result = cancelProjectChangeCheck(projectChangeTask);
		if (!result.isEmpty()) {
			return result;
		}
		ProjectChange pc = get(projectChangeTask.projectChange_id, ProjectChange.class);
		List<BasicDBObject> reviewer = new ArrayList<BasicDBObject>();
		List<ProjectChangeTask> reviewers = pc.getReviewer();
		for (ProjectChangeTask re : reviewers) {
			if (re.name.equals(projectChangeTask.name)) {
				re.user = projectChangeTask.user;
				re.choice = projectChangeTask.choice;
				re.date = projectChangeTask.date;
				re.comment = projectChangeTask.comment;
			}
			reviewer.add(getBson(re));
		}

		UpdateResult ur = c(ProjectChange.class).updateOne(new BasicDBObject("_id", projectChangeTask.projectChange_id),
				new BasicDBObject("$set", new BasicDBObject("reviewer", reviewer).append("status", ProjectChange.STATUS_CANCEL)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足取消条件的变更申请。"));
			return result;
		}
		String pmId = c("project").distinct("pmId", new Document("_id", pc.getProject_id()), String.class).first();
		sendMessage("项目变更申请已否决", "" + projectChangeTask.getUser() + " 否决了项目：" + pc.getProjectName() + " 的变更申请，", projectChangeTask.user,
				Arrays.asList(pmId, pc.getApplicantId()), null);

		return result;
	}

	private List<Result> cancelProjectChangeCheck(ProjectChangeTask projectChangeTask) {
		return new ArrayList<Result>();
	}

	@Override
	public long updateProjectChange(ProjectChangeTask projectChangeTask, ObjectId projectChange_id) {
		ProjectChange pc = get(projectChange_id, ProjectChange.class);
		List<ProjectChangeTask> reviewers = pc.getReviewer();
		List<BasicDBObject> reviewer = new ArrayList<BasicDBObject>();
		for (ProjectChangeTask re : reviewers) {
			if (re.name.equals(projectChangeTask.name)) {
				re.user = projectChangeTask.user;
			}
			reviewer.add(getBson(re));
		}

		UpdateResult ur = c(ProjectChange.class).updateOne(new BasicDBObject("_id", projectChange_id),
				new BasicDBObject("$set", new BasicDBObject("reviewer", reviewer)));

		return ur.getModifiedCount();
	}

	@Override
	public long checkCreateProjectChange(ObjectId _id) {
		return c("projectChange").countDocuments(new Document("project_id", _id).append("status",
				new Document("$nin", Arrays.asList(ProjectChange.STATUS_CANCEL, ProjectChange.STATUS_CONFIRM))));
	}

	@Override
	public long countReviewerProjectChange(BasicDBObject filter, String userId) {
		List<Bson> pipeline = (List<Bson>) new JQ("查询-项目变更-待审批").set("userId", userId).set("status", ProjectChange.STATUS_SUBMIT).array();

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		return c("projectChange").aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public List<ProjectChange> listReviewerProjectChange(BasicDBObject condition, String userId) {
		return listReviewerPC(condition, userId).into(new ArrayList<ProjectChange>());
	}

	@Override
	public List<Document> listReviewerProjectChangeCard(BasicDBObject condition, String userId) {
		return listReviewerPC(condition, userId).map(ProjectChangeRenderer::render).into(new ArrayList<>());
	}

	private AggregateIterable<ProjectChange> listReviewerPC(BasicDBObject condition, String userId) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
		}

		List<Bson> pipeline = (List<Bson>) new JQ("查询-项目变更-待审批").set("userId", userId).set("status", ProjectChange.STATUS_SUBMIT).array();

		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo");
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		pipeline.add(Aggregates.match(filter));

		pipeline.add(Aggregates.sort(new BasicDBObject("_id", -1)));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		AggregateIterable<ProjectChange> aggregate = c(ProjectChange.class).aggregate(pipeline);
		return aggregate;
	}

	@Override
	@Deprecated
	public List<Project> listAdministratedProjects(BasicDBObject condition, String managerId) {
		List<ObjectId> projectIds = getAdministratedProjects(managerId);
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("_id", new BasicDBObject("$in", projectIds));
		return list(condition);
	}

	@Override
	public List<Document> listAdministratedProjectsCard(BasicDBObject condition, String managerId) {
		List<ObjectId> projectIds = getAdministratedProjects(managerId);
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("_id", new BasicDBObject("$in", projectIds));

		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");

		List<Bson> pipeline = appendQueryPipeline(skip, limit, filter, sort, new ArrayList<>());
		return c(Project.class).aggregate(pipeline).map(ProjectRenderer::render).into(new ArrayList<>());
	}

	@Override
	public long countAdministratedProjects(BasicDBObject filter, String managerId) {
		List<ObjectId> projectIds = getAdministratedProjects(managerId);
		if (filter == null) {
			// filter = new BasicDBObject();
			return projectIds.size();
		}
		filter.append("_id", new BasicDBObject("$in", projectIds));
		return count(filter);
	}

	/**
	 * 获取所有项目，当用户具有项目总监、供应链管理、财务管理、制造管理角色时，显示全部项目信息，否则只显示当前用户作为项目PMO团队成员的项目
	 */
	@Override
	public List<Project> listAllProjects(BasicDBObject condition, String userid) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		if (sort == null) {
			// yangjun 2018/10/31
			sort = new BasicDBObject("creationInfo.date", -1).append("_id", -1);
		}

		List<Bson> pipeline = new ArrayList<Bson>();
		// 当前用户具有项目总监权限时显示全部，不显示全部时，加载PMO团队查询
		if (!checkUserRoles(userid, Role.SYS_ROLE_PD_ID)) {
			appendQueryUserInProjectPMO(pipeline, userid, "$_id");
		}

		appendQueryPipeline(skip, limit, filter, sort, pipeline);

		return c(Project.class).aggregate(pipeline).into(new ArrayList<Project>());
	}

	/**
	 * 获取所有项目数，当用户具有项目总监、供应链管理、财务管理、制造管理角色时，显示全部项目数，否则只显示当前用户作为项目PMO团队成员的项目数
	 */
	@Override
	public long countAllProjects(BasicDBObject filter, String userid) {
		// 如果当前用户具有项目总监的权限时，则全部项目总数
		if (checkUserRoles(userid, Role.SYS_ROLE_PD_ID)) {
			return count(filter);
		}
		// 不显示全部时，只返回用户在项目PMO团队中的项目数
		List<Bson> pipeline = new ArrayList<Bson>();
		appendQueryUserInProjectPMO(pipeline, userid, "$_id");
		return c(Project.class).aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public Integer schedule(ObjectId _id) {
		return super.schedule(_id);
	}

	@Override
	public List<ProjectScheduleInfo> listManagedProjectSchedules(BasicDBObject condition, String userid) {
		return listAllProjects(condition, userid).stream().map(project -> new ProjectScheduleInfo().setProject(project))
				.collect(Collectors.toList());
	}

	@Override
	public long countManagedProjectSchedules(BasicDBObject filter, String userid) {
		return countAllProjects(filter, userid);
	}

	@Override
	public List<ProjectScheduleInfo> listSubManagedProjectSchedules(ProjectScheduleInfo parent) {
		WorkServiceImpl workService = new WorkServiceImpl();
		if (parent.typeEquals(Project.class)) {
			return workService.listProjectRootTask(parent.get_id()).stream().map(work -> new ProjectScheduleInfo().setWork(work))
					.collect(Collectors.toList());
		} else if (parent.typeEquals(Work.class)) {
			return workService.listChildren(parent.get_id()).stream().map(work -> new ProjectScheduleInfo().setWork(work))
					.collect(Collectors.toList());
		} else {
			// TODO 其他类型
			return new ArrayList<ProjectScheduleInfo>();
		}
	}

	@Override
	public long countSubManagedProjectSchedules(ProjectScheduleInfo parent) {
		WorkServiceImpl workService = new WorkServiceImpl();
		if (parent.typeEquals(Project.class)) {
			return workService.countProjectRootTask(parent.get_id());
		} else if (parent.typeEquals(Work.class)) {
			return workService.countChildren(parent.get_id());
		} else {
			// TODO 其他类型
			return 0l;
		}
	}

}
