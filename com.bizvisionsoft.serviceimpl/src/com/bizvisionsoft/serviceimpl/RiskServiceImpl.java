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

import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.common.JQ;
import com.bizvisionsoft.service.model.DetectionInd;
import com.bizvisionsoft.service.model.QuanlityInfInd;
import com.bizvisionsoft.service.model.RBSItem;
import com.bizvisionsoft.service.model.RBSType;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.RiskEffect;
import com.bizvisionsoft.service.model.RiskResponse;
import com.bizvisionsoft.service.model.RiskScore;
import com.bizvisionsoft.service.model.RiskUrgencyInd;
import com.bizvisionsoft.serviceimpl.scheduling.MonteCarloSimulate;
import com.bizvisionsoft.serviceimpl.scheduling.Risk;
import com.bizvisionsoft.serviceimpl.scheduling.Route;
import com.bizvisionsoft.serviceimpl.scheduling.Task;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.DeleteResult;

public class RiskServiceImpl extends BasicServiceImpl implements RiskService {

	@Override
	public List<RBSType> listRBSType(String domain) {
		return c(RBSType.class, domain).find().into(new ArrayList<RBSType>());
	}

	@Override
	public RBSType insertRBSType(RBSType item, String domain) {
		return insert(item, RBSType.class, domain);
	}

	@Override
	public long deleteRBSType(ObjectId _id, String domain) {
		return delete(_id, RBSType.class, domain);
	}

	@Override
	public long updateRBSType(BasicDBObject fu, String domain) {
		return update(fu, RBSType.class, domain);
	}

	@Override
	public List<RBSItem> listRBSItem(BasicDBObject condition, String domain) {
		return createDataSet(condition, RBSItem.class, domain);
	}

	@Override
	public long countRBSItem(BasicDBObject filter, String domain) {
		return c("rbsItem", domain).countDocuments(filter);
	}

	@Override
	public RBSItem insertRBSItem(RBSItem item, String domain) {
		// 计算序号
		ObjectId pj_id = item.getProject_id();
		String typeId = item.getRbsType().getId();
		int idx = nextRBSItemIndex(pj_id, typeId,domain);
		item.setIndex(idx);
		// 计算评分
		String qty = item.getQtyInf();
		double cost = item.getCostInf();
		int time = item.getTimeInf();
		Double score = calculateRiskInfValue(qty, cost, time,domain);
		item.setInfValue(score);
		return insert(item, domain);
	}

	private double calculateRiskInfValue(String qty, double cost, int time,String domain){
		List<RiskScore> stdlist = listRiskScoreInd(domain);
		Double score = null;

		for (int i = 0; i < stdlist.size(); i++){
			RiskScore std = stdlist.get(i);
			if (std.getQuanlityImpact().stream().anyMatch(qid -> qid.value.equals(qty))){
				if (score == null || score.doubleValue() < std.getScore()){
					score = std.getScore();
					break;
				}
			}
		}
		for (int i = 0; i < stdlist.size(); i++){
			RiskScore std = stdlist.get(i);
			double maxCost = Optional.ofNullable(std.getMaxCostImpact()).orElse(Double.MAX_VALUE);
			double minCost = Optional.ofNullable(std.getMinCostImpact()).orElse(Double.MIN_VALUE);
			if (cost <= maxCost && cost >= minCost){
				if (score == null || score.doubleValue() < std.getScore()){
					score = std.getScore();
					break;
				}
			}
		}
		for (int i = 0; i < stdlist.size(); i++){
			RiskScore std = stdlist.get(i);
			int maxTime = Optional.ofNullable(std.getMaxTimeImpact()).orElse(Integer.MAX_VALUE);
			int minTime = Optional.ofNullable(std.getMinTimeImpact()).orElse(Integer.MIN_VALUE);
			if (time <= maxTime && time >= minTime){
				if (score == null || score.doubleValue() < std.getScore()){
					score = std.getScore();
					break;
				}
			}
		}

		return score == null ? 0d : score.doubleValue();
	}

