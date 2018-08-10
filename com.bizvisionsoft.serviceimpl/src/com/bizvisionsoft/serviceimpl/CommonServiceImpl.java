package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.model.AccountItem;
import com.bizvisionsoft.service.model.Calendar;
import com.bizvisionsoft.service.model.Certificate;
import com.bizvisionsoft.service.model.ChangeProcess;
import com.bizvisionsoft.service.model.Dictionary;
import com.bizvisionsoft.service.model.Equipment;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.IndexOptions;

public class CommonServiceImpl extends BasicServiceImpl implements CommonService {

	@Override
	public List<Certificate> getCertificates(BasicDBObject condition) {
		return createDataSet(condition, Certificate.class);
	}

	@Override
	public Certificate insertCertificate(Certificate cert) {
		return insert(cert);
	}

	@Override
	public long deleteCertificate(ObjectId _id) {
		// TODO 考虑已经使用的任职资格

		return delete(_id, Certificate.class);
	}

	@Override
	public long updateCertificate(BasicDBObject fu) {
		return update(fu, Certificate.class);
	}

	@Override
	public List<String> getCertificateNames() {
		ArrayList<String> result = new ArrayList<String>();
		c(Certificate.class).distinct("name", String.class).into(result);
		return result;
	}

	@Override
	public List<ResourceType> getResourceType() {
		List<ResourceType> result = new ArrayList<>();
		c(ResourceType.class).find().into(result);
		return result;
	}

	@Override
	public ResourceType insertResourceType(ResourceType resourceType) {
		return insert(resourceType, ResourceType.class);
	}

	@Override
	public long deleteResourceType(ObjectId _id) {
		// TODO 考虑资源类型被使用的状况
		return delete(_id, ResourceType.class);
	}

	@Override
	public long updateResourceType(BasicDBObject fu) {
		return update(fu, ResourceType.class);
	}

	@Override
	public List<Equipment> getERResources(ObjectId _id) {
		return queryEquipments(new BasicDBObject("resourceType_id", _id));
	}

	private ArrayList<Equipment> queryEquipments(BasicDBObject condition) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();
		if (condition != null)
			pipeline.add(Aggregates.match(condition));

		appendOrgFullName(pipeline, "org_id", "orgFullName");

