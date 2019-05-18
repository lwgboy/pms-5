package com.bizvisionsoft.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSPeriod;
import com.bizvisionsoft.service.model.CBSSubject;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Role;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.result.UpdateResult;

public class CBSServiceImpl extends BasicServiceImpl implements CBSService {

	@Override
	// yangjun 2018/10/31
	public CBSItem get(ObjectId _id, String domain) {
		List<CBSItem> ds = query(new Query().filter(new BasicDBObject("_id", _id)).bson(), domain);
		if (ds.size() == 0) {
			throw new ServiceException("没有_id为" + _id + "的CBS。");
		}
		return ds.get(0);

	}

	// yangjun 2018/10/31
	public List<CBSItem> query(BasicDBObject condition, String domain) {
		/*
		 * db.getCollection('cbs').aggregate([
		 * {$lookup:{"from":"cbsPeriod","localField":"_id","foreignField":"cbsItem_id",
		 * "as":"_period"}},
		 * {$addFields:{_budget:{$map:{"input":"$_period","as":"itm","in":{"k":
		 * "$$itm.id","v":"$$itm.budget"}}}}},
		 * {$addFields:{"budgetTotal":{$sum:"$_budget.v"}}}, {$addFields:{"budget":{
		 * $arrayToObject: "$_budget" }}},
		 * {$lookup:{"from":"cbs","localField":"_id","foreignField":"parent_id","as":
		 * "_children"}}, {$addFields:{"children":{$map:{input: "$_children._id",as:
		 * "id",in: "$$id" }}}},
		 * {$project:{"_period":false,"_budget":false,"_children":false}}, ])
		 */
		return list(CBSItem.class, domain, condition, Domain.getJQ(domain, "追加-CBS-预算和成本").array());
	}

	// /**
	// * db.getCollection('accountItem').aggregate([ {$match:{parent_id:null}},
	// * {$project:{_id:false,name:true,id:true,parent_id:"aaa",scope_id:"bbb"}} ])
	// *
	// * @param items
	// * @param scope_id
	// * @param parent_id
	// * @param acountItemParent_id
	// */
	// public void appendSubItemsFromTemplate(List<CBSItem> items, ObjectId
	// scope_id, ObjectId parent_id,
	// ObjectId acountItemParent_id) {
	// Iterable<Document> iter = c("accountItem",domain).find(new
	// BasicDBObject("parent_id", acountItemParent_id));
	// iter.forEach(d -> {
	// ObjectId _id = new ObjectId();
	// CBSItem item = new CBSItem().set_id(_id)//
	// .setName(d.getString("name"))//
	// .setId(d.getString("id"))//
	// .setScope_id(scope_id)//
	// .setParent_id(acountItemParent_id);//
	// items.add(item);
	// appendSubItemsFromTemplate(items, scope_id, _id, d.getObjectId("_id"));
	// });
	// }

	@Override
	public List<CBSItem> getScopeRoot(ObjectId scope_id, String domain) {
		// yangjun 2018/10/31
		return query(new Query().filter(new BasicDBObject("scope_id", scope_id).append("scopeRoot", true)).bson(), domain);
	}

	@Override
	public CBSItem insertCBSItem(CBSItem o, String domain) {
		try {
			CBSItem item = insert(o, CBSItem.class, domain);
			// yangjun 2018/10/31
			return get(item.get_id(), domain);
		} catch (Exception e) {
			if (e instanceof MongoBulkWriteException) {
				throw new ServiceException(e.getMessage());
			}
		}
		return null;
	}

	@Override
	public List<CBSItem> getSubCBSItems(ObjectId parent_id, String domain) {
		// yangjun 2018/10/31
		return query(new Query().filter(new BasicDBObject("parent_id", parent_id)).bson(), domain);
	}

	@Override
	public long countSubCBSItems(ObjectId parent_id, String domain) {
		return count(new BasicDBObject("parent_id", parent_id), CBSItem.class, domain);
	}

