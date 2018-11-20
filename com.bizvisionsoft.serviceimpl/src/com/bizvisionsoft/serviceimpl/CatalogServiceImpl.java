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
	 * ����Ա��������֯
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
		// ���parent����֯����ȡ�¼���֯����Դ����
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
		p.addAll(new JQ("��ѯ-��֯-��Դ����").set("org_id", org_id).array());
		p.add(Aggregates.count());
		return Optional.ofNullable(c(collection).aggregate(p).first()).map(m -> ((Number) m.get("count")).longValue()).orElse(0l);
	}

	private List<Catalog> listResourceType(ObjectId org_id, String collection, List<Catalog> into) {
		List<Bson> p = new JQ("��ѯ-��֯-��Դ����").set("org_id", org_id).array();
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
		// ��ȡ��ѯʱ�䣬������ʱ�乹����ѯ���
		List<?> dateRange = (List<?>) option.get("dateRange");

		Document match = new Document();
		Date start = (Date) dateRange.get(0);
		Date end = (Date) dateRange.get(1);
		match.append("$and", Arrays.asList(new Document("id", new Document("$gte", start)), new Document("id", new Document("$lte", end))));

		// ��ȡʱ�����ͺ���ֹʱ�䣬����group��_id��xAxis����
		Document group_id = new Document();
		String dateType = option.getString("dateType");
		List<String> xAxisData = new ArrayList<String>();
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);

		SimpleDateFormat sdf;
		if ("��".equals(dateType)) {
			group_id.append("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$id"));
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
		} else if ("��".equals(dateType)) {
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

		List<String> legendData = new ArrayList<String>();
		boolean isAggregate = option.getBoolean("isAggregate", false);
		if (isAggregate)
			legendData.add("�ۼƹ�ʱ");
		// ��ȡ�������ͣ��������������͹�������JQ
		List<String> dataType = (List<String>) option.get("dataType");

		String seriesType = option.getString("seriesType");
		if ("����".equals(seriesType)) {
			((List<Document>) condition.get("input")).forEach((Document doc) -> {
				match.append("resource_id", new Document("$in", addChildResource(doc)));
				appendSeries(series, match, group_id, xAxisData, legendData, dataType, "basicQty", "basicAmount",
						doc.getString("label") + "�ƻ���׼", doc.getString("label") + "ʵ�ʱ�׼", doc.getString("label") + "�ƻ�",
						doc.getString("label") + "ʵ��", isAggregate);
				// TODO ��Ҫ����markLineData
				appendSeries(series, match, group_id, xAxisData, legendData, dataType, "overTimeQty", "overTimeAmount",
						doc.getString("label") + "�ƻ��Ӱ�", doc.getString("label") + "ʵ�ʱ�׼", doc.getString("label") + "�ƻ�",
						doc.getString("label") + "ʵ��", isAggregate);
			});
		} else if ("����".equals(seriesType)) {
			((List<Document>) condition.get("input")).forEach((Document doc) -> {
				match.append("resource_id", new Document("$in", addChildResource(doc)));
				appendSeries(series, match, group_id, xAxisData, legendData, dataType, "totalQty", "totalAmount",
						doc.getString("label") + "�ƻ�", doc.getString("label") + "ʵ��", "�ƻ�", "ʵ��", isAggregate);
			});
		} else {
			List<ObjectId> resourceIds = new ArrayList<>();
			((List<Document>) condition.get("input")).forEach((Document doc) -> resourceIds.addAll(addChildResource(doc)));
			match.append("resource_id", new Document("$in", resourceIds));
			appendSeries(series, match, group_id, xAxisData, legendData, dataType, "totalQty", "totalAmount", "�ƻ�", "ʵ��", "�ƻ�", "ʵ��",
					isAggregate);

		}
		// ������Դͼ��
		// TODO legendData��series��˳����Ҫ����
		JQ jq = new JQ("ͼ��-��Դͼ��").set("title", "").set("legendData", legendData).set("xAxisData", xAxisData).set("series", series);
		System.out.println(jq.doc().toJson());
		return jq.doc();
	}

	/**
	 * ���ͼ��� series
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
	 * @param isAggregate
	 */
	private void appendSeries(List<Document> series, Document match, Document group_id, List<String> xAxisData, List<String> legendData,
			List<String> dataType, String qtyName, String amountName, String planName, String actualName, String planStack,
			String actualStack, boolean isAggregate) {
		if (dataType.contains("�ƻ�")) {
			// ��Ӽƻ�ͼ������
			Map<String, Double> qty = new HashMap<String, Double>();
			Map<String, Double> amount = new HashMap<String, Double>();
			// ��ѯ��ȡ�ƻ�����
			c("resourcePlan").aggregate(new JQ("��ѯ-��Դͼ��").set("$basicQty", "$planBasicQty").set("$overTimeQty", "$planOverTimeQty")
					.set("match", match).set("group_id", group_id).array()).forEach((Document doc) -> {
						qty.put(doc.getString("_id"), doc.getDouble(qtyName));
						amount.put(doc.getString("_id"), doc.getDouble(amountName) / 10000);
					});
			List<Double> workTimeData = new ArrayList<Double>();
			List<Double> amountData = new ArrayList<Double>();
			for (String xAxis : xAxisData) {
				workTimeData.add(qty.get(xAxis));
				amountData.add(amount.get(xAxis));
			}
			// ������ʱ�ͽ��ͼ��
			series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", planName + "��ʱ").set("stack", planStack).set("data", workTimeData)
					.set("markLineData", new Document()).doc());
			legendData.add(planName + "��ʱ");
			series.add(new JQ("ͼ��-��Դͼ��-���").set("name", planName + "���").set("data", amountData).doc());
			legendData.add(planName + "���");
		}
		if (dataType.contains("ʵ��")) {
			// ���ʵ��ͼ������
			Map<String, Double> qty = new HashMap<String, Double>();
			Map<String, Double> amount = new HashMap<String, Double>();
			// ��ѯ��ȡʵ������
			c("resourceActual").aggregate(new JQ("��ѯ-��Դͼ��").set("$basicQty", "$actualBasicQty").set("$overTimeQty", "$actualOverTimeQty")
					.set("match", match).set("group_id", group_id).array()).forEach((Document doc) -> {
						qty.put(doc.getString("_id"), doc.getDouble(qtyName));
						amount.put(doc.getString("_id"), doc.getDouble(amountName) / 10000);
					});
			List<Double> aggregateData = new ArrayList<Double>();
			List<Double> workTimeData = new ArrayList<Double>();
			List<Double> amountData = new ArrayList<Double>();
			double d = 0d;
			for (String xAxis : xAxisData) {
				d += Optional.ofNullable(qty.get(xAxis)).orElse(0d);
				aggregateData.add(d);
				workTimeData.add(qty.get(xAxis));
				amountData.add(amount.get(xAxis));
			}

			// ������ʱ�ͽ��ͼ��
			series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", actualName + "��ʱ").set("stack", actualStack).set("data", workTimeData)
					.set("markLineData", new Document()).doc());
			legendData.add(actualName + "��ʱ");
			series.add(new JQ("ͼ��-��Դͼ��-���").set("name", actualName + "���").set("data", amountData).doc());
			legendData.add(actualName + "���");
			// �����Ƿ���ʾ�ۼƹ�ʱ�������ۼƹ�˾ͼ��
			if (isAggregate) {
				series.add(new JQ("ͼ��-��Դͼ��-�ۼƹ�ʱ").set("name", actualName + "�ۼƹ�ʱ").set("data", aggregateData).doc());
				legendData.add(actualName + "�ۼƹ�ʱ");
			}
		}
	}

	/**
	 * ���ݴ���Ĳ�ѯ�����ݣ���ȡ����������Դid
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
	 * ��ȡ��֯�����¼���֯��������Դ
	 * 
	 * @param org_id
	 * @return
	 */
	private List<ObjectId> getResourceId(ObjectId org_id) {
		return getResourceId(
				new Document("org_id", new Document("$in", getDesentItems(Arrays.asList(org_id), "organization", "parent_id"))));
	}

	/**
	 * ��ȡ��Դ��������֯�µ�������Դ
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
