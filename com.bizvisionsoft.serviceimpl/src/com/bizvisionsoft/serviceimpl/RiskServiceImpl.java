package com.bizvisionsoft.serviceimpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.math.scheduling.MonteCarloSimulate;
import com.bizvisionsoft.math.scheduling.Risk;
import com.bizvisionsoft.math.scheduling.Route;
import com.bizvisionsoft.math.scheduling.Task;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.model.DetectionInd;
import com.bizvisionsoft.service.model.QuanlityInfInd;
import com.bizvisionsoft.service.model.RBSItem;
import com.bizvisionsoft.service.model.RBSType;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.RiskEffect;
import com.bizvisionsoft.service.model.RiskResponse;
import com.bizvisionsoft.service.model.RiskScore;
import com.bizvisionsoft.service.model.RiskUrgencyInd;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.DeleteResult;

public class RiskServiceImpl extends BasicServiceImpl implements RiskService {

	@Override
	public List<RBSType> listRBSType() {
		return c(RBSType.class).find().into(new ArrayList<RBSType>());
	}

	@Override
	public RBSType insertRBSType(RBSType item) {
		return insert(item, RBSType.class);
	}

	@Override
	public long deleteRBSType(ObjectId _id) {
		return delete(_id, RBSType.class);
	}

	@Override
	public long updateRBSType(BasicDBObject fu) {
		return update(fu, RBSType.class);
	}

	@Override
	public List<RBSItem> listRBSItem(BasicDBObject condition) {
		return createDataSet(condition, RBSItem.class);
	}

	@Override
	public long countRBSItem(BasicDBObject filter) {
		return c("rbsItem").countDocuments(filter);
	}

	@Override
	public RBSItem insertRBSItem(RBSItem item) {
		// 计算序号
		ObjectId pj_id = item.getProject_id();
		String typeId = item.getRbsType().getId();
		int idx = nextRBSItemIndex(pj_id, typeId);
		item.setIndex(idx);
		// 计算评分
		String qty = item.getQtyInf();
		double cost = item.getCostInf();
		int time = item.getTimeInf();
		Double score = calculateRiskInfValue(qty, cost, time);
		item.setInfValue(score);
		return insert(item);
	}

	private double calculateRiskInfValue(String qty, double cost, int time) {
		List<RiskScore> stdlist = listRiskScoreInd();
		Double score = null;

		for (int i = 0; i < stdlist.size(); i++) {
			RiskScore std = stdlist.get(i);
			if (std.getQuanlityImpact().stream().anyMatch(qid -> qid.value.equals(qty))) {
				if (score == null || score.doubleValue() < std.getScore()) {
					score = std.getScore();
					break;
				}
			}
		}
		for (int i = 0; i < stdlist.size(); i++) {
			RiskScore std = stdlist.get(i);
			double maxCost = Optional.ofNullable(std.getMaxCostImpact()).orElse(Double.MAX_VALUE);
			double minCost = Optional.ofNullable(std.getMinCostImpact()).orElse(Double.MIN_VALUE);
			if (cost <= maxCost && cost >= minCost) {
				if (score == null || score.doubleValue() < std.getScore()) {
					score = std.getScore();
					break;
				}
			}
		}
		for (int i = 0; i < stdlist.size(); i++) {
			RiskScore std = stdlist.get(i);
			int maxTime = Optional.ofNullable(std.getMaxTimeImpact()).orElse(Integer.MAX_VALUE);
			int minTime = Optional.ofNullable(std.getMinTimeImpact()).orElse(Integer.MIN_VALUE);
			if (time <= maxTime && time >= minTime) {
				if (score == null || score.doubleValue() < std.getScore()) {
					score = std.getScore();
					break;
				}
			}
		}

		return score == null ? 0d : score.doubleValue();
	}

