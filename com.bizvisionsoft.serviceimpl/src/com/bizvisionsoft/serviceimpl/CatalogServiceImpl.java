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

	@Override
	public Document createResourcePlanAndUserageChart(Document condition) {

		List<Document> series = new ArrayList<Document>();

		Document option = (Document) condition.get("option");
		// 获取查询时间，并根据时间构建查询语句
		List<?> dateRange = (List<?>) option.get("dateRange");

		Document match = new Document();
		Date start = (Date) dateRange.get(0);
		match.append("id", new Document("$gte", start));
		Date end = (Date) dateRange.get(1);
		match.append("id", new Document("$lte", end));

		// 获取时间类型，并根据时间类型构建group的_id
		Document group_id = new Document();
		String dateType = option.getString("dateType");

		// 根据起止时间构建xAxis数据
		List<String> xAxisData = new ArrayList<String>();
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);

		SimpleDateFormat sdf;
		if ("日".equals(dateType)) {
			group_id.append("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$id"));
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
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
		// 获取数据类型，并根据数据类型构建所需JQ
		List<String> dataType = (List<String>) option.get("dataType");

		String seriesType = option.getString("seriesType");
		if ("并列".equals(seriesType)) {

		} else if ("叠加".equals(seriesType)) {

		} else {
			List<ObjectId> resourceIds = new ArrayList<>();
			((List<Document>) condition.get("input")).forEach(doc -> resourceIds.addAll(addChildResource(doc)));
			match.append("resource_id", new Document("$in", resourceIds));
			if (dataType.contains("计划")) {
				Map<String, Double> totalQty = new HashMap<String, Double>();
				Map<String, Double> totalAmount = new HashMap<String, Double>();
				c("resourcePlan").aggregate(new JQ("查询-资源图表").set("$basicQty", "$planBasicQty").set("$overTimeQty", "$planOverTimeQty")
						.set("match", match).set("group_id", group_id).array()).forEach((Document doc) -> {
							totalQty.put(doc.getString("_id"), doc.getDouble("totalQty"));
							totalAmount.put(doc.getString("_id"), doc.getDouble("totalAmount"));
						});
				List<Double> workTimeData = new ArrayList<Double>();
				List<Double> amountData = new ArrayList<Double>();
				for (String xAxis : xAxisData) {
					workTimeData.add(totalQty.get(xAxis));
					amountData.add(totalAmount.get(xAxis));
				}
				series.add(new JQ("图表-资源图表-工时").set("name", "计划工时").set("stack", "计划工时").set("color", "#FF5722").set("data", workTimeData)
						.set("markLineData", new Document()).doc());
				series.add(new JQ("图表-资源图表-金额").set("name", "计划金额").set("data", amountData).doc());
			}
			if (dataType.contains("实际")) {
				Map<String, Double> totalQty = new HashMap<String, Double>();
				Map<String, Double> totalAmount = new HashMap<String, Double>();
				c("resourceActual").aggregate(new JQ("查询-资源图表").set("$basicQty", "$actualBasicQty")
						.set("$overTimeQty", "$actualOverTimeQty").set("match", match).set("group_id", group_id).array())
						.forEach((Document doc) -> {
							totalQty.put(doc.getString("_id"), doc.getDouble("totalQty"));
							totalAmount.put(doc.getString("_id"), doc.getDouble("totalAmount"));
						});
				List<Double> workTimeData = new ArrayList<Double>();
				List<Double> amountData = new ArrayList<Double>();
				for (String xAxis : xAxisData) {
					workTimeData.add(totalQty.get(xAxis));
					amountData.add(totalAmount.get(xAxis));
				}
				series.add(new JQ("图表-资源图表-工时").set("name", "计划工时").set("stack", "计划工时").set("color", "#FF5722").set("data", workTimeData)
						.set("markLineData", new Document()).doc());
				series.add(new JQ("图表-资源图表-金额").set("name", "计划金额").set("data", amountData).doc());
			}

		}
		// TODO Auto-generated method stub

		List<Double> aggregateData = new ArrayList<Double>();
		JQ aggregateJQ = new JQ("图表-资源图表-累计工时").set("data", aggregateData);

		List<Double> workTimeData = new ArrayList<Double>();
		JQ workTimeJQ = new JQ("图表-资源图表-工时").set("name", "").set("stack", "").set("data", workTimeData).set("markLineData", new Document());

		List<Double> amountData = new ArrayList<Double>();
		JQ aountJQ = new JQ("图表-资源图表-金额").set("name", "").set("data", amountData);

		JQ jq = new JQ("图表-资源图表").set("title", "111").set("xAxisData", xAxisData).set("series", series);
		System.out.println(jq.doc().toJson());
		return jq.doc();
	}

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
