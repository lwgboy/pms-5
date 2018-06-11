package com.bizvisionsoft.serviceimpl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSPeriod;
import com.bizvisionsoft.service.model.CBSSubject;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.result.UpdateResult;

public class CBSServiceImpl extends BasicServiceImpl implements CBSService {

	@Override
	public CBSItem get(ObjectId _id) {
		return query(new BasicDBObject("_id", _id)).first();
	}

	public AggregateIterable<CBSItem> query(BasicDBObject match) {
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

		List<Bson> pipeline = new ArrayList<Bson>();
		if (match != null)
			pipeline.add(Aggregates.match(match));

		pipeline.add(Aggregates.lookup("cbsPeriod", "_id", "cbsItem_id", "_period"));

		pipeline.add(Aggregates.addFields(//
				new Field<BasicDBObject>("_budget",
						new BasicDBObject("$map", new BasicDBObject("input", "$_period").append("as", "itm")
								.append("in", new BasicDBObject("k", "$$itm.id").append("v", "$$itm.budget"))))));

		pipeline.add(Aggregates.addFields(//
				new Field<BasicDBObject>("budgetTotal", new BasicDBObject("$sum", "$_budget.v"))));

		pipeline.add(Aggregates.addFields(//
				new Field<BasicDBObject>("budget", new BasicDBObject("$arrayToObject", "$_budget"))));

		pipeline.add(Aggregates.lookup("cbs", "_id", "parent_id", "_children"));

		pipeline.add(Aggregates.addFields(//
				new Field<BasicDBObject>("children", new BasicDBObject("$map", new BasicDBObject()
						.append("input", "$_children._id").append("as", "id").append("in", "$$id")))));

		pipeline.add(Aggregates
				.project(new BasicDBObject("_period", false).append("_budget", false).append("_children", false)));

		AggregateIterable<CBSItem> a = c(CBSItem.class).aggregate(pipeline);
		return a;
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
	// Iterable<Document> iter = c("accountItem").find(new
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
	public List<CBSItem> getScopeRoot(ObjectId scope_id) {
		return query(new BasicDBObject("scope_id", scope_id).append("scopeRoot", true)).into(new ArrayList<CBSItem>());
	}

	@Override
	public CBSItem insertCBSItem(CBSItem o) {
		CBSItem item = insert(o, CBSItem.class);
		return query(new BasicDBObject("_id", item.get_id())).first();
	}

	@Override
	public List<CBSItem> getSubCBSItems(ObjectId parent_id) {
		return query(new BasicDBObject("parent_id", parent_id)).into(new ArrayList<CBSItem>());
	}

	@Override
	public long countSubCBSItems(ObjectId parent_id) {
		return count(new BasicDBObject("parent_id", parent_id), CBSItem.class);
	}

	@Override
	public void delete(ObjectId _id) {
		ArrayList<ObjectId> deletecbsIds = new ArrayList<ObjectId>();
		deletecbsIds.add(_id);
		listDescendants(_id, cId -> deletecbsIds.add(cId));

		ArrayList<ObjectId> deletePeriodIds = c(CBSPeriod.class)
				.distinct("_id", new Document("cbsItem_id", new Document("$in", deletecbsIds)), ObjectId.class)
				.into(new ArrayList<ObjectId>());

		if (!deletePeriodIds.isEmpty())
			c(CBSPeriod.class).deleteMany(new Document("_id", new Document("$in", deletePeriodIds)));

		c(CBSItem.class).deleteMany(new Document("_id", new Document("$in", deletecbsIds)));
	}

	private void listDescendants(ObjectId cbsId, Consumer<ObjectId> consumer) {
		Iterable<Document> iter = c("cbs").find(new Document("parent_id", cbsId)).projection(new Document("_id", true));
		iter.forEach(e -> {
			ObjectId id = e.getObjectId("_id");
			consumer.accept(id);
			listDescendants(id, consumer);
		});
	}

	@Override
	public ObjectId updateCBSPeriodBudget(CBSPeriod o) {
		Document filter = new Document("cbsItem_id", o.getCbsItem_id()).append("id", o.getId());
		ObjectId _id = Optional.ofNullable(c("cbsPeriod").find(filter).first()).map(d -> d.getObjectId("_id"))
				.orElse(null);
		if (_id == null) {
			_id = new ObjectId();
			c("cbsPeriod").insertOne(
					filter.append("_id", _id).append("budget", Optional.ofNullable(o.getBudget()).orElse(0d)));
		} else {
			c("cbsPeriod").updateOne(filter,
					new Document("$set", new Document("budget", Optional.ofNullable(o.getBudget()).orElse(0d))));
		}
		return _id;
	}

	@Override
	public CBSSubject upsertCBSSubjectBudget(CBSSubject o) {
		Document filter = new Document("cbsItem_id", o.getCbsItem_id()).append("id", o.getId()).append("subjectNumber",
				o.getSubjectNumber());
		ObjectId _id = Optional.ofNullable(c("cbsSubject").find(filter).first()).map(d -> d.getObjectId("_id"))
				.orElse(null);
		if (_id == null) {
			_id = new ObjectId();
			c("cbsSubject").insertOne(
					filter.append("_id", _id).append("budget", Optional.ofNullable(o.getBudget()).orElse(0d)));
		} else {
			c("cbsSubject").updateOne(filter,
					new Document("$set", new Document("budget", Optional.ofNullable(o.getBudget()).orElse(0d))));
		}

		return get(_id, CBSSubject.class);
	}

	@Override
	public CBSSubject upsertCBSSubjectCost(CBSSubject o) {
		Document filter = new Document("cbsItem_id", o.getCbsItem_id()).append("id", o.getId()).append("subjectNumber",
				o.getSubjectNumber());
		ObjectId _id = Optional.ofNullable(c("cbsSubject").find(filter).first()).map(d -> d.getObjectId("_id"))
				.orElse(null);
		if (_id == null) {
			_id = new ObjectId();
			c("cbsSubject")
					.insertOne(filter.append("_id", _id).append("cost", Optional.ofNullable(o.getCost()).orElse(0d)));
		} else {
			c("cbsSubject").updateOne(filter,
					new Document("$set", new Document("cost", Optional.ofNullable(o.getCost()).orElse(0d))));
		}

		return get(_id, CBSSubject.class);
	}

	@Override
	public List<CBSItem> createDataSet(BasicDBObject filter) {
		return query(filter).into(new ArrayList<CBSItem>());
	}

	@Override
	public List<CBSSubject> getCBSSubject(ObjectId cbs_id) {
		return c(CBSSubject.class).find(new BasicDBObject("cbsItem_id", cbs_id)).into(new ArrayList<CBSSubject>());
	}

	@Override
	public List<CBSSubject> getAllSubCBSSubjectByNumber(ObjectId cbs_id, String number) {
		List<ObjectId> items = getDesentItems(Arrays.asList(cbs_id), "cbs", "parent_id");
		return c(CBSSubject.class)
				.find(new BasicDBObject("cbsItem_id", new BasicDBObject("$in", items)).append("subjectNumber", number))
				.into(new ArrayList<CBSSubject>());
	}

	@Override
	public CBSItem getCBSItemCost(ObjectId _id) {
		List<ObjectId> items = getDesentItems(Arrays.asList(_id), "cbs", "parent_id");

		List<? extends Bson> pipeline = Arrays.asList(
				new Document("$lookup",
						new Document("from", "cbsSubject").append("let", new Document())
								.append("pipeline",
										Arrays.asList(
												new Document("$match",
														new Document("$expr",
																new Document("$in",
																		Arrays.asList("$cbsItem_id", items)))),
												new Document("$group",
														new Document("_id", null)
																.append("cost", new Document("$sum", "$cost"))
																.append("budget", new Document("$sum", "$budget")))))
								.append("as", "cbsSubject")),
				new Document("$unwind", new Document("path", "$cbsSubject").append("preserveNullAndEmptyArrays", true)),
				new Document("$addFields", new Document("cbsSubjectBudget", "$cbsSubject.budget")
						.append("cbsSubjectCost", "$cbsSubject.cost")),
				new Document("$project", new Document("cbsSubject", false)));

		return c(CBSItem.class).aggregate(pipeline).first();
	}

	@Override
	public CBSItem allocateBudget(ObjectId _id, ObjectId scope_id, String scopename) {
		UpdateResult ur = c(Work.class).updateOne(new BasicDBObject("_id", scope_id),
				new BasicDBObject("$set", new BasicDBObject("cbs_id", _id)));
		ur = c(CBSItem.class).updateOne(new BasicDBObject("_id", _id), new BasicDBObject("$set",
				new BasicDBObject("scope_id", scope_id).append("scopename", scopename).append("scopeRoot", true)));
		if (ur.getModifiedCount() == 0) {

		}
		List<ObjectId> list = new ArrayList<ObjectId>();
		list.add(_id);
		List<ObjectId> desentItems = getDesentItems(list, "cbs", "parent_id");
		ur = c(CBSItem.class).updateMany(new BasicDBObject("_id", new BasicDBObject("$in", desentItems)),
				new BasicDBObject("$set", new BasicDBObject("scope_id", scope_id).append("scopename", scopename)));
		// TODO 错误返回
		if (ur.getModifiedCount() == 0) {

		}
		return get(_id);
	}

	@Override
	public CBSItem unallocateBudget(ObjectId _id, ObjectId parent_id) {
		CBSItem parent = get(parent_id);
		UpdateResult ur = c(Work.class).updateOne(new BasicDBObject("cbs_id", _id),
				new BasicDBObject("$unset", new BasicDBObject("cbs_id", 1)));
		ur = c(CBSItem.class).updateOne(new BasicDBObject("_id", _id),
				new BasicDBObject("$set", new BasicDBObject("scope_id", parent.getScope_id())
						.append("scopename", parent.getScopeName()).append("scopeRoot", false)));

		if (ur.getModifiedCount() == 0) {

		}
		List<ObjectId> list = new ArrayList<ObjectId>();
		list.add(_id);
		List<ObjectId> desentItems = getDesentItems(list, "cbs", "parent_id");
		ur = c(CBSItem.class).updateMany(new BasicDBObject("_id", new BasicDBObject("$in", desentItems)),
				new BasicDBObject("$set", new BasicDBObject("scope_id", parent.getScope_id()).append("scopename",
						parent.getScopeName())));
		// TODO 错误返回

		if (ur.getModifiedCount() == 0) {

		}
		return get(_id);
	}

	@Override
	public Result calculationBudget(ObjectId _id) {
		Double totalBudget = 0.0;
		List<CBSSubject> subjectBudget = getCBSSubject(_id);
		Map<String, Double> cbsPeriodMap = new HashMap<String, Double>();
		for (CBSSubject cbsSubject : subjectBudget) {
			String id = cbsSubject.getId();
			Double budget = cbsPeriodMap.get(id);
			cbsPeriodMap.put(id,
					(cbsSubject.getBudget() != null ? cbsSubject.getBudget() : 0d) + (budget != null ? budget : 0d));
			totalBudget += (cbsSubject.getBudget() != null ? cbsSubject.getBudget() : 0d);
		}

		List<Document> pipeline = Arrays.asList(new Document("$match", new Document("cbsItem_id", _id)), new Document(
				"$group", new Document("_id", "$cbsItem_id").append("totalBudget", new Document("$sum", "$budget"))));
		Document doc = c("cbsPeriod").aggregate(pipeline).first();

		if (doc != null && totalBudget.doubleValue() != doc.getDouble("totalBudget").doubleValue()) {
			return Result.cbsError("分配科目预算与预算总额不一致", Result.CODE_CBS_DEFF_BUDGET);
		}

		c("cbsPeriod").deleteMany(new BasicDBObject("cbsItem_id", _id));

		List<CBSPeriod> cbsPeriods = new ArrayList<CBSPeriod>();
		for (String id : cbsPeriodMap.keySet()) {
			CBSPeriod cbsPeriod = new CBSPeriod();
			cbsPeriod.setCBSItem_id(_id);
			cbsPeriod.setId(id);
			cbsPeriod.setBudget(cbsPeriodMap.get(id));
			cbsPeriods.add(cbsPeriod);
		}
		c(CBSPeriod.class).insertMany(cbsPeriods);

		return Result.cbsSuccess("提交预算成功");
	}

	@Override
	public List<CBSItem> addCBSItemByStage(ObjectId _id, ObjectId project_id) {
		List<CBSItem> cbsItemList = new ArrayList<CBSItem>();
		List<ObjectId> cbsItemIdList = new ArrayList<ObjectId>();
		CBSItem parentCBSItem = get(_id);
		List<Work> workList = c(Work.class)
				.find(new BasicDBObject("project_id", project_id).append("stage", Boolean.TRUE))
				.into(new ArrayList<Work>());
		if (workList.size() > 0) {
			for (Work work : workList) {
				CBSItem cbsItem = CBSItem.getInstance(parentCBSItem);
				cbsItem.setId(work.getCode());
				cbsItem.setName(work.toString());
				cbsItem.setParent_id(_id);
				cbsItemList.add(cbsItem);
				cbsItemIdList.add(cbsItem.get_id());
			}
			c(CBSItem.class).insertMany(cbsItemList);
		}
		return query(new BasicDBObject("_id", new BasicDBObject("$in", cbsItemIdList))).into(new ArrayList<CBSItem>());
	}

	@Override
	public List<CBSItem> listProjectCost(BasicDBObject condition) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");

		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(new Document("$lookup", new Document("from", "project").append("localField", "_id")
				.append("foreignField", "cbs_id").append("as", "project")));
		pipeline.add(new Document("$unwind", "$project"));
		pipeline.add(Aggregates.addFields(new Field<String>("scopeId", "$project.id")));
		pipeline.add(new Document("$match", new Document("project.status", new Document("$nin",
				Arrays.asList(ProjectStatus.Created, ProjectStatus.Closed, ProjectStatus.Terminated)))));

		pipeline.add(Aggregates.lookup("cbs", "_id", "parent_id", "_children"));

		pipeline.add(Aggregates.addFields(new Field<BasicDBObject>("children", new BasicDBObject("$map",
				new BasicDBObject().append("input", "$_children._id").append("as", "id").append("in", "$$id")))));

		pipeline.add(Aggregates.project(new BasicDBObject("_period", false).append("_budget", false)
				.append("_children", false).append("project", false)));

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		return c(CBSItem.class).aggregate(pipeline).into(new ArrayList<CBSItem>());
	}

