package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.common.JQ;
import com.bizvisionsoft.service.model.FolderInTemplate;
import com.bizvisionsoft.service.model.OBSInTemplate;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.OBSModule;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.ResourcePlanInTemplate;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.WBSModule;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.service.model.WorkLinkInTemplate;
import com.bizvisionsoft.service.model.WorkspaceGanttData;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.DeleteResult;

public class ProjectTemplateServiceImpl extends BasicServiceImpl implements ProjectTemplateService {

	@Override
	public ProjectTemplate insertProjectTemplate(ProjectTemplate prjt, String domain) {
		ProjectTemplate pt = insert(prjt, domain);
		// 添加默认全局项目设置
		List<Document> list = PROJECT_SETTING_NAMES.stream().map(name -> {
			Document setting = Optional.ofNullable(getSystemSetting(name, domain)).orElse(new Document());
			setting.put("name", name + "@" + pt.getScope_id());
			setting.remove("_id");
			return setting;
		}).collect(Collectors.toList());
		c("setting", domain).insertMany(list);
		return pt;
	}

	@Override
	public WBSModule insertWBSModule(WBSModule prjt, String domain) {
		return insert(prjt, domain);
	}

	@Override
	public ProjectTemplate get(ObjectId _id, String domain) {
		return get(_id, ProjectTemplate.class, domain);
	}

	@Override
	public long countProjectTemplate(BasicDBObject filter, String domain) {
		filter.append("module", false);
		return count(filter, ProjectTemplate.class, domain);
	}

	@Override
	public long countWBSModule(BasicDBObject filter, String domain) {
		filter.append("module", true);
		return count(filter, ProjectTemplate.class, domain);
	}

