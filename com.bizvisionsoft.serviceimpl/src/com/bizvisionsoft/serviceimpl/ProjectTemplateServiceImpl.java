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
import com.bizvisionsoft.service.common.query.JQ;
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
	public ProjectTemplate insertProjectTemplate(ProjectTemplate prjt) {
		ProjectTemplate pt = insert(prjt);
		// ���Ĭ��ȫ����Ŀ����
		List<Document> list = PROJECT_SETTING_NAMES.stream().map(name -> {
			Document setting = Optional.ofNullable(getSystemSetting(name)).orElse(new Document());
			setting.put("name", name + "@" + pt.getScope_id());
			setting.remove("_id");
			return setting;
		}).collect(Collectors.toList());
		c("setting").insertMany(list);
		return pt;
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
		return c(WorkInTemplate.class).find(new BasicDBObject("template_id", template_id)).sort(new BasicDBObject("index", 1))
				.into(new ArrayList<WorkInTemplate>());
	}

	@Override
	public List<WorkLinkInTemplate> listLinks(ObjectId template_id) {
		return c(WorkLinkInTemplate.class).find(new BasicDBObject("template_id", template_id)).into(new ArrayList<WorkLinkInTemplate>());
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
		// TODO ɾ��link
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
		Document doc = c("workInTemplate").find(condition).sort(new BasicDBObject("index", -1)).projection(new BasicDBObject("index", 1))
				.first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("index", 0)).orElse(0) + 1;
	}

	@Override
	public int nextOBSSeq(BasicDBObject condition) {
		Document doc = c("obsInTemplate").find(condition).sort(new BasicDBObject("seq", -1)).projection(new BasicDBObject("seq", 1))
				.first();
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
		OBSInTemplate obsRoot = OBSInTemplate.getInstanceTeam(_id, null, "��Ŀ��", OBSItem.ID_PM, OBSItem.NAME_PM, true);
		return insert(obsRoot);// �����¼
	}

	@Override
	public OBSInTemplate insertOBSItem(OBSInTemplate t) {
		try {
			return insert(t);
		} catch (Exception e) {
			throw handleMongoException(e, t.toString());
		}
	}

	@Override
	public long updateOBSItem(BasicDBObject fu) {
		return update(fu, OBSInTemplate.class);
	}

	@Override
	public List<WorkInTemplate> listWBSRoot(ObjectId template_id) {
		List<? extends Bson> pipeline = new JQ("��ѯ-ģ�幤��-WBS��").set("template_id", template_id).set("parent_id", null).array();
		return c(WorkInTemplate.class).aggregate(pipeline).into(new ArrayList<WorkInTemplate>());
	}

	@Override
	public long countWBSRoot(ObjectId template_id) {
		return count(new BasicDBObject("template_id", template_id).append("parent_id", null), "workInTemplate");
	}

	@Override
	public List<WorkInTemplate> listWBSChildren(ObjectId parent_id) {
		List<? extends Bson> pipeline = new JQ("��ѯ-ģ�幤��-�¼�").set("parent_id", parent_id).array();
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
		DeleteResult dr = c("resourcePlanInTemplate").deleteMany(new Document("work_id", work_id).append("usedHumanResId", resId));
		return dr.getDeletedCount();
	}

	@Override
	public long deleteEquipmentResourcePlan(ObjectId work_id, String resId) {
		DeleteResult dr = c("resourcePlanInTemplate").deleteMany(new Document("work_id", work_id).append("usedEquipResId", resId));
		return dr.getDeletedCount();
	}

	@Override
	public long deleteTypedResourcePlan(ObjectId work_id, String resId) {
		DeleteResult dr = c("resourcePlanInTemplate").deleteMany(new Document("work_id", work_id).append("usedTypedResId", resId));
		return dr.getDeletedCount();
	}

	@Override
	public List<ResourcePlanInTemplate> listResourcePlan(ObjectId _id) {
		return c(ResourcePlanInTemplate.class).aggregate(new JQ("��ѯ-ģ����Դ").set("work_id", _id).array())
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
		// ɾ������
		c("setting").deleteMany(new Document("name", Pattern.compile("@" + _id.toString(), Pattern.CASE_INSENSITIVE)));
		DeleteResult rs = c("projectTemplate").deleteOne(new Document("_id", _id));
		return rs.getDeletedCount();
	}

	@Override
	public void setEnabled(ObjectId _id, boolean enabled) {
		c("projectTemplate").updateOne(new Document("_id", _id), new Document("$set", new Document("enabled", enabled)));
	}

	@Override
	public void useTemplate(ObjectId template_id, ObjectId project_id, String user) {
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// �����ǰ�ı༭����
		// 1. ������е�work
		List<ObjectId> workIds = c("work").distinct("_id", new Document("project_id", project_id), ObjectId.class).into(new ArrayList<>());
		// 2.���������resourcePlan
		c("resourcePlan").deleteMany(new Document("work_id", new Document("$in", workIds)));
		// 3. ���������workPackage
		c("workPackage").deleteMany(new Document("work_id", new Document("$in", workIds)));
		// 4. ���worklinks
		c("worklinks").deleteMany(new Document("project_id", project_id));
		// 5. ���work
		c("work").deleteMany(new Document("project_id", project_id));
		// 6. ���workspace
		c("workspace").deleteMany(new Document("project_id", project_id));
		// 7. ���worklinksspace
		c("worklinksspace").deleteMany(new Document("project_id", project_id));
		// 9. ���obs
		c("obs").deleteMany(new Document("$or", Arrays.asList(new Document("scope_id", new Document("$in", workIds)),
				new Document("scopeRoot", false).append("scope_id", project_id))));
		// 10. ���cbs
		List<ObjectId> cbsIds = c("cbs")
				.distinct("_id",
						new Document("$or",
								Arrays.asList(new Document("scope_id", new Document("$in", workIds)),
										new Document("scopeRoot", false).append("scope_id", project_id))),
						ObjectId.class)
				.into(new ArrayList<>());
		c("cbs").deleteMany(new Document("_id", new Document("$in", cbsIds)));
		c("cbsPeriod").deleteMany(new Document("cbsItem_id", new Document("$in", cbsIds)));
		c("cbsSubject").deleteMany(new Document("cbsItem_id", new Document("$in", cbsIds)));
		// 11. ���folder
		c("folder").deleteMany(new Document("project_id", project_id));
		// 12. ���document
		// TODO

		// 13.�����Ŀ����
		c("setting").deleteMany(new Document("name", Pattern.compile("@" + project_id.toString(), Pattern.CASE_INSENSITIVE)));

		List<Document> tobeInsert = new ArrayList<>();// ����׼���������ݿ�ļ�¼

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// �����Ŀ
		ObjectId space_id = new ObjectId();
		Document project = c("project").find(new Document("_id", project_id)).first();
		c("project").updateOne(new Document("_id", project_id),
				new Document("$set", new Document("checkoutBy", user).append("space_id", space_id)));

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ������Ŀ�ļ���
		Map<ObjectId, ObjectId> folderIdMap = new HashMap<ObjectId, ObjectId>();// ׼��ģ���ļ��к��ļ���֮���id��Ӧ��
		c("folderInTemplate").find(new Document("template_id", template_id)).sort(new Document("parent_id", 1)).forEach((Document doc) -> {
			ObjectId folder_id = new ObjectId();
			folderIdMap.put(doc.getObjectId("_id"), folder_id);
			doc.append("_id", folder_id).append("project_id", project_id).append("parent_id",
					folderIdMap.get(doc.getObjectId("parent_id")));
			doc.remove("template_id");
			tobeInsert.add(doc);
		});
		if (!tobeInsert.isEmpty()) {
			c("folder").insertMany(tobeInsert);
			tobeInsert.clear();
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ��������
		boolean stageEnable = Boolean.TRUE.equals(project.getBoolean("stageEnable"));// ������Ŀ�Ƿ����ý׶������õ�һ�������Ƿ�Ϊ�׶�

		ObjectId obs_id = project.getObjectId("obs_id");// ��Ŀ��OBS_ID

		Date startOfProject = project.getDate("planStart");
		Date startOfTemplate = c("workInTemplate")
				.aggregate(Arrays.asList(new Document("$match", new Document("template_id", template_id)),
						new Document("$group", new Document("_id", null).append("planStart", new Document("$min", "$planStart")))))
				.first().getDate("planStart");
		long duration = startOfProject.getTime() - startOfTemplate.getTime();// �����Ŀ�ƻ���ʼʱ���ģ��ʱ��֮������޸Ĺ����ļƻ���ʼ�����ʱ��

		// ����׼�����뵽���ݿ�Ĺ����Ŀ�ʼ�����ʱ��
		Map<ObjectId, Calendar[]> workCalendars = new HashMap<>();

		Map<ObjectId, ObjectId> workIdMap = new HashMap<>();// ׼��ģ�������¶���֮���id��Ӧ��

		c("workInTemplate").find(new Document("template_id", template_id)).sort(new Document("wbsCode", 1)).forEach((Document doc) -> {
			ObjectId _id = new ObjectId();
			workIdMap.put(doc.getObjectId("_id"), _id);// ���� ģ�幤��_id ��Ӧ�� �¹���_id

			int index = generateCode(Generator.DEFAULT_NAME, "work");
			String code = String.format("%06d", index);// �������

			ObjectId parent_id = doc.getObjectId("parent_id");
			doc.append("_id", _id).append("space_id", space_id).append("stage", parent_id == null && stageEnable)
					.append("project_id", project_id).append("code", code);
			if (parent_id != null) {
				ObjectId newParentId = workIdMap.get(parent_id);
				doc.put("parent_id", newParentId);
			} else if (stageEnable) {
				doc.put("status", ProjectStatus.Created);
			}

			// ���¼ƻ���ʼ�����ʱ��
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
			c("workspace").insertMany(tobeInsert);
			tobeInsert.clear();
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ����������Ӧ�Ĺ�����
		Map<ObjectId, ObjectId> workPackageMap = new HashMap<>();
		c("workPackage").find(new Document("work_id", new Document("$in", workIdMap.keySet()))).forEach((Document d) -> {
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
			c("workPackage").insertMany(tobeInsert);
			tobeInsert.clear();
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ������������Ӧ�ĵ�����
		// TODO
		// workPackage_id
		// folderInTemplate_id
		c("docuSetting").find(new Document("workPackage_id", new Document("$in", new ArrayList<>(workPackageMap.keySet()))))
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
			c("docuSetting").insertMany(tobeInsert);
			tobeInsert.clear();
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ������������Դ�ƻ�
		c("resourcePlanInTemplate").find(new Document("work_id", new Document("$in", workIdMap.keySet()))).forEach((Document d) -> {
			ObjectId work_id = d.getObjectId("work_id");
			ObjectId newWorkId = workIdMap.get(work_id);
			ObjectId resTypeId = d.getObjectId("resTypeId");
			d.append("work_id", newWorkId);
			Calendar[] calendar = workCalendars.get(newWorkId);

			double works = getWorkingHoursPerDay(resTypeId);

			while (calendar[0].getTime().before(calendar[1].getTime())) {
				if (checkDayIsWorkingDay(calendar[0], resTypeId)) {
					tobeInsert.add(new Document(d)//
							.append("id", calendar[0].getTime())//
							.append("planBasicQty", works)//
							.append("_id", new ObjectId()));
				}
				calendar[0].add(Calendar.DAY_OF_MONTH, 1);
			}

		});
		if (!tobeInsert.isEmpty()) {
			c("resourcePlan").insertMany(tobeInsert);
			tobeInsert.clear();
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ���������Ĺ�����ϵ
		c("worklinkInTemplate").find(new Document("template_id", template_id)).forEach((Document doc) -> {
			ObjectId source = workIdMap.get(doc.get("source"));
			ObjectId target = workIdMap.get(doc.get("target"));
			doc.append("_id", new ObjectId()).append("project_id", project_id).append("space_id", space_id).append("source", source)
					.append("target", target).remove("template_id");
			tobeInsert.add(doc);
		});
		if (!tobeInsert.isEmpty()) {
			c("worklinksspace").insertMany(tobeInsert);
			tobeInsert.clear();
		}
		workIdMap.clear();

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ������֯�ṹ
		Map<ObjectId, ObjectId> obsIdMap = new HashMap<>();

		c("obsInTemplate").find(new Document("scope_id", template_id)).sort(new Document("_id", 1)).forEach((Document doc) -> {
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
			c("obs").insertMany(tobeInsert);
			tobeInsert.clear();
		}
		obsIdMap.clear();

		// �����Ŀ����
		List<Document> list = PROJECT_SETTING_NAMES.stream().map(name -> {
			Document setting = Optional.ofNullable(getSystemSetting(name + "@" + template_id.toString())).orElse(new Document());
			setting.put("name", name + "@" + project_id.toString());
			setting.remove("_id");
			return setting;
		}).collect(Collectors.toList());
		c("setting").insertMany(list);
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

	public long updateFolderInTemplate(BasicDBObject fu) {
		return update(fu, FolderInTemplate.class);
	}

	@Override
	public long update(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, "projectTemplate");
	}

	@Override
	public OBSModule insertOBSModule(OBSModule obsModule) {
		obsModule = insert(obsModule);
		// ʹ����֯ģ�����ƺ�id����OBSInTemplate�ĸ��ڵ�
		insert(OBSInTemplate.getInstanceTeam(obsModule.get_id(), null, obsModule.getName(), obsModule.getId(), obsModule.getName(), true));// ������ڵ��¼
		return obsModule;
	}

	@Override
	public long countOBSModule(BasicDBObject filter) {
		return count(filter, OBSModule.class);
	}

	@Override
	public List<OBSModule> listOBSModule(BasicDBObject condition) {
		// ����ģ���н�ɫ��Ϣ
		return list(OBSModule.class, condition, new JQ("׷��-��֯ģ��-��ɫ").array());
	}

	@Override
	public long deleteOBSModule(ObjectId _id) {
		c("obsInTemplate").deleteMany(new Document("scope_id", _id));
		return delete(_id, OBSModule.class);
	}

	@Override
	public long updateOBSModule(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, OBSModule.class);
	}

	@Override
	public List<OBSInTemplate> getOBSInTemplateByModule(ObjectId module_id) {
		return getOBSTemplate(module_id);
	}

	@Override
	public OBSModule getOBSModule(ObjectId _id) {

		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new BasicDBObject("_id", _id)));
		pipeline.addAll(new JQ("׷��-��֯ģ��-��ɫ").array());

		return c(OBSModule.class).aggregate(pipeline).first();
	}

	@Override
	public void useOBSModule(ObjectId obsModule_id, ObjectId project_id) {
		// �����OBS�������ĿOBS�ڵ㣬
		c("obs").deleteMany(new Document("scopeRoot", false).append("scope_id", project_id));
		// ׼��ģ�������¶���֮���id��Ӧ��
		Map<ObjectId, ObjectId> idMap = new HashMap<ObjectId, ObjectId>();
		// ����׼���������ݿ�ļ�¼
		List<Document> tobeInsert = new ArrayList<>();
		// ��Ŀ��OBS_ID
		ObjectId obs_id = c("project").distinct("obs_id", new Document("_id", project_id), ObjectId.class).first();

		// ������֯�ṹ
		c("obsInTemplate").find(new Document("scope_id", obsModule_id)).sort(new Document("_id", 1)).forEach((Document doc) -> {
			// ������Ŀ�Ŷ����¼���ϵ
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
		// ������Ŀ�Ŷ�
		if (!tobeInsert.isEmpty()) {
			c("obs").insertMany(tobeInsert);
		}
	}

	@Override
	public long countOBSModuleSelector(BasicDBObject filter, ObjectId project_id) {
		// ��ȡ��ǰ��Ŀ��eps_id
		ObjectId eps_id = c("project").distinct("eps_id", new Document("_id", project_id), ObjectId.class).first();

		// ����eps_id��ѯ��֯ģ��
		if (filter == null)
			filter = new BasicDBObject();
		filter.put("eps_id", eps_id);

		return count(filter, OBSModule.class);
	}

	@Override
	public List<OBSModule> listOBSModuleSelector(BasicDBObject condition, ObjectId project_id) {
		// ��ȡ��ǰ��Ŀ��eps_id
		ObjectId eps_id = c("project").distinct("eps_id", new Document("_id", project_id), ObjectId.class).first();

		// ����eps_id��ѯ��֯ģ��
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.put("eps_id", eps_id);

		return list(OBSModule.class, condition, new JQ("׷��-��֯ģ��-��ɫ").array());
	}

	@Override
	public void templateOBSSaveAsOBSModule(OBSModule obsModule, ObjectId template_id) {
		copyToOBSModule(obsModule, "obsInTemplate", new Document("scope_id", template_id));
	}

	/**
	 * ����OBS�ṹ����֯ģ��
	 * 
	 * @param obsModule
	 *            ��֯ģ��
	 * @param name
	 *            �踴�Ƶ�obs����
	 * @param filter
	 *            obs��ѯ����
	 */
	private void copyToOBSModule(OBSModule obsModule, String name, Document filter) {
		// 1.������֯ģ�壬
		obsModule = insert(obsModule);
		ObjectId scope_id = obsModule.get_id();
		OBSInTemplate rootOBS = createRootOBS(scope_id);

		// 2.����֯�ṹ���Ƶ���֯ģ����
		Map<ObjectId, ObjectId> idMap = new HashMap<ObjectId, ObjectId>();
		List<Document> tobeInsert = new ArrayList<>();

		// ������֯�ṹ
		c(name).find(filter).sort(new Document("_id", 1)).forEach((Document doc) -> {
			// ������Ŀ�Ŷ����¼���ϵ
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
		// ������֯ģ��
		if (!tobeInsert.isEmpty()) {
			c("obsInTemplate").insertMany(tobeInsert);
		}
	}

	@Override
	public void projectOBSSaveAsOBSModule(OBSModule obsModule, ObjectId project_id) {
		// TODO �ݲ����ǽ׶���֯�ṹ
		copyToOBSModule(obsModule, "obs", new Document("scope_id", project_id));
	}

}