	@Override
	public void delete(ObjectId _id, String domain) {
		ArrayList<ObjectId> deletecbsIds = new ArrayList<ObjectId>();
		deletecbsIds.add(_id);
		listDescendants(_id, cId -> deletecbsIds.add(cId), domain);

		Document query = new Document("cbsItem_id", new Document("$in", deletecbsIds));

		c(CBSPeriod.class, domain).deleteMany(query);
		c(CBSSubject.class, domain).deleteMany(query);

		c(CBSItem.class, domain).deleteMany(new Document("_id", new Document("$in", deletecbsIds)));
	}

	private void listDescendants(ObjectId cbsId, Consumer<ObjectId> consumer, String domain) {
		Iterable<Document> iter = c("cbs", domain).find(new Document("parent_id", cbsId)).projection(new Document("_id", true));
		iter.forEach(e -> {
			ObjectId id = e.getObjectId("_id");
			consumer.accept(id);
			listDescendants(id, consumer, domain);
		});
	}

	@Override
	public ObjectId updateCBSPeriodBudget(CBSPeriod o, String domain) {
		Document filter = new Document("cbsItem_id", o.getCbsItem_id()).append("id", o.getId());
		ObjectId _id = Optional.ofNullable(c("cbsPeriod", domain).find(filter).first()).map(d -> d.getObjectId("_id")).orElse(null);
		if (_id == null) {
			_id = new ObjectId();
			c("cbsPeriod", domain).insertOne(filter.append("_id", _id).append("budget", Optional.ofNullable(o.getBudget()).orElse(0d)));
		} else {
			c("cbsPeriod", domain).updateOne(filter,
					new Document("$set", new Document("budget", Optional.ofNullable(o.getBudget()).orElse(0d))));
		}
		return _id;
	}

	@Override
	public CBSSubject upsertCBSSubjectBudget(CBSSubject o, String domain) {
		Document filter = new Document("cbsItem_id", o.getCbsItem_id()).append("id", o.getId()).append("subjectNumber",
				o.getSubjectNumber());
		ObjectId _id = Optional.ofNullable(c("cbsSubject", domain).find(filter).first()).map(d -> d.getObjectId("_id")).orElse(null);
		if (_id == null) {
			_id = new ObjectId();
			c("cbsSubject", domain).insertOne(filter.append("_id", _id).append("budget", Optional.ofNullable(o.getBudget()).orElse(0d)));
		} else {
			c("cbsSubject", domain).updateOne(filter,
					new Document("$set", new Document("budget", Optional.ofNullable(o.getBudget()).orElse(0d))));
		}

		return get(_id, CBSSubject.class, domain);
	}

	@Override
	public CBSSubject upsertCBSSubjectCost(CBSSubject o, String domain) {
		Document filter = new Document("cbsItem_id", o.getCbsItem_id()).append("id", o.getId()).append("subjectNumber",
				o.getSubjectNumber());
		ObjectId _id = Optional.ofNullable(c("cbsSubject", domain).find(filter).first()).map(d -> d.getObjectId("_id")).orElse(null);
		if (_id == null) {
			_id = new ObjectId();
			c("cbsSubject", domain).insertOne(filter.append("_id", _id).append("cost", Optional.ofNullable(o.getCost()).orElse(0d)));
		} else {
			c("cbsSubject", domain).updateOne(filter,
					new Document("$set", new Document("cost", Optional.ofNullable(o.getCost()).orElse(0d))));
		}

		return get(_id, CBSSubject.class, domain);
	}

	@Override
	// yangjun 2018/10/31
	public List<CBSItem> createDataSet(BasicDBObject condition, String domain) {
		return query(condition, domain);
	}

	@Override
	public List<CBSSubject> getCBSSubject(ObjectId cbs_id, String domain) {
		return c(CBSSubject.class, domain).find(new BasicDBObject("cbsItem_id", cbs_id)).into(new ArrayList<CBSSubject>());
	}

	@Override
	public List<CBSPeriod> getCBSPeriod(ObjectId cbs_id, String domain) {
		return c(CBSPeriod.class, domain).find(new BasicDBObject("cbsItem_id", cbs_id)).into(new ArrayList<CBSPeriod>());
	}

	@Override
	public List<CBSSubject> getAllSubCBSSubjectByNumber(ObjectId cbs_id, String number, String domain) {
		List<ObjectId> items = getDesentItems(Arrays.asList(cbs_id), "cbs", "parent_id", domain);
		return c(CBSSubject.class, domain)
				.find(new BasicDBObject("cbsItem_id", new BasicDBObject("$in", items)).append("subjectNumber", number))
				.into(new ArrayList<CBSSubject>());
	}

