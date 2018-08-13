package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.bizvisionsoft.service.model.NewMessage;
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
		Service.db().listCollectionNames().forEach((String col) -> {
			c(col).dropIndexes();
		});

		createUniqueIndex("accountItem", new Document("id", 1), "id");
		createIndex("accountItem", new Document("parent_id", 1), "parent");

		createIndex("baseline", new Document("project_id", 1), "project");
		createIndex("baseline", new Document("creationDate", -1), "date");// 按时间倒序

		createIndex("baselineWork", new Document("project_id", 1), "project");
		createIndex("baselineWork", new Document("wbsCode", 1), "wbs");
		createIndex("baselineWork", new Document("index", 1), "index");
		createIndex("baselineWork", new Document("baseline_id", 1), "baseline");
		createIndex("baselineWork", new Document("parent_id", 1), "parent");

		createIndex("baselineWorkLinks", new Document("project_id", 1), "project");
		createIndex("baselineWorkLinks", new Document("source", 1), "source");
		createIndex("baselineWorkLinks", new Document("target", 1), "target");
		createIndex("baselineWorkLinks", new Document("baseline_id", 1), "baseline");

		createIndex("cbs", new Document("scope_id", 1), "scope");
		createIndex("cbs", new Document("parent_id", 1), "parent");
		createUniqueIndex("cbs", new Document("id", 1).append("scope_id", 1), "id_scope");

		createIndex("cbsPeriod", new Document("cbsItem_id", 1), "item");
		createIndex("cbsPeriod", new Document("id", 1), "id");
		createUniqueIndex("cbsPeriod", new Document("cbsItem_id", 1).append("id", 1), "id_cbsitem");

		createIndex("cbsSubject", new Document("cbsItem_id", 1), "item");
		createIndex("cbsSubject", new Document("id", 1), "id");
		createIndex("cbsSubject", new Document("subjectNumber", 1), "subject");
		createIndex("cbsSubject", new Document("id", 1).append("type", 1), "id_type");
		createUniqueIndex("cbsSubject", new Document("cbsItem_id", 1).append("id", 1).append("subjectNumber", 1),
				"id_item_subject");

		createIndex("docu", new Document("folder_id", 1), "folder");
		createIndex("docu", new Document("tag", 1), "tag");
		createIndex("docu", new Document("category", 1), "category");
		createIndex("docu", new Document("name", 1), "name");
		createIndex("docu", new Document("id", 1), "id");

		createUniqueIndex("eps", new Document("id", 1), "id");

		createIndex("equipment", new Document("org_id", 1), "org");
		createIndex("equipment", new Document("id", 1), "id");
		createIndex("equipment", new Document("resourceType_id", 1), "resourceType");

		createIndex("folder", new Document("project_id", 1), "project");
		createIndex("folder", new Document("parent_id", 1), "parent");
		createUniqueIndex("folder", new Document("name", 1).append("project_id", 1).append("parent_id", 1),
				"name_project");

		createIndex("funcPermission", new Document("id", 1).append("type", 1), "id_type");

		createIndex("message", new Document("receiver", 1), "receiver");
		createIndex("message", new Document("sendDate", 1), "sendDate");
		createIndex("message", new Document("content", 1), "content");

		createIndex("monteCarloSimulate", new Document("project_id", 1), "project");

		createIndex("obs", new Document("scope_id", 1), "scope");
		createIndex("obs", new Document("roleId", 1), "role");
		createIndex("obs", new Document("managerId", 1), "manager");
		createIndex("obs", new Document("parent_id", 1), "parent");
		createIndex("obs", new Document("seq", 1), "seq");
		createUniqueIndex("obs", new Document("roleId", 1).append("scope_id", 1), "role_scope");

		createIndex("obsInTemplate", new Document("scope_id", 1), "scope");
		createIndex("obsInTemplate", new Document("roleId", 1), "role");
		createIndex("obsInTemplate", new Document("parent_id", 1), "parent");
		createIndex("obsInTemplate", new Document("seq", 1), "seq");
		createUniqueIndex("obsInTemplate", new Document("roleId", 1).append("scope_id", 1), "role_scope");

		createIndex("organization", new Document("id", 1), "id");
		createIndex("organization", new Document("managerId", 1), "manager");
		createIndex("organization", new Document("parent_id", 1), "parent");

		createIndex("project", new Document("pmId", 1), "pm");
		createIndex("project", new Document("cbs_id", 1), "cbs");
		// createIndex("project", new Document("workOrder", 1), "workOrder");
		createIndex("project", new Document("obs_id", 1), "obs");
		createIndex("project", new Document("planStart", 1), "planStart");
		createIndex("project", new Document("planFinish", -1), "planFinish");
		createIndex("project", new Document("actualStart", 1), "actualStart");
		createIndex("project", new Document("actualFinish", 1), "actualFinish");
		createIndex("project", new Document("eps_id", 1), "eps");
		createUniqueIndex("project", new Document("id", 1), new IndexOptions().name("id").unique(true).sparse(true));

		createIndex("projectChange", new Document("project_id", 1), "project");
		createIndex("projectChange", new Document("applicantDate", -1), "date");

		// createIndex("projectSet", new Document("workOrder", 1), "workOrder");
		createIndex("projectSet", new Document("eps_id", 1), "eps");

		// createIndex("projectTemplate", new Document("id", 1), "id");

		createIndex("rbsItem", new Document("project_id", 1), "project");
		createIndex("rbsItem", new Document("parent_id", 1), "parent");
		createIndex("rbsItem", new Document("index", 1), "index");

		createIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedEquipResId", 1),
				"equip");
		createIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedTypedResId", 1),
				"type");
		createIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1),
				"hr");
		createIndex("resourceActual", new Document("id", 1), "id");
		createUniqueIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1)
				.append("usedHumanResId", 1).append("usedEquipResId", 1).append("usedTypedResId", 1).append("id", 1),
				"resource");

		createIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedEquipResId", 1),
				"equip");
		createIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedTypedResId", 1),
				"type");
		createIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1),
				"hr");
		createIndex("resourcePlan", new Document("id", 1), "id");
		createUniqueIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1)
				.append("usedEquipResId", 1).append("usedTypedResId", 1).append("id", 1), "resource");

		createIndex("resourceType", new Document("id", 1), "id");

		createIndex("riskEffect", new Document("project_id", 1), "project");
		createIndex("riskEffect", new Document("rbsItem_id", 1), "item");

		createIndex("salesItem", new Document("period", 1), "period");
		createIndex("salesItem", new Document("project_id", 1), "project");

		createIndex("stockholder", new Document("project_id", 1), "project");

		createIndex("user", new Document("userId", 1), "user");
		createIndex("user", new Document("org_id", 1), "org");
		createIndex("user", new Document("resourceType_id", 1), "resource");

		createIndex("work", new Document("project_id", 1), "project");
		createIndex("work", new Document("planStart", 1), "planStart");
		createIndex("work", new Document("planFinish", 1), "planFinish");
		createIndex("work", new Document("actualStart", 1), "actualStart");
		createIndex("work", new Document("actualFinish", 1), "actualFinish");
		createIndex("work", new Document("wbsCode", 1), "wbs");
		createIndex("work", new Document("index", 1), "index");
		createIndex("work", new Document("parent_id", 1), "parent");
		createIndex("work", new Document("chargerId", 1), "charger");
		createIndex("work", new Document("assignerId", 1), "assigner");
		createIndex("work", new Document("manageLevel", 1), "manageLevel");

		createIndex("workInTemplate", new Document("wbsCode", 1), "wbs");
		createIndex("workInTemplate", new Document("index", 1), "index");
		createIndex("workInTemplate", new Document("parent_id", 1), "parent");
		createIndex("workInTemplate", new Document("template_id", 1), "template");

		createIndex("workPackage", new Document("work_id", 1), "work");

		createIndex("workPackageProgress", new Document("package_id", 1), "package");
		createIndex("workPackageProgress", new Document("time", -1), "time");

		createIndex("workReport", new Document("project_id", 1).append("type", 1), "type_project");
		createIndex("workReport", new Document("period", 1), "period");
		createIndex("workReport", new Document("reporter", 1).append("type", 1), "type_reporter");

		createIndex("workReportItem", new Document("report_id", 1), "report");
		createIndex("workReportItem", new Document("project_id", 1), "project");

		createIndex("workReportResourceActual", new Document("workReportItem_id", 1).append("work_id", 1)
				.append("resTypeId", 1).append("usedEquipResId", 1), "equip");
		createIndex("workReportResourceActual", new Document("workReportItem_id", 1).append("work_id", 1)
				.append("resTypeId", 1).append("usedTypedResId", 1), "type");
		createIndex("workReportResourceActual", new Document("workReportItem_id", 1).append("work_id", 1)
				.append("resTypeId", 1).append("usedHumanResId", 1), "hr");
		createIndex("workReportResourceActual", new Document("id", 1), "id");
		createUniqueIndex("workReportResourceActual",
				new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1)
						.append("usedEquipResId", 1).append("usedTypedResId", 1).append("workReportItemId", 1)
						.append("id", 1),
				"resource");

		createUniqueIndex("resourcePlanInTemplate", new Document("work_id", 1).append("resTypeId", 1)
				.append("usedHumanResId", 1).append("usedEquipResId", 1).append("usedTypedResId", 1), "resource");

		createIndex("worklinks", new Document("project_id", 1), "project");
		createIndex("worklinks", new Document("space_id", 1), "space");

		createIndex("workspace", new Document("project_id", 1), "project");
		createIndex("workspace", new Document("wbsCode", 1), "wbs");
		createIndex("workspace", new Document("index", 1), "index");
		createIndex("workspace", new Document("parent_id", 1), "parent");
		createIndex("workspace", new Document("space_id", 1), "space");

		createIndex("traceInfo", new Document("date", -1), "date");

		createUniqueIndex("folderInTemplate", new Document("name", 1).append("template_id", 1).append("parent_id", 1),
				"name_template");

		createUniqueIndex("dictionary", new Document("type", 1).append("id", 1), "type");

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
			c(collectionName).createIndex(keys, new IndexOptions().name(name));
		} catch (Exception e) {
			throw new ServiceException("集合：" + collectionName + "创建索引错误。" + e.getMessage());
		}
	}

	private void createUniqueIndex(String collectionName, final Document keys, String name) {
		try {
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

	@Override
	public void sendMessage(NewMessage msg) {
		sendMessage(msg.subject, msg.content, Optional.ofNullable(msg.sender).map(s -> s.getUserId()).orElse(null),
				msg.receiver.getUserId(), null);
	}

}