	public int nextRBSItemIndex(ObjectId project_id, String type_id) {
		Document doc = c("rbsItem").find(new BasicDBObject("project_id", project_id).append("rbsType.id", type_id))
				.sort(new BasicDBObject("index", -1)).projection(new BasicDBObject("index", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("index", 0)).orElse(0) + 1;
	}

	@Override
	public long deleteRBSItem(ObjectId _id) {
		List<ObjectId> items = getDesentItems(Arrays.asList(_id), "rbsItem", "parent_id");
		DeleteResult rs = c("rbsItem").deleteMany(new BasicDBObject("_id", new BasicDBObject("$in", items)));
		c("riskEffect").deleteMany(new BasicDBObject("rbsItem_id", _id));
		return rs.getDeletedCount();
	}

	@Override
	public long updateRBSItem(BasicDBObject fu) {
		long count = update(fu, RBSItem.class);

		BasicDBObject filter = (BasicDBObject) fu.get("filter");
		c("rbsItem").find(filter).forEach((Document doc) -> {
			String qty = (String) doc.getString("qtyInf");
			double cost = doc.getDouble("costInf");
			int time = doc.getInteger("timeInf");
			double score = calculateRiskInfValue(qty, cost, time);
			c("rbsItem").updateOne(new Document("_id", doc.get("_id")),
					new Document("$set", new Document("infValue", score)));
		});

		return count;
	}

	@Override
	public RiskEffect addRiskEffect(RiskEffect re) {
		return insert(re);
	}

	@Override
	public List<RiskUrgencyInd> listRiskUrgencyInd() {
		return c(RiskUrgencyInd.class).find().into(new ArrayList<RiskUrgencyInd>());
	}

	@Override
	public RiskUrgencyInd insertRiskUrgencyInd(RiskUrgencyInd item) {
		return insert(item);
	}

	@Override
	public long deleteRiskUrgencyInd(ObjectId _id) {
		return delete(_id, RiskUrgencyInd.class);
	}

	@Override
	public long updateRiskUrgencyInd(BasicDBObject fu) {
		return update(fu, RiskUrgencyInd.class);
	}

	@Override
	public String getUrgencyText(long days) {
		String text = c("riskUrInd").distinct("text",
				new BasicDBObject("min", new BasicDBObject("$lt", days)).append("max", new BasicDBObject("$gte", days)),
				String.class).first();
		if (text != null) {
			return text;
		} else {
			return "";
		}
	}

	@Override
	public List<QuanlityInfInd> listRiskQuanlityInfInd() {
		return c(QuanlityInfInd.class).find().sort(new BasicDBObject("_id", 1)).into(new ArrayList<QuanlityInfInd>());
	}

	@Override
	public QuanlityInfInd insertRiskQuanlityInfInd(QuanlityInfInd item) {
		return insert(item);
	}

	@Override
	public long deleteRiskQuanlityInfInd(ObjectId _id) {
		return delete(_id, QuanlityInfInd.class);
	}

	@Override
	public long updateRiskQuanlityInfInd(BasicDBObject fu) {
		return update(fu, QuanlityInfInd.class);

	}

	@Override
	public List<DetectionInd> listRiskDetectionInd() {
		return c(DetectionInd.class).find().sort(new BasicDBObject("_id", 1)).into(new ArrayList<DetectionInd>());
	}

	@Override
	public DetectionInd insertRiskDetectionInd(DetectionInd item) {
		return insert(item);
	}

	@Override
	public long deleteRiskDetectionInd(ObjectId _id) {
		return delete(_id, DetectionInd.class);
	}

	@Override
	public long updateRiskDetectionInd(BasicDBObject fu) {
		return update(fu, DetectionInd.class);
	}

	@Override
	public List<RiskScore> listRiskScoreInd() {
		return c(RiskScore.class).find().sort(new BasicDBObject("score", -1)).into(new ArrayList<RiskScore>());
	}

	@Override
	public RiskScore insertRiskScoreInd(RiskScore item) {
		return insert(item);
	}

	@Override
	public long deleteRiskScoreInd(ObjectId _id) {
		return delete(_id, RiskScore.class);
	}

	@Override
	public long updateRiskScoreInd(BasicDBObject fu) {
		return update(fu, RiskScore.class);
	}

	@Override
	public List<RiskEffect> listRiskEffect(BasicDBObject condition) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = new BasicDBObject("work.aci", -1).append("work.acp", -1);// (BasicDBObject)
																						// condition.get("sort");//
																						// 不接受客户端的排序

		List<Bson> pipeline = new ArrayList<Bson>();
		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		appendWork(pipeline);

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendUserInfo(pipeline, "work.chargerId", "work.chargerInfo");

		pipeline.add(Aggregates.lookup("rbsItem", "rbsItem_id", "_id", "rbsItem"));

		pipeline.add(Aggregates.unwind("$rbsItem"));

		ArrayList<RiskEffect> result = c(RiskEffect.class).aggregate(pipeline).into(new ArrayList<>());
		return result;
	}

