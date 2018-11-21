package com.bizvisionsoft.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.bizvisionsoft.service.tools.Check;
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
		// 根据数据类型构建所需JQ
		if ("并列".equals(seriesType)) {
			((List<Document>) condition.get("input")).forEach((Document doc) -> {
				match.append("resource_id", new Document("$in", addChildResource(doc)));
				if ("标准".equals(showData)) {
					Object[] resourceData = appendSeries(series, match, group_id, xAxisData, legendData, dataType, "basicQty",
							"basicAmount", doc.getString("label") + "计划标准", doc.getString("label") + "实际标准", doc.getString("label") + "计划",
							doc.getString("label") + "实际", markLineData);
					if (resourceData != null) {
						appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, basicAggWorkTimeData,
								basicAggAmountData, resourceData);
					}
				} else if ("加班".equals(showData)) {
					Object[] resourceData = appendSeries(series, match, group_id, xAxisData, legendData, dataType, "overTimeQty",
							"overTimeAmount", doc.getString("label") + "计划加班", doc.getString("label") + "实际加班",
							doc.getString("label") + "计划", doc.getString("label") + "实际", markLineData);
					if (resourceData != null) {
						appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, overTimeAggWorkTimeData,
								overTimeAggAmountData, resourceData);
					}
				} else if ("汇总".equals(showData)) {
					appendSeries(series, match, group_id, xAxisData, legendData, dataType, "totalQty", "totalAmount",
							doc.getString("label") + "计划", doc.getString("label") + "实际", doc.getString("label") + "计划",
							doc.getString("label") + "实际", markLineData);
					if (dataType.contains("实际")) {
						Object[] resourceData = getResourceData("resourceActual", "$actualBasicQty", "$actualOverTimeQty", "basicQty",
								"basicAmount", xAxisData, match, group_id);
						if (resourceData != null) {
							appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, basicAggWorkTimeData,
									basicAggAmountData, resourceData);
						}
						resourceData = getResourceData("resourceActual", "$actualBasicQty", "$actualOverTimeQty", "overTimeQty",
								"overTimeAmount", xAxisData, match, group_id);
						if (resourceData != null) {
							appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, overTimeAggWorkTimeData,
									overTimeAggAmountData, resourceData);
						}
					}
				} else {
					Object[] resourceData = appendSeries(series, match, group_id, xAxisData, legendData, dataType, "basicQty",
							"basicAmount", doc.getString("label") + "计划标准", doc.getString("label") + "实际标准", doc.getString("label") + "计划",
							doc.getString("label") + "实际", markLineData);
					if (resourceData != null) {
						appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, basicAggWorkTimeData,
								basicAggAmountData, resourceData);
					}
					resourceData = appendSeries(series, match, group_id, xAxisData, legendData, dataType, "overTimeQty", "overTimeAmount",
							doc.getString("label") + "计划加班", doc.getString("label") + "实际加班", doc.getString("label") + "计划",
							doc.getString("label") + "实际", markLineData);
					if (resourceData != null) {
						appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, overTimeAggWorkTimeData,
								overTimeAggAmountData, resourceData);
					}
				}
			});
		} else {
			List<ObjectId> resourceIds = new ArrayList<>();
			((List<Document>) condition.get("input")).forEach((Document doc) -> resourceIds.addAll(addChildResource(doc)));
			match.append("resource_id", new Document("$in", resourceIds));
			if ("标准".equals(showData)) {
				Object[] resourceData = appendSeries(series, match, group_id, xAxisData, legendData, dataType, "basicQty", "basicAmount",
						"计划标准", "实际标准", "计划", "实际", markLineData);
				if (resourceData != null) {
					appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, basicAggWorkTimeData, basicAggAmountData,
							resourceData);
				}
			} else if ("加班".equals(showData)) {
				Object[] resourceData = appendSeries(series, match, group_id, xAxisData, legendData, dataType, "overTimeQty",
						"overTimeAmount", "计划加班", "实际加班", "计划", "实际", markLineData);
				if (resourceData != null) {
					appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, overTimeAggWorkTimeData,
							overTimeAggAmountData, resourceData);
				}
			} else if ("汇总".equals(showData)) {
				appendSeries(series, match, group_id, xAxisData, legendData, dataType, "totalQty", "totalAmount", "计划", "实际", "计划", "实际",
						markLineData);
				if (dataType.contains("实际")) {
					Object[] resourceData = getResourceData("resourceActual", "$actualBasicQty", "$actualOverTimeQty", "basicQty",
							"basicAmount", xAxisData, match, group_id);
					if (resourceData != null) {
						appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, basicAggWorkTimeData,
								basicAggAmountData, resourceData);
					}
					resourceData = getResourceData("resourceActual", "$actualBasicQty", "$actualOverTimeQty", "overTimeQty",
							"overTimeAmount", xAxisData, match, group_id);
					if (resourceData != null) {
						appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, overTimeAggWorkTimeData,
								overTimeAggAmountData, resourceData);
					}
				}
			} else {
				Object[] resourceData = appendSeries(series, match, group_id, xAxisData, legendData, dataType, "basicQty", "basicAmount",
						"计划标准", "实际标准", "计划", "实际", markLineData);
				if (resourceData != null) {
					appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, basicAggWorkTimeData, basicAggAmountData,
							resourceData);
				}
				resourceData = appendSeries(series, match, group_id, xAxisData, legendData, dataType, "overTimeQty", "overTimeAmount",
						"计划加班", "实际加班", "计划", "实际", markLineData);
				if (resourceData != null) {
					appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, overTimeAggWorkTimeData,
							overTimeAggAmountData, resourceData);
				}
			}

		}
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

	private void appendAggregateData(String aggregateType, List<Double> totalAggWorkTimeData, List<Double> totalAggAmountData,
			List<Double> aggWorkTimeData, List<Double> aggAmountData, Object[] resourceData) {
		if ("总计".equals(aggregateType)) {
			addAggData(totalAggWorkTimeData, totalAggAmountData, resourceData);
		} else if ("标准加班".equals(aggregateType)) {
			addAggData(aggWorkTimeData, aggAmountData, resourceData);
		}
	}

	@SuppressWarnings("unchecked")
	private void addAggData(List<Double> aggWorkTimeData, List<Double> aggAmountData, Object[] resourceData) {
		Double wd = 0d;
		Double ad = 0d;
		if (aggWorkTimeData.size() > 0) {
			for (int i = 0; i < aggWorkTimeData.size(); i++) {
				Double d = ((List<Double>) resourceData[0]).get(i);
				if (d != null)
					wd += d;

				aggWorkTimeData.set(i, wd + aggWorkTimeData.get(i));

				d = ((List<Double>) resourceData[1]).get(i);
				if (d != null)
					ad += d;
				aggAmountData.set(i, ad + aggAmountData.get(i));
			}
		} else {
			for (Double d : ((List<Double>) resourceData[0])) {
				if (d != null)
					wd += d;
				aggWorkTimeData.add(wd);
			}
			for (Double d : ((List<Double>) resourceData[1])) {
				if (d != null)
					ad += d;
				aggAmountData.add(ad);
			}
		}
	}

	/**
	 * 添加图表的 series,并返回实际工时数据和实际金额数据
	 * 
	 * @param series
	 * @param match
	 * @param group_id
	 * @param xAxisData
	 * @param legendData
	 * @param dataType
	 * @param qtyName
	 * @param amountName
	 * @param planName
	 * @param actualName
	 * @param planStack
	 * @param actualStack
	 * @return 实际工时数据和实际金额数据
	 */
	private Object[] appendSeries(List<Document> series, Document match, Document group_id, List<String> xAxisData, List<String> legendData,
			List<String> dataType, String qtyName, String amountName, String planName, String actualName, String planStack,
			String actualStack, Document markLineData) {
		if (dataType.contains("计划")) {
			// 添加计划图表数据
			Object[] resourceData = getResourceData("resourcePlan", "$planBasicQty", "$planOverTimeQty", qtyName, amountName, xAxisData,
					match, group_id);
			// 构建工时和金额图表
			series.add(new JQ("图表-资源图表-工时").set("name", planName + "工时").set("stack", planStack).set("data", resourceData[0])
					.set("markLineData", new Document()).doc());
			legendData.add(planName + "工时");
			series.add(new JQ("图表-资源图表-金额").set("name", planName + "金额").set("data", resourceData[1]).doc());
			legendData.add(planName + "金额");
		}
		if (dataType.contains("实际")) {
			// 添加实际图表数据
			Object[] resourceData = getResourceData("resourceActual", "$actualBasicQty", "$actualOverTimeQty", qtyName, amountName,
					xAxisData, match, group_id);

			// 构建工时和金额图表
			series.add(new JQ("图表-资源图表-工时").set("name", actualName + "工时").set("stack", actualStack).set("data", resourceData[0])
					.set("markLineData", markLineData).doc());
			legendData.add(actualName + "工时");
			series.add(new JQ("图表-资源图表-金额").set("name", actualName + "金额").set("data", resourceData[1]).doc());
			legendData.add(actualName + "金额");
			return resourceData;
		}
		return null;
	}

	private Object[] getResourceData(String collectionName, String basicQty, String overTimeQty, String qtyName, String amountName,
			List<String> xAxisData, Document match, Document group_id) {
		Map<String, Double> qty = new HashMap<String, Double>();
		Map<String, Double> amount = new HashMap<String, Double>();
		List<Double> workTimeData = new ArrayList<Double>();
		List<Double> amountData = new ArrayList<Double>();
		// 查询获取数据
		c(collectionName).aggregate(new JQ("查询-资源图表").set("$basicQty", basicQty).set("$overTimeQty", overTimeQty).set("match", match)
				.set("group_id", group_id).array()).forEach((Document doc) -> {
					qty.put(doc.getString("_id"), doc.getDouble(qtyName));
					amount.put(doc.getString("_id"), doc.getDouble(amountName) / 10000);
				});
		for (String xAxis : xAxisData) {
			workTimeData.add(qty.get(xAxis));
			amountData.add(amount.get(xAxis) == null ? 0d : amount.get(xAxis));
		}
		return new Object[] { workTimeData, amountData };
	}

	/**
	 * 根据传入的查询的数据，获取其从属其的资源id
	 * 
	 * @param doc
	 * @return
	 */
	private List<ObjectId> addChildResource(Document doc) {
		List<ObjectId> result = new ArrayList<>();
		if (Check.equals(ResourceType.class.getName(), doc.getString("type"))) {
			result = getResourceId(doc.getObjectId("_id"), ((Document) doc.get("meta")).getObjectId("org_id"));
		} else if (Check.equals(Organization.class.getName(), doc.getString("type"))) {
			result = getResourceId(doc.getObjectId("_id"));
		} else {
			result = Arrays.asList(doc.getObjectId("_id"));
		}
		doc.put("childResourceIds", result);
		return result;
	}

	/**
	 * 获取组织及其下级组织的所有资源
	 * 
	 * @param org_id
	 * @return
	 */
	private List<ObjectId> getResourceId(ObjectId org_id) {
		return getResourceId(
				new Document("org_id", new Document("$in", getDesentItems(Arrays.asList(org_id), "organization", "parent_id"))));
	}

	/**
	 * 获取资源类型在组织下的所有资源
	 * 
	 * @param resourceType_id
	 * @param org_id
	 * @return
	 */
	private List<ObjectId> getResourceId(ObjectId resourceType_id, ObjectId org_id) {
		return getResourceId(new Document("org_id", new Document("$in", getDesentItems(Arrays.asList(org_id), "organization", "parent_id")))
				.append("resourceType_id", resourceType_id));
	}

	private List<ObjectId> getResourceId(Document filter) {
		List<ObjectId> result = new ArrayList<>();
		result.addAll(c("equipment").distinct("_id", filter, ObjectId.class).into(new ArrayList<>()));
		result.addAll(c("user").distinct("_id", filter.append("resourceType_id", new BasicDBObject("$ne", null)), ObjectId.class)
				.into(new ArrayList<>()));
		return result;
	}

}
