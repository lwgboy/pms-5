package com.bizvisionsoft.serviceimpl;

import java.math.BigDecimal;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.mongocodex.tools.BsonTools;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.common.Domain;
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
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.renderer.ProjectChangeRenderer;
import com.bizvisionsoft.serviceimpl.renderer.ProjectRenderer;
import com.mongodb.BasicDBObject;
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
	public Project insert(Project input, String domain) {
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
				Document doc = c("program", domain).find(new BasicDBObject("_id", program_id))
						.projection(new BasicDBObject("obs_id", true).append("cbs_id", true)).first();
				obsParent_id = Optional.ofNullable(doc).map(d -> d.getObjectId("obs_id")).orElse(null);
				cbsParent_id = Optional.ofNullable(doc).map(d -> d.getObjectId("cbs_id")).orElse(null);

			}
			/////////////////////////////////////////////////////////////////////////////
			// 0. 创建项目
			project = insert(input.setOBS_id(obsRoot_id).setCBS_id(cbsRoot_id), Project.class, domain);
			/////////////////////////////////////////////////////////////////////////////
			// 1. 项目团队初始化
			OBSItem obsRoot = new OBSItem()// 创建本项目的OBS根节点
					.setDomain(domain).set_id(obsRoot_id)// 设置_id与项目关联
					.setScope_id(project.get_id())// 设置scope_id表明该组织节点是该项目的组织
					.setParent_id(obsParent_id)// 设置上级的id
					.setName(project.getName() + "项目组")// 设置该组织节点的默认名称
					.setRoleId(OBSItem.ID_PM)// 设置该组织节点的角色id
					.setRoleName(OBSItem.NAME_PM)// 设置该组织节点的名称
					.setManagerId(project.getPmId()) // 设置该组织节点的角色对应的人
					.setScopeRoot(true);// 区分这个节点是范围内的根节点
			insert(obsRoot, OBSItem.class, domain);

			/////////////////////////////////////////////////////////////////////////////
			// 2. 财务科目初始化
			// 创建根
			CBSItem cbsRoot = CBSItem.getInstance(project, true, domain);//
			cbsRoot.set_id(cbsRoot_id);//
			cbsRoot.setParent_id(cbsParent_id);//
			cbsRoot.setName(project.getName());
			insert(cbsRoot, CBSItem.class, domain);

		} else {
			project = insert(input, Project.class, domain);
		}

		/////////////////////////////////////////////////////////////////////////////
		// 项目设置
		// 添加默认全局项目设置
		ObjectId scope_id = project.getScope_id();
		List<Document> list = PROJECT_SETTING_NAMES.stream().map(name -> {
			Document setting = Optional.ofNullable(getSystemSetting(name, domain)).orElse(new Document());
			setting.put("name", name + "@" + scope_id.toString());
			setting.remove("_id");
			return setting;
		}).collect(Collectors.toList());
		c("setting", domain).insertMany(list);

		return get(project.get_id(), domain);
	}

	@Override
	public Project get(ObjectId _id, String domain) {
		List<Project> ds = list(new BasicDBObject("filter", new BasicDBObject("_id", _id)), domain);
		if (ds.size() == 0) {
			throw new ServiceException("没有_id为" + _id + "的项目。");
		}
		return ds.get(0);
	}

	@Override
	public long count(BasicDBObject filter, String domain) {
		return count(filter, Project.class, domain);
	}

	@Override
	public List<Project> list(BasicDBObject condition, String domain) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		return query(skip, limit, filter, sort, domain);
	}

	private List<Project> query(Integer skip, Integer limit, BasicDBObject filter, BasicDBObject sort, String domain) {
		List<Bson> pipeline = appendQueryPipeline(skip, limit, filter, sort, new ArrayList<>(), domain);
		return c(Project.class, domain).aggregate(pipeline).into(new ArrayList<Project>());
	}

	private List<Bson> appendQueryPipeline(Integer skip, Integer limit, BasicDBObject filter, BasicDBObject sort, List<Bson> pipeline,
			String domain) {
		// 1. 承担组织
		appendOrgFullName(pipeline, "impUnit_id", "impUnitOrgFullName");

		appendUserInfo(pipeline, "pmId", "pmInfo", domain);

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
		pipeline.addAll(Domain.getJQ(domain, "追加-项目-工期与工时").array());

		pipeline.addAll(Domain.getJQ(domain, "追加-项目-SAR").array());

		return pipeline;
	}

	@Override
	public List<Date> getPlanDateRange(ObjectId _id, String domain) {
		Project data = c(Project.class, domain).find(new BasicDBObject("_id", _id))
				.projection(new BasicDBObject().append("planStart", 1).append("planFinish", 1)).first();
		ArrayList<Date> result = new ArrayList<Date>();
		result.add(data.getPlanStart());
		result.add(data.getPlanFinish());
		return result;
	}

	@Override
	public long update(BasicDBObject fu, String domain) {
		return update(fu, "project", domain);
	}

	/**
	 * 使用update来修改项目
	 */
	@Override
	@Deprecated
	public void updateProjectId(ObjectId _id, String id, String domain) {
		Document cond = new Document("_id", _id);
		Project project = c(Project.class, domain).find(cond).first();

		if (!ProjectStatus.Created.equals(project.getStatus())) {
			throw new ServiceException("只能在项目启动前设置编号");
		}

		try {
			String workOrder = generateWorkOrder(_id, domain);
			c("project", domain).updateOne(cond, new Document("$set", new Document("id", id).append("workOrder", workOrder)));
		} catch (Exception e) {
			throw handleMongoException(e, "项目编号" + id);
		}
	}

	@Override
	public void approveProject(Command com, String domain) {
		Document cond = new Document("_id", com._id);
		Project project = c(Project.class, domain).find(cond).first();

		if (!ProjectStatus.Created.equals(project.getStatus())) {
			throw new ServiceException("已启动的项目无需再次批准");
		}

		if (Boolean.TRUE.equals(project.getStartApproved())) {
			throw new ServiceException("已批准的项目无需再次批准");
		}

		c("project", domain).updateOne(cond, new Document("$set", new Document("startApproved", true).append("approveInfo", com.info())));
	}

	@Override
	public List<Result> startProject(Command com, String domain) {
		/////////////////////////////////////////////////////////////////////////////
		// 检查项目启动
		List<Result> result = startProjectCheck(com._id, com.userId, domain);
		if (result.stream().anyMatch(r -> Result.TYPE_ERROR == r.type// 错误的必须返回
				|| (Result.TYPE_WARNING == r.type && ICommand.Start_Project.equals(com.name)))) {// 不忽略警告的必须返回
			return result;
		}
		/////////////////////////////////////////////////////////////////////////////
		// 修改项目状态
		c("project", domain).updateOne(new Document("_id", com._id),
				new Document("$set", new Document("status", ProjectStatus.Processing).append("startInfo", com.info())));

		List<ObjectId> ids;

		boolean stageEnabled = getValue("project", "stageEnable", com._id, Boolean.class, domain);

		if (stageEnabled) {
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 下达阶段计划
			ids = c("work", domain).distinct("_id", new Document("project_id", com._id)// 本项目中的所有阶段
					.append("stage", true), ObjectId.class).into(new ArrayList<ObjectId>());
		} else {
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 下达非阶段管理的项目的工作计划
			ids = c("work", domain).distinct("_id", new Document("project_id", com._id)// 本项目中的所有工作
					, ObjectId.class).into(new ArrayList<ObjectId>());
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 更新下达计划的工作和项目，记录下达信息
		if (!ids.isEmpty()) {
			Document distributeInfo = com.info();
			c("work", domain).updateMany(new Document("_id", new Document("$in", ids)), //
					new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
			c("project", domain).updateOne(new Document("_id", com._id), //
					new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
		}

		/////////////////////////////////////////////////////////////////////////////
		// 通知项目团队成员，项目已经启动
		List<String> memberIds = getProjectMembers(com._id, domain);
		sendMessage("项目启动通知", "项目：" + getName("project", com._id, domain) + " 已于 " + Message.format(com.date) + " 启动。", com.userId,
				memberIds, null, domain);
		return new ArrayList<>();
	}

	private List<String> getProjectMembers(ObjectId _id, String domain) {
		List<ObjectId> parentIds = c("obs", domain).distinct("_id", new BasicDBObject("scope_id", _id), ObjectId.class)
				.into(new ArrayList<>());
		List<ObjectId> ids = getDesentItems(parentIds, "obs", "parent_id", domain);
		ArrayList<String> memberIds = c("obs", domain).distinct("managerId",
				new BasicDBObject("_id", new BasicDBObject("$in", ids)).append("managerId", new BasicDBObject("$ne", null)), String.class)
				.into(new ArrayList<>());
		return memberIds;
	}

	/**
	 * 项目预算
	 */
	private static String START_SETTING_FIELD_BUDGET = "budget";
	/**
	 * 项目资源
	 */
	private static String START_SETTING_FIELD_RESOURCE = "resource";
	/**
	 * 项目团队
	 */
	private static String START_SETTING_FIELD_OBS = "obs";
	/**
	 * 项目风险
	 */
	private static String START_SETTING_FIELD_RBS = "rbs";
	/**
	 * 项目计划
	 */
	private static String START_SETTING_FIELD_PLAN = "plan";
	/**
	 * 一级管理节点
	 */
	private static String START_SETTING_FIELD_CHARGER_L1 = "asgnL1";
	/**
	 * 二级管理节点
	 */
	private static String START_SETTING_FIELD_CHARGER_L2 = "asgnL2";
	/**
	 * 三级管理节点
	 */
	private static String START_SETTING_FIELD_CHARGER_L3 = "asgnL3";
	/**
	 * 所有工作
	 */
	private static String START_SETTING_FIELD_CHARGER_ALL = "asgnAll";
	/**
	 * 启动批准
	 */
	private static String START_SETTING_FIELD_APPROVEONSTART = "appvOnStart";

	private List<Result> startProjectCheck(ObjectId _id, String executeBy, String domain) {
		//////////////////////////////////////////////////////////////////////
		// 须检查的信息
		// 1. 检查是否创建了第一层的WBS，并有计划，如果没有，提示警告
		// 2. 检查组织结构是否完成，如果只有根，警告
		// 3. 检查第一层的WBS是否指定了必要的角色，如果没有负责人角色，提示警告。
		// 4. 缺少干系人，警告
		// 5. 没有做预算，警告
		// 6. 预算没做完，警告
		// 7. 预算没有分配，警告
		ArrayList<Result> results = new ArrayList<Result>();

		Project project = get(_id, domain);
		// 获取项目启动设置
		Document systemSetting = getScopeSetting(_id, START_SETTING_NAME, domain);
		// 从项目启动设置中获取项目预算配置

		/////////////////////////////////////////////////////////////////////////////////////////
		// 【1】 预算 ，从项目启动设置中获取项目预算配置
		Object setting;
		setting = getSettingValue(systemSetting, START_SETTING_FIELD_BUDGET, "警告");
		// 设置为忽略时，不进行检查，
		if (!"忽略".equals(setting)) {
			// 根据项目的cbs_id获取当前项目的所有CBS节点Id。
			List<ObjectId> cbsIds = new ArrayList<>();
			lookupDesentItems(Arrays.asList(project.getCBS_id()), "cbs", domain, "parent_id", true).forEach((Document d) -> {
				cbsIds.add(d.getObjectId("_id"));
			});
			// 如果没有编制cbsItem的预算和科目预算，则进行提示
			long l = c("cbsPeriod", domain).countDocuments(new Document("cbsItem_id", new Document("$in", cbsIds)));
			if (l == 0) {
				l = c("cbsSubject", domain).countDocuments(new Document("cbsItem_id", new Document("$in", cbsIds)));
				if (l == 0)
					// 如果设置为警告，则返回警告；如果设置为要求，则返回错误。
					if ("警告".equals(setting))
						results.add(Result.warning("项目尚未编制预算。"));
					else if ("要求".equals(setting))
						results.add(Result.error("项目尚未编制预算。"));

			}
		}

		/////////////////////////////////////////////////////////////////////////////////////////
		// 【2】 团队，获取项目团队设置
		setting = getSettingValue(systemSetting, START_SETTING_FIELD_OBS, "警告");
		// 设置为忽略时，不进行检查，
		if (!"忽略".equals(setting)) {
			// 获取项目团队中非根节点，并且该节点没有担任者和成员的记录数，记录数为0时，则添加返回信息。
			long l = c("obs", domain).countDocuments(
					new Document("scope_id", _id).append("scopeRoot", false).append("managerId", null).append("member", null));
			if (l > 0)
				// 如果设置为警告，则返回警告；如果设置为要求，则返回错误。
				if ("警告".equals(setting))
					results.add(Result.warning("项目团队尚未组建完成。"));
				else if ("要求".equals(setting))
					results.add(Result.error("项目团队尚未组建完成。"));
		}

		/////////////////////////////////////////////////////////////////////////////////////////
		// 【3】 风险 获取项目风险设置
		setting = getSettingValue(systemSetting, START_SETTING_FIELD_RBS, "警告");
		// 设置为忽略时，不进行检查，
		if (!"忽略".equals(setting)) {
			// 获取项目风险项中的记录数，记录数为0时，则添加返回信息。
			long l = c("rbsItem", domain).countDocuments(new Document("project_id", _id).append("parent_id", null));
			if (l == 0)
				// 如果设置为警告，则返回警告；如果设置为要求，则返回错误。
				if ("警告".equals(setting))
					results.add(Result.warning("项目尚未创建风险项。"));
				else if ("要求".equals(setting))
					results.add(Result.error("项目尚未创建风险项。"));
		}

		/////////////////////////////////////////////////////////////////////////////////////////
		// 【4】 资源 获取项目资源设置
		setting = getSettingValue(systemSetting, START_SETTING_FIELD_RESOURCE, "警告");
		// 设置为忽略时，不进行检查，
		if (!"忽略".equals(setting)) {
			// 获得所有非里程碑的叶子节点的id
			List<ObjectId> workIds = c("work", domain)
					.distinct("_id", new Document("project_id", _id).append("milestone", false).append("summary", false), ObjectId.class)
					.into(new ArrayList<>());
			// 如果没有进度计划，不进行资源检查
			if (workIds.size() > 0) {
				// 获取填写了资源计划的工作id
				List<ObjectId> resourceWorkIds = c("resourcePlan", domain)
						.distinct("work_id", new Document("work_id", new Document("$in", workIds)), ObjectId.class).into(new ArrayList<>());
				String message = "";
				if (resourceWorkIds.size() == 0) {
					message = "项目尚未编制资源计划";
				} else {
					workIds.removeAll(resourceWorkIds);
					if (workIds.size() > 0) {
						message = "部分工作未指定资源：<br>" + Formatter.getString(
								c("work", domain).distinct("fullName", new Document("_id", new Document("$in", workIds)), String.class)
										.into(new ArrayList<>()));
					}
				}
				if (!message.isEmpty() && "警告".equals(setting))
					results.add(Result.warning(message));
				else if (!message.isEmpty() && "要求".equals(setting))
					results.add(Result.error(message));
			}

		}
		// 获取项目计划设置
		setting = getSettingValue(systemSetting, START_SETTING_FIELD_PLAN, "警告");
		// 设置为忽略时，不进行检查，
		if (!"忽略".equals(setting)) {
			// 获取项目进度计划中的任务记录数，记录数为0时，则添加返回信息。
			long l = c("work", domain).countDocuments(new Document("project_id", _id).append("parent_id", null));
			if (l == 0)
				// 如果设置为警告，则返回警告；如果设置为要求，则返回错误。
				if ("警告".equals(setting))
					results.add(Result.warning("项目尚未创建进度计划。"));
				else if ("要求".equals(setting))
					results.add(Result.error("项目尚未创建进度计划。"));

			// 获取非监控节点设置
			setting = getSettingValue(systemSetting, START_SETTING_FIELD_CHARGER_ALL, "警告");

			// 获取未设置负责人和参与者的节点名称
			List<Document> work = c("work", domain)
					.find(new Document("project_id", _id).append("milestone", false).append("assignerId", null).append("chargerId", null))
					.into(new ArrayList<>());
			if ("警告".equals(setting) && work.size() > 0) {
				results.add(Result.warning(Formatter.getString(work.stream().map(d -> d.getString("fullName")).collect(Collectors.toList()))
						+ " 没有指定负责人和指派者."));
			} else if ("要求".equals(setting) && work.size() > 0) {
				results.add(Result.error(Formatter.getString(work.stream().map(d -> d.getString("fullName")).collect(Collectors.toList()))
						+ " 没有指定负责人和指派者."));
			} else if ("忽略".equals(setting) && work.size() > 0) {
				Map<String, String> manageLevels = new HashMap<String, String>();
				List<String> warning = new ArrayList<String>();
				List<String> error = new ArrayList<String>();

				// 获取一级监控节点设置
				setting = getSettingValue(systemSetting, START_SETTING_FIELD_CHARGER_L1, "警告");

				if (!"忽略".equals(setting))
					manageLevels.put("1", setting.toString());

				// 获取二级监控节点设置
				setting = getSettingValue(systemSetting, START_SETTING_FIELD_CHARGER_L2, "警告");

				if (!"忽略".equals(setting))
					manageLevels.put("2", setting.toString());

				// 获取三级监控节点设置
				setting = getSettingValue(systemSetting, START_SETTING_FIELD_CHARGER_L3, "警告");

				if (!"忽略".equals(setting))
					manageLevels.put("3", setting.toString());

				work.forEach((Document d) -> {
					// 根据类型获取节点设置，并将其添加到相应的集合中。
					if ("警告".equals(manageLevels.get(d.getString("manageLevel"))))
						warning.add(d.getString("fullName"));
					else if ("要求".equals(manageLevels.get(d.getString("manageLevel"))))
						error.add(d.getString("fullName"));

				});
				// 添加警告提示
				if (warning.size() > 0)
					results.add(Result.warning(Formatter.getString(warning) + "，没有指定负责人和指派者."));
				// 添加错误提示
				if (error.size() > 0)
					results.add(Result.error(Formatter.getString(error) + "，没有指定负责人和指派者."));
			}

		}

		// 获取启动批准设置
		setting = getSettingValue(systemSetting, START_SETTING_FIELD_APPROVEONSTART, false);

		// 如果需要批准启动，并且项目没有批准启动，则返回错误
		if (Boolean.TRUE.equals(setting) && !Boolean.TRUE.equals(project.getStartApproved())) {
			results.add(Result.error("项目尚未获得启动批准。"));
		}
		return results;
	}

	public List<Result> distributeProjectPlan(Command com, String domain) {
		final List<Message> msg = new ArrayList<>();
		final Set<ObjectId> ids = new HashSet<>();

		Project project = get(com._id, Project.class, domain);
		final String projectName = project.getName();
		boolean stageEnabled = project.isStageEnable();

		if (stageEnabled) {
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 下达阶段计划(阶段 已创建)
			c("work", domain).find(//
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
			c("work", domain)
					.aggregate(Domain.getJQ(domain, "查询-工作-阶段需下达的工作计划").set("project_id", com._id).set("match", new Document()).array())
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
			c("work", domain).find(//
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
		c("work", domain).updateMany(new Document("_id", new Document("$in", new ArrayList<>(ids))), //
				new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
		c("project", domain).updateOne(new Document("_id", com._id), //
				new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 发出消息通知
		sendMessages(msg, domain);

		return new ArrayList<>();
	}

	@Override
	public List<Work> listStage(ObjectId _id, String domain) {
		return new WorkServiceImpl().query(null, null, new BasicDBObject("project_id", _id).append("stage", true),
				new BasicDBObject("wbsCode", 1), Work.class, domain);
	}

	@Override
	public long countStage(ObjectId _id, String domain) {
		return c("work", domain).countDocuments(new BasicDBObject("project_id", _id).append("stage", true));
	}

	@Override
	public List<Work> listMyStage(String userId, String domain) {
		return new WorkServiceImpl().createTaskDataSet(
				new BasicDBObject("$or", Arrays.asList(new BasicDBObject("chargerId", userId), new BasicDBObject("assignerId", userId)))
						.append("stage", true).append("status", new BasicDBObject("$in",
								Arrays.asList(ProjectStatus.Created, ProjectStatus.Processing, ProjectStatus.Closing))),
				domain);
	}

	@Override
	public long countMyStage(String userId, String domain) {
		return count(
				new BasicDBObject("$or", Arrays.asList(new BasicDBObject("chargerId", userId), new BasicDBObject("assignerId", userId)))
						.append("stage", true),
				"work");
	}

	@Override
	public List<Stockholder> getStockholders(BasicDBObject condition, ObjectId _id, String domain) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("project_id", _id);
		return createDataSet(condition, Stockholder.class, domain);
	}

	@Override
	public long countStockholders(BasicDBObject filter, ObjectId _id, String domain) {
		filter.append("project_id", _id);
		return count(filter, Stockholder.class, domain);
	}

	@Override
	public Stockholder insertStockholder(Stockholder c, String domain) {
		return insert(c, Stockholder.class, domain);
	}

	@Override
	public long updateStockholder(BasicDBObject fu, String domain) {
		return update(fu, Stockholder.class, domain);
	}

	@Override
	public List<Project> listManagedProjects(BasicDBObject condition, String userid, String domain) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		// 杨骏 2018/10/30
		if (filter == null) {
			filter = new BasicDBObject();
			// condition.put("filter", filter);
		}
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		// if (sort == null){
		// sort = new BasicDBObject();
		// condition.put("sort", sort);
		// }

		filter.put("pmId", userid);
		return query(skip, limit, filter, sort, domain);
	}

	@Override
	public long countManagedProjects(BasicDBObject filter, String userid, String domain) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.put("pmId", userid);
		return count(filter, Project.class, domain);
	}

	@Override
	public List<Project> listParticipatedProjects(BasicDBObject condition, String userId, String domain) {
		return iterateParticipatedProject(condition, userId, domain).into(new ArrayList<>());
	}

	@Override
	public List<Document> listParticipatedProjectsCard(BasicDBObject condition, String userId, String domain) {
		return iterateParticipatedProject(condition, userId, domain).map(d -> ProjectRenderer.render(d, domain)).into(new ArrayList<>());
	}

	private AggregateIterable<Project> iterateParticipatedProject(BasicDBObject condition, String userId, String domain) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");

		List<Bson> pipeline = new ArrayList<Bson>();
		// 杨骏 2018/11/1
		pipeline.addAll(Domain.getJQ(domain, "查询-项目-参与者").set("userId", userId).array());

		appendQueryPipeline(skip, limit, filter, sort, pipeline, domain);

		AggregateIterable<Project> iterable = c("obs", domain).aggregate(pipeline, Project.class);
		return iterable;
	}

	@Override
	public long countParticipatedProjects(BasicDBObject filter, String userId, String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();
		// 杨骏 2018/11/1
		pipeline.addAll(Domain.getJQ(domain, "查询-项目-参与者").set("userId", userId).array());
		if (filter != null)
			pipeline.add(new Document("$match", filter));

		return c("obs", domain).aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public List<Project> listParticipatedProjectsInDaily(BasicDBObject condition, String userId, String domain) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return listParticipatedProjects(condition, userId, cal.getTime(), domain);
	}

	@Override
	public long countParticipatedProjectsInDaily(BasicDBObject filter, String userId, String domain) {
		Date startWorkFinish = new Date();
		return countParticipatedProjects(filter, userId, startWorkFinish, domain);
	}

	@Override
	public List<Project> listParticipatedProjectsInWeekly(BasicDBObject condition, String userId, String domain) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.setFirstDayOfWeek(Calendar.SUNDAY);
		int i = cal.get(Calendar.DAY_OF_WEEK);

		cal.add(Calendar.DAY_OF_MONTH, cal.getFirstDayOfWeek() - i);
		return listParticipatedProjects(condition, userId, cal.getTime(), domain);
	}

	@Override
	public long countParticipatedProjectsInWeekly(BasicDBObject filter, String userId, String domain) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.setFirstDayOfWeek(Calendar.SUNDAY);
		int i = cal.get(Calendar.DAY_OF_WEEK);

		cal.add(Calendar.DAY_OF_MONTH, cal.getFirstDayOfWeek() - i);
		return countParticipatedProjects(filter, userId, cal.getTime(), domain);
	}

	@Override
	public List<Project> listParticipatedProjectsInMonthly(BasicDBObject condition, String userId, String domain) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return listParticipatedProjects(condition, userId, cal.getTime(), domain);
	}

	@Override
	public long countParticipatedProjectsInMonthly(BasicDBObject filter, String userId, String domain) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return countParticipatedProjects(filter, userId, cal.getTime(), domain);
	}

	private List<Project> listParticipatedProjects(BasicDBObject condition, String userId, Date startWorkFinish, String domain) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");

		List<ObjectId> project_ids = c("work", domain)
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

		appendUserInfo(pipeline, "pmId", "pmInfo", domain);

		ArrayList<Project> into = c("project", domain).aggregate(pipeline, Project.class).into(new ArrayList<Project>());
		return into;
	}

	private long countParticipatedProjects(BasicDBObject filter, String userId, Date startWorkFinish, String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();
		List<ObjectId> project_ids = c("work", domain)
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

		return c("project", domain).aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public long delete(ObjectId _id, String domain) {
		Project project = get(_id, domain);
		if (!ProjectStatus.Created.equals(project.getStatus())) {
			throw new ServiceException("当前项目不允许删除，只能删除已创建状态的项目。");
		}
		// 获得所有的work
		List<ObjectId> workIds = c("work", domain).distinct("_id", new Document("project_id", _id), ObjectId.class).into(new ArrayList<>());
		// 清除关联的resourcePlan
		c("resourcePlan", domain).deleteMany(new Document("work_id", new Document("$in", workIds)));
		// 清除关联的resourceActual
		c("resourceActual", domain).deleteMany(new Document("work_id", new Document("$in", workIds)));

		// 清除关联的workPackage
		c("workPackage", domain).deleteMany(new Document("work_id", new Document("$in", workIds)));
		// 清除worklinks
		c("worklinks", domain).deleteMany(new Document("project_id", _id));
		// 清除work
		c("work", domain).deleteMany(new Document("project_id", _id));
		// 清除workspace
		c("workspace", domain).deleteMany(new Document("project_id", _id));
		// 清除worklinksspace
		c("worklinksspace", domain).deleteMany(new Document("project_id", _id));

		// 清除obs
		c("obs", domain).deleteMany(
				new Document("$or", Arrays.asList(new Document("scope_id", new Document("$in", workIds)), new Document("scope_id", _id))));

		// 清除cbs
		List<ObjectId> cbsIds = c(
				"cbs", domain)
						.distinct("_id",
								new Document("$or",
										Arrays.asList(new Document("scope_id", new Document("$in", workIds)),
												new Document("scopeRoot", false).append("scope_id", _id))),
								ObjectId.class)
						.into(new ArrayList<>());
		c("cbs", domain).deleteMany(new Document("_id", new Document("$in", cbsIds)));
		c("cbsPeriod", domain).deleteMany(new Document("cbsItem_id", new Document("$in", cbsIds)));
		c("cbsSubject", domain).deleteMany(new Document("cbsItem_id", new Document("$in", cbsIds)));

		// 清除folder
		c("folder", domain).deleteMany(new Document("project_id", _id));

		// 清除baseline
		c("baseline", domain).deleteMany(new Document("project_id", _id));
		c("baselineWork", domain).deleteMany(new Document("project_id", _id));
		c("baselineWorkLinks", domain).deleteMany(new Document("project_id", _id));

		// 清除projectChange
		c("projectChange", domain).deleteMany(new Document("project_id", _id));

		// 清除rbs
		List<ObjectId> rbsIds = c("rbsItem", domain).distinct("_id", new Document("project_id", _id), ObjectId.class)
				.into(new ArrayList<>());
		c("rbsItem", domain).deleteMany(new Document("project_id", _id));
		c("riskEffect", domain).deleteMany(new Document("project_id", _id));
		c("riskResponse", domain).deleteMany(new Document("rbsItem_id", new Document("$in", rbsIds)));

		// 清除salesItem
		c("salesItem", domain).deleteMany(new Document("project_id", _id));

		// 清除stockholder
		c("stockholder", domain).deleteMany(new Document("project_id", _id));

		// 清除workReport
		c("workReport", domain).deleteMany(new Document("project_id", _id));
		c("workReportItem", domain).deleteMany(new Document("work_id", new Document("$in", workIds)));
		c("workReportResourceActual", domain).deleteMany(new Document("work_id", new Document("$in", workIds)));

		// 清除项目设置
		c("setting", domain).deleteMany(new Document("name", Pattern.compile("@" + _id.toString(), Pattern.CASE_INSENSITIVE)));

		return delete(_id, Project.class, domain);
	}

	@Override
	public Workspace getWorkspace(ObjectId _id, String domain) {
		BasicDBObject dbo = c("project", domain).find(new BasicDBObject("_id", _id), BasicDBObject.class)
				.projection(new BasicDBObject("space_id", Boolean.TRUE).append("checkoutBy", Boolean.TRUE)).first();
		return Workspace.newInstance(_id, dbo.getObjectId("space_id"), dbo.getString("checkoutBy"));
	}

	@Override
	public List<Result> finishProject(Command com, String domain) {
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 状态检查
		Document project = c("project", domain).find(new Document("_id", com._id)).first();
		if (!ProjectStatus.Processing.equals(project.getString("status"))) {// 必须是进行中才能收尾
			return Arrays.asList(Result.error("项目当前的状态不允许执行收尾操作"));
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 如果存在未完成的工作，警告
		if (ICommand.Finish_Project.equals(com.name)) {
			long count = c("work", domain).countDocuments(new BasicDBObject("project_id", com._id).append("actualFinish", null));
			if (count > 0) {
				return Arrays.asList(Result.warning("项目存在一些尚未完成的工作。"));
			}
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 获得时间
		// yangjun 2018/10/31
		Document latest = c("work", domain).find(new Document("parent_id", com._id)).projection(new Document("actualFinish", true))
				.sort(new Document("actualFinish", -1).append("_id", -1)).first();
		Date actualFinish = Optional.ofNullable(latest).map(l -> l.getDate("actualFinish")).orElse(new Date());

		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 修改项目状态
		c("project", domain).updateOne(new Document("_id", com._id), new Document("$set", new Document("status", ProjectStatus.Closing)
				.append("progress", 1d).append("finishInfo", com.info()).append("actualFinish", actualFinish)));

		// 通知项目团队成员，项目收尾
		List<String> memberIds = getProjectMembers(com._id, domain);
		sendMessage("项目收尾通知", "项目：" + project.getString("name") + " 已于 " + Formatter.getString(com.date) + " 进入收尾。", com.userId, memberIds,
				null, domain);

		return new ArrayList<>();
	}

	@Override
	public List<Result> closeProject(Command com, String domain) {

		/////////////////////////////////////////////////////////////////////////////
		// 检查项目完工
		List<Result> result = closeProjectCheck(com._id, com.date, com.userId, domain);
		if (result.stream().anyMatch(r -> Result.TYPE_ERROR == r.type// 错误的必须返回
				|| (Result.TYPE_QUESTION == r.type && ICommand.Close_Project.equals(com.name))
				|| (Result.TYPE_WARNING == r.type && ICommand.Close_Project.equals(com.name)))) {// 不忽略警告的必须返回
			return result;
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 状态检查
		Document project = c("project", domain).find(new Document("_id", com._id)).first();
		if (ProjectStatus.Closed.equals(project.getString("status"))) {
			return Arrays.asList(Result.error("项目当前的状态不允许执行关闭操作"));
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 如果存在未完成的工作，警告
		if (ICommand.Finish_Project.equals(com.name)) {
			long count = c("work", domain).countDocuments(new BasicDBObject("project_id", com._id).append("actualFinish", null));
			if (count > 0) {
				return Arrays.asList(Result.warning("项目存在一些尚未完成的工作，这些工作也将同时关闭。"));
			}
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 修改项目状态
		c("project", domain).updateOne(new Document("_id", com._id),
				new Document("$set", new Document("status", ProjectStatus.Closed).append("closeInfo", com.info())));
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 修改工作状态
		// 删除未开始的工作，skipStart
		// 获取要删除的工作
		List<ObjectId> delete = new ArrayList<ObjectId>();
		c("work", domain).find(new Document("project_id", com._id).append("actualStart", null).append("milestone", false))
				.sort(new Document("wbsCode", 1)).forEach((Document d) -> {
					// 如果工作不在需要删除的工作中，获取其及其下级工作的id添加到删除集合中
					if (!delete.contains(d.getObjectId("_id"))) {
						List<ObjectId> workIds = new ArrayList<>();
						lookupDesentItems(Arrays.asList(d.getObjectId("_id")), "work", domain, "parent_id", true)
								.forEach((Document doc) -> {
									workIds.add(doc.getObjectId("_id"));
								});
						delete.addAll(workIds);
					}
				});
		if (delete.size() > 0)
			c("work", domain).deleteMany(new Document("_id", new Document("$in", delete)));
		// 完工为完工的工作
		List<ObjectId> close = new ArrayList<ObjectId>();
		c("work", domain)
				.find(new Document("project_id", com._id).append("actualFinish", null).append("actualStart", new Document("$ne", null)))
				.sort(new Document("wbsCode", 1)).forEach((Document d) -> {
					// 如果工作不在完成工作中，添加到关闭集合中
					if (!close.contains(d.getObjectId("_id"))) {
						List<ObjectId> workIds = new ArrayList<>();
						lookupDesentItems(Arrays.asList(d.getObjectId("_id")), "work", domain, "parent_id", true)
								.forEach((Document doc) -> {
									workIds.add(doc.getObjectId("_id"));
								});
						close.addAll(workIds);
					}
				});
		if (close.size() > 0) {
			// 更新里程碑
			c("work", domain).updateMany(new Document("_id", new Document("$in", close)).append("milestone", true), new Document("$set",
					new Document("skipFinish", true).append("actualFinish", com.date).append("actualStart", com.date)));
			// 更新阶段
			c("work", domain).updateMany(new Document("_id", new Document("$in", close)).append("stage", true), new Document("$set",
					new Document("skipFinish", true).append("actualFinish", com.date).append("status", ProjectStatus.Closed)));
			// 更新其它工作
			c("work", domain).updateMany(new Document("_id", new Document("$in", close)).append("milestone", false).append("stage", false),
					new Document("$set", new Document("skipFinish", true).append("actualFinish", com.date)));
		}
		// 关闭项目变更
		c("projectChange", domain).updateMany(
				new Document("project_id", com._id).append("status", new Document("$ne", ProjectChange.STATUS_CONFIRM)),
				new Document("$set", new Document("skipFinish", true).append("verifyDate", com.date).append("verify", com.userId)
						.append("status", ProjectChange.STATUS_CONFIRM)));

		// 确认项目报告
		c("workReport", domain).updateMany(
				new Document("project_id", com._id).append("status", new Document("$ne", ProjectChange.STATUS_CONFIRM)),
				new Document("$set", new Document("skipFinish", true).append("verifyDate", com.date)
						.append("status", WorkReport.STATUS_CONFIRM).append("verifier", com.userId)));

		// 通知项目团队成员，项目已经关闭
		List<String> memberIds = getProjectMembers(com._id, domain);
		sendMessage("项目关闭通知",
				"项目：" + project.getString("name") + " 已于 " + new SimpleDateFormat(Formatter.DATE_FORMAT_DATE).format(com.date) + " 关闭。",
				com.userId, memberIds, null, domain);
		return new ArrayList<>();
	}

	/**
	 * 项目成本
	 */
	private static String CLOSE_SETTING_FIELD_COST = "cost";
	/**
	 * 资源用量
	 */
	private static String CLOSE_SETTING_FIELD_RESOURCE = "resource";
	/**
	 * 项目变更
	 */
	private static String CLOSE_SETTING_FIELD_CHANGE = "change";
	/**
	 * 项目报告
	 */
	private static String CLOSE_SETTING_FIELD_REPORT = "report";
	/**
	 * 成品物料
	 */
	private static String CLOSE_SETTING_FIELD_MATERIAL = "material";
	/**
	 * 所有工作（未启动）
	 */
	private static String CLOSE_SETTING_FIELD_START_ALL = "startAll";
	/**
	 * 一级管理节点（未启动）
	 */
	private static String CLOSE_SETTING_FIELD_START_L1 = "startL1";
	/**
	 * 二级管理节点（未启动）
	 */
	private static String CLOSE_SETTING_FIELD_START_L2 = "startL2";
	/**
	 * 三级管理节点（未启动）
	 */
	private static String CLOSE_SETTING_FIELD_START_L3 = "startL3";
	/**
	 * 所有工作（未完工）
	 */
	private static String CLOSE_SETTING_FIELD_FINISH_ALL = "finishAll";
	/**
	 * 一级管理节点（未完工）
	 */
	private static String CLOSE_SETTING_FIELD_FINISH_L1 = "finishL1";
	/**
	 * 二级管理节点（未完工）
	 */
	private static String CLOSE_SETTING_FIELD_FINISH_L2 = "finishL2";
	/**
	 * 三级管理节点（未完工）
	 */
	private static String CLOSE_SETTING_FIELD_FINISH_L3 = "finishL3";
	/**
	 * 里程碑（未完工）
	 */
	private static String CLOSE_SETTING_FIELD_FINISH_MILESTONE = "finishMilestone";

	/**
	 * 要求、禁止
	 */
	private static String CLOSE_SETTING_VALUE_REQUIREMENT = "1";
	/**
	 * 询问
	 */
	private static String CLOSE_SETTING_VALUE_QUESTION = "2";
	/**
	 * 忽略、删除、完成、确认、关闭
	 */
	private static String CLOSE_SETTING_VALUE_IGNORE = "3";

	private List<Result> closeProjectCheck(ObjectId _id, Date date, String userId, String domain) {
		List<Result> results = new ArrayList<Result>();

		Project project = get(_id, domain);

		Document systemSetting = getScopeSetting(_id, CLOSE_SETTING_NAME, domain);
		String setting;

		// 未开始工作判断及操作
		// 获取所有工作的设置，默认为删除
		setting = getSettingValue(systemSetting, CLOSE_SETTING_FIELD_START_ALL, CLOSE_SETTING_VALUE_IGNORE);
		// 获取未开始工作数，排出里程碑

		List<Document> start = c("work", domain)
				.find(new Document("project_id", _id).append("actualStart", null).append("milestone", false))
				.sort(new Document("wbsCode", 1)).into(new ArrayList<>());
		if (CLOSE_SETTING_VALUE_REQUIREMENT.equals(setting) && start.size() > 0) {// 设置成不允许时，添加错误提示
			results.add(Result.error("工作:"
					+ Formatter.getString(start.stream().map(d -> d.getString("fullName")).collect(Collectors.toList())) + " 未开始，不允许完工项目"));
		} else if (start.size() > 0) {// 所有工作设置成询问时，根据各级工作的设置进行操作
			Map<String, String> manageLevels = new HashMap<String, String>();
			manageLevels.put("1", getSettingValue(systemSetting, CLOSE_SETTING_FIELD_START_L1, CLOSE_SETTING_VALUE_IGNORE));
			manageLevels.put("2", getSettingValue(systemSetting, CLOSE_SETTING_FIELD_START_L2, CLOSE_SETTING_VALUE_IGNORE));
			manageLevels.put("3", getSettingValue(systemSetting, CLOSE_SETTING_FIELD_START_L3, CLOSE_SETTING_VALUE_IGNORE));

			List<ObjectId> delete = new ArrayList<ObjectId>();
			List<String> notallow = new ArrayList<String>();
			List<String> question = new ArrayList<String>();
			// 获取所有未开始的节点，进行判断
			start.forEach((Document d) -> {
				// 如果工作不在需要删除的工作中，进行设置检查
				if (!delete.contains(d.getObjectId("_id")))
					if (CLOSE_SETTING_VALUE_REQUIREMENT.equals(manageLevels.get(d.getString("manageLevel")))) // 设置成不允许时，添加错误提示
						notallow.add(d.getString("fullName"));
					else if (CLOSE_SETTING_VALUE_QUESTION.equals(manageLevels.get(d.getString("manageLevel")))) // 设置成询问时，添加询问提示
						question.add(d.getString("fullName"));

			});
			if (notallow.size() > 0)
				results.add(Result.error("工作:" + Formatter.getString(notallow) + " 未开始，不允许完工项目"));
			if (question.size() > 0)
				results.add(Result.question("工作:" + Formatter.getString(question) + " 未开始。"));

		}

		// 未完工工作判断及操作、默认为完成
		setting = getSettingValue(systemSetting, CLOSE_SETTING_FIELD_FINISH_ALL, CLOSE_SETTING_VALUE_IGNORE);

		// 获取未完工工作数
		List<Document> finish = c("work", domain)
				.find(new Document("project_id", _id).append("actualFinish", null).append("actualStart", new Document("$ne", null)))
				.sort(new Document("wbsCode", 1)).into(new ArrayList<>());
		if (CLOSE_SETTING_VALUE_REQUIREMENT.equals(setting) && finish.size() > 0) {// 设置成不允许时，添加错误提示
			results.add(Result.error("工作:"
					+ Formatter
							.getString(Formatter.getString(finish.stream().map(d -> d.getString("fullName")).collect(Collectors.toList())))
					+ " 未完工，不允许完工项目"));
		} else if (finish.size() > 0) {// 所有工作设置成询问时，根据各级工作的设置进行操作
			Map<String, Object> manageLevels = new HashMap<String, Object>();
			manageLevels.put("1", getSettingValue(systemSetting, CLOSE_SETTING_FIELD_FINISH_L1, CLOSE_SETTING_VALUE_IGNORE));
			manageLevels.put("2", getSettingValue(systemSetting, CLOSE_SETTING_FIELD_FINISH_L2, CLOSE_SETTING_VALUE_IGNORE));
			manageLevels.put("3", getSettingValue(systemSetting, CLOSE_SETTING_FIELD_FINISH_L3, CLOSE_SETTING_VALUE_IGNORE));
			manageLevels.put("milestone", getSettingValue(systemSetting, CLOSE_SETTING_FIELD_FINISH_MILESTONE, CLOSE_SETTING_VALUE_IGNORE));

			List<ObjectId> close = new ArrayList<ObjectId>();
			List<String> notallow = new ArrayList<String>();
			List<String> question = new ArrayList<String>();

			finish.forEach((Document d) -> {
				// 如果工作不在完成工作中，进行设置判断
				if (!close.contains(d.getObjectId("_id"))) {
					Object set;
					// 如果工作是里程碑，根据里程碑设置判断
					if (d.getBoolean("milestone", false)) {
						set = manageLevels.get("milestone");
					} else {
						set = manageLevels.get(d.getString("manageLevel"));
					}
					if (CLOSE_SETTING_VALUE_REQUIREMENT.equals(set)) // 设置成不允许时，添加错误提示
						notallow.add(d.getString("fullName"));
					else if (CLOSE_SETTING_VALUE_QUESTION.equals(set)) // 设置成询问时，添加询问提示
						question.add(d.getString("fullName"));
				}
			});

			if (notallow.size() > 0)
				results.add(Result.error("工作:" + Formatter.getString(notallow) + " 未完工，不允许完工项目"));
			if (question.size() > 0)
				results.add(Result.question("工作:" + Formatter.getString(question) + " 未完工。"));
		}

		// 项目变更，默认为确认
		setting = getSettingValue(systemSetting, CLOSE_SETTING_FIELD_CHANGE, CLOSE_SETTING_VALUE_IGNORE);

		// 获取未关闭项目变更数
		long l = c("projectChange", domain)
				.countDocuments(new Document("project_id", _id).append("status", new Document("$ne", ProjectChange.STATUS_CONFIRM)));
		if (CLOSE_SETTING_VALUE_REQUIREMENT.equals(setting) && l > 0) {// 设置成要求时，添加错误提示
			results.add(Result.error("存在未关闭的项目变更，不允许完工项目"));
		} else if (CLOSE_SETTING_VALUE_QUESTION.equals(setting) && l > 0) {// 设置成询问时，添加询问提示
			results.add(Result.question("存在未关闭的项目变更，是否自动关闭项目变更后，完工项目"));
		}

		// 项目报告，默认为关闭
		setting = getSettingValue(systemSetting, CLOSE_SETTING_FIELD_REPORT, CLOSE_SETTING_VALUE_IGNORE);
		// 获取未确认的项目报告数
		l = c("workReport", domain)
				.countDocuments(new Document("project_id", _id).append("status", new Document("$ne", WorkReport.STATUS_CONFIRM)));
		if (CLOSE_SETTING_VALUE_REQUIREMENT.equals(setting) && l > 0) {// 设置成要求时，添加错误提示
			results.add(Result.error("存在未确认的项目报告，不允许完工项目"));
		} else if (CLOSE_SETTING_VALUE_QUESTION.equals(setting) && l > 0) {// 设置成询问时，添加询问提示
			results.add(Result.question("存在未确认的项目报告更，是否自动确认后，完工项目"));
		}

		// 项目成本，默认为忽略
		setting = getSettingValue(systemSetting, CLOSE_SETTING_FIELD_COST, CLOSE_SETTING_VALUE_IGNORE);
		if (!CLOSE_SETTING_VALUE_IGNORE.equals(setting)) {
			// 获取成本项id
			List<ObjectId> cbsIds = new ArrayList<>();
			lookupDesentItems(Arrays.asList(project.getCBS_id()), "cbs", domain, "parent_id", true).forEach((Document d) -> {
				cbsIds.add(d.getObjectId("_id"));
			});
			// 如果没有cbsSubject没有成本，则进行提示
			l = c("cbsSubject", domain)
					.countDocuments(new Document("cbsItem_id", new Document("$in", cbsIds)).append("cost", new Document("$ne", null)));
			if (l == 0)
				// 如果设置为警告，则返回警告；如果设置为要求，则返回错误。
				if (CLOSE_SETTING_VALUE_QUESTION.equals(setting))
					results.add(Result.question("项目尚未维护成本。"));
				else if (CLOSE_SETTING_VALUE_REQUIREMENT.equals(setting))
					results.add(Result.error("项目尚未维护成本。"));

		}

		// 资源用量，默认为忽略
		setting = getSettingValue(systemSetting, CLOSE_SETTING_FIELD_RESOURCE, CLOSE_SETTING_VALUE_IGNORE);
		if (!CLOSE_SETTING_VALUE_IGNORE.equals(setting)) {
			// 获得所有非里程碑的叶子节点的id
			List<ObjectId> workIds = c("work", domain)
					.distinct("_id", new Document("project_id", _id).append("milestone", false).append("summary", false), ObjectId.class)
					.into(new ArrayList<>());
			// 如果没有进度计划，不进行资源检查
			if (workIds.size() > 0) {
				// 获取填写了资源计划的工作id
				List<ObjectId> resourceWorkIds = c("resourceActual", domain)
						.distinct("work_id", new Document("work_id", new Document("$in", workIds)), ObjectId.class).into(new ArrayList<>());
				String message = "";
				if (resourceWorkIds.size() == 0) {
					message = "项目尚未编制资源用量";
				} else {
					workIds.removeAll(resourceWorkIds);
					if (workIds.size() > 0) {
						message = Formatter.getString(
								c("work", domain).distinct("fullName", new Document("_id", new Document("$in", workIds)), String.class)
										.into(new ArrayList<>()));
						message = "工作：" + message + " 未编制资源用量";
					}
				}
				if (!message.isEmpty() && CLOSE_SETTING_VALUE_QUESTION.equals(setting))
					results.add(Result.question(message));
				else if (!message.isEmpty() && CLOSE_SETTING_VALUE_REQUIREMENT.equals(setting))
					results.add(Result.error(message));
			}
		}

		// TODO 成品物料，默认为忽略
		setting = getSettingValue(systemSetting, CLOSE_SETTING_FIELD_MATERIAL, CLOSE_SETTING_VALUE_IGNORE);
		if (!CLOSE_SETTING_VALUE_IGNORE.equals(setting)) {

		}

		return results;
	}

	/**
	 * TODO 迁移到前台进行
	 * 
	 * @param _id
	 * @return
	 */
	@Deprecated
	private String generateWorkOrder(ObjectId _id, String domain) {
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
		Project project = get(_id, domain);
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

		String orgNo = c("organization", domain).distinct("id", new Document("_id", impunit_id), String.class).first();
		workOrder += orgNo;

		if (parentproject_id != null) {
			String parentWorkOrder = c("project", domain).distinct("workOrder", new Document("_id", parentproject_id), String.class)
					.first();
			String[] workorders = parentWorkOrder.split("-");
			workOrder += "-" + workorders[1];
			int index = generateCode("Id_Gen", "projectno" + parentWorkOrder, domain);
			workOrder += "-" + String.format("%02d", index);

		} else if (program_id != null) {
			String programWorkOrder = c("program", domain).distinct("workOrder", new Document("_id", program_id), String.class).first();
			String[] workorders = programWorkOrder.split("-");
			workOrder += "-" + workorders[1];
			int index = generateCode("Id_Gen", "projectno" + programWorkOrder, domain);
			workOrder += "-" + String.format("%02d", index);
		} else {
			int index = generateCode("Id_Gen", "projectno" + year, domain);
			workOrder += "-" + String.format("%02d", index);
		}

		workOrder += "-" + String.format("%04d", year);

		return workOrder;
	}

	@Override
	public List<News> getRecentNews(ObjectId _id, int count, String domain) {
		ArrayList<News> result = new ArrayList<News>();
		List<Bson> pipeline = Domain.getJQ(domain, "查询-时间线")
				.set("match", new Document("manageLevel", new Document("$in", Arrays.asList("1", "2"))).append("project_id", _id))
				.set("limit", count).array();
		c("work", domain).aggregate(pipeline).forEach((Document doc) -> {
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
	public SalesItem insertSalesItem(SalesItem salesItem, String domain) {
		return insert(salesItem, domain);
	}

	@Override
	public long deleteStockholder(ObjectId _id, String domain) {
		return delete(_id, Stockholder.class, domain);
	}

	@Override
	public List<Baseline> listBaseline(BasicDBObject condition, ObjectId _id, String domain) {
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
		return query(skip, limit, filter, sort, Baseline.class, domain);
	}

	@Override
	public long countBaseline(BasicDBObject filter, ObjectId _id, String domain) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("project_id", _id);

		return count(filter, Baseline.class, domain);
	}

	@Override
	public Baseline createBaseline(Baseline baseline, String domain) {
		Baseline newBaseline = insert(baseline, Baseline.class, domain);
		ObjectId project_id = baseline.getProject_id();
		// 获取要存储到基线的项目
		Document projectDoc = c("project", domain).find(new Document("_id", project_id)).first();
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
		c("work", domain).find(new Document("project_id", project_id)).sort(new Document("parent_id", 1)).forEach((Document doc) -> {
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
		c("worklinks", domain).find(new Document("project_id", project_id)).forEach((Document doc) -> {
			ObjectId target = doc.getObjectId("target");
			doc.append("target", workIds.get(target));
			ObjectId source = doc.getObjectId("source");
			doc.append("source", workIds.get(source));

			doc.append("baseline_id", newBaseline_id);

			doc.remove("_id");

			worklinkDocs.add(doc);
		});

		// 插入项目和工作数据
		c(Baseline.class, domain).updateMany(new Document("_id", baseline.get_id()), new Document("$set", projectDoc));

		if (workDocs.size() > 0)
			c("baselineWork", domain).insertMany(workDocs);

		if (worklinkDocs.size() > 0)
			c("baselineWorkLinks", domain).insertMany(worklinkDocs);

		return get(newBaseline_id, Baseline.class, domain);
	}

	@Override
	public long deleteBaseline(ObjectId _id, String domain) {
		c("baselineWork", domain).deleteMany(new Document("baseline_id", _id));

		c("baselineWorkLinks", domain).deleteMany(new Document("baseline_id", _id));

		return delete(_id, Baseline.class, domain);
	}

	@Override
	public long updateBaseline(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, Baseline.class, domain);
	}

	@Override
	public List<BaselineComparable> getBaselineComparable(List<ObjectId> projectIds, String domain) {
		// TODO JQ
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("project_id", new Document("$in", projectIds))));
		pipeline.add(Aggregates.sort(new Document("project_id", 1).append("index", 1)));
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		pipeline.add(Aggregates.addFields(
				Arrays.asList(new Field<String>("projectName", "$project.name"), new Field<String>("projectNumber", "$project.id"))));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));
		appendUserInfo(pipeline, "chargerId", "chargerInfo", domain);
		appendUserInfo(pipeline, "assignerId", "assignerInfo", domain);
		List<Work> works = c("work", Work.class, domain).aggregate(pipeline).into(new ArrayList<Work>());

		pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("baseline_id", new Document("$in", projectIds))));
		pipeline.add(Aggregates.sort(new Document("baseline_id", 1).append("index", 1)));
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		pipeline.add(Aggregates.addFields(
				Arrays.asList(new Field<String>("projectName", "$project.name"), new Field<String>("projectNumber", "$project.id"))));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));
		appendUserInfo(pipeline, "chargerId", "chargerInfo", domain);
		appendUserInfo(pipeline, "assignerId", "assignerInfo", domain);
		List<Work> baselineWorks = c("baselineWork", Work.class, domain).aggregate(pipeline).into(new ArrayList<Work>());

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
	public List<ProjectChange> listProjectChange(BasicDBObject condition, ObjectId _id, String domain) {
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
		appendUserInfo(pipeline, "applicant", "applicantInfo", domain);
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");
		// yangjun 2018/10/31
		return c(ProjectChange.class, domain).aggregate(pipeline).into(new ArrayList<ProjectChange>());
	}

	@Override
	public long countProjectChange(BasicDBObject filter, ObjectId _id, String domain) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("project_id", _id);

		return count(filter, ProjectChange.class, domain);
	}

	@Override
	public ProjectChange createProjectChange(ProjectChange pc, String domain) {
		List<ProjectChangeTask> reviewer = pc.getReviewer();
		List<OBSItem> obsItems = new OBSServiceImpl().getScopeOBS(pc.getProject_id(), domain);
		List<ChangeProcess> changeProcesss = new CommonServiceImpl().createChangeProcessDataSet(domain);
		for (ChangeProcess changeProcess : changeProcesss) {
			if (changeProcess.getProjectOBSId() != null) {
				for (OBSItem obsItem : obsItems) {
					// yangjun 2018/10/31
					if (Check.equals(obsItem.getRoleId(), changeProcess.getProjectOBSId())) {
						ProjectChangeTask pct = new ProjectChangeTask();
						pct.domain = domain;
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

		ProjectChange newPC = insert(pc, ProjectChange.class, domain);
		return listProjectChangeInfo(newPC.get_id(), domain).get(0);
	}

	@Override
	public long deleteProjectChange(ObjectId _id, String domain) {
		return delete(_id, ProjectChange.class, domain);
	}

	@Override
	public long updateProjectChange(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, ProjectChange.class, domain);
	}

	@Override
	public ProjectChange getProjectChange(ObjectId _id, String domain) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new BasicDBObject("_id", _id)));

		pipeline.add(Aggregates.sort(new BasicDBObject("_id", -1)));

		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo", domain);
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		return c(ProjectChange.class, domain).aggregate(pipeline).first();
	}

	@Override
	public List<ProjectChange> listProjectChangeInfo(ObjectId _id, String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();

		pipeline.addAll(Domain.getJQ(domain, "查询-项目变更").set("match", new Document("_id", _id)).array());

		appendUserInfo(pipeline, "applicant", "applicantInfo", domain);
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		return c(ProjectChange.class, domain).aggregate(pipeline).into(new ArrayList<ProjectChange>());
	}

	@Override
	public List<Result> submitProjectChange(List<ObjectId> projectChangeIds, String domain) {
		List<Result> result = submitProjectChangeCheck(projectChangeIds, domain);
		if (!result.isEmpty()) {
			return result;
		}

		UpdateResult ur = c(ProjectChange.class, domain).updateMany(new Document("_id", new Document("$in", projectChangeIds)),
				new Document("$set", new Document("submitDate", new Date()).append("status", ProjectChange.STATUS_SUBMIT)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足提交条件的变更申请。"));
			return result;
		}

		// 发送通知
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new Document("_id", new Document("$in", projectChangeIds))));
		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo", domain);
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		c(ProjectChange.class, domain).aggregate(pipeline).forEach((ProjectChange projectChange) -> {
			List<String> receivers = new ArrayList<String>();
			projectChange.getReviewer().forEach((ProjectChangeTask receiver) -> {
				receivers.add(receiver.user);
			});
			sendMessage("项目变更申请", "" + projectChange.getApplicantInfo() + " 发起了项目：" + projectChange.getProjectName() + " 的变更申请，请您进行审核。",
					projectChange.getApplicantId(), receivers, null, domain);

			String pmId = c("project", domain).distinct("pmId", new Document("_id", projectChange.getProject_id()), String.class).first();
			if (!receivers.contains(pmId))
				sendMessage("项目变更申请", "" + projectChange.getApplicantInfo() + " 发起了项目：" + projectChange.getProjectName() + " 的变更申请，",
						projectChange.getApplicantId(), receivers, null, domain);

		});

		return result;
	}

	private List<Result> submitProjectChangeCheck(List<ObjectId> projectChangeIds, String domain) {
		List<Result> result = new ArrayList<Result>();
		long count = c(ProjectChange.class, domain)
				.countDocuments(new Document("_id", new Document("$in", projectChangeIds)).append("reviewer.user", null));
		if (count > 0) {
			result.add(Result.submitProjectChangeError("缺少审核人员"));
		}
		return result;
	}

	@Override
	public List<Result> passProjectChange(ProjectChangeTask projectChangeTask, String domain) {
		List<Result> result = passProjectChangeCheck(projectChangeTask, domain);
		if (!result.isEmpty()) {
			return result;
		}
		// yangjun 2018/10/31
		ProjectChange pc = getProjectChange(projectChangeTask.projectChange_id, domain);
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
			reviewer.add(BsonTools.getBasicDBObject(re));
		}

		UpdateResult ur = c(ProjectChange.class, domain).updateOne(new BasicDBObject("_id", projectChangeTask.projectChange_id),
				new BasicDBObject("$set", new BasicDBObject("reviewer", reviewer).append("status", status)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足确认条件的变更申请。"));
			return result;
		}

		// 发送变更批准通知
		sendMessage("项目变更申请已批准", "" + projectChangeTask.getUser() + " 批准了项目：" + pc.getProjectName() + " 的变更申请，", projectChangeTask.user,
				pc.getApplicantId(), null, domain);
		if (ProjectChange.STATUS_PASS.equals(status)) {
			String pmId = c("project", domain).distinct("pmId", new Document("_id", pc.getProject_id()), String.class).first();
			sendMessage("项目变更申请已通过", "项目：" + pc.getProjectName() + " 的变更申请已审核通过，", pc.getApplicantId(), pmId, null, domain);
		}

		return result;
	}

	private List<Result> passProjectChangeCheck(ProjectChangeTask projectChangeTask, String domain) {
		return new ArrayList<Result>();
	}

	@Override
	public List<Result> confirmProjectChange(List<ObjectId> projectChangeIds, String userId, String domain) {
		List<Result> result = confirmProjectChangeCheck(projectChangeIds, domain);
		if (!result.isEmpty()) {
			return result;
		}

		UpdateResult ur = c(ProjectChange.class, domain).updateMany(new Document("_id", new Document("$in", projectChangeIds)),
				new Document("$set",
						new Document("verifyDate", new Date()).append("verify", userId).append("status", ProjectChange.STATUS_CONFIRM)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足确认条件的变更申请。"));
			return result;
		}
		// 发送通知
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new Document("_id", new Document("$in", projectChangeIds))));
		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo", domain);
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		c(ProjectChange.class, domain).aggregate(pipeline).forEach((ProjectChange pc) -> {
			sendMessage("项目变更申请已关闭", "项目：" + pc.getProjectName() + " 的变更已关闭，", userId, pc.getApplicantId(), null, domain);
		});
		return result;
	}

	private List<Result> confirmProjectChangeCheck(List<ObjectId> projectChangeIds, String domain) {
		return new ArrayList<Result>();
	}

	public List<Result> cancelProjectChange(ProjectChangeTask projectChangeTask, String domain) {
		List<Result> result = cancelProjectChangeCheck(projectChangeTask);
		if (!result.isEmpty()) {
			return result;
		}
		ProjectChange pc = get(projectChangeTask.projectChange_id, ProjectChange.class, domain);
		List<BasicDBObject> reviewer = new ArrayList<BasicDBObject>();
		List<ProjectChangeTask> reviewers = pc.getReviewer();
		for (ProjectChangeTask re : reviewers) {
			if (re.name.equals(projectChangeTask.name)) {
				re.user = projectChangeTask.user;
				re.choice = projectChangeTask.choice;
				re.date = projectChangeTask.date;
				re.comment = projectChangeTask.comment;
			}
			reviewer.add(BsonTools.getBasicDBObject(re));
		}

		UpdateResult ur = c(ProjectChange.class, domain).updateOne(new BasicDBObject("_id", projectChangeTask.projectChange_id),
				new BasicDBObject("$set", new BasicDBObject("reviewer", reviewer).append("status", ProjectChange.STATUS_CANCEL)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足取消条件的变更申请。"));
			return result;
		}
		String pmId = c("project", domain).distinct("pmId", new Document("_id", pc.getProject_id()), String.class).first();
		sendMessage("项目变更申请已否决", "" + projectChangeTask.getUser() + " 否决了项目：" + pc.getProjectName() + " 的变更申请，", projectChangeTask.user,
				Arrays.asList(pmId, pc.getApplicantId()), null, domain);

		return result;
	}

	private List<Result> cancelProjectChangeCheck(ProjectChangeTask projectChangeTask) {
		return new ArrayList<Result>();
	}

	@Override
	public long updateProjectChange(ProjectChangeTask projectChangeTask, ObjectId projectChange_id, String domain) {
		ProjectChange pc = get(projectChange_id, ProjectChange.class, domain);
		List<ProjectChangeTask> reviewers = pc.getReviewer();
		List<BasicDBObject> reviewer = new ArrayList<BasicDBObject>();
		for (ProjectChangeTask re : reviewers) {
			if (re.name.equals(projectChangeTask.name)) {
				re.user = projectChangeTask.user;
			}
			reviewer.add(BsonTools.getBasicDBObject(re));
		}

		UpdateResult ur = c(ProjectChange.class, domain).updateOne(new BasicDBObject("_id", projectChange_id),
				new BasicDBObject("$set", new BasicDBObject("reviewer", reviewer)));

		return ur.getModifiedCount();
	}

	@Override
	public long checkCreateProjectChange(ObjectId _id, String domain) {
		return c("projectChange", domain).countDocuments(new Document("project_id", _id).append("status",
				new Document("$nin", Arrays.asList(ProjectChange.STATUS_CANCEL, ProjectChange.STATUS_CONFIRM))));
	}

	@Override
	public long countReviewerProjectChange(BasicDBObject filter, String userId, String domain) {
		List<Bson> pipeline = (List<Bson>) Domain.getJQ(domain, "查询-项目变更-待审批").set("userId", userId)
				.set("status", ProjectChange.STATUS_SUBMIT).array();

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		return c("projectChange", domain).aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public List<ProjectChange> listReviewerProjectChange(BasicDBObject condition, String userId, String domain) {
		return listReviewerPC(condition, userId, domain).into(new ArrayList<>());
	}

	@Override
	public List<Document> listReviewerProjectChangeCard(BasicDBObject condition, String userId, String domain) {
		return listReviewerPC(condition, userId, domain).map(ProjectChangeRenderer::render).into(new ArrayList<>());
	}

	private AggregateIterable<ProjectChange> listReviewerPC(BasicDBObject condition, String userId, String domain) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
		}

		List<Bson> pipeline = (List<Bson>) Domain.getJQ(domain, "查询-项目变更-待审批").set("userId", userId)
				.set("status", ProjectChange.STATUS_SUBMIT).array();

		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo", domain);
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		pipeline.add(Aggregates.match(filter));

		pipeline.add(Aggregates.sort(new BasicDBObject("_id", -1)));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		return c(ProjectChange.class, domain).aggregate(pipeline);
	}

	@Override
	@Deprecated
	public List<Project> listAdministratedProjects(BasicDBObject condition, String managerId, String domain) {
		List<ObjectId> projectIds = getAdministratedProjects(managerId, domain);
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("_id", new BasicDBObject("$in", projectIds));
		return list(condition, domain);
	}

	@Override
	public List<Document> listAdministratedProjectsCard(BasicDBObject condition, String managerId, String domain) {
		List<ObjectId> projectIds = getAdministratedProjects(managerId, domain);
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("_id", new BasicDBObject("$in", projectIds));

		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");

		List<Bson> pipeline = appendQueryPipeline(skip, limit, filter, sort, new ArrayList<>(), domain);
		return c(Project.class, domain).aggregate(pipeline).map(d -> ProjectRenderer.render(d, domain)).into(new ArrayList<>());
	}

	@Override
	public long countAdministratedProjects(BasicDBObject filter, String managerId, String domain) {
		List<ObjectId> projectIds = getAdministratedProjects(managerId, domain);
		if (filter == null) {
			// filter = new BasicDBObject();
			return projectIds.size();
		}
		filter.append("_id", new BasicDBObject("$in", projectIds));
		return count(filter, domain);
	}

	/**
	 * 获取所有项目，当用户具有项目总监、供应链管理、财务管理、制造管理角色时，显示全部项目信息，否则只显示当前用户作为项目PMO团队成员的项目
	 */
	@Override
	public List<Project> listAllProjects(BasicDBObject condition, String userid, String domain) {
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
		if (!checkUserRoles(userid, Role.SYS_ROLE_PD_ID, domain)) {
			appendQueryUserInProjectPMO(pipeline, userid, "$_id", domain);
		}

		appendQueryPipeline(skip, limit, filter, sort, pipeline, domain);

		return c(Project.class, domain).aggregate(pipeline).into(new ArrayList<>());
	}

	/**
	 * 获取所有项目数，当用户具有项目总监、供应链管理、财务管理、制造管理角色时，显示全部项目数，否则只显示当前用户作为项目PMO团队成员的项目数
	 */
	@Override
	public long countAllProjects(BasicDBObject filter, String userid, String domain) {
		// 如果当前用户具有项目总监的权限时，则全部项目总数
		if (checkUserRoles(userid, Role.SYS_ROLE_PD_ID, domain)) {
			return count(filter, domain);
		}
		// 不显示全部时，只返回用户在项目PMO团队中的项目数
		List<Bson> pipeline = new ArrayList<Bson>();
		appendQueryUserInProjectPMO(pipeline, userid, "$_id", domain);
		return c(Project.class, domain).aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public Integer schedule(ObjectId _id, String domain) {
		return super.schedule(_id, domain);
	}

	@Override
	public List<ProjectScheduleInfo> listManagedProjectSchedules(BasicDBObject condition, String userid, String domain) {
		return listAllProjects(condition, userid, domain).stream().map(project -> new ProjectScheduleInfo().setProject(project))
				.collect(Collectors.toList());
	}

	@Override
	public long countManagedProjectSchedules(BasicDBObject filter, String userid, String domain) {
		return countAllProjects(filter, userid, domain);
	}

	@Override
	public List<ProjectScheduleInfo> listSubManagedProjectSchedules(ProjectScheduleInfo parent, String domain) {
		WorkServiceImpl workService = new WorkServiceImpl();
		if (parent.typeEquals(Project.class)) {
			return workService.listProjectRootTask(parent.get_id(), domain).stream().map(work -> new ProjectScheduleInfo().setWork(work))
					.collect(Collectors.toList());
		} else if (parent.typeEquals(Work.class)) {
			return workService.listChildren(parent.get_id(), domain).stream().map(work -> new ProjectScheduleInfo().setWork(work))
					.collect(Collectors.toList());
		} else {
			// TODO 其他类型
			return new ArrayList<ProjectScheduleInfo>();
		}
	}

	@Override
	public long countSubManagedProjectSchedules(ProjectScheduleInfo parent, String domain) {
		WorkServiceImpl workService = new WorkServiceImpl();
		if (parent.typeEquals(Project.class)) {
			return workService.countProjectRootTask(parent.get_id(), domain);
		} else if (parent.typeEquals(Work.class)) {
			return workService.countChildren(parent.get_id(), domain);
		} else {
			// TODO 其他类型
			return 0l;
		}
	}

	@Override
	public Document getOrganizationSAR(String year, String userId, String domain) {
		return getOrganizationSAR(year, userId, "stageSar", year + " 年 项目计划完成率", domain);
	}

	@Override
	public Document getOrganizationSAR1(String year, String userId, String domain) {
		return getOrganizationSAR(year, userId, "sar1", year + " 年 一级计划完成率", domain);
	}

	@Override
	public Document getOrganizationSAR2(String year, String userId, String domain) {
		return getOrganizationSAR(year, userId, "sar2", year + " 年 二级计划完成率", domain);
	}

	@SuppressWarnings("unchecked")
	private Document getOrganizationSAR(String year, String userId, String sarName, String title, String domain) {
		List<String> xAxisData = new ArrayList<String>();
		for (int i = 1; i <= 12; i++) {
			String key = year + "-" + String.format("%02d", i);
			xAxisData.add(key);
		}
		List<String> legendData = new ArrayList<String>();
		List<Document> series = new ArrayList<Document>();

		c("organization", domain).aggregate(Domain.getJQ(domain, "查询-承担部门计划完成率").set("workmatch", new Document("$expr",
				new Document("$eq",
						Arrays.asList(year, new Document("$dateToString", new Document("format", "%Y").append("date", "$actualFinish"))))))
				.set("orgmatch", new Document()).set("projectmatch", new Document())// "_id", new Document("$in",
																					// getAdministratedProjects(userId))))
				.array()).forEach((Document d) -> {
					List<Document> project = (List<Document>) d.get("project");
					if (Check.isAssigned(project)) {
						String name = d.getString("name");
						double[] sard = new double[12];
						for (Document p : project) {
							int i = Integer.parseInt(p.getString("month").split("-")[1]) - 1;
							sard[i] = new BigDecimal(p.getDouble(sarName)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
						}
						series.add(new Document("name", name).append("type", "bar").append("data", Formatter.toList(sard)));
						legendData.add(name);
					}
				});
		Document doc = Domain.getJQ(domain, "图表-计划完成率").set("xAxisData", xAxisData).set("legendData", legendData).set("series", series)
				.set("title", title).doc();
		return doc;
	}

	@Override
	public Project appointmentProjectManger(ObjectId _id, String newPMId, String currentUserId, String domain) {
		String msgSubject = "项目指派通知";
		Project project = c(Project.class, domain).find(new Document("_id", _id)).first();
		String userId = project.getPmId();
		new WorkServiceImpl().removeUnStartWorkUser(Arrays.asList(userId), _id, currentUserId, domain);
		c("project", domain).updateOne(new BasicDBObject("_id", _id), new BasicDBObject("$set", new BasicDBObject("pmId", newPMId)));
		c("obs", domain).updateOne(new BasicDBObject("_id", project.getOBS_id()),
				new BasicDBObject("$set", new BasicDBObject("managerId", newPMId)));

		List<Message> messages = new ArrayList<>();
		String content = "项目：" + project.getName();
		messages.add(Message.newInstance(msgSubject, content + "，您已不再担任项目经理。", currentUserId, userId, null));
		messages.add(Message.newInstance(msgSubject, content + "，已指定您担任项目经理。", currentUserId, newPMId, null));
		// 消息
		String projectStatus = project.getStatus();
		if (messages.size() > 0 && !ProjectStatus.Created.equals(projectStatus))
			sendMessages(messages, domain);

		return get(_id, domain);
	}

}