	@Override
	public long countRiskEffect(BasicDBObject filter) {
		return count(filter, "riskEffect");
	}

	@Override
	public long deleteRiskEffect(ObjectId _id) {
		return delete(_id, RiskEffect.class);
	}

	@Override
	public long updateRiskEffect(BasicDBObject fu) {
		return update(fu, RiskEffect.class);
	}

	@Override
	public List<Result> monteCarloSimulate(ObjectId _id, int times) {
		Date pjStart = c("project").find(new Document("_id", _id)).first().getDate("planStart");

		ArrayList<Document> works = c("work").find(new Document("project_id", _id)).into(new ArrayList<>());
		ArrayList<Document> links = c("worklinks").find(new Document("project_id", _id)).into(new ArrayList<>());
		ArrayList<Document> riskEffect = c("rbsItem")
				.aggregate(new JQ("查询项目风险用于构造Graphic-Risk模型").set("project_id", _id).array()).into(new ArrayList<>());

		ArrayList<Task> tasks = new ArrayList<Task>();
		ArrayList<Route> routes = new ArrayList<Route>();
		ArrayList<Risk> risks = new ArrayList<Risk>();

		convertGraphicWithRisks(works, links, riskEffect, tasks, routes, risks);

		MonteCarloSimulate mcs = new MonteCarloSimulate(tasks, routes, risks, r -> {
			// 处理项目开始后应立即开始但延迟开始的工作
			String id = r.end2.getId();
			Document doc = works.stream().filter(d -> id.equals(d.getObjectId("_id").toHexString())).findFirst().get();
			Date aStart = doc.getDate("actualStart");
			Date pStart = doc.getDate("planStart");
			Date start = aStart == null ? pStart : aStart;
			r.relations.get(0).interval = (int) ((start.getTime() - pjStart.getTime()) / (1000 * 60 * 60 * 24));
		});
		mcs.simulate(times);

		List<List<Double>> tData = new ArrayList<>();
		mcs.TMap.entrySet().forEach(e -> {
			double key = e.getKey();
			double perc = new BigDecimal(100f * e.getValue() / times).setScale(4, BigDecimal.ROUND_HALF_UP)
					.doubleValue();
			tData.add(Arrays.asList(key, perc));
		});

		tData.sort((l1, l2) -> (int) (l1.get(0) - l2.get(0)));

		Document simulateResult = new Document("project_id", _id).append("times", times).append("date", new Date())
				.append("Tdata", tData).append("noRiskT", mcs.noRiskT);
		c("monteCarloSimulate").deleteMany(new Document("project_id", _id));
		c("monteCarloSimulate").insertOne(simulateResult);
		System.out.println("MonteCarlo Simulate Finished. x" + times);

		// 写入work
		tasks.forEach(t -> updateWorkRiskIndicators(new ObjectId(t.getId()), t.getACI(), t.getACP()));

		return new ArrayList<Result>();
	}

	private void updateWorkRiskIndicators(ObjectId _id, Float aci, Float acp) {
		c("work").updateOne(new Document("_id", _id),
				new Document("$set", new Document("aci", (double) aci).append("acp", (double) acp)));
	}

	@Override
	public Document monteCarloSimulateChartData(ObjectId project_id) {
		Document mcs = c("monteCarloSimulate").find(new Document("project_id", project_id)).first();
		Object value = null;
		if (mcs != null) {
			value = mcs.get("Tdata");
		}
		if (value == null)
			value = new ArrayList<>();
		return new JQ("蒙特卡洛单图").set("data", value).doc();
	}

	@Override
	public RiskResponse insertRiskResponse(RiskResponse resp) {
		return insert(resp);
	}

	@Override
	public long deleteRiskResponse(ObjectId _id) {
		return delete(_id, RiskResponse.class);
	}