	@Override
	@Deprecated
	// yangjun 2018/10/31
	public CBSItem getCBSItemCost(ObjectId _id, String domain) {
		return get(_id, domain);
		//
		//
		// List<ObjectId> items = getDesentItems(Arrays.asList(_id), "cbs",
		// "parent_id");
		//
		// List<? extends Bson> pipeline = Arrays.asList(
		// new Document("$lookup",
		// new Document("from", "cbsSubject").append("let", new Document())
		// .append("pipeline",
		// Arrays.asList(
		// new Document("$match",
		// new Document("$expr",
		// new Document("$in",
		// Arrays.asList("$cbsItem_id", items)))),
		// new Document("$group",
		// new Document("_id", null)
		// .append("cost", new Document("$sum", "$cost"))
		// .append("budget", new Document("$sum", "$budget")))))
		// .append("as", "cbsSubject")),
		// new Document("$unwind", new Document("path",
		// "$cbsSubject").append("preserveNullAndEmptyArrays", true)),
		// new Document("$addFields", new Document("cbsSubjectBudget",
		// "$cbsSubject.budget")
		// .append("cbsSubjectCost", "$cbsSubject.cost")),
		// new Document("$project", new Document("cbsSubject", false)));
		//
		// return c(CBSItem.class,domain).aggregate(pipeline).first();
	}

	@Override
	// yangjun 2018/10/31
	public CBSItem allocateBudget(ObjectId _id, ObjectId scope_id, String scopename, String domain) {
		try {
			update(new FilterAndUpdate().filter(new BasicDBObject("_id", scope_id)).set(new BasicDBObject("cbs_id", _id)).bson(),
					Work.class, domain);

			List<ObjectId> desentItems = getDesentItems(Arrays.asList(_id), "cbs", "parent_id", domain);
			update(new FilterAndUpdate().filter(new BasicDBObject("_id", new BasicDBObject("$in", desentItems)))
					.set(new BasicDBObject("scope_id", scope_id).append("scopename", scopename)).bson(), CBSItem.class, domain);

			update(new FilterAndUpdate().filter(new BasicDBObject("_id", _id)).set(new BasicDBObject("scopeRoot", true)).bson(),
					CBSItem.class, domain);
		} catch (Exception e) {
			handleMongoException(e, "分配CBS到" + scopename + " 错误");
		}
		return get(_id, domain);
	}

	@Override
	public CBSItem unallocateBudget(ObjectId _id, ObjectId parent_id, String domain) {
		CBSItem parent = get(parent_id, domain);
		UpdateResult ur = c(Work.class, domain).updateOne(new BasicDBObject("cbs_id", _id),
				new BasicDBObject("$unset", new BasicDBObject("cbs_id", 1)));
		ur = c(CBSItem.class, domain).updateOne(new BasicDBObject("_id", _id), new BasicDBObject("$set",
				new BasicDBObject("scope_id", parent.getScope_id()).append("scopename", parent.getScopeName()).append("scopeRoot", false)));

		if (ur.getModifiedCount() == 0) {

		}
		List<ObjectId> list = new ArrayList<ObjectId>();
		list.add(_id);
		List<ObjectId> desentItems = getDesentItems(list, "cbs", "parent_id", domain);
		ur = c(CBSItem.class, domain).updateMany(new BasicDBObject("_id", new BasicDBObject("$in", desentItems)),
				new BasicDBObject("$set", new BasicDBObject("scope_id", parent.getScope_id()).append("scopename", parent.getScopeName())));
		// TODO 错误返回

		if (ur.getModifiedCount() == 0) {

		}
		return get(_id, domain);
	}

