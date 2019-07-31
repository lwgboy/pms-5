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
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.Equipment;
import com.bizvisionsoft.service.model.ExportDocRule;
import com.bizvisionsoft.service.model.FormDef;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.NewMessage;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.RefDef;
import com.bizvisionsoft.service.model.ResourceActual;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.VaultFolder;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.renderer.MessageRenderer;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import com.mongodb.client.model.Field;

public class CommonServiceImpl extends BasicServiceImpl implements CommonService {

	@Override
	public List<Certificate> getCertificates(BasicDBObject condition, String domain) {
		return createDataSet(condition, Certificate.class, domain);
	}

	@Override
	public Certificate insertCertificate(Certificate cert, String domain) {
		return insert(cert, domain);
	}

	@Override
	public long deleteCertificate(ObjectId _id, String domain) {
		// TODO 考虑已经使用的任职资格

		return delete(_id, Certificate.class, domain);
	}

	@Override
	public long updateCertificate(BasicDBObject fu, String domain) {
		return update(fu, Certificate.class, domain);
	}

	@Override
	public List<String> getCertificateNames(String domain) {
		ArrayList<String> result = new ArrayList<String>();
		c(Certificate.class, domain).distinct("name", String.class).into(result);
		return result;
	}

	@Override
	public List<ResourceType> getResourceType(String domain) {
		return c(ResourceType.class, domain).find().into(new ArrayList<>());
	}

	@Override
	public ResourceType insertResourceType(ResourceType resourceType, String domain) {
		return insert(resourceType, ResourceType.class, domain);
	}