		ArrayList<Equipment> result = new ArrayList<Equipment>();
		c(Equipment.class).aggregate(pipeline).into(result);
		return result;
	}

	@Override
	public ResourceType getResourceType(ObjectId _id) {
		return get(_id, ResourceType.class);
	}

	@Override
	public long countERResources(ObjectId _id) {
		return c(Equipment.class).countDocuments(new BasicDBObject("resourceType_id", _id));
	}

	@Override
	public List<Equipment> getEquipments() {
		return queryEquipments(null);
	}

	@Override
	public Equipment insertEquipment(Equipment cert) {
		return insert(cert, Equipment.class);
	}

	@Override
	public long deleteEquipment(ObjectId _id) {
		// TODO 完整性问题
		return delete(_id, Equipment.class);
	}

	@Override
	public long updateEquipment(BasicDBObject fu) {
		return update(fu, Equipment.class);
	}

	@Override
	public List<Calendar> getCalendars() {
		return c(Calendar.class).find().into(new ArrayList<Calendar>());
	}

	@Override
	public Calendar getCalendar(ObjectId _id) {
		return get(_id, Calendar.class);
	}

	@Override
	public Calendar insertCalendar(Calendar obj) {
		return insert(obj, Calendar.class);
	}

	@Override
	public long deleteCalendar(ObjectId _id) {
		// TODO 完整性问题
		return delete(_id, Calendar.class);
	}

	@Override
	public long updateCalendar(BasicDBObject fu) {
		return update(fu, Calendar.class);
	}

	@Override
	public void addCalendarWorktime(BasicDBObject r, ObjectId _cal_id) {
		c(Calendar.class).updateOne(new BasicDBObject("_id", _cal_id),
				new BasicDBObject("$addToSet", new BasicDBObject("workTime", r)));
	}

	/**
	 * db.getCollection('calendar').updateOne({"workTime._id":ObjectId("5ad49b5a85e0fb335c355fae")},
	 * {$set:{"workTime.$":"aaa"}
	 * 
	 * })
	 */
	@Override
	public void updateCalendarWorkTime(BasicDBObject r) {
		c(Calendar.class).updateOne(new BasicDBObject("workTime._id", r.get("_id")),
				new BasicDBObject("$set", new BasicDBObject("workTime.$", r)));
	}

	@Override
	public void deleteCalendarWorkTime(ObjectId _id) {
		c(Calendar.class).updateOne(new BasicDBObject("workTime._id", _id),
				new BasicDBObject("$pull", new BasicDBObject("workTime", new BasicDBObject("_id", _id))));
	}

	@Override
	public List<Dictionary> getDictionary() {
		List<Dictionary> target = new ArrayList<Dictionary>();
		c(Dictionary.class).find().sort(new BasicDBObject("type", 1)).into(target);
		return target;
	}

	@Override
	public Dictionary insertResourceType(Dictionary dic) {
		return insert(dic, Dictionary.class);
	}

	@Override
	public long deleteDictionary(ObjectId _id) {
		return delete(_id, Dictionary.class);
	}

	@Override
	public long updateDictionary(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, Dictionary.class);
	}

	@Override
	public Map<String, String> getDictionary(String type) {
		Map<String, String> result = new HashMap<String, String>();
		Iterable<Document> itr = c("dictionary").find(new BasicDBObject("type", type));
		itr.forEach(d -> result.put(d.getString("name") + " [" + d.getString("id") + "]",
				d.getString("id") + "#" + d.getString("name")));
		return result;
	}

	@Override
	public Map<String, String> getDictionaryIdNamePair(String type) {
		Map<String, String> result = new HashMap<String, String>();
		Iterable<Document> itr = c("dictionary").find(new BasicDBObject("type", type));
		itr.forEach(d -> result.put(d.getString("id"), d.getString("name")));
		return result;
	}

	@Override
	public List<String> listDictionary(String type, String valueField) {
		return c("dictionary").distinct(valueField, (new BasicDBObject("type", type)), String.class)
				.into(new ArrayList<>());
	}

	@Override
	public List<AccountItem> getAccoutItemRoot() {
		return getAccoutItem(null);
	}

	@Override
	public List<AccountItem> getAccoutItem(ObjectId parent_id) {
		return queryAccountItem(new BasicDBObject("parent_id", parent_id));
	}

	@Override
	public long countAccoutItem(ObjectId _id) {
		return count(new BasicDBObject("parent_id", _id), AccountItem.class);
	}

	@Override
	public long countAccoutItemRoot() {
		return countAccoutItem(null);
	}

	@Override
	public AccountItem insertAccountItem(AccountItem ai) {
		return insert(ai, AccountItem.class);
	}

	@Override
	public long deleteAccountItem(ObjectId _id) {
		return delete(_id, AccountItem.class);
	}

	@Override
	public long updateAccountItem(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, AccountItem.class);
	}

	@Override
	public List<AccountItem> queryAccountItem(BasicDBObject filter) {
		List<Bson> pipeline = new ArrayList<Bson>();

		if (filter != null) {
			pipeline.add(Aggregates.match(filter));
		}

		pipeline.add(Aggregates.lookup("accountItem", "_id", "parent_id", "_children"));

		pipeline.add(Aggregates.addFields(//
				new Field<BasicDBObject>("children", new BasicDBObject("$map", new BasicDBObject()
						.append("input", "$_children._id").append("as", "id").append("in", "$$id")))));

		pipeline.add(Aggregates.project(new BasicDBObject("_children", false)));

		pipeline.add(Aggregates.sort(new BasicDBObject("id", 1)));

		return c(AccountItem.class).aggregate(pipeline).into(new ArrayList<AccountItem>());
	}

	@Override
	public List<Message> listMessage(BasicDBObject condition, String userId) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			condition.put("filter", filter = new BasicDBObject());
		}
		filter.put("receiver", userId);

		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		pipeline.add(Aggregates.sort(new BasicDBObject("sendDate", -1)));

		Integer skip = (Integer) condition.get("skip");
		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		Integer limit = (Integer) condition.get("limit");
		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendUserInfoAndHeadPic(pipeline, "sender", "senderInfo", "senderHeadPic");

		appendUserInfo(pipeline, "receiver", "receiverInfo");

		return c(Message.class).aggregate(pipeline).into(new ArrayList<Message>());
	}

	@Override
	public long countMessage(BasicDBObject filter, String userId) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.put("receiver", userId);
		return count(filter, Message.class);
	}

	@Override
	public int generateCode(String name, String key) {
		return super.generateCode(name, key);
	}

	@Override
	public List<TrackView> getTrackView() {
		return c(TrackView.class).find().into(new ArrayList<TrackView>());
	}

	@Override
	public TrackView insertTrackView(TrackView trackView) {
		return insert(trackView, TrackView.class);
	}

	@Override
	public long deleteTrackView(ObjectId _id) {
		return delete(_id, TrackView.class);
	}

	@Override
	public long updateTrackView(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, TrackView.class);
	}

	@Override
	public Date getCurrentCBSPeriod() {
		Document doc = c("project")
				.find(new Document("status",
						new Document("$nin", Arrays.asList(ProjectStatus.Created, ProjectStatus.Closed))))
				.sort(new Document("settlementDate", -1)).projection(new Document("settlementDate", 1)).first();
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.add(java.util.Calendar.MONTH, -1);
		if (doc != null) {
			Date settlementDate = doc.getDate("settlementDate");
			if (settlementDate != null) {
				cal.setTime(settlementDate);
			}
		}
		return cal.getTime();
	}

	@Override
	public long countCertificate(BasicDBObject filter) {
		return count(filter, Certificate.class);
	}

	@Override
	public List<ChangeProcess> createChangeProcessDataSet() {
		return c(ChangeProcess.class).find().into(new ArrayList<ChangeProcess>());
	}

	@Override
	public ChangeProcess getChangeProcess(ObjectId _id) {
		return get(_id, ChangeProcess.class);
	}

	@Override
	public ChangeProcess insertChangeProcess(ChangeProcess changeProcess) {
		return insert(changeProcess, ChangeProcess.class);
	}

	@Override
	public long deleteChangeProcess(ObjectId _id) {
		return delete(_id, ChangeProcess.class);
	}

	@Override
	public long updateChangeProcess(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, ChangeProcess.class);
	}

	@Override
	public void createIndex() {
		// accountItem->
		// id
		// parent_id
		createIndex("accountItem", new Document("id", 1), "id");
		createIndex("accountItem", new Document("parent_id", 1), "parent_id");
		// baseline->
		// project_id
		// creationDate
		createIndex("baseline", new Document("project_id", 1), "project_id");
		createIndex("baseline", new Document("creationDate", -1), "creationDate");// 按时间倒序
		// baselineWork->
		// project_id
		// wbsCode
		// index
		// baseline_id
		// parent_id
		createIndex("baselineWork", new Document("project_id", 1), "project_id");
		createIndex("baselineWork", new Document("wbsCode", 1), "wbsCode");
		createIndex("baselineWork", new Document("index", 1), "index");
		createIndex("baselineWork", new Document("baseline_id", 1), "baseline_id");
		createIndex("baselineWork", new Document("parent_id", 1), "parent_id");
		// baselineWorkLinks->
		// project_id
		// source
		// target
		// baseline_id
		createIndex("baselineWorkLinks", new Document("project_id", 1), "project_id");
		createIndex("baselineWorkLinks", new Document("source", 1), "source");
		createIndex("baselineWorkLinks", new Document("target", 1), "target");
		createIndex("baselineWorkLinks", new Document("baseline_id", 1), "baseline_id");
		// cbs->
		// scope_id
		// parent_id
		createIndex("cbs", new Document("scope_id", 1), "scope_id");
		createIndex("cbs", new Document("parent_id", 1), "parent_id");
		// cbsPeriod->
		// cbsItem_id
		// id
		createIndex("cbsPeriod", new Document("cbsItem_id", 1), "cbsItem_id");
		createIndex("cbsPeriod", new Document("id", 1), "id");
		// cbsSubject->
		// cbsItem_id
		// id
		// subjectNumber
		createIndex("cbsSubject", new Document("cbsItem_id", 1), "cbsItem_id");
		createIndex("cbsSubject", new Document("id", 1), "id");
		createIndex("cbsSubject", new Document("subjectNumber", 1), "subjectNumber");
		// dictionary->
		// id+type
		createIndex("cbsSubject", new Document("id", 1).append("type", 1), "id_type");
		// docu->
		// folder_id
		// tag
		// category
		// name
		// id
		createIndex("docu", new Document("folder_id", 1), "folder_id");
		createIndex("docu", new Document("tag", 1), "tag");
		createIndex("docu", new Document("category", 1), "category");
		createIndex("docu", new Document("name", 1), "name");
		createIndex("docu", new Document("id", 1), "id");
		// eps->
		// id
		createIndex("eps", new Document("id", 1), "id");
		// equipment->
		// org_id
		// id
		// resourceType_id
		createIndex("equipment", new Document("org_id", 1), "org_id");
		createIndex("equipment", new Document("id", 1), "id");
		createIndex("equipment", new Document("resourceType_id", 1), "resourceType_id");
		// folder->
		// project_id
		// parent_id
		createIndex("folder", new Document("project_id", 1), "project_id");
		createIndex("folder", new Document("parent_id", 1), "parent_id");
		// funcPermission->
		// id+type
		createIndex("funcPermission", new Document("id", 1).append("type", 1), "id_type");
		// message->
		// receiver
		// sendDate
		// content
		createIndex("message", new Document("receiver", 1), "receiver");
		createIndex("message", new Document("sendDate", 1), "sendDate");
		createIndex("message", new Document("content", 1), "content");
		// monteCarloSimulate->
		// project_id
		createIndex("monteCarloSimulate", new Document("project_id", 1), "project_id");
		// obs->
		// scope_id
		// roleId
		// managerId
		// parent_id
		// seq
		createIndex("obs", new Document("scope_id", 1), "scope_id");
		createIndex("obs", new Document("roleId", 1), "roleId");
		createIndex("obs", new Document("managerId", 1), "managerId");
		createIndex("obs", new Document("parent_id", 1), "parent_id");
		createIndex("obs", new Document("seq", 1), "seq");
		// obs->
		// scope_id
		// roleId
		// parent_id
		// seq
		createIndex("obsInTemplate", new Document("scope_id", 1), "scope_id");
		createIndex("obsInTemplate", new Document("roleId", 1), "roleId");
		createIndex("obsInTemplate", new Document("parent_id", 1), "parent_id");
		createIndex("obsInTemplate", new Document("seq", 1), "seq");
		// organization->
		// id
		// managerId
		// parent_id
		createIndex("organization", new Document("id", 1), "id");
		createIndex("organization", new Document("managerId", 1), "managerId");
		createIndex("organization", new Document("parent_id", 1), "parent_id");
		// project->
		// pmId
		// cbs_id
		// planStart
		// workOrder
		// planFinish
		// obs_id
		// actualStart
		// actualFinish
		// eps_id
		createIndex("project", new Document("pmId", 1), "pmId");
		createIndex("project", new Document("cbs_id", 1), "cbs_id");
		createIndex("project", new Document("workOrder", 1), "workOrder");
		createIndex("project", new Document("obs_id", 1), "obs_id");
		createIndex("project", new Document("planStart", 1), "planStart");
		createIndex("project", new Document("planFinish", -1), "planFinish");
		createIndex("project", new Document("actualStart", 1), "actualStart");
		createIndex("project", new Document("actualFinish", 1), "actualFinish");
		createIndex("project", new Document("eps_id", 1), "eps_id");
		// projectChange->
		// project_id
		// applicantDate
		createIndex("projectChange", new Document("project_id", 1), "project_id");
		createIndex("projectChange", new Document("applicantDate", -1), "applicantDate");
		// projectSet->
		// workOrder
		// eps_id
		createIndex("projectSet", new Document("workOrder", 1), "workOrder");
		createIndex("projectSet", new Document("eps_id", 1), "eps_id");
		// projectTemplate->
		// id
		createIndex("projectTemplate", new Document("id", 1), "id");
		// rbsItem->
		// project_id
		// parent_id
		// index
		createIndex("rbsItem", new Document("project_id", 1), "project_id");
		createIndex("rbsItem", new Document("parent_id", 1), "parent_id");
		createIndex("rbsItem", new Document("index", 1), "index");
		// resourceActual->
		// work_id+resTypeId+usedEquipResId
		// work_id+resTypeId+usedTypedResId
		// work_id+resTypeId+usedHumanResId
		// id
		createIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedEquipResId", 1),
				"equip");
		createIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedTypedResId", 1),
				"restype");
		createIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1),
				"hr");
		createIndex("resourceActual", new Document("id", 1), "id");
		// resourcePlan->
		// work_id+resTypeId+usedEquipResId
		// work_id+resTypeId+usedTypedResId
		// work_id+resTypeId+usedHumanResId
		// id
		createIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedEquipResId", 1),
				"equipment");
		createIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedTypedResId", 1),
				"restype");
		createIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1),
				"hr");
		createIndex("resourcePlan", new Document("id", 1), "id");
		// resourceType->
		// id
		createIndex("resourceType", new Document("id", 1), "id");
		// riskEffect->
		// project_id
		// rbsItem_id
		createIndex("riskEffect", new Document("project_id", 1), "project_id");
		createIndex("riskEffect", new Document("rbsItem_id", 1), "rbsItem_id");
		// salesItem->
		// period
		// project_id
		createIndex("salesItem", new Document("period", 1), "period");
		createIndex("salesItem", new Document("project_id", 1), "project_id");
		// stockholder->
		// project_id
		createIndex("stockholder", new Document("project_id", 1), "project_id");
		// user->
		// userid
		// org_id
		// resourceType_id
		createIndex("user", new Document("userId", 1), "userId");
		createIndex("user", new Document("org_id", 1), "org_id");
		createIndex("user", new Document("resourceType_id", 1), "resourceType_id");
		// work->
		// project_id
		// planStart
		// planFinish
		// actualStart
		// actualFinish
		// wbsCode
		// index
		// parent_id
		// chargerId
		// assignerId
		// manageLevel
		createIndex("work", new Document("project_id", 1), "project_id");
		createIndex("work", new Document("planStart", 1), "planStart");
		createIndex("work", new Document("planFinish", 1), "planFinish");
		createIndex("work", new Document("actualStart", 1), "actualStart");
		createIndex("work", new Document("actualFinish", 1), "actualFinish");
		createIndex("work", new Document("wbsCode", 1), "wbsCode");
		createIndex("work", new Document("index", 1), "index");
		createIndex("work", new Document("parent_id", 1), "parent_id");
		createIndex("work", new Document("chargerId", 1), "chargerId");
		createIndex("work", new Document("assignerId", 1), "assignerId");
		createIndex("work", new Document("manageLevel", 1), "manageLevel");
		// workInTemplate->
		// wbsCode
		// index
		// parent_id
		// template_id
		createIndex("workInTemplate", new Document("wbsCode", 1), "wbsCode");
		createIndex("workInTemplate", new Document("index", 1), "index");
		createIndex("workInTemplate", new Document("parent_id", 1), "parent_id");
		createIndex("workInTemplate", new Document("template_id", 1), "template_id");
		// workPackage->
		// work_id
		createIndex("workPackage", new Document("work_id", 1), "work_id");
		// workPackageProgress->
		// package_id
		// time:-1
		createIndex("workPackageProgress", new Document("package_id", 1), "package_id");
		createIndex("workPackageProgress", new Document("time", -1), "time");
		// workReport->
		// project_id+type
		// period
		// reporter+type
		createIndex("workReport", new Document("project_id", 1).append("type", 1), "project_id_type");
		createIndex("workReport", new Document("period", 1), "period");
		createIndex("workReport", new Document("reporter", 1).append("type", 1), "reporter_type");
		// workReportItem->
		// report_id
		// project_id
		createIndex("workReportItem", new Document("report_id", 1), "report_id");
		createIndex("workReportItem", new Document("project_id", 1), "project_id");
		// workReportResourceActual->
		// workReportItem_id+work_id+resTypeId+usedEquipResId
		// workReportItem_id+work_id+resTypeId+usedTypedResId
		// workReportItem_id+work_id+resTypeId+usedHumanResId
		// id
		createIndex("workReportResourceActual", new Document("workReportItem_id", 1).append("work_id", 1)
				.append("resTypeId", 1).append("usedEquipResId", 1), "equipment");
		createIndex("workReportResourceActual", new Document("workReportItem_id", 1).append("work_id", 1)
				.append("resTypeId", 1).append("usedTypedResId", 1), "restype");
		createIndex("workReportResourceActual", new Document("workReportItem_id", 1).append("work_id", 1)
				.append("resTypeId", 1).append("usedHumanResId", 1), "hr");
		createIndex("workReportResourceActual", new Document("id", 1), "id");
		// worklinks->
		// project_id
		// space_id
		createIndex("worklinks", new Document("project_id", 1), "project_id");
		createIndex("worklinks", new Document("space_id", 1), "space_id");
		// workspace->
		// project_id
		// wbsCode
		// index
		// parent_id
		// space_id
		createIndex("workspace", new Document("project_id", 1), "project_id");
		createIndex("workspace", new Document("wbsCode", 1), "wbsCode");
		createIndex("workspace", new Document("index", 1), "index");
		createIndex("workspace", new Document("parent_id", 1), "parent_id");
		createIndex("workspace", new Document("space_id", 1), "space_id");

		// cbs->
		// id、scope_id
		createUniqueIndex("cbs", new Document("id", 1).append("scope_id", 1), "id_scope_id");

		// project->
		// id
		createUniqueIndex("project", new Document("id", 1), new IndexOptions().name("id").unique(true).sparse(true));

		// obs->
		// roleId、scope_id
		createUniqueIndex("obs", new Document("roleId", 1).append("scope_id", 1), "roleId_scope_id");

		// obsInTemplate->
		// roleId、scope_id
		createUniqueIndex("obsInTemplate", new Document("roleId", 1).append("scope_id", 1), "roleId_scope_id");

		// resourcePlan->
		// work_id、resTypeId、usedHumanResId、usedEquipResId、usedTypedResId、id
		createUniqueIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1)
				.append("usedEquipResId", 1).append("usedTypedResId", 1).append("id", 1), "workRes");

		// resourcePlanInTemplate->
		// work_id、resTypeId、usedHumanResId、usedEquipResId、usedTypedResId
		createUniqueIndex("resourcePlanInTemplate", new Document("work_id", 1).append("resTypeId", 1)
				.append("usedHumanResId", 1).append("usedEquipResId", 1).append("usedTypedResId", 1), "workRes");

		// resourceActual->
		// work_id、resTypeId、usedHumanResId、usedEquipResId、usedTypedResId
		createUniqueIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1)
				.append("usedHumanResId", 1).append("usedEquipResId", 1).append("usedTypedResId", 1).append("id", 1),
				"workRes");

		// workReportResourceActual->
		// work_id、resTypeId、usedHumanResId、usedEquipResId、usedTypedResId、workReportItemId
		createUniqueIndex("workReportResourceActual",
				new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1)
						.append("usedEquipResId", 1).append("usedTypedResId", 1).append("workReportItemId", 1)
						.append("id", 1),
				"workRes");

		// work->
		// project_id、fullName
		// createUniqueIndex("work", new Document("project_id", 1).append("fullName",
		// 1), "project_id_fullName");

		// workspace->
		// project_id、fullName、space_id
		// createUniqueIndex("workspace", new Document("project_id",
		// 1).append("fullName", 1).append("space_id", 1),
		// "project_id_fullName_space_id");

		// workInTemplate->
		// template_id、fullName
		// createUniqueIndex("workInTemplate", new Document("template_id",
		// 1).append("fullName", 1),
		// "template_id_fullName");

		// // workLinks->
		// // project_id、source、target
		// createUniqueIndex("workLinks", new Document("project_id", 1).append("source",
		// 1).append("target", 1),
		// "project_id_source_target");//错误
		//
		// // workLinksspace->
		// // project_id、source、target、space_id
		// createUniqueIndex("workLinksspace",
		// new Document("project_id", 1).append("source", 1).append("target",
		// 1).append("space_id", 1),
		// "project_id_source_target_space_id");//错误

		// folder->
		// name、project_id、parent_id
		createUniqueIndex("folder", new Document("name", 1).append("project_id", 1).append("parent_id", 1),
				"name_project_id_parent_id");

		// folderInTemplate->
		// name、template_id、parent_id
		createUniqueIndex("folderInTemplate", new Document("name", 1).append("template_id", 1).append("parent_id", 1),
				"name_template_id_parent_id");

		// eps->
		// id
		createUniqueIndex("eps", new Document("id", 1), "id");

		// cbsSubject->
		// cbsItem_id、id、subjectNumber
		createUniqueIndex("cbsSubject", new Document("cbsItem_id", 1).append("id", 1).append("subjectNumber", 1),
				"cbsItem_id_id_subjectNumber");

		// cbsPeriod->
		// cbsItem_id、id
		createUniqueIndex("cbsPeriod", new Document("cbsItem_id", 1).append("id", 1), "cbsItem_id_id");

		// accountItem->
		// id
		createUniqueIndex("accountItem", new Document("id", 1), "id");

		// dictionary -》
		// id,type
		createUniqueIndex("dictionary", new Document("type", 1).append("id", 1), "type_id");

	}

	private void createUniqueIndex(String collectionName, final Document keys, IndexOptions indexOptions) {
		try {
			c(collectionName).listIndexes().forEach((Document doc) -> {
				if (doc.get("key").equals(keys)) {
					c(collectionName).dropIndex((Bson) doc.get("key"));
					return;
				}
			});
			c(collectionName).createIndex(keys, indexOptions);
		} catch (Exception e) {
			throw new ServiceException("集合：" + collectionName + "创建唯一性索引错误。" + e.getMessage());
		}
	}

	private void createIndex(String collectionName, final Document keys, String name) {
		try {
			c(collectionName).listIndexes().forEach((Document doc) -> {
				if (doc.get("key").equals(keys)) {
					c(collectionName).dropIndex((Bson) doc.get("key"));
					return;
				}

			});
			c(collectionName).createIndex(keys, new IndexOptions().name(name));
		} catch (Exception e) {
			throw new ServiceException("集合：" + collectionName + "创建索引错误。" + e.getMessage());
		}
	}

	private void createUniqueIndex(String collectionName, final Document keys, String name) {
		try {
			c(collectionName).listIndexes().forEach((Document doc) -> {
				if (doc.get("key").equals(keys)) {
					c(collectionName).dropIndex((Bson) doc.get("key"));
					return;
				}
			});
			c(collectionName).createIndex(keys, new IndexOptions().name(name).unique(true));
		} catch (Exception e) {
			throw new ServiceException("集合：" + collectionName + "创建唯一性索引错误。" + e.getMessage());
		}
	}

	@Override
	public List<String> listWorkTag() {
		return new ArrayList<String>(getDictionaryIdNamePair("工作标签").values());
	}

	@Override
	public List<Document> listStructuredData(BasicDBObject query) {
		return c("structuredData").find(query).sort(new Document("index", 1)).into(new ArrayList<>());
	}

	@Override
	public void insertStructuredData(List<Document> result) {
		c("structuredData").insertMany(result);
	}

	@Override
	public void updateStructuredData(BasicDBObject fu) {
		update(fu, "structuredData");
	}

	///////////////////////////////////////
	// DEMO DATA
	@Override
	public void mockupSalesData() {
		// 创建产品主数据
		List<Bson> pipeline = Arrays.asList(new Document("$group",
				new Document("_id", new Document("a", "$productId").append("b", "$project_id"))));
		c("salesItem").aggregate(pipeline).forEach((Document d) -> {
			Document _r = (Document) d.get("_id");
			String productId = _r.getString("a");
			if (Arrays.asList("02302", "02303", "02304", "02305", "02306", "02307").contains(productId)) {
				return;
			}

			ObjectId project_id = _r.getObjectId("b");
			String series = getRandomSeries();
			String name = series + "-" + _10_to_N(System.currentTimeMillis(), 24);
			String position = "定位XXX";
			String sellPoint = "滑翔，变形，声光电。";
			ObjectId benchmarking_id = new ObjectId("5b4acc0be37dab0cfcb4458e");

			Document product = new Document("_id", new ObjectId()).append("sellPoint", sellPoint)
					.append("project_id", project_id).append("series", series).append("name", name)
					.append("id", productId).append("position", position).append("benchmarking_id", benchmarking_id);
			c("product").insertOne(product);

		});

		// 制造销售额和销售量
		c("salesItem").find().forEach((Document d) -> {
			Number profit = (Number) d.get("profit");
			double income = profit.doubleValue() / 0.4;
			int val = getRandomInt();
			c("salesItem").updateOne(new Document("_id", d.get("_id")),
					new Document("$set", new Document("income", income).append("volumn", val)));
		});

	}

	private String getRandomSeries() {
		String[] series = new String[] { "超级飞侠", "萌鸡小队", "巴啦啦小魔仙", "火力少年王", "铠甲勇士" };
		return series[new Random().nextInt(series.length)];
	}

	private int getRandomInt() {
		int[] series = new int[] { 10, 20, 30, 40, 15, 5, 12, 50 };
		return series[new Random().nextInt(series.length)];
	}

	public static String _10_to_N(long number, int N) {
		char[] array = "0123456789ABCDEFGHJKMNPQRSTUVWXYZ".toCharArray();
		Long rest = number;
		Stack<Character> stack = new Stack<Character>();
		StringBuilder result = new StringBuilder(0);
		while (rest != 0) {
			stack.add(array[new Long((rest % N)).intValue()]);
			rest = rest / N;
		}
		for (; !stack.isEmpty();) {
			result.append(stack.pop());
		}
		return result.length() == 0 ? "0" : result.toString();
	}

	@Override
	public long updateMessage(BasicDBObject fu) {
		return update(fu, Message.class);
	}

}
