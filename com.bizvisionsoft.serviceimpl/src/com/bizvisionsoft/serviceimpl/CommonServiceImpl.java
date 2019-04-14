package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.AccountIncome;
import com.bizvisionsoft.service.model.AccountItem;
import com.bizvisionsoft.service.model.Calendar;
import com.bizvisionsoft.service.model.Certificate;
import com.bizvisionsoft.service.model.ChangeProcess;
import com.bizvisionsoft.service.model.Dictionary;
import com.bizvisionsoft.service.model.Equipment;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.NewMessage;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.ResourceActual;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.renderer.MessageRenderer;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

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
		if (count(new BasicDBObject("_id", _id), "equipment") > 0) {
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id)).set(new BasicDBObject("resourceType_id", null))
					.bson();
			return updateEquipment(fu);
		} else if (count(new BasicDBObject("_id", _id), "user") > 0) {
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id)).set(new BasicDBObject("resourceType_id", null))
					.bson();
			return new UserServiceImpl().update(fu);
		} else {
			String id = c("resourceType").distinct("id", new BasicDBObject("_id", _id), String.class).first();

			// 检查资源计划
			if (count(new BasicDBObject("usedTypedResId", id), ResourcePlan.class) != 0)
				throw new ServiceException("不能删除在项目中作为资源计划的资源类型。");

			// 检查资源用量
			if (count(new BasicDBObject("usedTypedResId", id), ResourceActual.class) != 0)
				throw new ServiceException("不能删除在项目中作为资源用量的资源类型。");

			if (count(new BasicDBObject("resourceType_id", _id), "equipment") > 0) {// 删除与该资源类型关联的设备资源
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
						.set(new BasicDBObject("resourceType_id", null)).bson();
				updateEquipment(fu);
			} else if (count(new BasicDBObject("resourceType_id", _id), "user") > 0) {// 删除与该资源类型关联的人力资源
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
						.set(new BasicDBObject("resourceType_id", null)).bson();
				new UserServiceImpl().update(fu);
			}
			return delete(_id, ResourceType.class);
		}
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
		String id = c("equipment").distinct("id", new BasicDBObject("_id", _id), String.class).first();

		// 检查资源计划
		if (count(new BasicDBObject("usedEquipResId", id), ResourcePlan.class) != 0)
			throw new ServiceException("不能删除在项目中作为资源计划的设备设施。");
		// 检查资源用量
		if (count(new BasicDBObject("usedEquipResId", id), ResourceActual.class) != 0)
			throw new ServiceException("不能删除在项目中作为资源用量的设备设施。");

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
		c(Calendar.class).updateOne(new BasicDBObject("_id", _cal_id), new BasicDBObject("$addToSet", new BasicDBObject("workTime", r)));
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
	public long countDictionary() {
		return count(new BasicDBObject(), "dictionary");
	}

	@Override
	public List<Dictionary> getProjectRole() {
		return c(Dictionary.class).find(new BasicDBObject("type", "角色名称")).into(new ArrayList<Dictionary>());
	}

	@Override
	public Dictionary getProjectRole(String id) {
		return c(Dictionary.class).find(new BasicDBObject("type", "角色名称").append("id", id)).first();
	}

	@Override
	public long countProjectRole() {
		return c("dictionary").countDocuments(new BasicDBObject("type", "角色名称"));
	}

	@Override
	public Map<String, String> getDictionary(String type) {
		Map<String, String> result = new HashMap<String, String>();
		Iterable<Document> itr = c("dictionary").find(new BasicDBObject("type", type));
		itr.forEach(d -> result.put(d.getString("name") + " [" + d.getString("id") + "]", d.getString("id") + "#" + d.getString("name")));
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
		return c("dictionary").distinct(valueField, (new BasicDBObject("type", type)), String.class).into(new ArrayList<>());
	}

	@Override
	public List<AccountItem> getAccoutItemRoot() {
		return getAccoutItem(null);
	}

	@Override
	public List<AccountIncome> getAccoutIncomeRoot() {
		return getAccoutIncome(null);
	}

	@Override
	public List<AccountItem> getAccoutItem(String id) {
		return queryAccountItem(new BasicDBObject("parentId", id));
	}

	public List<AccountIncome> getAccoutIncome(String parentId) {
		return queryAccountIncome(new BasicDBObject("parentId", parentId));
	}

	@Override
	public long countAccoutItem(String id) {
		return count(new BasicDBObject("parentId", id), AccountItem.class);
	}

	@Override
	public long countAccoutIncome(String parentId) {
		return count(new BasicDBObject("parentId", parentId), AccountIncome.class);
	}

	@Override
	public long countAccoutItemRoot() {
		return countAccoutItem(null);
	}

	@Override
	public long countAccoutIncomeRoot() {
		return countAccoutIncome(null);
	}

	@Override
	public AccountItem insertAccountItem(AccountItem ai) {
		ai = insert(ai, AccountItem.class);
		String parentId = ai.getParentId();
		if (parentId != null) {
			c("accountItem").updateMany(
					new Document("$or", Arrays.asList(new Document("id", parentId), new Document("subAccounts", parentId))),
					new Document("$push", new Document("subAccounts", ai.getId())));
		}
		return ai;
	}

	@Override
	public AccountIncome insertAccountIncome(AccountIncome ai) {
		ai = insert(ai, AccountIncome.class);
		String parentId = ai.getParentId();
		if (parentId != null) {
			c("accountIncome").updateMany(
					new Document("$or", Arrays.asList(new Document("id", parentId), new Document("subAccounts", parentId))),
					new Document("$push", new Document("subAccounts", ai.getId())));
		}
		return ai;
	}

	@Override
	public long deleteAccountItem(ObjectId _id) {
		Document doc = c("accountItem").find(new Document("_id", _id)).first();
		if (doc != null) {
			String id = doc.getString("id");
			ArrayList<Object> toDelete = new ArrayList<>();
			toDelete.add(id);
			List<?> accounts = (List<?>) doc.get("subAccounts");
			if (accounts != null) {
				toDelete.addAll(accounts);
			}

			// 引用的
			long refCnt = c("cbsSubject").countDocuments(new Document("subjectNumber", new Document("$in", toDelete)));
			if (refCnt > 0) {
				throw new ServiceException("不能删除项目预算和成本正在使用的科目。");
			}

			c("accountItem").updateMany(new Document("subAccounts", new Document("$in", toDelete)),
					new Document("$pullAll", new Document("subAccounts", toDelete)));

			c("accountItem").deleteMany(new Document("id", new Document("$in", toDelete)));

			return 1;
		}
		return 0;
	}

	@Override
	public long deleteAccountIncome(ObjectId _id) {
		Document doc = c("accountIncome").find(new Document("_id", _id)).first();
		if (doc != null) {
			String id = doc.getString("id");
			ArrayList<Object> toDelete = new ArrayList<>();
			toDelete.add(id);
			List<?> accounts = (List<?>) doc.get("subAccounts");
			if (accounts != null) {
				toDelete.addAll(accounts);
			}

			// 引用的
			long refCnt = c("revenueForecastItem").countDocuments(new Document("subject", new Document("$in", toDelete)));
			if (refCnt > 0) {
				throw new ServiceException("不能删除项目收益预测正在使用的科目。");
			}

			refCnt = c("revenueRealizeItem").countDocuments(new Document("subject", new Document("$in", toDelete)));
			if (refCnt > 0) {
				throw new ServiceException("不能删除项目收益实现正在使用的科目。");
			}

			c("accountIncome").updateMany(new Document("subAccounts", new Document("$in", toDelete)),
					new Document("$pullAll", new Document("subAccounts", toDelete)));

			c("accountIncome").deleteMany(new Document("id", new Document("$in", toDelete)));
			return 1;
		}
		return 0;
	}

	@Override
	public long updateAccountItem(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, AccountItem.class);
	}

	@Override
	public long updateAccountIncome(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, AccountIncome.class);
	}

	@Override
	public List<AccountItem> queryAccountItem(BasicDBObject filter) {
		List<Bson> pipeline = new ArrayList<Bson>();

		if (filter != null) {
			pipeline.add(Aggregates.match(filter));
		}

		pipeline.add(Aggregates.sort(new BasicDBObject("id", 1)));

		return c(AccountItem.class).aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public List<AccountIncome> queryAccountIncome(BasicDBObject filter) {
		List<Bson> pipeline = new ArrayList<Bson>();

		if (filter != null) {
			pipeline.add(Aggregates.match(filter));
		}

		pipeline.add(Aggregates.sort(new BasicDBObject("id", 1)));

		return c(AccountIncome.class).aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public Message getMessage(ObjectId _id) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new Document("_id", _id)));

		appendUserInfoAndHeadPic(pipeline, "sender", "senderInfo", "senderHeadPic");

		appendUserInfo(pipeline, "receiver", "receiverInfo");

		return c(Message.class).aggregate(pipeline).first();
	}

	@Override
	public List<Message> listMessage(BasicDBObject condition, String userId) {
		ArrayList<Bson> pipeline = createUserMessagePippline(condition, userId);
		return c(Message.class).aggregate(pipeline).into(new ArrayList<Message>());
	}

	private ArrayList<Bson> createUserMessagePippline(BasicDBObject condition, String userId) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			condition.put("filter", filter = new BasicDBObject());
		}
		filter.put("receiver", userId);
		filter.put("read", false);

		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		pipeline.add(Aggregates.sort(new BasicDBObject("sendDate", -1).append("_id", -1)));

		Integer skip = (Integer) condition.get("skip");
		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		Integer limit = (Integer) condition.get("limit");
		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendUserInfoAndHeadPic(pipeline, "sender", "senderInfo", "senderHeadPic");

		appendUserInfo(pipeline, "receiver", "receiverInfo");
		return pipeline;
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
	public List<Document> listUnreadMessage(BasicDBObject condition, String userId) {
		if (condition == null) {
			condition = new BasicDBObject();
		}
		condition.put("read", false);
		ArrayList<Bson> pipeline = createUserMessagePippline(condition, userId);
		return c(Message.class).aggregate(pipeline).map(MessageRenderer::render).into(new ArrayList<>());

	}

	@Override
	public long countUnreadMessage(BasicDBObject filter, String userId) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.put("receiver", userId);
		filter.put("read", false);
		return count(filter, Message.class);
	}

	@Override
	public int generateCode(String name, String key) {
		return super.generateCode(name, key);
	}

	@Override
	public List<TrackView> listTrackView(BasicDBObject condition) {
		return createDataSet(condition, TrackView.class);
	}

	@Override
	public long countTrackView(BasicDBObject filter) {
		return count(filter, TrackView.class);
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
				.find(new Document("status", new Document("$nin", Arrays.asList(ProjectStatus.Created, ProjectStatus.Closed))))
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

	// ///////////////////////////////////////
	// // DEMO DATA
	// // TODO REMOVE
	// @Override
	// @Deprecated
	// public void mockupSalesData() {
	// // 创建产品主数据
	// List<Bson> pipeline = Arrays.asList(new Document("$group",
	// new Document("_id", new Document("a", "$productId").append("b",
	// "$project_id"))));
	// c("salesItem").aggregate(pipeline).forEach((Document d) -> {
	// Document _r = (Document) d.get("_id");
	// String productId = _r.getString("a");
	// if (Arrays.asList("02302", "02303", "02304", "02305", "02306",
	// "02307").contains(productId)) {
	// return;
	// }
	//
	// ObjectId project_id = _r.getObjectId("b");
	// String series = getRandomSeries();
	// String name = series + "-" + Formatter.dec_n(System.currentTimeMillis(), 24);
	// String position = "定位XXX";
	// String sellPoint = "滑翔，变形，声光电。";
	// ObjectId benchmarking_id = new ObjectId("5b4acc0be37dab0cfcb4458e");
	//
	// Document product = new Document("_id", new ObjectId()).append("sellPoint",
	// sellPoint)
	// .append("project_id", project_id).append("series", series).append("name",
	// name)
	// .append("id", productId).append("position",
	// position).append("benchmarking_id", benchmarking_id);
	// c("product").insertOne(product);
	//
	// });
	//
	// // 制造销售额和销售量
	// c("salesItem").find().forEach((Document d) -> {
	// Number profit = (Number) d.get("profit");
	// double income = profit.doubleValue() / 0.4;
	// int val = getRandomInt();
	// c("salesItem").updateOne(new Document("_id", d.get("_id")),
	// new Document("$set", new Document("income", income).append("volumn", val)));
	// });
	//
	// }
	//
	// @Deprecated
	// // TODO REMOVE
	// private String getRandomSeries() {
	// String[] series = new String[] { "超级飞侠", "萌鸡小队", "巴啦啦小魔仙", "火力少年王", "铠甲勇士" };
	// return series[new Random().nextInt(series.length)];
	// }
	//
	// @Deprecated
	// // TODO REMOVE
	// private int getRandomInt() {
	// int[] series = new int[] { 10, 20, 30, 40, 15, 5, 12, 50 };
	// return series[new Random().nextInt(series.length)];
	// }

	@Override
	public long updateMessage(BasicDBObject fu) {
		return update(fu, Message.class);
	}

	@Override
	public void sendMessage(NewMessage msg) {
		sendMessage(msg.subject, msg.content, Optional.ofNullable(msg.sender).map(s -> s.getUserId()).orElse(null),
				msg.receiver.getUserId(), null);
	}

	@Override
	public boolean hasSomethingNewOfMyWork(String userId) {
		WorkServiceImpl impl = new WorkServiceImpl();
		if (impl.countChargerProcessingWorkDataSet(new BasicDBObject(), userId) > 0) {
			return true;
		}
		if (impl.countMyAssignmentWork(new BasicDBObject(), userId) > 0) {
			return true;
		}
		if (new ProjectServiceImpl().countReviewerProjectChange(new BasicDBObject(), userId) > 0) {
			return true;
		}
		if (new WorkReportServiceImpl().countWorkReportDataSet(new BasicDBObject(), userId) > 0) {
			return true;
		}
		return false;
	}

	@Override
	public void syncOrgFullName() {
		c("organization").find().into(new ArrayList<>()).forEach((Document d) -> {
			c("organization").updateOne(new Document("_id", d.get("_id")), new Document("$set", new Document("fullName", d.get("name"))));
		});
	}

	@Override
	public Document getSetting(String name) {
		return super.getSystemSetting(name);
	}

	@Override
	public void updateSetting(Document setting) {
		Object name = setting.get("name");
		setting.remove("_id");
		long cnt = c("setting").countDocuments(new Document("name", name));
		if (cnt == 0) {
			c("setting").insertOne(setting);
		} else {
			c("setting").updateOne(new Document("name", name), new Document("$set", setting));
		}
	}

	@Override
	public List<Document> getAllAccoutItemsHasParentIds() {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.lookup("accountItem", "id", "subAccounts", "parentIds"));
		pipeline.add(Aggregates.project(new BasicDBObject("parentIds", "$parentIds.id").append("id", true)));
		pipeline.add(Aggregates.sort(new BasicDBObject("id", 1)));
		return c("accountItem").aggregate(pipeline).into(new ArrayList<Document>());
	}

	@Override
	public List<Dictionary> listFunctionRoles(BasicDBObject condition) {
		BasicDBObject filter = Optional.ofNullable((BasicDBObject) condition.get("filter")).orElse(new BasicDBObject()).append("type",
				"功能角色");
		condition.append("filter", filter);
		return createDataSet(condition, Dictionary.class);
	}

	@Override
	public long countFunctionRoles(BasicDBObject filter) {
		if (filter == null)
			filter = new BasicDBObject();
		filter.put("type", "功能角色");
		return c("dictionary").countDocuments(filter);
	}

}