	@Override
	public Result calculationBudget(ObjectId _id, String userId, String domain) {
		// TODO 根据新讨论的预算编写方式，该方法没用
		Double totalBudget = 0.0;
		List<CBSSubject> subjectBudget = getCBSSubject(_id, domain);
		Map<String, Double> cbsPeriodMap = new HashMap<String, Double>();
		for (CBSSubject cbsSubject : subjectBudget) {
			String id = cbsSubject.getId();
			Double budget = cbsPeriodMap.get(id);
			cbsPeriodMap.put(id, (cbsSubject.getBudget() != null ? cbsSubject.getBudget() : 0d) + (budget != null ? budget : 0d));
			totalBudget += (cbsSubject.getBudget() != null ? cbsSubject.getBudget() : 0d);
		}

		List<Document> pipeline = Arrays.asList(new Document("$match", new Document("cbsItem_id", _id)),
				new Document("$group", new Document("_id", "$cbsItem_id").append("totalBudget", new Document("$sum", "$budget"))));
		Document doc = c("cbsPeriod", domain).aggregate(pipeline).first();

		if (doc != null && totalBudget.doubleValue() != doc.getDouble("totalBudget").doubleValue()) {
			return Result.cbsError("分配科目预算与预算总额不一致", Result.CODE_CBS_DEFF_BUDGET);
		}

		c("cbsPeriod", domain).deleteMany(new BasicDBObject("cbsItem_id", _id));

		// List<CBSPeriod> cbsPeriods = new ArrayList<CBSPeriod>();
		// for (String id : cbsPeriodMap.keySet()) {
		// CBSPeriod cbsPeriod = new CBSPeriod();
		// cbsPeriod.setCBSItem_id(_id);
		// cbsPeriod.setId(id);
		// cbsPeriod.setBudget(cbsPeriodMap.get(id));
		// cbsPeriods.add(cbsPeriod);
		// }
		// c(CBSPeriod.class).insertMany(cbsPeriods);

		ObjectId scope_id = c(CBSItem.class, domain).distinct("scope_id", new Document("_id", _id), ObjectId.class).first();
		Project project = c(Project.class, domain).find(new Document("_id", scope_id)).first();
		if (project == null) {
			ObjectId project_id = c(Work.class, domain).distinct("project_id", new Document("_id", scope_id), ObjectId.class).first();
			project = c(Project.class, domain).find(new Document("_id", project_id)).first();
		}

		sendMessage("项目预算编制完成", "您负责的项目：" + project.getName() + " 已完成项目预算。", userId, project.getPmId(), null, domain);

		return Result.cbsSuccess("提交预算成功");
	}

	@Override
	public List<CBSItem> addCBSItemByStage(ObjectId _id, ObjectId project_id, String domain) {
		List<CBSItem> cbsItemList = new ArrayList<CBSItem>();
		List<ObjectId> cbsItemIdList = new ArrayList<ObjectId>();
		CBSItem parentCBSItem = get(_id, domain);
		List<Work> workList = c(Work.class, domain).find(new BasicDBObject("project_id", project_id).append("stage", Boolean.TRUE))
				.into(new ArrayList<Work>());
		if (workList.size() > 0) {
			for (Work work : workList) {
				CBSItem cbsItem = CBSItem.getInstance(parentCBSItem, domain);
				cbsItem.setName(work.toString());
				cbsItem.setParent_id(_id);
				cbsItemList.add(cbsItem);
				cbsItemIdList.add(cbsItem.get_id());
			}
			try {
				c(CBSItem.class, domain).insertMany(cbsItemList);
			} catch (Exception e) {
				handleMongoException(e, "");
			}
		}
		// yangjun 2018/10/31
		return query(new Query().filter(new BasicDBObject("_id", new BasicDBObject("$in", cbsItemIdList))).bson(), domain);
	}

	@Override
	public List<CBSItem> listProjectCost(BasicDBObject condition, String userId, String domain) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");

		List<Bson> pipeline = new ArrayList<Bson>();
		// 当前用户具有财务管理权限时显示全部，否则只显示其在pmo团队中的成本信息
		if (!checkUserRoles(userId, Arrays.asList(Role.SYS_ROLE_FM_ID, Role.SYS_ROLE_PD_ID), domain)) {
			pipeline.add(Aggregates.match(new Document("_id", new Document("$in", getUserInPMOCBSId(userId, domain)))));
		}

		// yangjun 2018/10/31
		pipeline.addAll(Domain.getJQ(domain, "查询-CBS-项目进行中和收尾中").array());

		pipeline.add(Aggregates.lookup("cbs", "_id", "parent_id", "_children"));

