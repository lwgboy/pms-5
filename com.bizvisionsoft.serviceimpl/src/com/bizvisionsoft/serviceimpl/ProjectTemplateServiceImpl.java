package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.FolderInTemplate;
import com.bizvisionsoft.service.model.OBSInTemplate;
import com.bizvisionsoft.service.model.OBSItem;
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
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.DeleteResult;

public class ProjectTemplateServiceImpl extends BasicServiceImpl implements ProjectTemplateService {

	@Override
	public ProjectTemplate insertProjectTemplate(ProjectTemplate prjt) {
		return insert(prjt);
	}

	@Override
	public WBSModule insertWBSModule(WBSModule prjt) {
		return insert(prjt);
	}

	@Override
	public ProjectTemplate get(ObjectId _id) {
		return get(_id, ProjectTemplate.class);
	}

	@Override
	public long countProjectTemplate(BasicDBObject filter) {
		filter.append("module", false);
		return count(filter, ProjectTemplate.class);
	}

	@Override
	public long countWBSModule(BasicDBObject filter) {
		filter.append("module", true);
		return count(filter, ProjectTemplate.class);
	}

	@Override
	public List<ProjectTemplate> listProjectTemplate(BasicDBObject condition) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("module", new BasicDBObject("$ne", true));
		return createDataSet(condition, ProjectTemplate.class);
	}

	@Override
	public List<WBSModule> listWBSModule(BasicDBObject condition) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("module", true);
		return createDataSet(condition, WBSModule.class);
	}

	@Override
	public WorkInTemplate getWorkInTemplate(ObjectId work_id) {
		return get(work_id, WorkInTemplate.class);
	}

	@Override
	public List<WorkInTemplate> listWorks(ObjectId template_id) {
		return c(WorkInTemplate.class).find(new BasicDBObject("template_id", template_id))
				.sort(new BasicDBObject("index", 1)).into(new ArrayList<WorkInTemplate>());
	}

	@Override
	public List<WorkLinkInTemplate> listLinks(ObjectId template_id) {
		return c(WorkLinkInTemplate.class).find(new BasicDBObject("template_id", template_id))
				.into(new ArrayList<WorkLinkInTemplate>());
	}

	@Override
	public WorkInTemplate insertWork(WorkInTemplate work) {
		return insert(work);
	}

	@Override
	public long updateWork(BasicDBObject bson) {
		return update(bson, WorkInTemplate.class);
	}

	@Override
	public long deleteWork(ObjectId work_id) {
		// TODO 删除link
		return delete(work_id, WorkInTemplate.class);
	}

	@Override
	public WorkLinkInTemplate insertLink(WorkLinkInTemplate link) {
		return insert(link);
	}

	@Override
	public long updateLink(BasicDBObject bson) {
		return update(bson, WorkLinkInTemplate.class);
	}

	@Override
	public long deleteLink(ObjectId link_id) {
		return delete(link_id, WorkLinkInTemplate.class);
	}

	@Override
	public int nextWBSIndex(BasicDBObject condition) {
		Document doc = c("workInTemplate").find(condition).sort(new BasicDBObject("index", -1))
				.projection(new BasicDBObject("index", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("index", 0)).orElse(0) + 1;
	}

	@Override
	public int nextOBSSeq(BasicDBObject condition) {
		Document doc = c("obsInTemplate").find(condition).sort(new BasicDBObject("seq", -1))
				.projection(new BasicDBObject("seq", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("seq", 0)).orElse(0) + 1;
	}

	@Override
	public List<OBSInTemplate> getOBSTemplate(ObjectId template_id) {
		return queryOBSTemplate(new BasicDBObject("scope_id", template_id));
	}

	private List<OBSInTemplate> queryOBSTemplate(BasicDBObject match) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(match));

		appendUserInfoAndHeadPic(pipeline, "managerId", "managerInfo", "managerHeadPic");

		appendSortBy(pipeline, "seq", 1);

		return c(OBSInTemplate.class).aggregate(pipeline).into(new ArrayList<OBSInTemplate>());
	}

	@Override
	public boolean hasOBS(ObjectId _id) {
		return c("obsInTemplate").countDocuments(new BasicDBObject("scope_id", _id)) > 0;
	}

	@Override
	public OBSInTemplate createRootOBS(ObjectId _id) {
		OBSInTemplate obsRoot = new OBSInTemplate()// 创建本项目的OBS根节点
				.set_id(new ObjectId())// 设置_id与项目关联
				.setScope_id(_id)// 设置scope_id表明该组织节点是该项目的组织
				.setParent_id(null)// 设置上级的id
				.setName("项目组")// 设置该组织节点的默认名称
				.setRoleId(OBSItem.ID_PM)// 设置该组织节点的角色id
				.setRoleName(OBSItem.NAME_PM)// 设置该组织节点的名称
				.setScopeRoot(true);// 区分这个节点是范围内的根节点
		return insert(obsRoot);// 插入记录
	}

	@Override
	public OBSInTemplate insertOBSItem(OBSInTemplate t) {
		try {
			return insert(t);
		} catch (Exception e) {
			throw handleDuplicateIndexError(e, "角色编号重复");
		}
	}

	@Override
	public long updateOBSItem(BasicDBObject fu) {
		return update(fu, OBSInTemplate.class);
	}

	@Override
	public List<WorkInTemplate> listWBSRoot(ObjectId template_id) {
		List<? extends Bson> pipeline = new JQ("获得项目模板WBS根节点").set("template_id", template_id).set("parent_id", null)
				.array();
		return c(WorkInTemplate.class).aggregate(pipeline).into(new ArrayList<WorkInTemplate>());
	}

	@Override
	public long countWBSRoot(ObjectId template_id) {
		return count(new BasicDBObject("template_id", template_id).append("parent_id", null), "workInTemplate");
	}

	@Override
	public List<WorkInTemplate> listWBSChildren(ObjectId parent_id) {
		List<? extends Bson> pipeline = new JQ("获得项目模板WBS下级节点").set("parent_id", parent_id).array();
		return c(WorkInTemplate.class).aggregate(pipeline).into(new ArrayList<WorkInTemplate>());
	}

	@Override
	public long countWBSChildren(ObjectId parent_id) {
		return count(new BasicDBObject("parent_id", parent_id), "workInTemplate");
	}

	@Override
	public List<OBSInTemplate> getOBSRoleTemplate(ObjectId template_id) {
		return queryOBSTemplate(new BasicDBObject("scope_id", template_id));
	}

	@Override
	public List<ResourcePlanInTemplate> addResourcePlan(List<ResourceAssignment> resas) {
		List<ResourcePlanInTemplate> documents = new ArrayList<ResourcePlanInTemplate>();
		resas.forEach(r -> documents.add(r.getResourcePlanInTemplate()));
		c(ResourcePlanInTemplate.class).insertMany(documents);
		return documents;
	}

	@Override
	public long updateResourcePlan(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, ResourcePlan.class);
	}

	@Override
	public long deleteHumanResourcePlan(ObjectId work_id, String resId) {
		DeleteResult dr = c("resourcePlanInTemplate")
				.deleteMany(new Document("work_id", work_id).append("usedHumanResId", resId));
		return dr.getDeletedCount();
	}

	@Override
	public long deleteEquipmentResourcePlan(ObjectId work_id, String resId) {
		DeleteResult dr = c("resourcePlanInTemplate")
				.deleteMany(new Document("work_id", work_id).append("usedEquipResId", resId));
		return dr.getDeletedCount();
	}

	@Override
	public long deleteTypedResourcePlan(ObjectId work_id, String resId) {
		DeleteResult dr = c("resourcePlanInTemplate")
				.deleteMany(new Document("work_id", work_id).append("usedTypedResId", resId));
		return dr.getDeletedCount();
	}

	@Override
	public List<ResourcePlanInTemplate> listResourcePlan(ObjectId _id) {
		return c(ResourcePlanInTemplate.class).aggregate(new JQ("查询模板工作的资源").set("work_id", _id).array())
				.into(new ArrayList<ResourcePlanInTemplate>());
	}

	@Override
	public long delete(ObjectId _id) {
		ArrayList<ObjectId> ids = c("workInTemplate").distinct("_id", new Document("template_id", _id), ObjectId.class)
				.into(new ArrayList<>());
		c("resourcePlanInTemplate").deleteMany(new Document("work_id", new Document("$in", ids)));
		c("worklinkInTemplate").deleteMany(new Document("template_id", _id));
		c("workInTemplate").deleteMany(new Document("template_id", _id));
		c("obsInTemplate").deleteMany(new Document("scope_id", _id));
		DeleteResult rs = c("projectTemplate").deleteOne(new Document("_id", _id));
		return rs.getDeletedCount();
	}

	@Override
	public void setEnabled(ObjectId _id, boolean enabled) {
		c("projectTemplate").updateOne(new Document("_id", _id),
				new Document("$set", new Document("enabled", enabled)));
	}

	@Override
	public void useTemplate(ObjectId template_id, ObjectId project_id, String user) {
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 清除当前的编辑数据
		// 1. 获得所有的work
		List<ObjectId> workIds = c("work").distinct("_id", new Document("project_id", project_id), ObjectId.class)
				.into(new ArrayList<>());
		// 2.清除关联的resourcePlan
		c("resourcePlan").deleteMany(new Document("work_id", new Document("$in", workIds)));
		// 3. 清除关联的workPackage
		c("workPackage").deleteMany(new Document("work_id", new Document("$in", workIds)));
		// 4. 清除worklinks
		c("worklinks").deleteMany(new Document("project_id", project_id));
		// 5. 清除work
		c("work").deleteMany(new Document("project_id", project_id));
		// 6. 清除workspace
		c("workspace").deleteMany(new Document("project_id", project_id));
		// 7. 清除worklinksspace
		c("worklinksspace").deleteMany(new Document("project_id", project_id));

		// 9. 清除obs
		c("obs").deleteMany(new Document("$or", Arrays.asList(new Document("scope_id", new Document("$in", workIds)),
				new Document("scopeRoot", false).append("scope_id", project_id))));
		// 10. 清除cbs
		List<ObjectId> cbsIds = c("cbs").distinct("_id",
				new Document("$or",
						Arrays.asList(new Document("scope_id", new Document("$in", workIds)),
								new Document("scopeRoot", false).append("scope_id", project_id))),
				ObjectId.class).into(new ArrayList<>());

		c("cbs").deleteMany(new Document("_id", new Document("$in", cbsIds)));
		c("cbsPeriod").deleteMany(new Document("cbsItem_id", new Document("$in", cbsIds)));
		c("cbsSubject").deleteMany(new Document("cbsItem_id", new Document("$in", cbsIds)));

		// 11. 清除folder
		c("folder").deleteMany(new Document("project_id", project_id));

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 检出项目
		ObjectId space_id = new ObjectId();
		Document project = c("project").find(new Document("_id", project_id)).first();
		c("project").updateOne(new Document("_id", project_id),
				new Document("$set", new Document("checkoutBy", user).append("space_id", space_id)));

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 创建工作
		//
		Map<ObjectId, ObjectId> idMap = new HashMap<ObjectId, ObjectId>();
		List<Document> tobeInsert = new ArrayList<>();

		boolean stageEnable = Boolean.TRUE.equals(project.getBoolean("stageEnable"));
		ObjectId obs_id = project.getObjectId("obs_id");

		Date planStartInProject = project.getDate("planStart");
		Date planStartInTemplate = c("workInTemplate")
				.aggregate(Arrays.asList(new Document("$match", new Document("template_id", template_id)),
						new Document("$group",
								new Document("_id", null).append("planStart", new Document("$min", "$planStart")))))
				.first().getDate("planStart");

		long duration = planStartInProject.getTime() - planStartInTemplate.getTime();

		Map<ObjectId, List<Calendar>> workDates = new HashMap<ObjectId, List<Calendar>>();

		c("workInTemplate").find(new Document("template_id", template_id)).sort(new Document("wbsCode", 1))
				.forEach((Document doc) -> {
					ObjectId _id = new ObjectId();
					idMap.put(doc.getObjectId("_id"), _id);

					int index = generateCode(Generator.DEFAULT_NAME, "work");
					String code = String.format("%06d", index);

					ObjectId parent_id = doc.getObjectId("parent_id");
					doc.append("_id", _id).append("space_id", space_id)
							.append("stage", parent_id == null && stageEnable).append("project_id", project_id)
							.append("code", code);
					if (parent_id != null) {
						ObjectId newParentId = idMap.get(parent_id);
						doc.put("parent_id", newParentId);
					} else if (stageEnable) {
						doc.put("status", ProjectStatus.Created);
					}
					// 更新计划开始、完成时间
					Date planStart = doc.getDate("planStart");
					Date planFinish = doc.getDate("planFinish");

					Calendar planStartCal = Calendar.getInstance();
					planStartCal.setTimeInMillis(planStart.getTime() + duration);

					Calendar planFinishCal = Calendar.getInstance();
					planFinishCal.setTimeInMillis(planFinish.getTime() + duration);

					doc.append("planStart", planStartCal.getTime());
					doc.append("planFinish", planFinishCal.getTime());
					List<Calendar> value = new ArrayList<Calendar>();
					value.add(planStartCal);
					value.add(planFinishCal);
					workDates.put(_id, value);

					// 生成资源计划

					doc.remove("template_id");
					tobeInsert.add(doc);
				});
		if (!tobeInsert.isEmpty()) {
			c("workspace").insertMany(tobeInsert);
			tobeInsert.clear();
		}

		c("workPackage").find(new Document("work_id", new Document("$in", idMap.keySet()))).forEach((Document d) -> {
			ObjectId newWorkId = idMap.get(d.get("work_id"));
			d.append("work_id", newWorkId).append("_id", new ObjectId()).remove("chargerId");
			tobeInsert.add(d);
		});
		if (!tobeInsert.isEmpty()) {
			c("workPackage").insertMany(tobeInsert);
			tobeInsert.clear();
		}

		c("resourcePlanInTemplate").find(new Document("work_id", new Document("$in", idMap.keySet())))
				.forEach((Document d) -> {
					ObjectId work_id = d.getObjectId("work_id");
					ObjectId newWorkId = idMap.get(work_id);
					ObjectId resTypeId = d.getObjectId("resTypeId");
					d.append("work_id", newWorkId);
					List<Calendar> workDate = workDates.get(newWorkId);
					Calendar planStartCal = workDate.get(0);
					Calendar planFinishCal = workDate.get(1);

					double works = getWorkingHoursPerDay(resTypeId);

					while (planStartCal.getTime().before(planFinishCal.getTime())) {
						Document doc = new Document(d);
						if (checkDayIsWorkingDay(planStartCal, resTypeId)) {
							doc.append("id", planStartCal.getTime());
							doc.append("planBasicQty", works);
							doc.append("_id", new ObjectId());
							tobeInsert.add(doc);
						}
						planStartCal.add(Calendar.DAY_OF_MONTH, 1);
					}

				});
		if (!tobeInsert.isEmpty()) {
			c("resourcePlan").insertMany(tobeInsert);
			tobeInsert.clear();
		}

		c("worklinkInTemplate").find(new Document("template_id", template_id)).forEach((Document doc) -> {
			ObjectId source = idMap.get(doc.get("source"));
			ObjectId target = idMap.get(doc.get("target"));
			doc.append("_id", new ObjectId()).append("project_id", project_id).append("space_id", space_id)
					.append("source", source).append("target", target).remove("template_id");
			tobeInsert.add(doc);
		});
		if (!tobeInsert.isEmpty()) {
			c("worklinksspace").insertMany(tobeInsert);
			tobeInsert.clear();
		}
		idMap.clear();

		c("obsInTemplate").find(new Document("scope_id", template_id)).sort(new Document("_id", 1))
				.forEach((Document doc) -> {
					ObjectId parent_id = doc.getObjectId("parent_id");
					if (parent_id == null) {
						idMap.put(doc.getObjectId("_id"), obs_id);
					} else {
						ObjectId _id = new ObjectId();
						idMap.put(doc.getObjectId("_id"), _id);
						doc.append("_id", _id).append("scope_id", project_id).append("parent_id", idMap.get(parent_id));
						tobeInsert.add(doc);
					}

				});
		if (!tobeInsert.isEmpty()) {
			c("obs").insertMany(tobeInsert);
			tobeInsert.clear();
		}
		idMap.clear();

		c("folderInTemplate").find(new Document("template_id", template_id)).sort(new Document("parent_id", 1))
				.forEach((Document doc) -> {
					ObjectId folder_id = new ObjectId();
					idMap.put(doc.getObjectId("_id"), folder_id);
					doc.append("_id", folder_id).append("project_id", project_id).append("parent_id",
							idMap.get(doc.getObjectId("parent_id")));
					doc.remove("template_id");
					tobeInsert.add(doc);
				});
		if (!tobeInsert.isEmpty()) {
			c("folder").insertMany(tobeInsert);
			tobeInsert.clear();
		}
		idMap.clear();

	}

	@Override
	public void deleteOBSItem(ObjectId _id) {
		List<ObjectId> desentItems = getDesentItems(Arrays.asList(_id), "obsInTemplate", "parent_id");
		c(OBSInTemplate.class).deleteMany(new Document("_id", new Document("$in", desentItems)));
		// delete(_id, OBSInTemplate.class);
	}

	@Override
	public Result updateGanttData(WorkspaceGanttData ganttData) {
		ObjectId template_id = ganttData.getTemplate_id();

		c(WorkInTemplate.class).deleteMany(new Document("template_id", template_id));

		c(WorkLinkInTemplate.class).deleteMany(new Document("template_id", template_id));

		List<WorkInTemplate> workInTemplates = ganttData.getWorkInTemplates();
		for (int i = 0; i < workInTemplates.size(); i++) {
			workInTemplates.get(i).setIndex(i);
		}

		if (workInTemplates.size() > 0)
			c(WorkInTemplate.class).insertMany(workInTemplates);

		List<WorkLinkInTemplate> workLinkInTemplates = ganttData.getWorkLinkInTemplates();
		if (workLinkInTemplates.size() > 0)
			c(WorkLinkInTemplate.class).insertMany(workLinkInTemplates);

		return new Result();
	}

	@Override
	public long countEnabledTemplate(BasicDBObject filter) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("enabled", true);
		filter.append("module", new BasicDBObject("$ne", true));

		return count(filter, ProjectTemplate.class);
	}

	@Override
	public List<ProjectTemplate> createEnabledTemplateDataSet(BasicDBObject condition) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.append("filter", filter);
		}
		filter.append("enabled", true);
		filter.append("module", new BasicDBObject("$ne", true));
		return createDataSet(condition, ProjectTemplate.class);
	}

	@Override
	public FolderInTemplate insertFolderInTemplate(FolderInTemplate folder) {
		return insert(folder, FolderInTemplate.class);
	}

	@Override
	public long deleteFolderInTemplate(ObjectId _id) {
		List<ObjectId> desentItems = getDesentItems(Arrays.asList(_id), "folderInTemplate", "parent_id");
		return c(FolderInTemplate.class).deleteOne(new BasicDBObject("_id", new BasicDBObject("$in", desentItems)))
				.getDeletedCount();
	}

	@Override
	public FolderInTemplate getFolderInTemplate(ObjectId _id) {
		return get(_id, FolderInTemplate.class);
	}

	@Override
	public long countFolderInTemplate(BasicDBObject filter, ObjectId _id) {
		if (filter == null)
			filter = new BasicDBObject();
		filter.append("template_id", _id);
		filter.append("parent_id", null);
		return count(filter, FolderInTemplate.class);
	}

	@Override
	public List<FolderInTemplate> createFolderInTemplateDataSet(BasicDBObject condition, ObjectId _id) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.append("filter", filter);
		}
		filter.append("template_id", _id);
		filter.append("parent_id", null);
		return createDataSet(condition, FolderInTemplate.class);
	}

	public long updateFolderInTemplate(BasicDBObject fu) {
		return update(fu, FolderInTemplate.class);
	}

	@Override
	public List<FolderInTemplate> listChildrenFolderInTemplate(ObjectId _id) {
		return createDataSet(new Query().filter(new BasicDBObject("parent_id", _id)).bson(), FolderInTemplate.class);
	}

	@Override
	public long countChildrenFolderInTemplate(ObjectId _id) {
		return count(new BasicDBObject("parent_id", _id), FolderInTemplate.class);
	}

	@Override
	public long update(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, "projectTemplate");
	}

}