	@Override
	public List<ProjectTemplate> listProjectTemplate(BasicDBObject condition, String domain) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("module", new BasicDBObject("$ne", true));
		return createDataSet(condition, ProjectTemplate.class, domain);
	}

	@Override
	public List<WBSModule> listWBSModule(BasicDBObject condition, String domain) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("module", true);
		return createDataSet(condition, WBSModule.class, domain);
	}

	@Override
	public WorkInTemplate getWorkInTemplate(ObjectId work_id, String domain) {
		return get(work_id, WorkInTemplate.class, domain);
	}

	@Override
	public List<WorkInTemplate> listWorks(ObjectId template_id, String domain) {
		return c(WorkInTemplate.class, domain).find(new BasicDBObject("template_id", template_id)).sort(new BasicDBObject("index", 1))
				.into(new ArrayList<WorkInTemplate>());
	}

	@Override
	public List<WorkLinkInTemplate> listLinks(ObjectId template_id, String domain) {
		return c(WorkLinkInTemplate.class, domain).find(new BasicDBObject("template_id", template_id))
				.into(new ArrayList<WorkLinkInTemplate>());
	}

	@Override
	public WorkInTemplate insertWork(WorkInTemplate work, String domain) {
		return insert(work, domain);
	}

	@Override
	public long updateWork(BasicDBObject bson, String domain) {
		return update(bson, WorkInTemplate.class, domain);
	}

	@Override
	public long deleteWork(ObjectId work_id, String domain) {
		// TODO 删除link
		return delete(work_id, WorkInTemplate.class, domain);
	}

	@Override
	public WorkLinkInTemplate insertLink(WorkLinkInTemplate link, String domain) {
		return insert(link, domain);
	}

	@Override
	public long updateLink(BasicDBObject bson, String domain) {
		return update(bson, WorkLinkInTemplate.class, domain);
	}

	@Override
	public long deleteLink(ObjectId link_id, String domain) {
		return delete(link_id, WorkLinkInTemplate.class, domain);
	}

	@Override
	public int nextWBSIndex(BasicDBObject condition, String domain) {
		Document doc = c("workInTemplate", domain).find(condition).sort(new BasicDBObject("index", -1))
				.projection(new BasicDBObject("index", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("index", 0)).orElse(0) + 1;
	}

	@Override
	public int nextOBSSeq(BasicDBObject condition, String domain) {
		Document doc = c("obsInTemplate", domain).find(condition).sort(new BasicDBObject("seq", -1)).projection(new BasicDBObject("seq", 1))
				.first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("seq", 0)).orElse(0) + 1;
	}

	@Override
	public List<OBSInTemplate> getOBSTemplate(ObjectId template_id, String domain) {
		return queryOBSTemplate(new BasicDBObject("scope_id", template_id), domain);
	}

	private List<OBSInTemplate> queryOBSTemplate(BasicDBObject match, String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(match));

		appendUserInfoAndHeadPic(pipeline, "managerId", "managerInfo", "managerHeadPic");

		appendSortBy(pipeline, "seq", 1);

		return c(OBSInTemplate.class, domain).aggregate(pipeline).into(new ArrayList<OBSInTemplate>());
	}

	@Override
	public boolean hasOBS(ObjectId _id, String domain) {
		return c("obsInTemplate", domain).countDocuments(new BasicDBObject("scope_id", _id)) > 0;
	}

	@Override
	public OBSInTemplate createRootOBS(ObjectId _id, String domain) {
		OBSInTemplate obsRoot = OBSInTemplate.getInstanceTeam(_id, null, "项目组", OBSItem.ID_PM, OBSItem.NAME_PM, true);
		return insert(obsRoot, domain);// 插入记录
	}

	@Override
	public OBSInTemplate insertOBSItem(OBSInTemplate t, String domain) {
		try {
			return insert(t, domain);
		} catch (Exception e) {
			throw handleMongoException(e, t.toString());
		}
	}

	@Override
	public long updateOBSItem(BasicDBObject fu, String domain) {
		return update(fu, OBSInTemplate.class, domain);
	}

	@Override
	public List<WorkInTemplate> listWBSRoot(ObjectId template_id, String domain) {
		List<? extends Bson> pipeline = Domain.getJQ(domain, "查询-模板工作-WBS根").set("template_id", template_id).set("parent_id", null).array();
		return c(WorkInTemplate.class, domain).aggregate(pipeline).into(new ArrayList<WorkInTemplate>());
	}

	@Override
	public long countWBSRoot(ObjectId template_id, String domain) {
		return count(new BasicDBObject("template_id", template_id).append("parent_id", null), "workInTemplate", domain);
	}

	@Override
	public List<WorkInTemplate> listWBSChildren(ObjectId parent_id, String domain) {
		List<? extends Bson> pipeline = Domain.getJQ(domain, "查询-模板工作-下级").set("parent_id", parent_id).array();
		return c(WorkInTemplate.class, domain).aggregate(pipeline).into(new ArrayList<WorkInTemplate>());
	}

	@Override
	public long countWBSChildren(ObjectId parent_id, String domain) {
		return count(new BasicDBObject("parent_id", parent_id), "workInTemplate", domain);
	}

	@Override
	public List<OBSInTemplate> getOBSRoleTemplate(ObjectId template_id, String domain) {
		return queryOBSTemplate(new BasicDBObject("scope_id", template_id), domain);
	}

	@Override
	public List<ResourcePlanInTemplate> addResourcePlan(List<ResourceAssignment> resas, String domain) {
		List<ResourcePlanInTemplate> documents = new ArrayList<ResourcePlanInTemplate>();
		resas.forEach(r -> documents.add(r.getResourcePlanInTemplate()));
		c(ResourcePlanInTemplate.class, domain).insertMany(documents);
		return documents;
	}

	@Override
	public long updateResourcePlan(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, ResourcePlan.class, domain);
	}

	@Override
	public long deleteHumanResourcePlan(ObjectId work_id, String resId, String domain) {
		DeleteResult dr = c("resourcePlanInTemplate", domain).deleteMany(new Document("work_id", work_id).append("usedHumanResId", resId));
		return dr.getDeletedCount();
	}

	@Override
	public long deleteEquipmentResourcePlan(ObjectId work_id, String resId, String domain) {
		DeleteResult dr = c("resourcePlanInTemplate", domain).deleteMany(new Document("work_id", work_id).append("usedEquipResId", resId));
		return dr.getDeletedCount();
	}

	@Override
	public long deleteTypedResourcePlan(ObjectId work_id, String resId, String domain) {
		DeleteResult dr = c("resourcePlanInTemplate", domain).deleteMany(new Document("work_id", work_id).append("usedTypedResId", resId));
		return dr.getDeletedCount();
	}

	@Override
	public List<ResourcePlanInTemplate> listResourcePlan(ObjectId _id, String domain) {
		return c(ResourcePlanInTemplate.class, domain).aggregate(Domain.getJQ(domain, "查询-模板资源").set("work_id", _id).array())
				.into(new ArrayList<ResourcePlanInTemplate>());
	}

	@Override
	public long delete(ObjectId _id, String domain) {
		ArrayList<ObjectId> ids = c("workInTemplate", domain).distinct("_id", new Document("template_id", _id), ObjectId.class)
				.into(new ArrayList<>());
		c("resourcePlanInTemplate", domain).deleteMany(new Document("work_id", new Document("$in", ids)));
		c("worklinkInTemplate", domain).deleteMany(new Document("template_id", _id));
		c("workInTemplate", domain).deleteMany(new Document("template_id", _id));
		c("obsInTemplate", domain).deleteMany(new Document("scope_id", _id));
		// 删除设置
		c("setting", domain).deleteMany(new Document("name", Pattern.compile("@" + _id.toString(), Pattern.CASE_INSENSITIVE)));
		DeleteResult rs = c("projectTemplate", domain).deleteOne(new Document("_id", _id));
		return rs.getDeletedCount();
	}

	@Override
	public void setEnabled(ObjectId _id, boolean enabled, String domain) {
		c("projectTemplate", domain).updateOne(new Document("_id", _id), new Document("$set", new Document("enabled", enabled)));
	}

	@Override
	public void useTemplate(ObjectId template_id, ObjectId project_id, String user, String domain) {
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 清除当前的编辑数据
		// 1. 获得所有的work
		List<ObjectId> workIds = c("work", domain).distinct("_id", new Document("project_id", project_id), ObjectId.class)
				.into(new ArrayList<>());
		// 2.清除关联的resourcePlan
		c("resourcePlan", domain).deleteMany(new Document("work_id", new Document("$in", workIds)));
		// 3. 清除关联的workPackage
		c("workPackage", domain).deleteMany(new Document("work_id", new Document("$in", workIds)));
		// 4. 清除worklinks
		c("worklinks", domain).deleteMany(new Document("project_id", project_id));
		// 5. 清除work
		c("work", domain).deleteMany(new Document("project_id", project_id));
		// 6. 清除workspace
		c("workspace", domain).deleteMany(new Document("project_id", project_id));
		// 7. 清除worklinksspace
		c("worklinksspace", domain).deleteMany(new Document("project_id", project_id));
		// 9. 清除obs
		c("obs", domain).deleteMany(new Document("$or", Arrays.asList(new Document("scope_id", new Document("$in", workIds)),
				new Document("scopeRoot", false).append("scope_id", project_id))));
		// 10. 清除cbs
		List<ObjectId> cbsIds = c("cbs", domain)
				.distinct("_id",
						new Document("$or",
								Arrays.asList(new Document("scope_id", new Document("$in", workIds)),
										new Document("scopeRoot", false).append("scope_id", project_id))),
						ObjectId.class)
				.into(new ArrayList<>());
		c("cbs", domain).deleteMany(new Document("_id", new Document("$in", cbsIds)));
		c("cbsPeriod", domain).deleteMany(new Document("cbsItem_id", new Document("$in", cbsIds)));
		c("cbsSubject", domain).deleteMany(new Document("cbsItem_id", new Document("$in", cbsIds)));
		// 11. 清除folder
		c("folder", domain).deleteMany(new Document("project_id", project_id));
		// 12. 清楚document
		// TODO

		// 13.清除项目设置
		c("setting", domain).deleteMany(new Document("name", Pattern.compile("@" + project_id.toString(), Pattern.CASE_INSENSITIVE)));

		List<Document> tobeInsert = new ArrayList<>();// 保存准备插入数据库的记录

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 检出项目
		ObjectId space_id = new ObjectId();
		Document project = c("project", domain).find(new Document("_id", project_id)).first();
		c("project", domain).updateOne(new Document("_id", project_id),
				new Document("$set", new Document("checkoutBy", user).append("space_id", space_id)));

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 创建项目文件夹
		Map<ObjectId, ObjectId> folderIdMap = new HashMap<ObjectId, ObjectId>();// 准备模板文件夹和文件夹之间的id对应表
		c("folderInTemplate", domain).find(new Document("template_id", template_id)).sort(new Document("parent_id", 1))
				.forEach((Document doc) -> {
					ObjectId folder_id = new ObjectId();
					folderIdMap.put(doc.getObjectId("_id"), folder_id);
					doc.append("_id", folder_id).append("project_id", project_id).append("parent_id",
							folderIdMap.get(doc.getObjectId("parent_id")));
					doc.remove("template_id");
					tobeInsert.add(doc);
				});
		if (!tobeInsert.isEmpty()) {
			c("folder", domain).insertMany(tobeInsert);
			tobeInsert.clear();
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 创建工作
		boolean stageEnable = Boolean.TRUE.equals(project.getBoolean("stageEnable"));// 根据项目是否启用阶段来设置第一级工作是否为阶段

		ObjectId obs_id = project.getObjectId("obs_id");// 项目的OBS_ID

		Date startOfProject = project.getDate("planStart");
		Date startOfTemplate = c("workInTemplate", domain)
				.aggregate(Arrays.asList(new Document("$match", new Document("template_id", template_id)),
						new Document("$group", new Document("_id", null).append("planStart", new Document("$min", "$planStart")))))
				.first().getDate("planStart");
		long duration = startOfProject.getTime() - startOfTemplate.getTime();// 求出项目计划开始时间和模板时间之差，用于修改工作的计划开始和完成时间

		// 保存准备插入到数据库的工作的开始和完成时间
		Map<ObjectId, Calendar[]> workCalendars = new HashMap<>();

		Map<ObjectId, ObjectId> workIdMap = new HashMap<>();// 准备模板对象和新对象之间的id对应表

		c("workInTemplate", domain).find(new Document("template_id", template_id)).sort(new Document("wbsCode", 1))
				.forEach((Document doc) -> {
					ObjectId _id = new ObjectId();
					workIdMap.put(doc.getObjectId("_id"), _id);// 保存 模板工作_id 对应的 新工作_id

					int index = generateCode(Generator.DEFAULT_NAME, "work", domain);
					String code = String.format("%06d", index);// 生成序号

					ObjectId parent_id = doc.getObjectId("parent_id");
					doc.append("_id", _id).append("space_id", space_id).append("stage", parent_id == null && stageEnable)
							.append("project_id", project_id).append("code", code);
					if (parent_id != null) {
						ObjectId newParentId = workIdMap.get(parent_id);
						doc.put("parent_id", newParentId);
					} else if (stageEnable) {
						doc.put("status", ProjectStatus.Created);
					}

					// 更新计划开始、完成时间
					Calendar planStartCal = Calendar.getInstance();
					Date planStart = doc.getDate("planStart");
					planStartCal.setTimeInMillis(planStart.getTime() + duration);
					doc.append("planStart", planStartCal.getTime());

					Calendar planFinishCal = Calendar.getInstance();
					Date planFinish = doc.getDate("planFinish");
					planFinishCal.setTimeInMillis(planFinish.getTime() + duration);
					doc.append("planFinish", planFinishCal.getTime());

					workCalendars.put(_id, new Calendar[] { planStartCal, planFinishCal });

					doc.remove("template_id");
					tobeInsert.add(doc);
				});
		if (!tobeInsert.isEmpty()) {
			c("workspace", domain).insertMany(tobeInsert);
			tobeInsert.clear();
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 创建工作对应的工作包
		Map<ObjectId, ObjectId> workPackageMap = new HashMap<>();
		c("workPackage", domain).find(new Document("work_id", new Document("$in", workIdMap.keySet()))).forEach((Document d) -> {
			ObjectId newWorkId = workIdMap.get(d.get("work_id"));
			ObjectId oldId = d.getObjectId("_id");
			ObjectId newId = new ObjectId();
			d.append("work_id", newWorkId)//
					.append("workClass", "Work")//
					.append("_id", newId)//
					.remove("chargerId");
			tobeInsert.add(d);
			workPackageMap.put(oldId, newId);
		});
		if (!tobeInsert.isEmpty()) {
			c("workPackage", domain).insertMany(tobeInsert);
			tobeInsert.clear();
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 创建工作包对应文档设置
		// TODO
		// workPackage_id
		// folderInTemplate_id
		c("docuSetting", domain).find(new Document("workPackage_id", new Document("$in", new ArrayList<>(workPackageMap.keySet()))))
				.forEach((Document d) -> {
					ObjectId oldWorkPackage_id = d.getObjectId("workPackage_id");
					ObjectId newWorkPackage_id = workPackageMap.get(oldWorkPackage_id);
					ObjectId oldFolderId = d.getObjectId("folderInTemplate_id");
					ObjectId newWFolder_id = folderIdMap.get(oldFolderId);

					d.append("_id", new ObjectId())//
							.append("workPackage_id", newWorkPackage_id)//
							.append("folder_id", newWFolder_id)//
							.remove("folderInTemplate_id");
					tobeInsert.add(d);
				});
		if (!tobeInsert.isEmpty()) {
			c("docuSetting", domain).insertMany(tobeInsert);
			tobeInsert.clear();
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 创建工作的资源计划
		c("resourcePlanInTemplate", domain).find(new Document("work_id", new Document("$in", workIdMap.keySet()))).forEach((Document d) -> {
			ObjectId work_id = d.getObjectId("work_id");
			ObjectId newWorkId = workIdMap.get(work_id);
			ObjectId resTypeId = d.getObjectId("resTypeId");
			d.append("work_id", newWorkId);
			Calendar[] calendar = workCalendars.get(newWorkId);

			double works = getWorkingHoursPerDay(resTypeId, domain);

			while (calendar[0].getTime().before(calendar[1].getTime())) {
				if (checkDayIsWorkingDay(calendar[0], resTypeId, domain)) {
					tobeInsert.add(new Document(d)//
							.append("id", calendar[0].getTime())//
							.append("planBasicQty", works)//
							.append("_id", new ObjectId()));
				}
				calendar[0].add(Calendar.DAY_OF_MONTH, 1);
			}

		});
		if (!tobeInsert.isEmpty()) {
			c("resourcePlan", domain).insertMany(tobeInsert);
			tobeInsert.clear();
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 创建工作的关联关系
		c("worklinkInTemplate", domain).find(new Document("template_id", template_id)).forEach((Document doc) -> {
			ObjectId source = workIdMap.get(doc.get("source"));
			ObjectId target = workIdMap.get(doc.get("target"));
			doc.append("_id", new ObjectId()).append("project_id", project_id).append("space_id", space_id).append("source", source)
					.append("target", target).remove("template_id");
			tobeInsert.add(doc);
		});
		if (!tobeInsert.isEmpty()) {
			c("worklinksspace", domain).insertMany(tobeInsert);
			tobeInsert.clear();
		}
		workIdMap.clear();

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 创建组织结构
		Map<ObjectId, ObjectId> obsIdMap = new HashMap<>();

		c("obsInTemplate", domain).find(new Document("scope_id", template_id)).sort(new Document("_id", 1)).forEach((Document doc) -> {
			ObjectId parent_id = doc.getObjectId("parent_id");
			if (parent_id == null) {
				obsIdMap.put(doc.getObjectId("_id"), obs_id);
			} else {
				ObjectId _id = new ObjectId();
				obsIdMap.put(doc.getObjectId("_id"), _id);
				doc.append("_id", _id).append("scope_id", project_id).append("parent_id", obsIdMap.get(parent_id));
				tobeInsert.add(doc);
			}

		});
		if (!tobeInsert.isEmpty()) {
			c("obs", domain).insertMany(tobeInsert);
			tobeInsert.clear();
		}
		obsIdMap.clear();

		// 添加项目设置
		List<Document> list = PROJECT_SETTING_NAMES.stream().map(name -> {
			Document setting = Optional.ofNullable(getSystemSetting(name + "@" + template_id.toString(), domain)).orElse(new Document());
			setting.put("name", name + "@" + project_id.toString());
			setting.remove("_id");
			return setting;
		}).collect(Collectors.toList());
		c("setting", domain).insertMany(list);
	}

	@Override
	public void deleteOBSItem(ObjectId _id, String domain) {
		List<ObjectId> desentItems = getDesentItems(Arrays.asList(_id), "obsInTemplate", "parent_id", domain);
		c(OBSInTemplate.class, domain).deleteMany(new Document("_id", new Document("$in", desentItems)));
		// delete(_id, OBSInTemplate.class,domain);
	}

	@Override
	public Result updateGanttData(WorkspaceGanttData ganttData, String domain) {
		ObjectId template_id = ganttData.getTemplate_id();

		c(WorkInTemplate.class, domain).deleteMany(new Document("template_id", template_id));

		c(WorkLinkInTemplate.class, domain).deleteMany(new Document("template_id", template_id));

		List<WorkInTemplate> workInTemplates = ganttData.getWorkInTemplates();
		for (int i = 0; i < workInTemplates.size(); i++) {
			workInTemplates.get(i).setIndex(i);
		}

		if (workInTemplates.size() > 0)
			c(WorkInTemplate.class, domain).insertMany(workInTemplates);

		List<WorkLinkInTemplate> workLinkInTemplates = ganttData.getWorkLinkInTemplates();
		if (workLinkInTemplates.size() > 0)
			c(WorkLinkInTemplate.class, domain).insertMany(workLinkInTemplates);

		return new Result();
	}

	@Override
	public long countEnabledTemplate(BasicDBObject filter, String domain) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("enabled", true);
		filter.append("module", new BasicDBObject("$ne", true));

		return count(filter, ProjectTemplate.class, domain);
	}

	@Override
	public List<ProjectTemplate> createEnabledTemplateDataSet(BasicDBObject condition,String domain){
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null){
			filter = new BasicDBObject();
			condition.append("filter", filter);
		}
		filter.append("enabled", true);
		filter.append("module", new BasicDBObject("$ne", true));
		return createDataSet(condition, ProjectTemplate.class,domain);
	}

	public long updateFolderInTemplate(BasicDBObject fu, String domain) {
		return update(fu, FolderInTemplate.class, domain);
	}

	@Override
	public long update(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, "projectTemplate");
	}

	@Override
	public OBSModule insertOBSModule(OBSModule obsModule, String domain) {
		obsModule = insert(obsModule,domain);
		// 使用组织模板名称和id构建OBSInTemplate的根节点
		insert(OBSInTemplate.getInstanceTeam(obsModule.get_id(), null, obsModule.getName(), obsModule.getId(), obsModule.getName(), true),domain);// 插入根节点记录
		return obsModule;
	}

	@Override
	public long countOBSModule(BasicDBObject filter, String domain) {
		return count(filter, OBSModule.class, domain);
	}

	@Override
	public List<OBSModule> listOBSModule(BasicDBObject condition, String domain) {
		// 附加模板中角色信息
		return list(OBSModule.class, domain, condition, Domain.getJQ(domain, "追加-组织模板-角色").array());
	}

	@Override
	public long deleteOBSModule(ObjectId _id, String domain) {
		c("obsInTemplate", domain).deleteMany(new Document("scope_id", _id));
		return delete(_id, OBSModule.class, domain);
	}

	@Override
	public long updateOBSModule(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, OBSModule.class, domain);
	}

	@Override
	public List<OBSInTemplate> getOBSInTemplateByModule(ObjectId module_id, String domain) {
		return getOBSTemplate(module_id,domain);
	}

	@Override
	public OBSModule getOBSModule(ObjectId _id, String domain) {

		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new BasicDBObject("_id", _id)));
		pipeline.addAll(Domain.getJQ(domain, "追加-组织模板-角色").array());

		return c(OBSModule.class, domain).aggregate(pipeline).first();
	}

	@Override
	public void useOBSModule(ObjectId obsModule_id, ObjectId project_id,String domain){
		// 清除除OBS根外的项目OBS节点，
		c("obs",domain).deleteMany(new Document("scopeRoot", false).append("scope_id", project_id));
		// 准备模板对象和新对象之间的id对应表
		Map<ObjectId, ObjectId> idMap = new HashMap<ObjectId, ObjectId>();
		// 保存准备插入数据库的记录
		List<Document> tobeInsert = new ArrayList<>();
		// 项目的OBS_ID
		ObjectId obs_id = c("project",domain).distinct("obs_id", new Document("_id", project_id), ObjectId.class).first();

		// 创建组织结构
		c("obsInTemplate",domain).find(new Document("scope_id", obsModule_id)).sort(new Document("_id", 1)).forEach((Document doc) -> {
			// 建立项目团队上下级关系
			ObjectId parent_id = doc.getObjectId("parent_id");
			if (parent_id == null){
				idMap.put(doc.getObjectId("_id"), obs_id);
			} else {
				ObjectId _id = new ObjectId();
				idMap.put(doc.getObjectId("_id"), _id);
				doc.append("_id", _id).append("scope_id", project_id).append("parent_id", idMap.get(parent_id));
				tobeInsert.add(doc);
			}

		});
		// 插入项目团队
		if (!tobeInsert.isEmpty()){
			c("obs",domain).insertMany(tobeInsert);
		}
	}

	@Override
	public long countOBSModuleSelector(BasicDBObject filter, ObjectId project_id, String domain) {
		// 获取当前项目的eps_id
		ObjectId eps_id = c("project", domain).distinct("eps_id", new Document("_id", project_id), ObjectId.class).first();

		// 根据eps_id查询组织模板
		if (filter == null)
			filter = new BasicDBObject();
		filter.put("eps_id", eps_id);

		return count(filter, OBSModule.class, domain);
	}

	@Override
	public List<OBSModule> listOBSModuleSelector(BasicDBObject condition, ObjectId project_id, String domain) {
		// 获取当前项目的eps_id
		ObjectId eps_id = c("project", domain).distinct("eps_id", new Document("_id", project_id), ObjectId.class).first();

		// 根据eps_id查询组织模板
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.put("eps_id", eps_id);

		return list(OBSModule.class, domain, condition, Domain.getJQ(domain, "追加-组织模板-角色").array());
	}

	@Override
	public void templateOBSSaveAsOBSModule(OBSModule obsModule, ObjectId template_id, String domain) {
		copyToOBSModule(obsModule, "obsInTemplate", new Document("scope_id", template_id), domain);
	}

	/**
	 * 复制OBS结构到组织模板
	 * 
	 * @param obsModule
	 *            组织模板
	 * @param name
	 *            需复制的obs集合
	 * @param filter
	 *            obs查询条件
	 */
	private void copyToOBSModule(OBSModule obsModule, String name, Document filter, String domain) {
		// 1.创建组织模板，
		obsModule = insert(obsModule,domain);
		ObjectId scope_id = obsModule.get_id();
		OBSInTemplate rootOBS = createRootOBS(scope_id, domain);

		// 2.将组织结构复制到组织模板中
		Map<ObjectId, ObjectId> idMap = new HashMap<ObjectId, ObjectId>();
		List<Document> tobeInsert = new ArrayList<>();

		// 创建组织结构
		c(name,domain).find(filter).sort(new Document("_id", 1)).forEach((Document doc) -> {
			// 建立项目团队上下级关系
			ObjectId parent_id = doc.getObjectId("parent_id");
			if (parent_id == null) {
				idMap.put(doc.getObjectId("_id"), rootOBS.get_id());
			} else {
				ObjectId _id = new ObjectId();
				idMap.put(doc.getObjectId("_id"), _id);
				doc.append("_id", _id).append("scope_id", scope_id).append("parent_id", idMap.get(parent_id));
				tobeInsert.add(doc);
			}

		});
		// 插入组织模板
		if (!tobeInsert.isEmpty()) {
			c("obsInTemplate", domain).insertMany(tobeInsert);
		}
	}

	@Override
	public void projectOBSSaveAsOBSModule(OBSModule obsModule, ObjectId project_id, String domain) {
		// TODO 暂不考虑阶段组织结构
		copyToOBSModule(obsModule, "obs", new Document("scope_id", project_id), domain);
	}

}