	public int nextRBSItemIndex(ObjectId project_id, String type_id, String domain) {
		Document doc = c("rbsItem", domain).find(new BasicDBObject("project_id", project_id).append("rbsType.id", type_id))
				.sort(new BasicDBObject("index", -1)).projection(new BasicDBObject("index", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("index", 0)).orElse(0) + 1;
	}

	@Override
	public long deleteRBSItem(ObjectId _id, String domain) {
		List<ObjectId> items = getDesentItems(Arrays.asList(_id), "rbsItem", "parent_id",domain);
		DeleteResult rs = c("rbsItem", domain).deleteMany(new BasicDBObject("_id", new BasicDBObject("$in", items)));
		c("riskEffect", domain).deleteMany(new BasicDBObject("rbsItem_id", _id));
		return rs.getDeletedCount();
	}

	@Override
	public long updateRBSItem(BasicDBObject fu, String domain) {
		long count = update(fu, RBSItem.class, domain);

		BasicDBObject filter = (BasicDBObject) fu.get("filter");
		c("rbsItem", domain).find(filter).forEach((Document doc) -> {
			String qty = (String) doc.getString("qtyInf");
			double cost = doc.getDouble("costInf");
			int time = doc.getInteger("timeInf");
			double score = calculateRiskInfValue(qty, cost, time,domain);
			c("rbsItem", domain).updateOne(new Document("_id", doc.get("_id")), new Document("$set", new Document("infValue", score)));
		});

		return count;
	}

	@Override
	public RiskEffect addRiskEffect(RiskEffect re, String domain) {
		return insert(re,domain);
	}

	@Override
	public List<RiskUrgencyInd> listRiskUrgencyInd(String domain) {
		return c(RiskUrgencyInd.class, domain).find().into(new ArrayList<RiskUrgencyInd>());
	}

	@Override
	public RiskUrgencyInd insertRiskUrgencyInd(RiskUrgencyInd item, String domain) {
		return insert(item, domain);
	}

	@Override
	public long deleteRiskUrgencyInd(ObjectId _id, String domain) {
		return delete(_id, RiskUrgencyInd.class, domain);
	}

	@Override
	public long updateRiskUrgencyInd(BasicDBObject fu, String domain) {
		return update(fu, RiskUrgencyInd.class, domain);
	}

	@Override
	public String getUrgencyText(long days, String domain) {
		// TODO 友好的显示方式，同时应考虑和取数合并
		String text = c("riskUrInd", domain).distinct("text",
				new BasicDBObject("min", new BasicDBObject("$lt", days)).append("max", new BasicDBObject("$gte", days)), String.class)
				.first();
		if (text != null) {
			return text;
		} else {
			return "";
		}
	}

	@Override
	public List<QuanlityInfInd> listRiskQuanlityInfInd(String domain) {
		return c(QuanlityInfInd.class, domain).find().sort(new BasicDBObject("_id", 1)).into(new ArrayList<QuanlityInfInd>());
	}

	@Override
	public QuanlityInfInd insertRiskQuanlityInfInd(QuanlityInfInd item, String domain) {
		return insert(item, domain);
	}

	@Override
	public long deleteRiskQuanlityInfInd(ObjectId _id, String domain) {
		return delete(_id, QuanlityInfInd.class, domain);
	}

	@Override
	public long updateRiskQuanlityInfInd(BasicDBObject fu, String domain) {
		return update(fu, QuanlityInfInd.class, domain);
	}

	@Override
	public List<DetectionInd> listRiskDetectionInd(String domain) {
		return c(DetectionInd.class, domain).find().sort(new BasicDBObject("_id", 1)).into(new ArrayList<DetectionInd>());
	}

	@Override
	public DetectionInd insertRiskDetectionInd(DetectionInd item, String domain) {
		return insert(item, domain);
	}

	@Override
	public long deleteRiskDetectionInd(ObjectId _id, String domain) {
		return delete(_id, DetectionInd.class, domain);
	}

	@Override
	public long updateRiskDetectionInd(BasicDBObject fu, String domain) {
		return update(fu, DetectionInd.class, domain);
	}

	@Override
	public List<RiskScore> listRiskScoreInd(String domain) {
		return c(RiskScore.class, domain).find().sort(new BasicDBObject("score", -1)).into(new ArrayList<RiskScore>());
	}

	@Override
	public RiskScore insertRiskScoreInd(RiskScore item, String domain) {
		return insert(item, domain);
	}

	@Override
	public long deleteRiskScoreInd(ObjectId _id, String domain) {
		return delete(_id, RiskScore.class, domain);
	}

	@Override
	public long updateRiskScoreInd(BasicDBObject fu, String domain) {
		return update(fu, RiskScore.class, domain);
	}

	@Override
	public List<RiskEffect> listRiskEffect(BasicDBObject condition, ObjectId _id, String domain) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = new BasicDBObject("work.aci", -1).append("work.acp", -1);// (BasicDBObject)
																						// condition.get("sort");//
																						// 不接受客户端的排序

		List<Bson> pipeline = new ArrayList<Bson>();
		if (filter == null)
			filter = new BasicDBObject("project_id", _id);

		pipeline.add(Aggregates.match(filter));

		appendWork(pipeline);

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendUserInfo(pipeline, "work.chargerId", "work.chargerInfo", domain);

		pipeline.add(Aggregates.lookup("rbsItem", "rbsItem_id", "_id", "rbsItem"));

		pipeline.add(Aggregates.unwind("$rbsItem"));

		ArrayList<RiskEffect> result = c(RiskEffect.class, domain).aggregate(pipeline).into(new ArrayList<>());
		return result;
	}

