package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.OBSInTemplate;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.ResourcePlanInTemplate;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.service.model.WorkLinkInTemplate;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

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
		return c("obsInTemplate").count(new BasicDBObject("scope_id", _id)) > 0;
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
	public long update(BasicDBObject fu) {
		return update(fu, OBSInTemplate.class);
	}

	@Override
	public List<WorkInTemplate> listWBSRoot(ObjectId template_id) {
		List<? extends Bson> pipeline = new JQ("获得项目模板WBS根节点").set("template_id", template_id)
				.set("parent_id", null).buildArray();
		return c(WorkInTemplate.class).aggregate(pipeline).into(new ArrayList<WorkInTemplate>());
	}

	@Override
	public long countWBSRoot(ObjectId template_id) {
		return count(new BasicDBObject("template_id", template_id).append("parent_id", null), "workInTemplate");
	}

	@Override
	public List<WorkInTemplate> listWBSChildren(ObjectId parent_id) {
		List<? extends Bson> pipeline = new JQ("获得项目模板WBS下级节点").set("parent_id", parent_id).buildArray();
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
		Set<ObjectId> workIds = new HashSet<ObjectId>();
		List<ResourcePlanInTemplate> documents = new ArrayList<ResourcePlanInTemplate>();
		resas.forEach(resa -> {
			double works = getWorkingHoursPerDay(resa.resTypeId);
			Document doc = c("workInTemplate").find(new Document("_id", resa.work_id))
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

					ResourcePlanInTemplate res = resa.getResourcePlanInTemplate();
					res.setId(planStartCal.getTime());
					res.setPlanBasicQty(works);

					documents.add(res);
				}
				planStartCal.add(Calendar.DAY_OF_MONTH, 1);
			}
			workIds.add(resa.work_id);
		});
		c(ResourcePlanInTemplate.class).insertMany(documents);
		workIds.forEach(work_id -> {
			updateWorkPlanWorks(work_id);
		});

		// ResourcePlan r = insert(res, ResourcePlan.class);
		// queryResourceUsage(new Document("_id", r.get_id())).get(0);

		return documents;
	}

	@Override
	public long updateResourcePlan(BasicDBObject filterAndUpdate) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long deleteHumanResourcePlan(ObjectId work_id, String hrResId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long deleteEquipmentResourcePlan(ObjectId work_id, String eqResId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long deleteTypedResourcePlan(ObjectId work_id, String tyResId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<ResourcePlanInTemplate> listResourcePlan(ObjectId _id) {
		// TODO Auto-generated method stub
		return null;
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

			WorkInTemplate work = c(WorkInTemplate.class).findOneAndUpdate(new Document("_id", work_id),
					new Document("$set", new Document("planWorks", works)));
			updateWorkParentPlanWorks(work);
		}
	}
	
	private void updateWorkParentPlanWorks(WorkInTemplate work) {
//		ObjectId parent_id = work.getParent_id();
//		if (parent_id != null) {
//			List<? extends Bson> pipeline = Arrays.asList(
//					new Document().append("$match", new Document().append("parent_id", parent_id)),
//					new Document().append("$group", new Document().append("_id", "parent_id").append("planWorks",
//							new Document().append("$sum", "$planWorks"))));
//			Document doc = c("work").aggregate(pipeline).first();
//
//			WorkInTemplate parentWork = c(WorkInTemplate.class).findOneAndUpdate(new Document("_id", parent_id),
//					new Document("$set", new Document("planWorks", doc.get("planWorks"))));
//			updateWorkParentPlanWorks(parentWork);
//		} else {
//			ObjectId project_id = work.getProject_id();
//			List<? extends Bson> pipeline = Arrays.asList(
//					new Document().append("$match",
//							new Document().append("parent_id", null).append("project_id", project_id)),
//					new Document().append("$group", new Document().append("_id", "parent_id").append("planWorks",
//							new Document().append("$sum", "$planWorks"))));
//			Document doc = c("work").aggregate(pipeline).first();
//			c("project").updateOne(new Document("_id", project_id),
//					new Document("$set", new Document("planWorks", doc.get("planWorks"))));
//		}
	}

}
