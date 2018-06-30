package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.OBSInTemplate;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.ResourcePlanInTemplate;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.service.model.WorkLinkInTemplate;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.DeleteResult;

public class ProjectTemplateServiceImpl extends BasicServiceImpl implements ProjectTemplateService {

	@Override
	public ProjectTemplate insert(ProjectTemplate prjt) {
		return insert(prjt, ProjectTemplate.class);
	}

	@Override
	public ProjectTemplate get(ObjectId _id) {
		return get(_id, ProjectTemplate.class);
	}

	@Override
	public long count(BasicDBObject filter) {
		return count(filter, ProjectTemplate.class);
	}

	@Override
	public List<ProjectTemplate> createDataSet(BasicDBObject condition) {
		return createDataSet(condition, ProjectTemplate.class);
	}

	@Override
	public WorkInTemplate getWorkInTemplate(ObjectId work_id) {
		return get(work_id, WorkInTemplate.class);
	}

	@Override
	public List<WorkInTemplate> listWorks(ObjectId template_id) {
		return c(WorkInTemplate.class).find(new BasicDBObject("template_id", template_id))
				.into(new ArrayList<WorkInTemplate>());
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
		return insert(t);
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
		return queryOBSTemplate(new BasicDBObject("scope_id", template_id).append("isRole", true));
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
		List<ObjectId> ids = c("work").distinct("_id", new Document("project_id", project_id), ObjectId.class)
				.into(new ArrayList<>());
		// 2.清除关联的resourcePlan
		c("resourcePlan").deleteMany(new Document("work_id", new Document("$in", ids)));
		// 3. 清除关联的workPackage
		c("workPackage").deleteMany(new Document("work_id", new Document("$in", ids)));
		// 4. 清除worklinks
		c("worklinks").deleteMany(new Document("project_id", project_id));
		// 5. 清除work
		c("work").deleteMany(new Document("project_id", project_id));
		// 6. 清除workspace
		c("workspace").deleteMany(new Document("project_id", project_id));
		// 7. 清除worklinksspace
		c("worklinksspace").deleteMany(new Document("project_id", project_id));

		// 9. 清除obs
		c("obs").deleteMany(new Document("$or", Arrays.asList(new Document("scope_id", new Document("$in", ids)),
				new Document("scopeRoot", false).append("scope_id", project_id))));
		// 10. 清除cbs
		c("cbs").deleteMany(new Document("$or", Arrays.asList(new Document("scope_id", new Document("$in", ids)),
				new Document("scopeRoot", false).append("scope_id", project_id))));

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
		String pjNo = project.getString("id");
		ObjectId obs_id = project.getObjectId("obs_id");

		c("workInTemplate").find(new Document("template_id", template_id)).sort(new Document("wbsCode", 1))
				.forEach((Document doc) -> {
					ObjectId _id = new ObjectId();
					idMap.put(doc.getObjectId("_id"), _id);

					int index = generateCode(Generator.DEFAULT_NAME, "work");
					String code = pjNo + String.format("%04d", index);

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
	}

	@Override
	public void deleteOBSItem(ObjectId _id) {
		delete(_id, OBSInTemplate.class);
	}

}