	@Override
	public long countRiskEffect(BasicDBObject filter, ObjectId _id, String domain) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.append("project_id", _id);

		return count(filter, "riskEffect",domain);
	}

	@Override
	public long deleteRiskEffect(ObjectId _id, String domain) {
		return delete(_id, RiskEffect.class, domain);
	}

	@Override
	public long updateRiskEffect(BasicDBObject fu, String domain) {
		return update(fu, RiskEffect.class, domain);
	}

	@Override
	public List<Result> monteCarloSimulate(ObjectId _id, int times, String domain) {
		Date pjStart = c("project", domain).find(new Document("_id", _id)).first().getDate("planStart");

		ArrayList<Document> works = c("work", domain).find(new Document("project_id", _id)).into(new ArrayList<>());
		ArrayList<Document> links = c("worklinks", domain).find(new Document("project_id", _id)).into(new ArrayList<>());
		ArrayList<Document> riskEffect = c("riskEffect", domain).aggregate(Domain.getJQ(domain, "查询-风险-GraphicRisk模型").set("project_id", _id).array())
				.into(new ArrayList<>());

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
			double perc = new BigDecimal(100f * e.getValue() / times).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
			tData.add(Arrays.asList(key, perc));
		});

		tData.sort((l1, l2) -> (int) (l1.get(0) - l2.get(0)));

		Document simulateResult = new Document("project_id", _id).append("times", times).append("date", new Date()).append("Tdata", tData)
				.append("noRiskT", mcs.noRiskT);
		c("monteCarloSimulate",domain).deleteMany(new Document("project_id", _id));
		c("monteCarloSimulate",domain).insertOne(simulateResult);

		// 写入work
		tasks.forEach(t -> updateWorkRiskIndicators(new ObjectId(t.getId()), t.getACI(), t.getACP(),domain));

		return new ArrayList<Result>();
	}

	private void updateWorkRiskIndicators(ObjectId _id, Float aci, Float acp, String domain) {
		c("work", domain).updateOne(new Document("_id", _id), new Document("$set", new Document("aci", aci).append("acp", acp)));
	}

	@Override
	public Document monteCarloSimulateChartData(ObjectId project_id, String domain) {
		Document mcs = c("monteCarloSimulate",domain).find(new Document("project_id", project_id)).first();
		List<?> tData = null;
		if (mcs != null) {
			tData = (List<?>) mcs.get("Tdata");
		}
		if (tData == null)
			tData = new ArrayList<>();

		ArrayList<List<Double>> sData = new ArrayList<>();
		tData.forEach(elem -> {
			Double v1 = (Double) ((List<?>) elem).get(0);
			Double v2 = (Double) ((List<?>) elem).get(1);
			Double sum;
			if (sData.size() == 0) {
				sum = v2;
			} else {
				sum = sData.get(sData.size() - 1).get(1) + v2;
			}
			sum = (double) Math.round(sum * 100) / 100;
			sData.add(Arrays.asList(new Double[] { v1, sum }));
		});

		return Domain.getJQ(domain, "图表-MCS-项目").set("data", tData).set("sum", sData).doc();
	}

	@Override
	public RiskResponse insertRiskResponse(RiskResponse resp, String domain) {
		return insert(resp,domain);
	}

	@Override
	public long deleteRiskResponse(ObjectId _id, String domain) {
		return delete(_id, RiskResponse.class, domain);
	}

	@Override
	public long updateRiskResponse(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, RiskResponse.class, domain);
	}

	@Override
	public List<RiskResponse> listRiskResponse(BasicDBObject cond, String domain) {
		return createDataSet(cond, RiskResponse.class, domain);
	}

	@Override
	public long countRiskResponse(BasicDBObject filter, String domain) {
		return count(filter, "riskResponse",domain);
	}

	@Override
	public Double getDurationProbability(ObjectId project_id, String domain) {
		Document pj = c("project", domain).find(new Document("_id", project_id)).first();
		if (pj == null) {
			return null;
		}
		if (c("monteCarloSimulate",domain).countDocuments(new Document("project_id", project_id)) == 0) {
			return null;
		}

		long t = (pj.getDate("planFinish").getTime() - pj.getDate("planStart").getTime()) / (1000 * 60 * 60 * 24);
		Document doc = c("monteCarloSimulate",domain).aggregate(Domain.getJQ(domain, "查询-MCS-工期概率").set("project_id", project_id).set("T", t).array()).first();

		return Optional.ofNullable(doc).map(d -> (Number) d.get("prob")).map(p -> p.doubleValue() / 100).orElse(0d);
	}

	@Override
	public List<List<Double>> getDurationForcast(ObjectId project_id, String domain) {
		Document doc = c("monteCarloSimulate",domain).aggregate(Domain.getJQ(domain, "查询-MCS-估计工期").set("project_id", project_id).array()).first();

		if (doc == null) {
			return null;
		}

		Document minT = (Document) doc.get("minT");

		Document maxT = (Document) doc.get("maxT");

		Document maxP = (Document) doc.get("maxP");
		doc = c("monteCarloSimulate",domain).aggregate(Domain.getJQ(domain, "查询-MCS-工期概率").set("project_id", project_id).set("T", maxP.get("t")).array())
				.first();

		ArrayList<List<Double>> result = new ArrayList<List<Double>>();
		result.add(Arrays.asList(((Number) minT.get("t")).doubleValue(), ((Number) minT.get("p")).doubleValue()));
		result.add(Arrays.asList(((Number) maxT.get("t")).doubleValue(), 1d));
		result.add(Arrays.asList(((Number) maxP.get("t")).doubleValue(), ((Number) doc.get("prob")).doubleValue()));

		return result;
	}

	@Override
	public Document getRiskProximityChart(ObjectId project_id, String domain) {
		ArrayList<List<Object>> data = getRiskProximityData(project_id,domain);
		String size = "function (data){return Math.sqrt(data[2])*8;}";
		return Domain.getJQ(domain, "图表-风险临近性可检测性").set("data", data).set("size", size).doc();
	}

	@Override
	public Document getRiskProximityChart2(ObjectId project_id, String domain) {
		ArrayList<List<Object>> data = getRiskProximityData(project_id,domain);
		String size = "function (data){return Math.sqrt(data[2])*8;}";
		return Domain.getJQ(domain, "图表-风险临近性可检测性2").set("data", data).set("size", size).doc();
	}

	private ArrayList<List<Object>> getRiskProximityData(ObjectId project_id, String domain) {
		ArrayList<List<Object>> data = new ArrayList<List<Object>>();
		c("rbsItem", domain).find(new Document("project_id", project_id)).forEach((Document d) -> {
			Double _i = d.getDouble("infValue");
			Double _p = d.get("probability", 50d);
			int _d = 10 - d.getInteger("detectable", 1);// 已经确认为int类型，由小到大，1为绝对探测，10为无法探测
			if (_i != null && _p != null) {
				Double rci = ((Number) _i).doubleValue() * ((Number) _p).doubleValue();
				Integer urgency = (int) ((d.getDate("forecast").getTime() - Calendar.getInstance().getTimeInMillis())
						/ (24 * 60 * 60 * 1000));
				data.add(Arrays.asList(urgency, _d, rci, d.get("name")));
			}
		});
		return data;
	}

}
