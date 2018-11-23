package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.CatalogService;
import com.bizvisionsoft.service.model.Catalog;
import com.bizvisionsoft.service.model.Equipment;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.bizvisionsoft.serviceimpl.renderer.CatalogRenderer;
import com.mongodb.Function;
import com.mongodb.client.model.Aggregates;

public class CatalogServiceImpl extends BasicServiceImpl implements CatalogService {

	@Override
	public Document createDefaultOption() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		cal.set(year, 0, 1, 0, 0);
		Date start = cal.getTime();
		cal.add(Calendar.YEAR, 1);
		cal.add(Calendar.MINUTE, -1);
		Date end = cal.getTime();
		return new Document("dateRange", Arrays.asList(start, end)).append("dateType", "月").append("seriesType", "汇总")
				.append("dataType", new ArrayList<String>(Arrays.asList("计划", "实际"))).append("showData", "叠加")
				.append("aggregateType", "不累计");
	}

	/**
	 * 该人员所属的组织
	 */
	@Override
	public List<Catalog> listResRoot(String userId) {
		List<Bson> pipeline = Arrays.asList(Aggregates.match(new Document("userId", userId)),
				Aggregates.lookup("organization", "org_id", "_id", "org"), Aggregates.unwind("$org"));
		return c("user").aggregate(pipeline).map(d -> (Document) d.get("org")).map(this::org2Catalog).into(new ArrayList<>());
	}

	@Override
	public List<Catalog> listResStructure(Catalog parent) {
		List<Catalog> result = new ArrayList<Catalog>();
		if (typeEquals(parent, Organization.class)) {
			listSubOrg(parent._id, result);
			listResourceType(parent._id, "user", result);
			listResourceType(parent._id, "equipment", result);
		} else if (typeEquals(parent, ResourceType.class)) {
			ObjectId resourceType_id = parent._id;
			Object org_id = parent.meta.get("org_id");
			listHRResource(org_id, resourceType_id, this::user2Catalog, result);
			listEQResource(org_id, resourceType_id, this::equipment2Catalog, result);
		}
		return result;
	}

	@Override
	public long countResStructure(Catalog parent) {
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

	private <T> List<T> listHRResource(Object org_id, ObjectId resourceType_id, Function<Document, T> map, List<T> into) {
		return c("user").find(new Document("org_id", org_id).append("resourceType_id", resourceType_id)).map(map).into(into);
	}

	private <T> List<T> listEQResource(Object org_id, ObjectId resourceType_id, Function<Document, T> map, List<T> into) {
		return c("equipment").find(new Document("org_id", org_id).append("resourceType_id", resourceType_id)).map(map).into(into);
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
	public Document createResChart(Document condition) {

		checkResChartOption(condition);

		CatalogRenderer cr = new CatalogRenderer((Document) condition.get("option"));

		List<Document> input;
		if ("并列".equals(cr.getSeriesType())) {
			input = ((List<Document>) condition.get("input")).stream().map((Document doc) -> {
				addChildResource(doc);
				if (cr.getDataType().contains("计划")) {
					// 获取计划资源数据
					getResourceData("plan", cr, doc);
				}
				if (cr.getDataType().contains("实际")) {
					// 获取实际资源数据
					getResourceData("actual", cr, doc);
				}
				return doc;
			}).collect(Collectors.toList());
		} else {
			List<ObjectId> resourceIds = new ArrayList<>();
			((List<Document>) condition.get("input")).forEach((Document doc) -> resourceIds.addAll(addChildResource(doc)));
			Document doc = new Document("childResourceIds", resourceIds).append("label", "");
			if (cr.getDataType().contains("计划")) {
				// 获取计划资源数据
				getResourceData("plan", cr, doc);
			}
			if (cr.getDataType().contains("实际")) {
				// 获取实际资源数据
				getResourceData("actual", cr, doc);
			}
			input = Arrays.asList(doc);
		}
		return cr.render(input);
	}

	private Document getResourceData(String key, CatalogRenderer cr, Document doc) {
		String collectionName, basicQtyName, overTimeQtyName;
		if ("plan".equals(key)) {
			collectionName = "resourcePlan";
			basicQtyName = "$planBasicQty";
			overTimeQtyName = "$planOverTimeQty";
		} else {
			collectionName = "resourceActual";
			basicQtyName = "$actualBasicQty";
			overTimeQtyName = "$actualOverTimeQty";
		}

		HashMap<String, Double> basicQty = new HashMap<String, Double>();
		HashMap<String, Double> overTimeQty = new HashMap<String, Double>();
		HashMap<String, Double> totalQty = new HashMap<String, Double>();
		HashMap<String, Double> basicAmount = new HashMap<String, Double>();
		HashMap<String, Double> overTimeAmount = new HashMap<String, Double>();
		HashMap<String, Double> totalAmount = new HashMap<String, Double>();
		List<Double> works = new ArrayList<Double>();
		// 查询获取数据
		c(collectionName).aggregate(new JQ("查询-资源图表").set("$basicQty", basicQtyName).set("$overTimeQty", overTimeQtyName)
				.set("match",
						new Document("$and",
								Arrays.asList(new Document("id", new Document("$gte", cr.getStart())),
										new Document("id", new Document("$lte", cr.getEnd())))).append("resource_id",
												new Document("$in", doc.get("childResourceIds"))))
				.set("group_id", cr.getGroup_id()).array()).forEach((Document d) -> {
					basicQty.put(d.getString("_id"), d.getDouble("basicQty"));
					overTimeQty.put(d.getString("_id"), d.getDouble("overTimeQty"));
					totalQty.put(d.getString("_id"), d.getDouble("totalQty"));
					basicAmount.put(d.getString("_id"), d.getDouble("basicAmount"));
					overTimeAmount.put(d.getString("_id"), d.getDouble("overTimeAmount"));
					totalAmount.put(d.getString("_id"), d.getDouble("totalAmount"));
					works.add(d.getDouble("basicWorks"));
				});
		List<Double> basicQtys = new ArrayList<Double>();
		List<Double> overTimeQtys = new ArrayList<Double>();
		List<Double> totalQtys = new ArrayList<Double>();
		List<Double> basicAmounts = new ArrayList<Double>();
		List<Double> overTimeAmounts = new ArrayList<Double>();
		List<Double> totalAmounts = new ArrayList<Double>();

		// 根据xAxisData构建返回值
		for (String xAxis : cr.getxAxisData()) {
			basicQtys.add(basicQty.get(xAxis));
			overTimeQtys.add(overTimeQty.get(xAxis));
			totalQtys.add(totalQty.get(xAxis));

			basicAmounts.add(basicAmount.get(xAxis));
			overTimeAmounts.add(overTimeAmount.get(xAxis));
			totalAmounts.add(totalAmount.get(xAxis));
		}

		doc.append(key,
				new Document("basicQty", basicQtys).append("overTimeQty", overTimeQtys).append("totalQty", totalQtys)
						.append("basicAmount", basicAmounts).append("overTimeAmount", overTimeAmounts).append("totalAmount", totalAmounts)
						.append("works", works.isEmpty() ? null : works.get(0)));

		return doc;
	}

	/**
	 * 检查图表条件，抛出错误
	 * 
	 * @param condition
	 */
	private void checkResChartOption(Document condition) {
		Object option = condition.get("option");
		if (option instanceof Document) {
			Object dateRange = ((Document) option).get("dateRange");
			if (dateRange instanceof List<?>) {
				if (((List<?>) dateRange).size() == 2) {
					Object d0 = ((List<?>) dateRange).get(0);
					Object d1 = ((List<?>) dateRange).get(1);
					if (!(d0 instanceof Date) || !(d1 instanceof Date) || !((Date) d0).before((Date) d1)) {
						throw new ServiceException("日期范围数据不合法");
					}
				} else {
					throw new ServiceException("日期范围数据不合法");
				}
			} else {
				throw new ServiceException("日期范围类型错误");
			}
		} else {
			throw new ServiceException("选项类型错误");
		}
	}

	/**
	 * 根据传入的查询的数据，获取其从属其的资源id
	 * 
	 * @param doc
	 * @return
	 */
	private List<ObjectId> addChildResource(Document doc) {
		List<ObjectId> childResourceIds = new ArrayList<>();
		String type = doc.getString("type");
		if (ResourceType.class.getName().equals(type)) {
			ObjectId resType_id = doc.getObjectId("_id");
			Object org_id = ((Document) doc.get("meta")).getObjectId("org_id");
			listEQResource(org_id, resType_id, d -> d.getObjectId("_id"), childResourceIds);
			listHRResource(org_id, resType_id, d -> d.getObjectId("_id"), childResourceIds);
		} else if (Organization.class.getName().equals(type)) {
			List<Bson> pipe = new ArrayList<>();
			pipe.add(Aggregates.match(new Document("_id", doc.get("_id"))));
			pipe.addAll(new JQ("查询-通用-下级迭代取出-含本级").set("from", "organization").set("startWith", "$_id").set("connectFromField", "_id")
					.set("connectToField", "parent_id").array());
			pipe.addAll(new JQ("追加-组织下资源").array());

			debugPipeline(pipe);
			c("organization").aggregate(pipe).map(d -> d.getObjectId("_id")).into(childResourceIds);
		} else if (User.class.getName().equals(type) || Equipment.class.getName().equals(type)) {
			childResourceIds.add(doc.getObjectId("_id"));
		}
		doc.put("childResourceIds", childResourceIds);
		return childResourceIds;
	}

}