	@Override
	public long updateRiskResponse(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, RiskResponse.class);
	}

	@Override
	public List<RiskResponse> listRiskResponse(BasicDBObject cond) {
		return createDataSet(cond, RiskResponse.class);
	}

	@Override
	public long countRiskResponse(BasicDBObject filter) {
		return count(filter, "riskResponse");
	}

	@Override
	public Double getDurationProbability(ObjectId project_id) {
		Document pj = c("project").find(new Document("_id", project_id)).first();
		if (pj == null) {
			return null;
		}
		if (c("monteCarloSimulate").countDocuments(new Document("project_id", project_id)) == 0) {
			return null;
		}

		long t = (pj.getDate("planFinish").getTime() - pj.getDate("planStart").getTime()) / (1000 * 60 * 60 * 24);
		Document doc = c("monteCarloSimulate")
				.aggregate(new JQ("查询项目某个工期的概率").set("project_id", project_id).set("T", t).array()).first();

		return Optional.ofNullable(doc).map(d -> (Number) d.get("prob")).map(p -> p.doubleValue() / 100).orElse(0d);
	}

	@Override
	public List<List<Double>> getDurationForcast(ObjectId project_id) {
		Document doc = c("monteCarloSimulate").aggregate(new JQ("查询项目乐观悲观的估计工期").set("project_id", project_id).array())
				.first();

		if (doc == null) {
			return null;
		}

		Document minT = (Document) doc.get("minT");

		Document maxT = (Document) doc.get("maxT");

		Document maxP = (Document) doc.get("maxP");
		doc = c("monteCarloSimulate")
				.aggregate(new JQ("查询项目某个工期的概率").set("project_id", project_id).set("T", maxP.get("t")).array()).first();

		ArrayList<List<Double>> result = new ArrayList<List<Double>>();
		result.add(Arrays.asList(((Number) minT.get("t")).doubleValue(), ((Number) minT.get("p")).doubleValue()));
		result.add(Arrays.asList(((Number) maxT.get("t")).doubleValue(), 1d));
		result.add(Arrays.asList(((Number) maxP.get("t")).doubleValue(), ((Number) doc.get("prob")).doubleValue()));

		return result;
	}

	@Override
	public Document getRiskProximityChart(ObjectId project_id) {
		String size = "function (data) {return Math.sqrt(data[2])*8;}";
		ArrayList<List<Object>> data = new ArrayList<List<Object>>();
		c("rbsItem").find(new Document("project_id", project_id)).forEach((Document d) -> {
			Object _i = d.get("infValue");
			Object _p = d.get("probability");
			Object _d = d.get("detectable");
			if (_i != null && _p != null && _d != null) {
				Double rci = ((Number) _i).doubleValue() * ((Number) _p).doubleValue();
				Integer urgency = (int) ((d.getDate("forecast").getTime() - Calendar.getInstance().getTimeInMillis())
						/ (24 * 60 * 60 * 1000));
				Integer detectable = Arrays.asList("很低", "低", "中", "高", "很高").indexOf(_d);
				data.add(Arrays.asList(urgency, detectable, rci,d.get("name")));
			}
		});
		return new JQ("项目风险临近性图表").set("data", data).set("size", size).doc();
	}

	@Override
	public Document getRiskProximityChart2(ObjectId project_id) {
		String size = "function (data) {return Math.sqrt(data[2])*8;}";
		ArrayList<List<Object>> data = new ArrayList<List<Object>>();
		c("rbsItem").find(new Document("project_id", project_id)).forEach((Document d) -> {
			Object _i = d.get("infValue");
			Object _p = d.get("probability");
			Object _d = d.get("detectable");
			if (_i != null && _p != null && _d != null) {
				Double rci = ((Number) _i).doubleValue() * ((Number) _p).doubleValue();
				Integer urgency = (int) ((d.getDate("forecast").getTime() - Calendar.getInstance().getTimeInMillis())
						/ (24 * 60 * 60 * 1000));
				Integer detectable = Arrays.asList("很低", "低", "中", "高", "很高").indexOf(_d);
				data.add(Arrays.asList(urgency, detectable, rci,d.get("name")));
			}
		});
		return new JQ("项目风险临近性图表2").set("data", data).set("size", size).doc();
	}

}