	@Override
	public long deleteResourceType(ObjectId _id, String domain) {
		// TODO 考虑资源类型被使用的状况
		if (count(new BasicDBObject("_id", _id), "equipment", domain) > 0) {
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id)).set(new BasicDBObject("resourceType_id", null))
					.bson();
			return updateEquipment(fu, domain);
		} else if (count(new BasicDBObject("_id", _id), "user", domain) > 0) {
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id)).set(new BasicDBObject("resourceType_id", null))
					.bson();
			return new UserServiceImpl().update(fu, domain);
		} else {
			String id = c("resourceType", domain).distinct("id", new BasicDBObject("_id", _id), String.class).first();

			// 检查资源计划
			if (count(new BasicDBObject("usedTypedResId", id), ResourcePlan.class, domain) != 0)
				throw new ServiceException("不能删除在项目中作为资源计划的资源类型。");

			// 检查资源用量
			if (count(new BasicDBObject("usedTypedResId", id), ResourceActual.class, domain) != 0)
				throw new ServiceException("不能删除在项目中作为资源用量的资源类型。");

			if (count(new BasicDBObject("resourceType_id", _id), "equipment", domain) > 0) {// 删除与该资源类型关联的设备资源
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
						.set(new BasicDBObject("resourceType_id", null)).bson();
				updateEquipment(fu, domain);
			} else if (count(new BasicDBObject("resourceType_id", _id), "user", domain) > 0) {// 删除与该资源类型关联的人力资源
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
						.set(new BasicDBObject("resourceType_id", null)).bson();
				new UserServiceImpl().update(fu, domain);
			}
			return delete(_id, ResourceType.class, domain);
		}
	}

	@Override
	public long updateResourceType(BasicDBObject fu, String domain) {
		return update(fu, ResourceType.class, domain);
	}

	@Override
	public List<Equipment> getERResources(ObjectId _id, String domain) {
		return queryEquipments(new BasicDBObject("resourceType_id", _id), domain);
	}

	private ArrayList<Equipment> queryEquipments(BasicDBObject condition, String domain) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();
		if (condition != null)
			pipeline.add(Aggregates.match(condition));

		appendOrgFullName(pipeline, "org_id", "orgFullName");

		ArrayList<Equipment> result = new ArrayList<Equipment>();
		c(Equipment.class, domain).aggregate(pipeline).into(result);
		return result;
	}

	@Override
	public ResourceType getResourceType(ObjectId _id, String domain) {
		return get(_id, ResourceType.class, domain);
	}

	@Override
	public long countERResources(ObjectId _id, String domain) {
		return c(Equipment.class, domain).countDocuments(new BasicDBObject("resourceType_id", _id));
	}

	@Override
	public List<Equipment> getEquipments(String domain) {
		return queryEquipments(null, domain);
	}

	@Override
	public Equipment insertEquipment(Equipment cert, String domain) {
		return insert(cert, Equipment.class, domain);
	}

	@Override
	public long deleteEquipment(ObjectId _id, String domain) {
		// TODO 完整性问题
		String id = c("equipment", domain).distinct("id", new BasicDBObject("_id", _id), String.class).first();

		// 检查资源计划
		if (count(new BasicDBObject("usedEquipResId", id), ResourcePlan.class, domain) != 0)
			throw new ServiceException("不能删除在项目中作为资源计划的设备设施。");
		// 检查资源用量
		if (count(new BasicDBObject("usedEquipResId", id), ResourceActual.class, domain) != 0)
			throw new ServiceException("不能删除在项目中作为资源用量的设备设施。");

		return delete(_id, Equipment.class, domain);
	}

	@Override
	public long updateEquipment(BasicDBObject fu, String domain) {
		return update(fu, Equipment.class, domain);
	}

	@Override
	public List<Calendar> getCalendars(String domain) {
		return c(Calendar.class, domain).find().into(new ArrayList<Calendar>());
	}

	@Override
	public Calendar getCalendar(ObjectId _id, String domain) {
		return get(_id, Calendar.class, domain);
	}

	@Override
	public Calendar insertCalendar(Calendar obj, String domain) {
		return insert(obj, Calendar.class, domain);
	}

	@Override
	public long deleteCalendar(ObjectId _id, String domain) {
		// TODO 完整性问题
		return delete(_id, Calendar.class, domain);
	}

	@Override
	public long updateCalendar(BasicDBObject fu, String domain) {
		return update(fu, Calendar.class, domain);
	}

	@Override
	public void addCalendarWorktime(BasicDBObject r, ObjectId _cal_id, String domain) {
		c(Calendar.class, domain).updateOne(new BasicDBObject("_id", _cal_id),
				new BasicDBObject("$addToSet", new BasicDBObject("workTime", r)));
	}

	/**
	 * db.getCollection('calendar').updateOne({"workTime._id":ObjectId("5ad49b5a85e0fb335c355fae")},
	 * {$set:{"workTime.$":"aaa"}
	 * 
	 * })
	 */
	@Override
	public void updateCalendarWorkTime(BasicDBObject r, String domain) {
		c(Calendar.class, domain).updateOne(new BasicDBObject("workTime._id", r.get("_id")),
				new BasicDBObject("$set", new BasicDBObject("workTime.$", r)));
	}

	@Override
	public void deleteCalendarWorkTime(ObjectId _id, String domain) {
		c(Calendar.class, domain).updateOne(new BasicDBObject("workTime._id", _id),
				new BasicDBObject("$pull", new BasicDBObject("workTime", new BasicDBObject("_id", _id))));
	}

	@Override
	public List<Dictionary> getDictionary(String domain) {
		List<Dictionary> target = new ArrayList<Dictionary>();
		c(Dictionary.class, domain).find().sort(new BasicDBObject("type", 1)).into(target);
		return target;
	}

	@Override
	public Dictionary insertResourceType(Dictionary dic, String domain) {
		return insert(dic, Dictionary.class, domain);
	}

	@Override
	public long deleteDictionary(ObjectId _id, String domain) {
		return delete(_id, Dictionary.class, domain);
	}

	@Override
	public long updateDictionary(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, Dictionary.class, domain);
	}

	@Override
	public long countDictionary(String domain) {
		return count(new BasicDBObject(), "dictionary", domain);
	}

	@Override
	public List<Dictionary> getProjectRole(String domain) {
		return c(Dictionary.class, domain).find(new BasicDBObject("type", "角色名称")).into(new ArrayList<Dictionary>());
	}

	@Override
	public Dictionary getProjectRole(String id, String domain) {
		return c(Dictionary.class, domain).find(new BasicDBObject("type", "角色名称").append("id", id)).first();
	}

	@Override
	public long countProjectRole(String domain) {
		return c("dictionary", domain).countDocuments(new BasicDBObject("type", "角色名称"));
	}

	@Override
	public Map<String, String> getDictionary(String type, String domain) {
		Map<String, String> result = new HashMap<String, String>();
		Iterable<Document> itr = c("dictionary", domain).find(new BasicDBObject("type", type));
		itr.forEach(d -> result.put(d.getString("name") + " [" + d.getString("id") + "]", d.getString("id") + "#" + d.getString("name")));
		return result;
	}

	@Override
	public Map<String, String> getDictionaryIdNamePair(String type, String domain) {
		Map<String, String> result = new HashMap<String, String>();
		Iterable<Document> itr = c("dictionary", domain).find(new BasicDBObject("type", type));
		itr.forEach(d -> result.put(d.getString("id"), d.getString("name")));
		return result;
	}

	@Override
	public List<String> listDictionary(String type, String valueField, String domain) {
		return c("dictionary", domain).find(new Document("type", type)).sort(new Document("index", -1)).map(d -> d.getString(valueField))
				.into(new ArrayList<>());
	}

	@Override
	public List<AccountItem> getAccoutItemRoot(String domain) {
		return getAccoutItem(null, domain);
	}

	@Override
	public List<AccountIncome> getAccoutIncomeRoot(String domain) {
		return getAccoutIncome(null, domain);
	}

	@Override
	public List<AccountItem> getAccoutItem(String id, String domain) {
		return queryAccountItem(new BasicDBObject("parentId", id), domain);
	}

	public List<AccountIncome> getAccoutIncome(String parentId, String domain) {
		return queryAccountIncome(new BasicDBObject("parentId", parentId), domain);
	}

	@Override
	public long countAccoutItem(String id, String domain) {
		return count(new BasicDBObject("parentId", id), AccountItem.class, domain);
	}

	@Override
	public long countAccoutIncome(String parentId, String domain) {
		return count(new BasicDBObject("parentId", parentId), AccountIncome.class, domain);
	}

	@Override
	public long countAccoutItemRoot(String domain) {
		return countAccoutItem(null, domain);
	}

	@Override
	public long countAccoutIncomeRoot(String domain) {
		return countAccoutIncome(null, domain);
	}

	@Override
	public AccountItem insertAccountItem(AccountItem ai, String domain) {
		ai = insert(ai, AccountItem.class, domain);
		String parentId = ai.getParentId();
		if (parentId != null) {
			c("accountItem", domain).updateMany(
					new Document("$or", Arrays.asList(new Document("id", parentId), new Document("subAccounts", parentId))),
					new Document("$push", new Document("subAccounts", ai.getId())));
		}
		return ai;
	}

	@Override
	public AccountIncome insertAccountIncome(AccountIncome ai, String domain) {
		ai = insert(ai, AccountIncome.class, domain);
		String parentId = ai.getParentId();
		if (parentId != null) {
			c("accountIncome", domain).updateMany(
					new Document("$or", Arrays.asList(new Document("id", parentId), new Document("subAccounts", parentId))),
					new Document("$push", new Document("subAccounts", ai.getId())));
		}
		return ai;
	}

	@Override
	public long deleteAccountItem(ObjectId _id, String domain) {
		Document doc = c("accountItem", domain).find(new Document("_id", _id)).first();
		if (doc != null) {
			String id = doc.getString("id");
			ArrayList<Object> toDelete = new ArrayList<>();
			toDelete.add(id);
			List<?> accounts = (List<?>) doc.get("subAccounts");
			if (accounts != null) {
				toDelete.addAll(accounts);
			}

			// 引用的
			long refCnt = c("cbsSubject", domain).countDocuments(new Document("subjectNumber", new Document("$in", toDelete)));
			if (refCnt > 0) {
				throw new ServiceException("不能删除项目预算和成本正在使用的科目。");
			}

			c("accountItem", domain).updateMany(new Document("subAccounts", new Document("$in", toDelete)),
					new Document("$pullAll", new Document("subAccounts", toDelete)));

			c("accountItem", domain).deleteMany(new Document("id", new Document("$in", toDelete)));

			return 1;
		}
		return 0;
	}

	@Override
	public long deleteAccountIncome(ObjectId _id, String domain) {
		Document doc = c("accountIncome", domain).find(new Document("_id", _id)).first();
		if (doc != null) {
			String id = doc.getString("id");
			ArrayList<Object> toDelete = new ArrayList<>();
			toDelete.add(id);
			List<?> accounts = (List<?>) doc.get("subAccounts");
			if (accounts != null) {
				toDelete.addAll(accounts);
			}

			// 引用的
			long refCnt = c("revenueForecastItem", domain).countDocuments(new Document("subject", new Document("$in", toDelete)));
			if (refCnt > 0) {
				throw new ServiceException("不能删除项目收益预测正在使用的科目。");
			}

			refCnt = c("revenueRealizeItem", domain).countDocuments(new Document("subject", new Document("$in", toDelete)));
			if (refCnt > 0) {
				throw new ServiceException("不能删除项目收益实现正在使用的科目。");
			}

			c("accountIncome", domain).updateMany(new Document("subAccounts", new Document("$in", toDelete)),
					new Document("$pullAll", new Document("subAccounts", toDelete)));

			c("accountIncome", domain).deleteMany(new Document("id", new Document("$in", toDelete)));
			return 1;
		}
		return 0;
	}

	@Override
	public long updateAccountItem(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, AccountItem.class, domain);
	}

	@Override
	public long updateAccountIncome(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, AccountIncome.class, domain);
	}

	@Override
	public List<AccountItem> queryAccountItem(BasicDBObject filter, String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();

		if (filter != null) {
			pipeline.add(Aggregates.match(filter));
		}

		pipeline.add(Aggregates.sort(new BasicDBObject("id", 1)));

		return c(AccountItem.class, domain).aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public List<AccountIncome> queryAccountIncome(BasicDBObject filter, String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();

		if (filter != null) {
			pipeline.add(Aggregates.match(filter));
		}

		pipeline.add(Aggregates.sort(new BasicDBObject("id", 1)));

		return c(AccountIncome.class, domain).aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public Message getMessage(ObjectId _id, String domain) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new Document("_id", _id)));

		appendUserInfoAndHeadPic(pipeline, "sender", "senderInfo", "senderHeadPic");

		appendUserInfo(pipeline, "receiver", "receiverInfo", domain);

		return c(Message.class, domain).aggregate(pipeline).first();
	}

	@Override
	public List<Message> listMessage(BasicDBObject condition, String userId, String domain) {
		ArrayList<Bson> pipeline = createUserMessagePippline(condition, userId, domain);
		return c(Message.class, domain).aggregate(pipeline).into(new ArrayList<Message>());
	}

	private ArrayList<Bson> createUserMessagePippline(BasicDBObject condition, String userId, String domain) {
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

		appendUserInfo(pipeline, "receiver", "receiverInfo", domain);
		return pipeline;
	}

	@Override
	public long countMessage(BasicDBObject filter, String userId, String domain) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.put("receiver", userId);
		return count(filter, Message.class, domain);
	}

	@Override
	public List<Document> listUnreadMessage(BasicDBObject condition, String userId, String domain) {
		if (condition == null) {
			condition = new BasicDBObject();
		}
		condition.put("read", false);
		ArrayList<Bson> pipeline = createUserMessagePippline(condition, userId, domain);
		return c(Message.class, domain).aggregate(pipeline).map(MessageRenderer::render).into(new ArrayList<>());

	}

	@Override
	public long countUnreadMessage(BasicDBObject filter, String userId, String domain) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.put("receiver", userId);
		filter.put("read", false);
		return count(filter, Message.class, domain);
	}

	@Override
	public List<TrackView> listTrackView(BasicDBObject condition, String domain) {
		return createDataSet(condition, TrackView.class, domain);
	}

	@Override
	public long countTrackView(BasicDBObject filter, String domain) {
		return count(filter, TrackView.class, domain);
	}

	@Override
	public TrackView insertTrackView(TrackView trackView, String domain) {
		return insert(trackView, TrackView.class, domain);
	}

	@Override
	public long deleteTrackView(ObjectId _id, String domain) {
		return delete(_id, TrackView.class, domain);
	}

	@Override
	public long updateTrackView(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, TrackView.class, domain);
	}

	@Override
	public Date getCurrentCBSPeriod(String domain) {
		Document doc = c("project", domain)
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
	public long countCertificate(BasicDBObject filter, String domain) {
		return count(filter, Certificate.class, domain);
	}

	@Override
	public List<ChangeProcess> createChangeProcessDataSet(String domain) {
		return c(ChangeProcess.class, domain).find().sort(new Document("index", 1)).into(new ArrayList<ChangeProcess>());
	}

	@Override
	public ChangeProcess getChangeProcess(ObjectId _id, String domain) {
		return get(_id, ChangeProcess.class, domain);
	}

	@Override
	public ChangeProcess insertChangeProcess(ChangeProcess changeProcess, String domain) {

		Document doc = c("changeProcess", domain).find().sort(new Document("index", -1)).projection(new Document("index", 1)).first();
		if (doc != null)
			changeProcess.setIndex(doc.getInteger("index", 0) + 1);
		else
			changeProcess.setIndex(0);
		return insert(changeProcess, ChangeProcess.class, domain);
	}

	@Override
	public long deleteChangeProcess(ObjectId _id, String domain) {
		return delete(_id, ChangeProcess.class, domain);
	}

	@Override
	public long updateChangeProcess(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, ChangeProcess.class, domain);
	}

	@Override
	public List<String> listWorkTag(String domain) {
		return new ArrayList<String>(getDictionaryIdNamePair("工作标签", domain).values());
	}

	@Override
	public List<Document> listStructuredData(BasicDBObject query, String domain) {
		return c("structuredData", domain).find(query).sort(new Document("index", 1)).into(new ArrayList<>());
	}

	@Override
	public void insertStructuredData(List<Document> result, String domain) {
		c("structuredData", domain).insertMany(result);
	}

	@Override
	public void updateStructuredData(BasicDBObject fu, String domain) {
		update(fu, "structuredData", domain);
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
	// c("salesItem",domain).aggregate(pipeline).forEach((Document d) -> {
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
	// c("salesItem",domain).find().forEach((Document d) -> {
	// Number profit = (Number) d.get("profit");
	// double income = profit.doubleValue() / 0.4;
	// int val = getRandomInt();
	// c("salesItem",domain).updateOne(new Document("_id", d.get("_id")),
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
	public long updateMessage(BasicDBObject fu, String domain) {
		return update(fu, Message.class, domain);
	}

	@Override
	public void sendMessage(NewMessage msg, String domain) {
		sendMessage(msg.subject, msg.content, Optional.ofNullable(msg.sender).map(s -> s.getUserId()).orElse(null),
				msg.receiver.getUserId(), null, domain);
	}

	@Override
	public boolean hasSomethingNewOfMyWork(String userId, String domain) {
		WorkServiceImpl impl = new WorkServiceImpl();
		if (impl.countChargerProcessingWorkDataSet(new BasicDBObject(), userId, domain) > 0) {
			return true;
		}
		if (impl.countMyAssignmentWork(new BasicDBObject(), userId, domain) > 0) {
			return true;
		}
		if (new ProjectServiceImpl().countReviewerProjectChange(new BasicDBObject(), userId, domain) > 0) {
			return true;
		}
		if (new WorkReportServiceImpl().countWorkReportDataSet(new BasicDBObject(), userId, domain) > 0) {
			return true;
		}
		return false;
	}

	@Override
	public void syncOrgFullName(String domain) {
		c("organization", domain).find().into(new ArrayList<>()).forEach((Document d) -> {
			c("organization", domain).updateOne(new Document("_id", d.get("_id")),
					new Document("$set", new Document("fullName", d.get("name"))));
		});
	}

	@Override
	public Document getSetting(String name, String domain) {
		return super.getSystemSetting(name, domain);
	}

	@Override
	public Document getSetting(String name) {
		return super.getSystemSetting(name);
	}

	@Override
	public void updateSetting(Document setting, String domain) {
		MongoCollection<Document> col;
		if (domain == null) {
			col = c("setting");
		} else {
			col = c("setting", domain);
		}
		Object name = setting.get("name");
		setting.remove("_id");
		long cnt = col.countDocuments(new Document("name", name));
		if (cnt == 0) {
			col.insertOne(setting);
		} else {
			col.updateOne(new Document("name", name), new Document("$set", setting));
		}
	}

	@Override
	public void updateSetting(Document setting) {
		updateSetting(setting, null);
	}

	@Override
	public List<Document> getAllAccoutItemsHasParentIds(String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.lookup("accountItem", "id", "subAccounts", "parentIds"));
		pipeline.add(Aggregates.project(new BasicDBObject("parentIds", "$parentIds.id").append("id", true)));
		pipeline.add(Aggregates.sort(new BasicDBObject("id", 1)));
		return c("accountItem", domain).aggregate(pipeline).into(new ArrayList<Document>());
	}

	@Override
	public List<Dictionary> listFunctionRoles(BasicDBObject condition, String domain) {
		BasicDBObject filter = Optional.ofNullable((BasicDBObject) condition.get("filter")).orElse(new BasicDBObject()).append("type",
				"功能角色");
		condition.append("filter", filter);
		return createDataSet(condition, Dictionary.class, domain);
	}

	@Override
	public long countFunctionRoles(BasicDBObject filter, String domain) {
		if (filter == null)
			filter = new BasicDBObject();
		filter.put("type", "功能角色");
		return c("dictionary", domain).countDocuments(filter);
	}

	@Override
	public List<VaultFolder> listContainer(BasicDBObject condition, String domain) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = Optional.ofNullable((BasicDBObject) condition.get("sort")).orElse(new BasicDBObject()).append("desc", 1);

		return query(pipeline -> {
			pipeline.add(Aggregates.match(new BasicDBObject("iscontainer", true)));
			pipeline.add(Aggregates.lookup("organization", "org_id", "_id", "_org"));
			pipeline.add(Aggregates.addFields(new Field<String>("orgFullName", "$_org.fullName")));
			pipeline.add(Aggregates.project(new BasicDBObject("_org", false)));//
		}, skip, limit, filter, sort, null, VaultFolder.class, domain);
	}

	@Override
	public long countContainer(BasicDBObject filter, String domain) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new BasicDBObject("iscontainer", true)));
		pipeline.add(Aggregates.lookup("organization", "org_id", "_id", "_org"));
		pipeline.add(Aggregates.addFields(new Field<String>("orgFullName", "$_org.fullName")));
		pipeline.add(Aggregates.project(new BasicDBObject("_org", false)));//

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		return c(VaultFolder.class, domain).aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public long deleteContainer(ObjectId _id, String domain) {
		// TODO 能否删除判断
		return delete(_id, VaultFolder.class, domain);
	}

	@Override
	public long updateContainer(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, VaultFolder.class, domain);
	}

	@Override
	public VaultFolder insertContainer(VaultFolder vf, String domain) {
		// TODO 添加资料库默认属性
		vf.setIsContainer(true);
		return insert(vf, VaultFolder.class, domain);
	}

	@Override
	public VaultFolder getVaultFolder(ObjectId _id, String domain) {
		return get(_id, VaultFolder.class, domain);
	}

	@Override
	public List<FormDef> listFormDef(BasicDBObject condition, String domain) {
		BasicDBObject sort = new BasicDBObject();
		sort.append("name", 1);
		sort.append("vid", -1);

		if (condition.get("sort") != null)
			sort.putAll(((BasicDBObject) condition.get("sort")).toMap());

		condition.put("sort", sort);

		return createDataSet(condition, FormDef.class, domain);
	}

	@Override
	public long countFormDef(BasicDBObject filter, String domain) {
		return count(filter, FormDef.class, domain);
	}

	@Override
	public List<Document> listNameOfFormDef(BasicDBObject condition, String domain) {
		ArrayList<Document> into = new ArrayList<Document>();
		c("formDef", domain)
				.distinct("name", Optional.ofNullable((BasicDBObject) condition.get("filter")).orElse(new BasicDBObject()), String.class)
				.forEach((String s) -> {
					into.add(new Document("name", s));
				});

		return into;
	}

	@Override
	public long countNameOfFormDef(BasicDBObject filter, String domain) {
		return c("formDef", domain)
				.distinct("name", Optional.ofNullable(filter).orElse(new BasicDBObject()).append("activated", true), String.class)
				.into(new ArrayList<String>()).size();
	}

	@Override
	public long deleteFormDef(ObjectId _id, String domain) {
		Document doc = c("formDef", domain).find(new BasicDBObject("_id", _id)).first();
		if (doc == null)
			throw new ServiceException("表单定义已被删除。");

		// 检查表单定义是否启用,启用的表单定义不允许被删除
		if (doc.getBoolean("activated", true))
			throw new ServiceException("表单定义已启用,不允许删除。");

		// 检查表单定义是否被使用,被使用的表单定义不允许被删除
		// TODO docu增加formDef_id索引
		if (count(new BasicDBObject("formDef_id", doc.get("_id")), Docu.class, domain) > 0)
			throw new ServiceException("表单定义在使用中,不允许无法删除。");

		c(ExportDocRule.class, domain).deleteMany(new BasicDBObject("formDef_id", _id));

		c(RefDef.class, domain).deleteMany(new BasicDBObject("formDef_id", _id));

		return delete(_id, FormDef.class, domain);
	}

	@Override
	public long updateFormDef(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, FormDef.class, domain);
	}

	@Override
	public FormDef insertFormDef(FormDef formDef, String domain) {
		if (count(new BasicDBObject("name", formDef.getName()), FormDef.class, domain) > 0)
			throw new ServiceException("表单定义重名。");

		return insert(formDef, FormDef.class, domain);
	}

	@Override
	public FormDef getFormDef(ObjectId _id, String domain) {
		return get(_id, FormDef.class, domain);
	}

	@Override
	public FormDef upgradeFormDef(ObjectId _id, String domain) {
		Document formDefDoc = c("formDef", domain).find(new BasicDBObject("_id", _id)).first();
		if (formDefDoc == null)
			throw new ServiceException("表单定义已被删除，无法升版。");

		Document vidDoc = c("formDef", domain)
				.aggregate(Arrays.asList(Aggregates.match(new BasicDBObject("name", formDefDoc.get("name"))), Aggregates.group(
						new BasicDBObject("_id", "$name"), Arrays.asList(new BsonField("value", new BasicDBObject("$max", "$vid"))))))
				.first();

		int version = Optional.ofNullable((Number) vidDoc.get("value")).map(i -> i.intValue() + 1).orElse(0);

		// 设置新表单定义的_id和版本号
		ObjectId formDef_id = new ObjectId();
		formDefDoc.put("_id", formDef_id);
		formDefDoc.put("vid", version);
		formDefDoc.put("activated", false);

		// 复制文档导出规则
		List<Document> exportDocRuleDoc = new ArrayList<Document>();
		c("exportDocRule", domain).find(new BasicDBObject("formDef_id", _id)).forEach((Document doc) -> {
			doc.remove("_id");
			doc.put("formDef_id", formDef_id);
			exportDocRuleDoc.add(doc);
		});

		// 复制参照定义
		List<Document> refDefDoc = new ArrayList<Document>();
		c("refDef", domain).find(new BasicDBObject("formDef_id", _id)).forEach((Document doc) -> {
			doc.remove("_id");
			doc.put("formDef_id", formDef_id);
			refDefDoc.add(doc);
		});

		if (exportDocRuleDoc.size() > 0)
			c("exportDocRule", domain).insertMany(exportDocRuleDoc);

		if (refDefDoc.size() > 0)
			c("refDef", domain).insertMany(refDefDoc);

		c("formDef", domain).insertOne(formDefDoc);

		return getFormDef(formDef_id, domain);
	}

	@Override
	public List<ExportDocRule> listExportDocRule(BasicDBObject condition, String domain) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");

		return query(c -> {
			c.add(Aggregates.lookup("formDef", "formDef_id", "_id", "formDef"));
			c.add(Aggregates.unwind("$formDef"));
		}, skip, limit, filter, sort, null, ExportDocRule.class, domain);
	}

	@Override
	public long countExportDocRule(BasicDBObject filter, String domain) {
		return count(filter, ExportDocRule.class, domain);
	}

	@Override
	public ExportDocRule insertExportDocRule(ExportDocRule exportDocRule, String domain) {
		return insert(exportDocRule, ExportDocRule.class, domain);
	}

	@Override
	public long updateExportDocRule(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, ExportDocRule.class, domain);
	}

	@Override
	public long deleteExportDocRule(ObjectId _id, String domain) {
		// TODO 检查表单定义是否启用,启用的表单定义不允许被删除

		// TODO 删除时检查是否被使用

		return delete(_id, ExportDocRule.class, domain);
	}

	@Override
	public ExportDocRule getExportDocRule(ObjectId _id, String domain) {
		return get(_id, ExportDocRule.class, domain);
	}

	@Override
	public List<FormDef> listFormDefSelector(VaultFolder folder, String domain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long countFormDefSelector(VaultFolder folder, String domain) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<RefDef> listRefDef(BasicDBObject condition, ObjectId parent_id, String domain) {
		condition.put("filter",
				Optional.ofNullable((BasicDBObject) condition.get("filter")).orElse(new BasicDBObject()).append("formDef_id", parent_id));
		return createDataSet(condition, RefDef.class, domain);
	}

	@Override
	public long countRefDef(BasicDBObject filter, ObjectId parent_id, String domain) {
		return count(Optional.ofNullable(filter).orElse(new BasicDBObject()).append("formDef_id", parent_id), RefDef.class, domain);
	}

	@Override
	public long deleteRefDef(ObjectId _id, String domain) {
		return delete(_id, RefDef.class, domain);
	}

	@Override
	public long updateRefDef(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, RefDef.class, domain);
	}

	@Override
	public RefDef insertRefDef(RefDef refDef, ObjectId parent_id, String domain) {
		refDef.setFormDef_id(parent_id);
		return insert(refDef, RefDef.class, domain);
	}

}