	@Override
	public List<CBSItem> listProjectCostAnalysis(BasicDBObject condition) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");

		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(new Document("$lookup", new Document("from", "project").append("localField", "_id")
				.append("foreignField", "cbs_id").append("as", "project")));
		pipeline.add(new Document("$unwind", "$project"));
		pipeline.add(Aggregates.addFields(new Field<String>("scopeId", "$project.id")));
		pipeline.add(new Document("$match", new Document("project.status",
				new Document("$nin", Arrays.asList(ProjectStatus.Created, ProjectStatus.Terminated)))));

		appendUserInfo(pipeline, "project.pmId", "scopeCharger");

		pipeline.add(Aggregates.lookup("cbs", "_id", "parent_id", "_children"));

		pipeline.add(Aggregates.addFields(
				new Field<BasicDBObject>("children",
						new BasicDBObject("$map",
								new BasicDBObject().append("input", "$_children._id").append("as", "id").append("in",
										"$$id"))),
				new Field<String>("scopePlanStart", "$project.planStart"),
				new Field<String>("scopePlanFinish", "$project.planFinish"),
				new Field<String>("scopeStatus", "$project.status")));

		pipeline.add(Aggregates.project(new BasicDBObject("_period", false).append("_budget", false)
				.append("_children", false).append("project", false)));

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		ArrayList<CBSItem> into = c(CBSItem.class).aggregate(pipeline).into(new ArrayList<CBSItem>());
		return into;
	}

	@Override
	public long countProjectCost(BasicDBObject filter) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(new Document("$lookup", new Document("from", "project").append("localField", "_id")
				.append("foreignField", "cbs_id").append("as", "project")));
		pipeline.add(new Document("$unwind", "$project"));
		pipeline.add(new Document("$match", new Document("project.status", new Document("$nin",
				Arrays.asList(ProjectStatus.Created, ProjectStatus.Created, ProjectStatus.Terminated)))));
		pipeline.add(new Document("$project", new Document("project", false)));

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		return c(CBSItem.class).aggregate(pipeline).into(new ArrayList<CBSItem>()).size();
	}

	@Override
	public Date getNextSettlementDate(ObjectId scope_id) {
		Document workDoc = c("work").find(new Document("_id", scope_id)).projection(new Document("project_id", 1))
				.first();
		if (workDoc != null) {
			scope_id = workDoc.getObjectId("project_id");
		}
		Document doc = c("project").find(new Document("_id", scope_id))
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
	public List<Result> submitCBSSubjectCost(ObjectId scope_id, String id) {
		List<Result> result = submitCBSSubjectCostCheck(scope_id, id);
		if (!result.isEmpty()) {
			return result;
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, Integer.parseInt(id.substring(0, 4)));
		cal.set(Calendar.MONTH, Integer.parseInt(id.substring(4, 6)));
		// 修改项目结算时间
		c(Project.class).updateOne(new BasicDBObject("_id", scope_id),
				new BasicDBObject("$set", new BasicDBObject("settlementDate", cal.getTime())));

		return result;
	}

	private List<Result> submitCBSSubjectCostCheck(ObjectId scope_id, String id) {
		return new ArrayList<Result>();
	}

	@Override
	public Document getCostCompositionAnalysis(ObjectId cbsScope_id, String year) {

		Document cbsSubjectMatch = new Document();
		if (cbsScope_id != null)
			cbsSubjectMatch.append("$in", Arrays.asList("$cbsItem_id", getCBSItemId(cbsScope_id)));

		// TODO JsonArray查询获取
		List<? extends Bson> pipeline = Arrays.asList(
				new Document("$lookup",
						new Document("from", "cbsSubject")
								.append("let",
										new Document("subjectNumber", "$id"))
								.append("pipeline", Arrays.asList(
										new Document("$match", new Document("$expr", new Document("$and", Arrays.asList(
												new Document("$eq", Arrays.asList("$subjectNumber", "$$subjectNumber")),
												new Document("$eq",
														Arrays.asList(new Document("$indexOfBytes",
																Arrays.asList("$id", year)), 0)),
												cbsSubjectMatch)))),
										new Document("$group",
												new Document("_id", "$subjectNumber")
														.append("cost", new Document("$sum", "$cost"))
														.append("count", new Document("$sum", 1)))))
								.append("as", "cbsSubject")),
				new Document("$unwind", new Document("path", "$cbsSubject").append("preserveNullAndEmptyArrays", true)),
				new Document("$addFields",
						new Document("cost", "$cbsSubject.cost").append("count", "$cbsSubject.count")),
				new Document("$project", new Document("cbsSubject", false)));

		List<String> data1 = new ArrayList<String>();
		List<Document> data2 = new ArrayList<Document>();
		c("accountItem").aggregate(pipeline).forEach((Document doc) -> {
			Object count = doc.get("count");
			if (count != null && ((Number) count).doubleValue() > 0) {
				data2.add(new Document("name", doc.getString("name")).append("value", getDoubleValue(doc.get("cost"))));
				data1.add(doc.getString("name"));
			}
		});
		List<Document> series = Arrays.asList(new Document("name", "成本组成").append("type", "pie").append("radius", "55%")
				.append("center", Arrays.asList("50%", "60%"))
				.append("label", new Document("normal", new Document("formatter", "{b|{b}：{c}万元} {per|{d}%}").append(
						"rich",
						new Document("b",
								new Document("color", "#747474").append("lineHeight", 22).append("align", "center"))
										.append("hr",
												new Document("color", "#aaa").append("width", "100%")
														.append("borderWidth", 0.5).append("height", 0))
										.append("per",
												new Document("color", "#eee").append("backgroundColor", "#334455")
														.append("padding", Arrays.asList(2, 4))
														.append("borderRadius", 2)))))

				.append("data", data2));
		return getPieChart(year + "年 项目成本组成分析（万元）", data1, series);
	}

	@Override
	public Document getPeriodCostCompositionAnalysis(ObjectId cbsScope_id, String startPeriod, String endPeriod) {
		String title;
		if (startPeriod.equals(endPeriod)) {
			title = startPeriod.substring(0, 4) + "年" + Integer.parseInt(startPeriod.substring(4, 6))
					+ "月 项目成本组成分析（万元）";
		} else {
			title = startPeriod.substring(0, 4) + "年" + Integer.parseInt(startPeriod.substring(4, 6)) + "月-"
					+ endPeriod.substring(0, 4) + "年" + Integer.parseInt(endPeriod.substring(4, 6)) + "月 成本组成";
		}

		Document cbsSubjectMatch = new Document();
		if (cbsScope_id != null)
			cbsSubjectMatch.append("$in", Arrays.asList("$cbsItem_id", getCBSItemId(cbsScope_id)));

		List<? extends Bson> pipeline = Arrays.asList(
				new Document("$lookup",
						new Document("from", "cbsSubject").append("let", new Document("subjectNumber", "$id"))
								.append("pipeline",
										Arrays.asList(
												new Document("$match",
														new Document("$expr", new Document("$and", Arrays.asList(
																new Document("$eq",
																		Arrays.asList("$subjectNumber",
																				"$$subjectNumber")),
																new Document("$gte", Arrays.asList("$id", startPeriod)),
																new Document("$lte", Arrays.asList("$id", endPeriod)),
																cbsSubjectMatch)))),
												new Document("$group",
														new Document("_id", "$subjectNumber")
																.append("cost", new Document("$sum", "$cost"))
																.append("count", new Document("$sum", 1)))))
								.append("as", "cbsSubject")),
				new Document("$unwind", new Document("path", "$cbsSubject").append("preserveNullAndEmptyArrays", true)),
				new Document("$addFields",
						new Document("cost", "$cbsSubject.cost").append("count", "$cbsSubject.count")),
				new Document("$project", new Document("cbsSubject", false)));

		List<String> data1 = new ArrayList<String>();
		List<Document> data2 = new ArrayList<Document>();
		c("accountItem").aggregate(pipeline).forEach((Document doc) -> {
			Object count = doc.get("count");
			if (count != null && ((Number) count).doubleValue() > 0) {
				data2.add(new Document("name", doc.getString("name")).append("value", getDoubleValue(doc.get("cost"))));
				data1.add(doc.getString("name"));
			}
		});

		List<Document> series = Arrays.asList(new Document("name", "成本组成").append("type", "pie").append("radius", "55%")
				.append("center", Arrays.asList("50%", "60%"))
				.append("label", new Document("normal", new Document("formatter", "{b|{b}：{c}万元} {per|{d}%}").append(
						"rich",
						new Document("b",
								new Document("color", "#747474").append("lineHeight", 22).append("align", "center"))
										.append("hr",
												new Document("color", "#aaa").append("width", "100%")
														.append("borderWidth", 0.5).append("height", 0))
										.append("per",
												new Document("color", "#eee").append("backgroundColor", "#334455")
														.append("padding", Arrays.asList(2, 4))
														.append("borderRadius", 2)))))

				.append("data", data2));
		return getPieChart(title, data1, series);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Document getMonthCostCompositionAnalysis(ObjectId cbsScope_id, String year) {

		Document cbsSubjectMatch = new Document();
		if (cbsScope_id != null)
			cbsSubjectMatch.append("$in", Arrays.asList("$cbsItem_id", getCBSItemId(cbsScope_id)));

		// TODO JsonArray查询获取
		List<? extends Bson> pipeline = Arrays.asList(
				new Document("$lookup",
						new Document("from", "cbsSubject")
								.append("let",
										new Document("subjectNumber", "$id"))
								.append("pipeline", Arrays.asList(
										new Document("$match", new Document("$expr", new Document("$and", Arrays.asList(
												new Document("$eq", Arrays.asList("$subjectNumber", "$$subjectNumber")),
												new Document("$eq",
														Arrays.asList(new Document("$indexOfBytes",
																Arrays.asList("$id", year)), 0)),
												cbsSubjectMatch)))),
										new Document("$group",
												new Document("_id", "$id").append("cost", new Document("$sum", "$cost"))
														.append("budget", new Document("$sum", "$budget"))),
										new Document("$sort", new Document("_id", 1))))
								.append("as", "cbsSubject")),
				new Document("$sort", new Document("id", 1)));

		List<String> data1 = new ArrayList<String>();
		List<Document> data2 = new ArrayList<Document>();
		c("accountItem").aggregate(pipeline).forEach((Document doc) -> {
			Object cbsSubjects = doc.get("cbsSubject");
			if (cbsSubjects != null && cbsSubjects instanceof List && ((List<Document>) cbsSubjects).size() > 0) {
				String name = doc.getString("name");
				data1.add(name);
				Document budgetDoc = new Document();
				data2.add(budgetDoc);
				budgetDoc.append("name", name);
				budgetDoc.append("type", "bar");
				budgetDoc.append("stack", "预算");
				budgetDoc.append("label",
						new Document("normal", new Document("show", true).append("position", "inside")));
				Document costDoc = new Document();
				data2.add(costDoc);
				costDoc.append("name", name);
				costDoc.append("type", "bar");
				costDoc.append("stack", "成本");
				costDoc.append("itemStyle", new Document("opacity", 0.5));
				costDoc.append("label",
						new Document("normal", new Document("show", true).append("position", "inside")));
				List<Object> cost = new ArrayList<Object>();
				List<Object> budget = new ArrayList<Object>();
				costDoc.append("data", cost);
				budgetDoc.append("data", budget);
				((List<Document>) cbsSubjects).forEach(cbsSubject -> {
					String id = cbsSubject.getString("_id");
					if ((year + "01").equals(id)) {
						cost.add(cbsSubject.get("cost"));
						budget.add(cbsSubject.get("budget"));
					} else if ((year + "02").equals(id)) {
						while (cost.size() < 1) {
							cost.add(null);
							budget.add(null);
						}
						cost.add(getDoubleValue(cbsSubject.get("cost")));
						budget.add(getDoubleValue(cbsSubject.get("budget")));
					} else if ((year + "03").equals(id)) {
						while (cost.size() < 2) {
							cost.add(null);
							budget.add(null);
						}
						cost.add(getDoubleValue(cbsSubject.get("cost")));
						budget.add(getDoubleValue(cbsSubject.get("budget")));
					} else if ((year + "04").equals(id)) {
						while (cost.size() < 3) {
							cost.add(null);
							budget.add(null);
						}
						cost.add(getDoubleValue(cbsSubject.get("cost")));
						budget.add(getDoubleValue(cbsSubject.get("budget")));
					} else if ((year + "05").equals(id)) {
						while (cost.size() < 4) {
							cost.add(null);
							budget.add(null);
						}
						cost.add(getDoubleValue(cbsSubject.get("cost")));
						budget.add(getDoubleValue(cbsSubject.get("budget")));
					} else if ((year + "06").equals(id)) {
						while (cost.size() < 5) {
							cost.add(null);
							budget.add(null);
						}
						cost.add(getDoubleValue(cbsSubject.get("cost")));
						budget.add(getDoubleValue(cbsSubject.get("budget")));
					} else if ((year + "07").equals(id)) {
						while (cost.size() < 6) {
							cost.add(null);
							budget.add(null);
						}
						cost.add(getDoubleValue(cbsSubject.get("cost")));
						budget.add(getDoubleValue(cbsSubject.get("budget")));
					} else if ((year + "08").equals(id)) {
						while (cost.size() < 7) {
							cost.add(null);
							budget.add(null);
						}
						cost.add(getDoubleValue(cbsSubject.get("cost")));
						budget.add(getDoubleValue(cbsSubject.get("budget")));
					} else if ((year + "09").equals(id)) {
						while (cost.size() < 8) {
							cost.add(null);
							budget.add(null);
						}
						cost.add(getDoubleValue(cbsSubject.get("cost")));
						budget.add(getDoubleValue(cbsSubject.get("budget")));
					} else if ((year + "10").equals(id)) {
						while (cost.size() < 9) {
							cost.add(null);
							budget.add(null);
						}
						cost.add(getDoubleValue(cbsSubject.get("cost")));
						budget.add(getDoubleValue(cbsSubject.get("budget")));
					} else if ((year + "11").equals(id)) {
						while (cost.size() < 10) {
							cost.add(null);
							budget.add(null);
						}
						cost.add(getDoubleValue(cbsSubject.get("cost")));
						budget.add(getDoubleValue(cbsSubject.get("budget")));
					} else if ((year + "12").equals(id)) {
						while (cost.size() < 11) {
							cost.add(null);
							budget.add(null);
						}
						cost.add(getDoubleValue(cbsSubject.get("cost")));
						budget.add(getDoubleValue(cbsSubject.get("budget")));
					}
				});
			}
		});
		return getBarChart(year + "年 各月项目预算和成本分析（万元）", data1, data2);
	}

	private String getDoubleValue(Object value) {
		if (value instanceof Number) {
			double d = ((Number) value).doubleValue();
			if (d != 0d) {
				return new DecimalFormat("#.0").format(d);
			}
		}
		return null;
	}

	private List<ObjectId> getCBSItemId(ObjectId cbsScope_id) {
		List<ObjectId> cbsItem_id = c("cbs").distinct("_id", new Document("scope_id", cbsScope_id), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		cbsItem_id = getDesentItems(cbsItem_id, "cbs", "parent_id");
		return cbsItem_id;
	}

	@Override
	public Document getCostCompositionAnalysis(String year) {
		return getCostCompositionAnalysis(null, year);
	}

	@Override
	public Document getPeriodCostCompositionAnalysis(String startPeriod, String endPeriod) {
		return getPeriodCostCompositionAnalysis(null, startPeriod, endPeriod);
	}

	@Override
	public Document getMonthCostCompositionAnalysis(String year) {
		return getMonthCostCompositionAnalysis(null, year);
	}

	@Override
	public Document getCBSSummary(ObjectId cbsScope_id, String startPeriod, String endPeriod) {
		List<ObjectId> cbsItemId = getCBSItemId(cbsScope_id);
		List<? extends Bson> pipeline = Arrays.asList(
				new Document("$match", new Document("_id",
						new Document("$in", cbsItemId))),
				new Document("$lookup",
						new Document(
								"from", "cbs")
										.append("let",
												new Document("parent_id", "$_id"))
										.append("pipeline", Arrays.asList(
												new Document("$match",
														new Document("$expr", new Document("$and",
																Arrays.asList(new Document("$eq",
																		Arrays.asList("$parent_id", "$$parent_id")))))),
												new Document("$count", "count")))
										.append("as", "count")),
				new Document("$unwind", new Document("path", "$count").append("preserveNullAndEmptyArrays", true)),
				new Document("$match", new Document("count", null)),
				new Document("$lookup",
						new Document("from", "cbsSubject")
								.append("let",
										new Document("cbsItem_id", "$_id"))
								.append("pipeline", Arrays.asList(
										new Document("$match",
												new Document("$expr", new Document("$and",
														Arrays.asList(new Document("$eq",
																Arrays.asList("$cbsItem_id", "$$cbsItem_id")))))),
										new Document("$group",
												new Document("_id", null).append("cost", new Document("$sum", "$cost"))
														.append("budget", new Document("$sum", "$budget")))))
								.append("as", "cbsSubject1")),
				new Document("$unwind",
						new Document("path", "$cbsSubject1").append("preserveNullAndEmptyArrays", true)),
				new Document("$lookup",
						new Document("from", "cbsSubject")
								.append("let",
										new Document("cbsItem_id", "$_id"))
								.append("pipeline", Arrays.asList(
										new Document("$match", new Document("$expr", new Document("$and",
												Arrays.asList(
														new Document("$eq",
																Arrays.asList("$cbsItem_id", "$$cbsItem_id")),
														new Document("$gte", Arrays.asList("$id", startPeriod)),
														new Document("$lte", Arrays.asList("$id", endPeriod)))))),
										new Document("$group",
												new Document("_id", null).append("cost", new Document("$sum", "$cost"))
														.append("budget", new Document("$sum", "$budget")))))
								.append("as", "cbsSubject2")),
				new Document("$unwind",
						new Document("path", "$cbsSubject2").append("preserveNullAndEmptyArrays", true)),
				new Document("$addFields",
						new Document("totalCost", "$cbsSubject1.cost").append("totalBudget", "$cbsSubject1.budget")
								.append("cost", "$cbsSubject2.cost").append("budget", "$cbsSubject2.budget")),
				new Document("$group",
						new Document("_id", null).append("totalCost", new Document("$sum", "$totalCost"))
								.append("totalBudget", new Document("$sum", "$totalBudget"))
								.append("cost", new Document("$sum", "$cost"))
								.append("budget", new Document("$sum", "$budget"))));
		return c("cbs").aggregate(pipeline).first();
	}

	@Override
	public Document getCBSSummary(String startPeriod, String endPeriod) {
		List<? extends Bson> pipeline = Arrays.asList(
				new Document("$lookup",
						new Document(
								"from", "cbs")
										.append("let",
												new Document("parent_id", "$_id"))
										.append("pipeline", Arrays.asList(
												new Document("$match",
														new Document("$expr", new Document("$and",
																Arrays.asList(new Document("$eq",
																		Arrays.asList("$parent_id", "$$parent_id")))))),
												new Document("$count", "count")))
										.append("as", "count")),
				new Document("$unwind", new Document("path", "$count").append("preserveNullAndEmptyArrays", true)),
				new Document("$match", new Document("count", null)),
				new Document("$lookup",
						new Document("from", "cbsSubject")
								.append("let",
										new Document("cbsItem_id", "$_id"))
								.append("pipeline", Arrays.asList(
										new Document("$match",
												new Document("$expr", new Document("$and",
														Arrays.asList(new Document("$eq",
																Arrays.asList("$cbsItem_id", "$$cbsItem_id")))))),
										new Document("$group",
												new Document("_id", null).append("cost", new Document("$sum", "$cost"))
														.append("budget", new Document("$sum", "$budget")))))
								.append("as", "cbsSubject1")),
				new Document("$unwind",
						new Document("path", "$cbsSubject1").append("preserveNullAndEmptyArrays", true)),
				new Document("$lookup",
						new Document("from", "cbsSubject")
								.append("let",
										new Document("cbsItem_id", "$_id"))
								.append("pipeline", Arrays.asList(
										new Document("$match", new Document("$expr", new Document("$and",
												Arrays.asList(
														new Document("$eq",
																Arrays.asList("$cbsItem_id", "$$cbsItem_id")),
														new Document("$gte", Arrays.asList("$id", startPeriod)),
														new Document("$lte", Arrays.asList("$id", endPeriod)))))),
										new Document("$group",
												new Document("_id", null).append("cost", new Document("$sum", "$cost"))
														.append("budget", new Document("$sum", "$budget")))))
								.append("as", "cbsSubject2")),
				new Document("$unwind",
						new Document("path", "$cbsSubject2").append("preserveNullAndEmptyArrays", true)),
				new Document("$addFields",
						new Document("totalCost", "$cbsSubject1.cost").append("totalBudget", "$cbsSubject1.budget")
								.append("cost", "$cbsSubject2.cost").append("budget", "$cbsSubject2.budget")),
				new Document("$group",
						new Document("_id", null).append("totalCost", new Document("$sum", "$totalCost"))
								.append("totalBudget", new Document("$sum", "$totalBudget"))
								.append("cost", new Document("$sum", "$cost"))
								.append("budget", new Document("$sum", "$budget"))));
		return c("cbs").aggregate(pipeline).first();
	}
}
