package com.bizvisionsoft.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.CatalogService;
import com.bizvisionsoft.service.model.Catalog;
import com.bizvisionsoft.service.model.Equipment;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class CatalogServiceImpl extends BasicServiceImpl implements CatalogService {

	/**
	 * 该人员所属的组织
	 */
	@Override
	public List<Catalog> listOrganizationCatalog(String userId) {
		List<Bson> pipeline = Arrays.asList(Aggregates.match(new Document("userId", userId)),
				Aggregates.lookup("organization", "org_id", "_id", "org"), Aggregates.unwind("$org"));
		return c("user").aggregate(pipeline).map(d -> (Document) d.get("org")).map(this::org2Catalog).into(new ArrayList<>());
	}

	@Override
	public List<Catalog> listOrganizationSubCatalog(Catalog parent) {
		List<Catalog> result = new ArrayList<Catalog>();
		if (typeEquals(parent, Organization.class)) {
			listSubOrg(parent._id, result);
			listResourceType(parent._id, "user", result);
			listResourceType(parent._id, "equipment", result);
		} else if (typeEquals(parent, ResourceType.class)) {
			ObjectId resourceType_id = parent._id;
			Object org_id = parent.meta.get("org_id");
			listHRResource(org_id, resourceType_id, result);
			listEQResource(org_id, resourceType_id, result);
		}
		return result;
	}

	@Override
	public long countOrganizationSubCatalog(Catalog parent) {
		// 如果parent是组织，获取下级组织和资源类型
		long count = 0;
		if (typeEquals(parent, Organization.class)) {
			count += countSubOrg(parent._id);
			count += countResourceType(parent._id, "user");
			count += countResourceType(parent._id, "equipment");
		} else if (typeEquals(parent, ResourceType.class)) {
			ObjectId resourceType_id = parent._id;
			Object org_id = parent.meta.get("org_id");
			count += countResource(org_id, resourceType_id, "user");
			count += countResource(org_id, resourceType_id, "equipment");
		}
		return count;
	}

	private long countResourceType(ObjectId org_id, String collection) {
		List<Bson> p = new ArrayList<>();
		p.addAll(new JQ("查询-组织-资源类型").set("org_id", org_id).array());
		p.add(Aggregates.count());
		return Optional.ofNullable(c(collection).aggregate(p).first()).map(m -> ((Number) m.get("count")).longValue()).orElse(0l);
	}

	private List<Catalog> listResourceType(ObjectId org_id, String collection, List<Catalog> into) {
		List<Bson> p = new JQ("查询-组织-资源类型").set("org_id", org_id).array();
		return c(collection).aggregate(p)
				.map(d -> ((Document) d.get("resourceType")).append("org_id", ((Document) d.get("_id")).get("org_id")))
				.map(this::resourceType2Catalog).into(into);
	}

	private long countSubOrg(ObjectId org_id) {
		return c("organization").countDocuments(new Document("parent_id", org_id));
	}

	private List<Catalog> listSubOrg(ObjectId org_id, List<Catalog> into) {
		return c("organization").find(new Document("parent_id", org_id)).map(this::org2Catalog).into(into);
	}

	private List<Catalog> listHRResource(Object org_id, ObjectId resourceType_id, List<Catalog> into) {
		return c("user").find(new Document("org_id", org_id).append("resourceType_id", resourceType_id)).map(this::user2Catalog).into(into);
	}

	private List<Catalog> listEQResource(Object org_id, ObjectId resourceType_id, List<Catalog> into) {
		return c("equipment").find(new Document("org_id", org_id).append("resourceType_id", resourceType_id)).map(this::equipment2Catalog)
				.into(into);
	}

	private long countResource(Object org_id, ObjectId resourceType_id, String collection) {
		return c(collection).countDocuments(new Document("org_id", org_id).append("resourceType_id", resourceType_id));
	}

	private Catalog setType(Catalog c, Class<?> clas) {
		c.type = clas.getName();
		return c;
	}

	private boolean typeEquals(Catalog c, Class<?> clas) {
		return clas.getName().equals(c.type);
	}

	private Catalog org2Catalog(Document doc) {
		Catalog c = new Catalog();
		c._id = doc.getObjectId("_id");
		c.label = doc.getString("fullName");
		setType(c, Organization.class);
		c.icon = "img/org_c.svg";
		c.meta = doc;
		return c;
	}

	private Catalog resourceType2Catalog(Document doc) {
		Catalog c = new Catalog();
		c._id = doc.getObjectId("_id");
		c.label = doc.getString("name");
		setType(c, ResourceType.class);
		c.icon = "img/resource_c.svg";
		c.meta = doc;
		return c;
	}

	private Catalog user2Catalog(Document doc) {
		Catalog c = new Catalog();
		c._id = doc.getObjectId("_id");
		c.label = doc.getString("name");
		setType(c, User.class);
		c.icon = "img/user_c.svg";
		c.meta = doc;
		return c;
	}

	private Catalog equipment2Catalog(Document doc) {
		Catalog c = new Catalog();
		c._id = doc.getObjectId("_id");
		c.label = doc.getString("name");
		setType(c, Equipment.class);
		c.icon = "img/equipment_c.svg";
		c.meta = doc;
		return c;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Document createResourcePlanAndUserageChart(Document condition) {

		List<Document> series = new ArrayList<Document>();

		Document option = (Document) condition.get("option");
		List<?> dateRange = (List<?>) option.get("dateRange");
		String dateType = option.getString("dateType");
		List<String> dataType = (List<String>) option.get("dataType");
		String seriesType = option.getString("seriesType");
		String showData = option.getString("showData");
		String aggregateType = option.getString("aggregateType");

		// 并根据时间构建查询语句
		Document match = new Document();
		Date start = (Date) dateRange.get(0);
		Date end = (Date) dateRange.get(1);
		match.append("$and", Arrays.asList(new Document("id", new Document("$gte", start)), new Document("id", new Document("$lte", end))));

		// 根据时间类型和起止时间，构建group的_id和xAxis数据
		Document group_id = new Document();
		List<String> xAxisData = new ArrayList<String>();
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);

		Document markLineData = new Document();

		SimpleDateFormat sdf;
		if ("日".equals(dateType)) {
			group_id.append("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$id"));
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
			// TODO 需要根据所选资源的资源日历进行设置
			markLineData.append("data", Arrays.asList(new Document("yAxis", 8).append("name", "aaaa")));
		} else if ("月".equals(dateType)) {
			group_id.append("$dateToString", new Document("format", "%Y-%m").append("date", "$id"));
			sdf = new SimpleDateFormat("yyyy-MM");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.MONTH, 1);
			}
		} else {
			group_id.append("$dateToString", new Document("format", "%Y").append("date", "$id"));
			sdf = new SimpleDateFormat("yyyy");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.YEAR, 1);
			}
		}
		List<Double> totalAggWorkTimeData = new ArrayList<Double>();
		List<Double> totalAggAmountData = new ArrayList<Double>();
		List<Double> basicAggWorkTimeData = new ArrayList<Double>();
		List<Double> basicAggAmountData = new ArrayList<Double>();
		List<Double> overTimeAggWorkTimeData = new ArrayList<Double>();
		List<Double> overTimeAggAmountData = new ArrayList<Double>();

		List<String> legendData = new ArrayList<String>();
		// 根据系列类型构建series
		if ("并列".equals(seriesType)) {
			((List<Document>) condition.get("input")).forEach((Document doc) -> {
				match.append("resource_id", new Document("$in", addChildResource(doc)));
				appendSeries(series, dataType, showData, aggregateType, match, group_id, xAxisData, markLineData, totalAggWorkTimeData,
						totalAggAmountData, basicAggWorkTimeData, basicAggAmountData, overTimeAggWorkTimeData, overTimeAggAmountData,
						legendData, doc.getString("label"));
			});
		} else {
			List<ObjectId> resourceIds = new ArrayList<>();
			((List<Document>) condition.get("input")).forEach((Document doc) -> resourceIds.addAll(addChildResource(doc)));
			match.append("resource_id", new Document("$in", resourceIds));
			appendSeries(series, dataType, showData, aggregateType, match, group_id, xAxisData, markLineData, totalAggWorkTimeData,
					totalAggAmountData, basicAggWorkTimeData, basicAggAmountData, overTimeAggWorkTimeData, overTimeAggAmountData,
					legendData, "");

		}
		// 根据累计值类型构建series
		if ("总计".equals(aggregateType)) {
			series.add(new JQ("图表-资源图表-累计工时").set("name", "累计工时").set("data", totalAggWorkTimeData).doc());
			legendData.add("累计工时");
			series.add(new JQ("图表-资源图表-累计金额").set("name", "累计金额").set("data", totalAggAmountData).doc());
			legendData.add("累计金额");
		} else if ("标准加班".equals(aggregateType)) {
			series.add(new JQ("图表-资源图表-累计工时").set("name", "累计标准工时").set("data", basicAggWorkTimeData).doc());
			legendData.add("累计标准工时");
			series.add(new JQ("图表-资源图表-累计金额").set("name", "累计标准金额").set("data", basicAggAmountData).doc());
			legendData.add("累计标准金额");
			series.add(new JQ("图表-资源图表-累计工时").set("name", "累计加班工时").set("data", overTimeAggWorkTimeData).doc());
			legendData.add("累计加班工时");
			series.add(new JQ("图表-资源图表-累计金额").set("name", "累计加班金额").set("data", overTimeAggAmountData).doc());
			legendData.add("累计加班金额");
		}

		// 生成资源图表
		// TODO legendData和series的顺序需要调整
		JQ jq = new JQ("图表-资源图表").set("title", "").set("legendData", legendData).set("xAxisData", xAxisData).set("series", series);
		System.out.println(jq.doc().toJson());
		return jq.doc();
	}

	@SuppressWarnings("unchecked")
	private void appendSeries(List<Document> series, List<String> dataType, String showData, String aggregateType, Document match,
			Document group_id, List<String> xAxisData, Document markLineData, List<Double> totalAggWorkTimeData,
			List<Double> totalAggAmountData, List<Double> basicAggWorkTimeData, List<Double> basicAggAmountData,
			List<Double> overTimeAggWorkTimeData, List<Double> overTimeAggAmountData, List<String> legendData, String label) {
		if (dataType.contains("计划")) {
			// 添加计划图表数据
			Object[] resourceData = getResourceData("resourcePlan", "$planBasicQty", "$planOverTimeQty", match, group_id, xAxisData);
			if ("标准".equals(showData)) {
				series.add(new JQ("图表-资源图表-工时").set("name", label + "计划标准" + "工时").set("stack", label + "计划").set("data", resourceData[0])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "计划标准" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "计划标准" + "金额").set("data", resourceData[3]).doc());
				legendData.add(label + "计划标准" + "金额");
			} else if ("加班".equals(showData)) {
				series.add(new JQ("图表-资源图表-工时").set("name", label + "计划加班" + "工时").set("stack", label + "计划").set("data", resourceData[1])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "计划加班" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "计划加班" + "金额").set("data", resourceData[4]).doc());
				legendData.add(label + "计划加班" + "金额");
			} else if ("汇总".equals(showData)) {
				series.add(new JQ("图表-资源图表-工时").set("name", label + "计划" + "工时").set("stack", label + "计划").set("data", resourceData[2])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "计划" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "计划" + "金额").set("data", resourceData[5]).doc());
				legendData.add(label + "计划" + "金额");
			} else {
				series.add(new JQ("图表-资源图表-工时").set("name", label + "计划标准" + "工时").set("stack", label + "计划").set("data", resourceData[0])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "计划标准" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "计划标准" + "金额").set("data", resourceData[3]).doc());
				legendData.add(label + "计划标准" + "金额");

				series.add(new JQ("图表-资源图表-工时").set("name", label + "计划加班" + "工时").set("stack", label + "计划").set("data", resourceData[1])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "计划加班" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "计划加班" + "金额").set("data", resourceData[4]).doc());
				legendData.add(label + "计划加班" + "金额");
			}
		}
		if (dataType.contains("实际")) {
			// 添加实际图表数据
			Object[] resourceData = getResourceData("resourceActual", "$actualBasicQty", "$actualOverTimeQty", match, group_id, xAxisData);
			if ("标准".equals(showData)) {
				series.add(new JQ("图表-资源图表-工时").set("name", label + "实际标准" + "工时").set("stack", label + "实际").set("data", resourceData[0])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "实际标准" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "实际标准" + "金额").set("data", resourceData[3]).doc());
				legendData.add(label + "实际标准" + "金额");

				appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, basicAggWorkTimeData, basicAggAmountData,
						(List<Double>) resourceData[0], (List<Double>) resourceData[3]);
			} else {
				if ("加班".equals(showData)) {
					series.add(new JQ("图表-资源图表-工时").set("name", label + "实际加班" + "工时").set("stack", label + "实际")
							.set("data", resourceData[1]).set("markLineData", new Document()).doc());
					legendData.add(label + "实际加班" + "工时");
					series.add(new JQ("图表-资源图表-金额").set("name", label + "实际加班" + "金额").set("data", resourceData[4]).doc());
					legendData.add(label + "实际加班" + "金额");

					appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, overTimeAggWorkTimeData,
							overTimeAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
				} else if ("汇总".equals(showData)) {
					series.add(new JQ("图表-资源图表-工时").set("name", label + "实际" + "工时").set("stack", label + "实际").set("data", resourceData[2])
							.set("markLineData", new Document()).doc());
					legendData.add(label + "实际" + "工时");
					series.add(new JQ("图表-资源图表-金额").set("name", label + "实际" + "金额").set("data", resourceData[5]).doc());
					legendData.add(label + "实际" + "金额");

					appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, basicAggWorkTimeData, basicAggAmountData,
							(List<Double>) resourceData[0], (List<Double>) resourceData[3]);

					appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, overTimeAggWorkTimeData,
							overTimeAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
				} else {
					series.add(new JQ("图表-资源图表-工时").set("name", label + "实际标准" + "工时").set("stack", label + "实际")
							.set("data", resourceData[0]).set("markLineData", new Document()).doc());
					legendData.add(label + "实际标准" + "工时");
					series.add(new JQ("图表-资源图表-金额").set("name", label + "实际标准" + "金额").set("data", resourceData[3]).doc());
					legendData.add(label + "实际标准" + "金额");
					appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, basicAggWorkTimeData, basicAggAmountData,
							(List<Double>) resourceData[0], (List<Double>) resourceData[3]);

					series.add(new JQ("图表-资源图表-工时").set("name", label + "实际加班" + "工时").set("stack", label + "实际")
							.set("data", resourceData[1]).set("markLineData", new Document()).doc());
					legendData.add(label + "实际加班" + "工时");
					series.add(new JQ("图表-资源图表-金额").set("name", label + "实际加班" + "金额").set("data", resourceData[4]).doc());
					legendData.add(label + "实际加班" + "金额");
					appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, overTimeAggWorkTimeData,
							overTimeAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
				}
			}
		}

	}

	private void appendAggregateData(String aggregateType, List<Double> totalAggWorkTimeData, List<Double> totalAggAmountData,
			List<Double> aggWorkTimeData, List<Double> aggAmountData, List<Double> resourceWorkTimeData, List<Double> resourceAmountData) {
		if ("总计".equals(aggregateType)) {
			addAggData(totalAggWorkTimeData, totalAggAmountData, resourceWorkTimeData, resourceAmountData);
		} else if ("标准加班".equals(aggregateType)) {
			addAggData(aggWorkTimeData, aggAmountData, resourceWorkTimeData, resourceAmountData);
		}
	}

	private void addAggData(List<Double> aggWorkTimeData, List<Double> aggAmountData, List<Double> resourceWorkTimeData,
			List<Double> resourceAmountData) {
		Double wd = 0d;
		Double ad = 0d;
		if (aggWorkTimeData.size() > 0) {
			for (int i = 0; i < aggWorkTimeData.size(); i++) {
				Double d = resourceWorkTimeData.get(i);
				if (d != null)
					wd += d;

				aggWorkTimeData.set(i, wd + aggWorkTimeData.get(i));

				d = resourceAmountData.get(i);
				if (d != null)
					ad += d;
				aggAmountData.set(i, ad + aggAmountData.get(i));
			}
		} else {
			for (Double d : resourceWorkTimeData) {
				if (d != null)
					wd += d;
				aggWorkTimeData.add(wd);
			}
			for (Double d : resourceAmountData) {
				if (d != null)
					ad += d;
				aggAmountData.add(ad);
			}
		}
	}

	/**
	 * 查询资源图表数据，返回List<Double>构成的Array
	 * 
	 * @param collectionName
	 * @param basicQtyName
	 * @param overTimeQtyName
	 * @param match
	 * @param group_id
	 * @param xAxisData
	 * @return 元素1：标准数量，元素2：加班数量，元素3：数量合计，元素4：标准金额，元素5：加班金额，元素6：金额合计.
	 */
	private Object[] getResourceData(String collectionName, String basicQtyName, String overTimeQtyName, Document match, Document group_id,
			List<String> xAxisData) {
		HashMap<String, Double> basicQty = new HashMap<String, Double>();
		HashMap<String, Double> overTimeQty = new HashMap<String, Double>();
		HashMap<String, Double> totalQty = new HashMap<String, Double>();
		HashMap<String, Double> basicAmount = new HashMap<String, Double>();
		HashMap<String, Double> overTimeAmount = new HashMap<String, Double>();
		HashMap<String, Double> totalAmount = new HashMap<String, Double>();
		// 查询获取数据
		c(collectionName).aggregate(new JQ("查询-资源图表").set("$basicQty", basicQtyName).set("$overTimeQty", overTimeQtyName)
				.set("match", match).set("group_id", group_id).array()).forEach((Document doc) -> {
					basicQty.put(doc.getString("_id"), doc.getDouble("basicQty"));
					overTimeQty.put(doc.getString("_id"), doc.getDouble("overTimeQty"));
					totalQty.put(doc.getString("_id"), doc.getDouble("totalQty"));
					basicAmount.put(doc.getString("_id"), doc.getDouble("basicAmount"));
					overTimeAmount.put(doc.getString("_id"), doc.getDouble("overTimeAmount"));
					totalAmount.put(doc.getString("_id"), doc.getDouble("totalAmount"));
				});
		List<Double> basicQtys = new ArrayList<Double>();
		List<Double> overTimeQtys = new ArrayList<Double>();
		List<Double> totalQtys = new ArrayList<Double>();
		List<Double> basicAmounts = new ArrayList<Double>();
		List<Double> overTimeAmounts = new ArrayList<Double>();
		List<Double> totalAmounts = new ArrayList<Double>();

		for (String xAxis : xAxisData) {
			basicQtys.add(basicQty.get(xAxis));
			overTimeQtys.add(overTimeQty.get(xAxis));
			totalQtys.add(totalQty.get(xAxis));

			basicAmounts.add(basicAmount.get(xAxis) == null ? 0d : (basicAmount.get(xAxis) / 10000));
			overTimeAmounts.add(overTimeAmount.get(xAxis) == null ? 0d : (overTimeAmount.get(xAxis) / 10000));
			totalAmounts.add(totalAmount.get(xAxis) == null ? 0d : (totalAmount.get(xAxis) / 10000));
		}

		return new Object[] { basicQtys, overTimeQtys, totalQtys, basicAmounts, overTimeAmounts, totalAmounts };
	}

	/**
	 * 根据传入的查询的数据，获取其从属其的资源id
	 * 
	 * @param doc
	 * @return
	 */
	private List<ObjectId> addChildResource(Document doc) {
		final List<ObjectId> result = new ArrayList<>();
		if (ResourceType.class.getName().equals(doc.getString("type"))) {
			List<Catalog> catalogs = new ArrayList<>();
			listHRResource(((Document) doc.get("meta")).getObjectId("org_id"), doc.getObjectId("_id"), catalogs);
			listEQResource(((Document) doc.get("meta")).getObjectId("org_id"), doc.getObjectId("_id"), catalogs);
			catalogs.stream().map(c -> result.add(c._id));
		} else if (Organization.class.getName().equals(doc.getString("type"))) {
			result.addAll(getResourceId(doc.getObjectId("_id")));
		} else {
			result.add(doc.getObjectId("_id"));
		}
		doc.put("childResourceIds", result);
		return result;
	}

	/**
	 * 获取组织及其下级组织的所有资源id
	 * 
	 * @param org_id
	 * @return
	 */
	private List<ObjectId> getResourceId(ObjectId org_id) {
		return getResourceId(
				new Document("org_id", new Document("$in", getDesentItems(Arrays.asList(org_id), "organization", "parent_id"))));
	}

	private List<ObjectId> getResourceId(Document filter) {
		List<ObjectId> result = new ArrayList<>();
		result.addAll(c("equipment").distinct("_id", filter, ObjectId.class).into(new ArrayList<>()));
		result.addAll(c("user").distinct("_id", filter.append("resourceType_id", new BasicDBObject("$ne", null)), ObjectId.class)
				.into(new ArrayList<>()));
		return result;
	}

}
