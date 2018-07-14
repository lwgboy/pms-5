package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;

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
						new Document("$nin",
								Arrays.asList(ProjectStatus.Created, ProjectStatus.Created, ProjectStatus.Terminated))))
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
		c("accountItem").createIndex(new Document("id", 1));
		c("accountItem").createIndex(new Document("parent_id", 1));
		// baseline->
		// project_id
		// creationDate
		c("baseline").createIndex(new Document("project_id", 1));
		c("baseline").createIndex(new Document("creationDate", -1));// 按时间倒序
		// baselineWork->
		// project_id
		// wbsCode
		// index
		// baseline_id
		// parent_id
		c("baselineWork").createIndex(new Document("project_id", 1));
		c("baselineWork").createIndex(new Document("wbsCode", 1));
		c("baselineWork").createIndex(new Document("index", 1));
		c("baselineWork").createIndex(new Document("baseline_id", 1));
		c("baselineWork").createIndex(new Document("parent_id", 1));
		// baselineWorkLinks->
		// project_id
		// source
		// target
		// baseline_id
		c("baselineWorkLinks").createIndex(new Document("project_id", 1));
		c("baselineWorkLinks").createIndex(new Document("source", 1));
		c("baselineWorkLinks").createIndex(new Document("target", 1));
		c("baselineWorkLinks").createIndex(new Document("baseline_id", 1));
		// cbs->
		// scope_id
		// parent_id
		c("cbs").createIndex(new Document("scope_id", 1));
		c("cbs").createIndex(new Document("parent_id", 1));
		// cbsPeriod->
		// cbsItem_id
		// id
		c("cbsPeriod").createIndex(new Document("cbsItem_id", 1));
		c("cbsPeriod").createIndex(new Document("id", 1));
		// cbsSubject->
		// cbsItem_id
		// id
		// subjectNumber
		c("cbsSubject").createIndex(new Document("cbsItem_id", 1));
		c("cbsSubject").createIndex(new Document("id", 1));
		c("cbsSubject").createIndex(new Document("subjectNumber", 1));
		// dictionary->
		// id+type
		c("cbsSubject").createIndex(new Document("id", 1).append("type", 1));
		// docu->
		// folder_id
		// tag
		// category
		// name
		// id
		c("docu").createIndex(new Document("folder_id", 1));
		c("docu").createIndex(new Document("tag", 1));
		c("docu").createIndex(new Document("category", 1));
		c("docu").createIndex(new Document("name", 1));
		c("docu").createIndex(new Document("id", 1));
		// eps->
		// id
		c("eps").createIndex(new Document("id", 1));
		// equipment->
		// org_id
		// id
		// resourceType_id
		c("equipment").createIndex(new Document("org_id", 1));
		c("equipment").createIndex(new Document("id", 1));
		c("equipment").createIndex(new Document("resourceType_id", 1));
		// folder->
		// project_id
		// parent_id
		c("folder").createIndex(new Document("project_id", 1));
		c("folder").createIndex(new Document("parent_id", 1));
		// funcPermission->
		// id+type
		c("funcPermission").createIndex(new Document("id", 1).append("type", 1));
		// message->
		// receiver
		// sendDate
		// content
		c("message").createIndex(new Document("receiver", 1));
		c("message").createIndex(new Document("sendDate", 1));
		c("message").createIndex(new Document("content", 1));
		// monteCarloSimulate->
		// project_id
		c("monteCarloSimulate").createIndex(new Document("project_id", 1));
		// obs->
		// scope_id
		// roleId
		// managerId
		// parent_id
		// seq
		c("obs").createIndex(new Document("scope_id", 1));
		c("obs").createIndex(new Document("roleId", 1));
		c("obs").createIndex(new Document("managerId", 1));
		c("obs").createIndex(new Document("parent_id", 1));
		c("obs").createIndex(new Document("seq", 1));
		// obs->
		// scope_id
		// roleId
		// parent_id
		// seq
		c("obsInTemplate").createIndex(new Document("scope_id", 1));
		c("obsInTemplate").createIndex(new Document("roleId", 1));
		c("obsInTemplate").createIndex(new Document("parent_id", 1));
		c("obsInTemplate").createIndex(new Document("seq", 1));
		// organization->
		// id
		// managerId
		// parent_id
		c("organization").createIndex(new Document("id", 1));
		c("organization").createIndex(new Document("managerId", 1));
		c("organization").createIndex(new Document("parent_id", 1));
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
		c("project").createIndex(new Document("pmId", 1));
		c("project").createIndex(new Document("cbs_id", 1));
		c("project").createIndex(new Document("workOrder", 1));
		c("project").createIndex(new Document("obs_id", 1));
		c("project").createIndex(new Document("planStart", 1));
		c("project").createIndex(new Document("planFinish", -1));
		c("project").createIndex(new Document("actualStart", 1));
		c("project").createIndex(new Document("actualFinish", 1));
		c("project").createIndex(new Document("eps_id", 1));
		// projectChange->
		// project_id
		// applicantDate
		c("projectChange").createIndex(new Document("project_id", 1));
		c("projectChange").createIndex(new Document("applicantDate", -1));
		// projectSet->
		// workOrder
		// eps_id
		c("projectSet").createIndex(new Document("workOrder", 1));
		c("projectSet").createIndex(new Document("eps_id", 1));
		// projectTemplate->
		// id
		c("projectTemplate").createIndex(new Document("id", 1));
		// rbsItem->
		// project_id
		// parent_id
		// index
		c("rbsItem").createIndex(new Document("project_id", 1));
		c("rbsItem").createIndex(new Document("parent_id", 1));
		c("rbsItem").createIndex(new Document("index", 1));
		// resourceActual->
		// work_id+resTypeId+usedEquipResId
		// work_id+resTypeId+usedTypedResId
		// work_id+resTypeId+usedHumanResId
		// id
		c("resourceActual").createIndex(new Document("work_id", 1).append("resTypeId", 1).append("usedEquipResId", 1));
		c("resourceActual").createIndex(new Document("work_id", 1).append("resTypeId", 1).append("usedTypedResId", 1));
		c("resourceActual").createIndex(new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1));
		c("resourceActual").createIndex(new Document("id", 1));
		// resourcePlan->
		// work_id+resTypeId+usedEquipResId
		// work_id+resTypeId+usedTypedResId
		// work_id+resTypeId+usedHumanResId
		// id
		c("resourcePlan").createIndex(new Document("work_id", 1).append("resTypeId", 1).append("usedEquipResId", 1));
		c("resourcePlan").createIndex(new Document("work_id", 1).append("resTypeId", 1).append("usedTypedResId", 1));
		c("resourcePlan").createIndex(new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1));
		c("resourcePlan").createIndex(new Document("id", 1));
		// resourceType->
		// id
		c("resourceType").createIndex(new Document("id", 1));
		// riskEffect->
		// project_id
		// rbsItem_id
		c("riskEffect").createIndex(new Document("project_id", 1));
		c("riskEffect").createIndex(new Document("rbsItem_id", 1));
		// salesItem->
		// period
		// project_id
		c("salesItem").createIndex(new Document("period", 1));
		c("salesItem").createIndex(new Document("project_id", 1));
		// stockholder->
		// project_id
		c("stockholder").createIndex(new Document("project_id", 1));
		// user->
		// userid
		// org_id
		// resourceType_id
		c("user").createIndex(new Document("userId", 1));
		c("user").createIndex(new Document("org_id", 1));
		c("user").createIndex(new Document("resourceType_id", 1));
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
		c("work").createIndex(new Document("project_id", 1));
		c("work").createIndex(new Document("planStart", 1));
		c("work").createIndex(new Document("planFinish", 1));
		c("work").createIndex(new Document("actualStart", 1));
		c("work").createIndex(new Document("actualFinish", 1));
		c("work").createIndex(new Document("wbsCode", 1));
		c("work").createIndex(new Document("index", 1));
		c("work").createIndex(new Document("parent_id", 1));
		c("work").createIndex(new Document("chargerId", 1));
		c("work").createIndex(new Document("assignerId", 1));
		c("work").createIndex(new Document("manageLevel", 1));
		// workInTemplate->
		// wbsCode
		// index
		// parent_id
		// template_id
		c("workInTemplate").createIndex(new Document("wbsCode", 1));
		c("workInTemplate").createIndex(new Document("index", 1));
		c("workInTemplate").createIndex(new Document("parent_id", 1));
		c("workInTemplate").createIndex(new Document("template_id", 1));
		// workPackage->
		// work_id
		c("workPackage").createIndex(new Document("work_id", 1));
		// workPackageProgress->
		// package_id
		// time:-1
		c("workPackageProgress").createIndex(new Document("package_id", 1));
		c("workPackageProgress").createIndex(new Document("time", -1));
		// workReport->
		// project_id+type
		// period
		// reporter+type
		c("workReport").createIndex(new Document("project_id", 1).append("type", 1));
		c("workReport").createIndex(new Document("period", 1));
		c("workReport").createIndex(new Document("reporter", 1).append("type", 1));
		// workReportItem->
		// report_id
		// project_id
		c("workReportItem").createIndex(new Document("report_id", 1));
		c("workReportItem").createIndex(new Document("project_id", 1));
		// workReportResourceActual->
		// workReportItem_id+work_id+resTypeId+usedEquipResId
		// workReportItem_id+work_id+resTypeId+usedTypedResId
		// workReportItem_id+work_id+resTypeId+usedHumanResId
		// id
		c("workReportResourceActual").createIndex(new Document("workReportItem_id", 1).append("work_id", 1)
				.append("resTypeId", 1).append("usedEquipResId", 1));
		c("workReportResourceActual").createIndex(new Document("workReportItem_id", 1).append("work_id", 1)
				.append("resTypeId", 1).append("usedTypedResId", 1));
		c("workReportResourceActual").createIndex(new Document("workReportItem_id", 1).append("work_id", 1)
				.append("resTypeId", 1).append("usedHumanResId", 1));
		c("workReportResourceActual").createIndex(new Document("id", 1));
		// worklinks->
		// project_id
		// space_id
		c("worklinks").createIndex(new Document("project_id", 1));
		c("worklinks").createIndex(new Document("space_id", 1));
		// workspace->
		// project_id
		// wbsCode
		// index
		// parent_id
		// space_id
		c("workspace").createIndex(new Document("project_id", 1));
		c("workspace").createIndex(new Document("wbsCode", 1));
		c("workspace").createIndex(new Document("index", 1));
		c("workspace").createIndex(new Document("parent_id", 1));
		c("workspace").createIndex(new Document("space_id", 1));
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

}