		pipeline.add(Aggregates.addFields(new Field<BasicDBObject>("children",
				new BasicDBObject("$map", new BasicDBObject().append("input", "$_children._id").append("as", "id").append("in", "$$id")))));
		// yangjun 2018/10/31
		pipeline.add(Aggregates.project(new BasicDBObject("_children", false)));

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		return c(CBSItem.class, domain).aggregate(pipeline).into(new ArrayList<CBSItem>());
	}

	/**
	 * 获取当前用户作为项目团队PMO成员的cbs_id
	 * 
	 * @param userid
	 * @return
	 */
	private List<ObjectId> getUserInPMOCBSId(String userid, String domain) {
		List<ObjectId> result = new ArrayList<ObjectId>();
		// 获取用户在項目PMO团队中的项目id
		List<Bson> pipeline = new ArrayList<Bson>();
		appendQueryUserInProjectPMO(pipeline, userid, "$_id", domain);
		pipeline.add(Aggregates.project(new BasicDBObject("cbs_id", true)));
		c("project", domain).aggregate(pipeline).forEach((Document doc) -> {
			result.add(doc.getObjectId("cbs_id"));
		});
		;
		return result;
	}

	@Override
	public List<CBSItem> listProjectCostAnalysis(BasicDBObject condition, String userId, String domain) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");

		List<Bson> pipeline = new ArrayList<Bson>();
		// 当前用户具有财务管理权限时显示全部，否则只显示其在pmo团队中的成本信息
		if (!checkUserRoles(userId, Arrays.asList(Role.SYS_ROLE_FM_ID, Role.SYS_ROLE_PD_ID), domain)) {
			pipeline.add(Aggregates.match(new Document("_id", new Document("$in", getUserInPMOCBSId(userId, domain)))));
		}

		// yangjun 2018/10/31
		pipeline.addAll(Domain.getJQ(domain, "查询-CBS-项目进行中和收尾中").array());

		pipeline.add(Aggregates.lookup("cbs", "_id", "parent_id", "_children"));

		pipeline.add(
				Aggregates
						.addFields(
								new Field<BasicDBObject>("children",
										new BasicDBObject("$map",
												new BasicDBObject().append("input", "$_children._id").append("as", "id").append("in",
														"$$id"))),
								new Field<String>("scopePlanStart", "$project.planStart"),
								new Field<String>("scopePlanFinish", "$project.planFinish"),
								new Field<String>("scopeStatus", "$project.status")));

		pipeline.add(Aggregates.project(new BasicDBObject("_children", false)));

		appendUserInfo(pipeline, "pmId", "scopeChargerInfo", domain);

		appendOrgFullName(pipeline, "impUnit_id", "impUnitOrgFullName");

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		ArrayList<CBSItem> into = c(CBSItem.class, domain).aggregate(pipeline).into(new ArrayList<CBSItem>());
		return into;
	}

	@Override
	public long countProjectCost(BasicDBObject filter, String userId, String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();
		// 当前用户具有财务管理权限时，只显示其在pmo团队中的成本信息
		if (!checkUserRoles(userId, Arrays.asList(Role.SYS_ROLE_FM_ID, Role.SYS_ROLE_PD_ID), domain)) {
			pipeline.add(Aggregates.match(new Document("_id", new Document("$in", getUserInPMOCBSId(userId, domain)))));
		}

		// yangjun 2018/10/31
		pipeline.addAll(Domain.getJQ(domain, "查询-CBS-项目进行中和收尾中").array());

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		return c(CBSItem.class, domain).aggregate(pipeline).into(new ArrayList<CBSItem>()).size();
	}

	@Override
	public Date getNextSettlementDate(ObjectId scope_id, String domain) {
		Document workDoc = c("work", domain).find(new Document("_id", scope_id)).projection(new Document("project_id", 1)).first();
		if (workDoc != null) {
			scope_id = workDoc.getObjectId("project_id");
		}
		Document doc = c("project", domain).find(new Document("_id", scope_id))
				.projection(new Document("settlementDate", 1).append("actualStart", 1)).first();
		java.util.Calendar cal = java.util.Calendar.getInstance();
		if (doc != null) {
			Date settlementDate = doc.getDate("settlementDate");
			Date actualStart = doc.getDate("actualStart");
			if (settlementDate != null) {
				cal.setTime(settlementDate);
				cal.add(java.util.Calendar.MONTH, 1);
			} else if (actualStart != null) {
				cal.setTime(actualStart);
			}
		}
		return cal.getTime();
	}

	@Override
	public List<Result> submitCBSSubjectCost(Date id, ObjectId scope_id, String domain) {
		List<Result> result = submitCBSSubjectCostCheck(scope_id, id, domain);
		if (!result.isEmpty()) {
			return result;
		}
		// 修改项目结算时间
		c(Project.class, domain).updateOne(new BasicDBObject("_id", scope_id),
				new BasicDBObject("$set", new BasicDBObject("settlementDate", id)));

		return result;
	}

	private List<Result> submitCBSSubjectCostCheck(ObjectId scope_id, Date id, String domain) {
		List<Result> result = new ArrayList<Result>();
		Date date = getNextSettlementDate(scope_id, domain);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		if (format.format(date).equals(format.format(id))) {
			result.add(Result.submitCBSSubjectError("重复提交期间费用"));
		}
		return result;
	}

	@Override
	// yangjun 2018/10/31
	public Document getCostCompositionAnalysis(ObjectId cbsScope_id, String year, String userId, String domain) {
		List<String> data1 = new ArrayList<String>();
		List<Document> data2 = new ArrayList<Document>();
		c("accountItem", domain).aggregate(
				Domain.getJQ(domain, "查询-费用科目-成本-年").set("year", year).set("cbsItem_id", getCBSItemId(cbsScope_id, userId, domain)).array())
				.forEach((Document doc) -> {
					Object count = doc.get("count");
					if (count != null && ((Number) count).doubleValue() > 0) {
						data2.add(new Document("name", doc.getString("name")).append("value", getStringValue(doc.get("cost"))));
						data1.add(doc.getString("name"));
					}
				});
		return Domain.getJQ(domain, "图表-项目成本组成分析（年）").set("title", year + "年 项目成本组成分析（万元）").set("data1", data1).set("data2", data2).doc();
	}

	@Override
	// yangjun 2018/10/31
	public Document getPeriodCostCompositionAnalysis(ObjectId cbsScope_id, String startPeriod, String endPeriod, String userId,
			String domain) {
		String title;
		if (startPeriod.equals(endPeriod)) {
			title = startPeriod.substring(0, 4) + "年" + Integer.parseInt(startPeriod.substring(4, 6)) + "月 项目成本组成分析（万元）";
		} else {
			title = startPeriod.substring(0, 4) + "年" + Integer.parseInt(startPeriod.substring(4, 6)) + "月-" + endPeriod.substring(0, 4)
					+ "年" + Integer.parseInt(endPeriod.substring(4, 6)) + "月 成本组成";
		}

		List<String> data1 = new ArrayList<String>();
		List<Document> data2 = new ArrayList<Document>();
		List<Bson> array = Domain.getJQ(domain, "查询-费用科目-成本-期间").set("startPeriod", startPeriod).set("endPeriod", endPeriod)
				.set("cbsItem_id", getCBSItemId(cbsScope_id, userId, domain)).array();
		c("accountItem", domain).aggregate(array).forEach((Document doc) -> {
			Object count = doc.get("count");
			if (count != null && ((Number) count).doubleValue() > 0) {
				data2.add(new Document("name", doc.getString("name")).append("value", getStringValue(doc.get("cost"))));
				data1.add(doc.getString("name"));
			}
		});

		return Domain.getJQ(domain, "图表-项目成本组成分析（期间）").set("title", title).set("data1", data1).set("data2", data2).doc();
	}

	@Override
	// yangjun 2018/10/31
	public Document getMonthlyCostAndBudgetChart(ObjectId cbsScope_id, String domain) {
		String year = new SimpleDateFormat("yyyy").format(new Date());

		Map<String, Double> costMap = new LinkedHashMap<String, Double>();
		Map<String, Double> budgetMap = new LinkedHashMap<String, Double>();
		for (int i = 1; i < 12; i++) {
			String key = year + String.format("%02d", i);
			costMap.put(key, 0d);
			budgetMap.put(key, 0d);
		}

		c("accountItem", domain).aggregate(Domain.getJQ(domain, "查询-费用科目-预算和成本-年").set("year", year)
				.set("cbsItem_id", getCBSItemId(cbsScope_id, null, domain)).array()).forEach((Document doc) -> {
					Object subjects = doc.get("cbsSubject");
					if (subjects instanceof List && ((List<?>) subjects).size() > 0) {
						((List<?>) subjects).forEach(item -> {
							String id = ((Document) item).getString("_id");
							Double costD = costMap.get(id);
							if (costD != null) {
								Object cost = ((Document) item).get("cost");
								if (cost != null) {
									costD += ((Number) cost).doubleValue();
									costMap.put(id, (double) Math.round(costD * 10) / 10);
								}
							}

							Double budgetD = budgetMap.get(id);
							if (budgetD != null) {
								Object budget = ((Document) item).get("budget");
								if (budget != null) {
									budgetD += ((Number) budget).doubleValue();
									budgetMap.put(id, (double) Math.round(budgetD * 10) / 10);
								}
							}
						});
					}
				});

		return Domain.getJQ(domain, "图表-预算和实际-项目").set("title", year + "年 资金预算和使用状况（万元）")
				.set("budget", Arrays.asList(budgetMap.values().toArray(new Double[0])))
				.set("cost", Arrays.asList(costMap.values().toArray(new Double[0]))).doc();
	}

	@SuppressWarnings("unchecked")
	@Override
	// yangjun 2018/10/31
	public Document getMonthCostCompositionAnalysis(ObjectId cbsScope_id, String year, String userId, String domain) {

		Document cbsSubjectMatch = new Document();
		cbsSubjectMatch.append("$in", Arrays.asList("$cbsItem_id", getCBSItemId(cbsScope_id, userId, domain)));

		Map<String, Double> costMap = new TreeMap<String, Double>();
		Map<String, Double> budgetMap = new TreeMap<String, Double>();
		for (int i = 1; i < 12; i++) {
			String key = year + String.format("%02d", i);
			costMap.put(key, 0d);
			budgetMap.put(key, 0d);
		}

		c("accountItem", domain).aggregate(Domain.getJQ(domain, "查询-费用科目-预算和成本-年").set("year", year)
				.set("cbsItem_id", getCBSItemId(cbsScope_id, userId, domain)).array()).forEach((Document doc) -> {
					Object cbsSubjects = doc.get("cbsSubject");
					if (cbsSubjects != null && cbsSubjects instanceof List && ((List<Document>) cbsSubjects).size() > 0) {
						((List<Document>) cbsSubjects).forEach(cbsSubject -> {
							String id = cbsSubject.getString("_id");
							Double costD = costMap.get(id);
							if (costD != null) {
								Object cost = cbsSubject.get("cost");
								if (cost != null) {
									costD += ((Number) cost).doubleValue();
									costMap.put(id, costD);
								}
							}

							Double budgetD = budgetMap.get(id);
							if (budgetD != null) {
								Object budget = cbsSubject.get("budget");
								if (budget != null) {
									budgetD += ((Number) budget).doubleValue();
									budgetMap.put(id, budgetD);
								}
							}

						});
					}
				});

		List<Object> budgetdata = new ArrayList<Object>();
		for (Double d : budgetMap.values()) {
			budgetdata.add(getStringValue(d));
		}

		List<Object> costData = new ArrayList<Object>();
		for (Double d : costMap.values()) {
			costData.add(getStringValue(d));
		}
		return Domain.getJQ(domain, "图表-项目成本组成分析（月）").set("title", year + "年 各月项目预算和成本分析（万元）").set("budget", budgetdata)
				.set("cost", costData).doc();
	}

	private String getStringValue(Object value) {
		if (value instanceof Number) {
			double d = ((Number) value).doubleValue();
			if (d != 0d) {
				return Formatter.getString(d);
			}
		}
		return null;
	}

	/**
	 * cbsScope_id不为空时，为取特定项目的CBS叶子节点Id。 userId不为空时是根据用户权限获取CBS叶子节点id，
	 * cbsScope_id和userId都为空时，表示传入的数据有误，不获取任何CBS节点Id。
	 * 
	 * @param cbsScope_id
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<ObjectId> getCBSItemId(ObjectId cbsScope_id, String userId, String domain) {
		Set<ObjectId> result = new HashSet<ObjectId>();
		// cbsScope_id不为空时，为取特定项目的CBS节点Id。userId不为空时是根据用户权限获取CBS节点id，cbsScope_id和userId都为空时，表示传入的数据有误，不获取任何CBS节点Id。
		// TODO 根据修改后的查询-项目PMO成员.js else 可以继续优化进行合并，
		if (cbsScope_id == null && userId == null) {
			return new ArrayList<ObjectId>();
		} else if (cbsScope_id != null || checkUserRoles(userId, Arrays.asList(Role.SYS_ROLE_FM_ID, Role.SYS_ROLE_PD_ID), domain)) {
			List<Bson> pipeline = new ArrayList<>();

			if (cbsScope_id != null)
				pipeline.add(Aggregates.match(new BasicDBObject("scope_id", cbsScope_id)));

			pipeline.addAll(Domain.getJQ(domain, "追加-CBS-CBS叶子节点ID").array());
			pipeline.add(Aggregates.project(new BasicDBObject("cbsChild_id", true)));

			c("cbs", domain).aggregate(pipeline).forEach((Document doc) -> {
				Object cbsChild_ids = doc.get("cbsChild_id");
				if (cbsChild_ids instanceof List<?>) {
					result.addAll((List<ObjectId>) cbsChild_ids);
				}
			});
		} else {
			List<Bson> pipeline = new ArrayList<>();
			appendQueryUserInProjectPMO(pipeline, userId, "$_id", domain);
			pipeline.addAll(Domain.getJQ(domain, "追加-CBSScope-CBS叶子节点ID").array());
			pipeline.add(Aggregates.project(new BasicDBObject("cbsChild_id", true)));
			c("project", domain).aggregate(pipeline).forEach((Document doc) -> {
				Object cbsChild_ids = doc.get("cbsChild_id");
				if (cbsChild_ids instanceof List<?>) {
					result.addAll((List<ObjectId>) cbsChild_ids);
				}
			});
		}

		return new ArrayList<ObjectId>(result);
	}

	@Override
	public Document getCostCompositionAnalysis(String year, String userId, String domain) {
		return getCostCompositionAnalysis(null, year, userId);
	}

	@Override
	public Document getPeriodCostCompositionAnalysis(String startPeriod, String endPeriod, String userId, String domain) {
		return getPeriodCostCompositionAnalysis(null, startPeriod, endPeriod, userId);
	}

	@Override
	public Document getMonthCostCompositionAnalysis(String year, String userId, String domain) {
		return getMonthCostCompositionAnalysis(null, year, userId);
	}

	@Override
	// yangjun 2018/10/31
	public Document getCBSSummary(ObjectId cbsScope_id, String startPeriod, String endPeriod, String userId, String domain) {
		return c("cbs", domain).aggregate(Domain.getJQ(domain, "查询-预算成本对比分析").set("cbsItem_id", getCBSItemId(cbsScope_id, userId, domain))
				.set("startPeriod", startPeriod).set("endPeriod", endPeriod).array()).first();
	}

	@Override
	public Document getCBSSummary(String startPeriod, String endPeriod, String userId, String domain) {
		return getCBSSummary(null, startPeriod, endPeriod, userId);
	}

	@Override
	public Project getICBSScopeRootProject(ObjectId scope_id, String domain) {
		Work work = getICBSScopeRootWork(scope_id, domain);
		if (work != null)
			return work.getProject();
		return new ProjectServiceImpl().get(scope_id, domain);
	}

	@Override
	public Work getICBSScopeRootWork(ObjectId scope_id, String domain) {
		return new WorkServiceImpl().getWork(scope_id, domain);
	}

	//////////////////////////////////////////////////////////////////////
	//// BUG: 在预算编辑页面上的编辑CBSItem保存没有起作用
	//////////////////////////////////////////////////////////////////////
	@Override
	public long update(BasicDBObject filterAndUpdate, String domain) {
		try {
			return update(filterAndUpdate, CBSItem.class, domain);
		} catch (Exception e) {
			if (e instanceof MongoBulkWriteException) {
				throw new ServiceException(e.getMessage());
			}
		}
		return 0;
	}
}
