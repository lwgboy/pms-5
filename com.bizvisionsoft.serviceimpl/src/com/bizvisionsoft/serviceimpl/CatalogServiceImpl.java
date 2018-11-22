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
		List<?> dateRange = (List<?>) option.get("dateRange");
		String dateType = option.getString("dateType");
		List<String> dataType = (List<String>) option.get("dataType");
		String seriesType = option.getString("seriesType");
		String showData = option.getString("showData");
		String aggregateType = option.getString("aggregateType");

		// ������ʱ�乹����ѯ���
		Document match = new Document();
		Date start = (Date) dateRange.get(0);
		Date end = (Date) dateRange.get(1);
		match.append("$and", Arrays.asList(new Document("id", new Document("$gte", start)), new Document("id", new Document("$lte", end))));

		// ����ʱ�����ͺ���ֹʱ�䣬����group��_id��xAxis����
		Document group_id = new Document();
		List<String> xAxisData = new ArrayList<String>();
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);

		Document markLineData = new Document();

		SimpleDateFormat sdf;
		if ("��".equals(dateType)) {
			group_id.append("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$id"));
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
			// TODO ��Ҫ������ѡ��Դ����Դ������������
			markLineData.append("data", Arrays.asList(new Document("yAxis", 8).append("name", "aaaa")));
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
		List<Double> totalAggWorkTimeData = new ArrayList<Double>();
		List<Double> totalAggAmountData = new ArrayList<Double>();
		List<Double> basicAggWorkTimeData = new ArrayList<Double>();
		List<Double> basicAggAmountData = new ArrayList<Double>();
		List<Double> overTimeAggWorkTimeData = new ArrayList<Double>();
		List<Double> overTimeAggAmountData = new ArrayList<Double>();

		List<String> legendData = new ArrayList<String>();
		// ����ϵ�����͹���series
		if ("����".equals(seriesType)) {
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
		// �����ۼ�ֵ���͹���series
		if ("�ܼ�".equals(aggregateType)) {
			series.add(new JQ("ͼ��-��Դͼ��-�ۼƹ�ʱ").set("name", "�ۼƹ�ʱ").set("data", totalAggWorkTimeData).doc());
			legendData.add("�ۼƹ�ʱ");
			series.add(new JQ("ͼ��-��Դͼ��-�ۼƽ��").set("name", "�ۼƽ��").set("data", totalAggAmountData).doc());
			legendData.add("�ۼƽ��");
		} else if ("��׼�Ӱ�".equals(aggregateType)) {
			series.add(new JQ("ͼ��-��Դͼ��-�ۼƹ�ʱ").set("name", "�ۼƱ�׼��ʱ").set("data", basicAggWorkTimeData).doc());
			legendData.add("�ۼƱ�׼��ʱ");
			series.add(new JQ("ͼ��-��Դͼ��-�ۼƽ��").set("name", "�ۼƱ�׼���").set("data", basicAggAmountData).doc());
			legendData.add("�ۼƱ�׼���");
			series.add(new JQ("ͼ��-��Դͼ��-�ۼƹ�ʱ").set("name", "�ۼƼӰ๤ʱ").set("data", overTimeAggWorkTimeData).doc());
			legendData.add("�ۼƼӰ๤ʱ");
			series.add(new JQ("ͼ��-��Դͼ��-�ۼƽ��").set("name", "�ۼƼӰ���").set("data", overTimeAggAmountData).doc());
			legendData.add("�ۼƼӰ���");
		}

		// ������Դͼ��
		// TODO legendData��series��˳����Ҫ����
		JQ jq = new JQ("ͼ��-��Դͼ��").set("title", "").set("legendData", legendData).set("xAxisData", xAxisData).set("series", series);
		System.out.println(jq.doc().toJson());
		return jq.doc();
	}

	@SuppressWarnings("unchecked")
	private void appendSeries(List<Document> series, List<String> dataType, String showData, String aggregateType, Document match,
			Document group_id, List<String> xAxisData, Document markLineData, List<Double> totalAggWorkTimeData,
			List<Double> totalAggAmountData, List<Double> basicAggWorkTimeData, List<Double> basicAggAmountData,
			List<Double> overTimeAggWorkTimeData, List<Double> overTimeAggAmountData, List<String> legendData, String label) {
		if (dataType.contains("�ƻ�")) {
			// ��Ӽƻ�ͼ������
			Object[] resourceData = getResourceData("resourcePlan", "$planBasicQty", "$planOverTimeQty", match, group_id, xAxisData);
			if ("��׼".equals(showData)) {
				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "�ƻ���׼" + "��ʱ").set("stack", label + "�ƻ�").set("data", resourceData[0])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "�ƻ���׼" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "�ƻ���׼" + "���").set("data", resourceData[3]).doc());
				legendData.add(label + "�ƻ���׼" + "���");
			} else if ("�Ӱ�".equals(showData)) {
				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "�ƻ��Ӱ�" + "��ʱ").set("stack", label + "�ƻ�").set("data", resourceData[1])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "�ƻ��Ӱ�" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "�ƻ��Ӱ�" + "���").set("data", resourceData[4]).doc());
				legendData.add(label + "�ƻ��Ӱ�" + "���");
			} else if ("����".equals(showData)) {
				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "�ƻ�" + "��ʱ").set("stack", label + "�ƻ�").set("data", resourceData[2])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "�ƻ�" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "�ƻ�" + "���").set("data", resourceData[5]).doc());
				legendData.add(label + "�ƻ�" + "���");
			} else {
				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "�ƻ���׼" + "��ʱ").set("stack", label + "�ƻ�").set("data", resourceData[0])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "�ƻ���׼" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "�ƻ���׼" + "���").set("data", resourceData[3]).doc());
				legendData.add(label + "�ƻ���׼" + "���");

				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "�ƻ��Ӱ�" + "��ʱ").set("stack", label + "�ƻ�").set("data", resourceData[1])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "�ƻ��Ӱ�" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "�ƻ��Ӱ�" + "���").set("data", resourceData[4]).doc());
				legendData.add(label + "�ƻ��Ӱ�" + "���");
			}
		}
		if (dataType.contains("ʵ��")) {
			// ���ʵ��ͼ������
			Object[] resourceData = getResourceData("resourceActual", "$actualBasicQty", "$actualOverTimeQty", match, group_id, xAxisData);
			if ("��׼".equals(showData)) {
				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "ʵ�ʱ�׼" + "��ʱ").set("stack", label + "ʵ��").set("data", resourceData[0])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "ʵ�ʱ�׼" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "ʵ�ʱ�׼" + "���").set("data", resourceData[3]).doc());
				legendData.add(label + "ʵ�ʱ�׼" + "���");

				appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, basicAggWorkTimeData, basicAggAmountData,
						(List<Double>) resourceData[0], (List<Double>) resourceData[3]);
			} else {
				if ("�Ӱ�".equals(showData)) {
					series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "ʵ�ʼӰ�" + "��ʱ").set("stack", label + "ʵ��")
							.set("data", resourceData[1]).set("markLineData", new Document()).doc());
					legendData.add(label + "ʵ�ʼӰ�" + "��ʱ");
					series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "ʵ�ʼӰ�" + "���").set("data", resourceData[4]).doc());
					legendData.add(label + "ʵ�ʼӰ�" + "���");

					appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, overTimeAggWorkTimeData,
							overTimeAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
				} else if ("����".equals(showData)) {
					series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "ʵ��" + "��ʱ").set("stack", label + "ʵ��").set("data", resourceData[2])
							.set("markLineData", new Document()).doc());
					legendData.add(label + "ʵ��" + "��ʱ");
					series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "ʵ��" + "���").set("data", resourceData[5]).doc());
					legendData.add(label + "ʵ��" + "���");

					appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, basicAggWorkTimeData, basicAggAmountData,
							(List<Double>) resourceData[0], (List<Double>) resourceData[3]);

					appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, overTimeAggWorkTimeData,
							overTimeAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
				} else {
					series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "ʵ�ʱ�׼" + "��ʱ").set("stack", label + "ʵ��")
							.set("data", resourceData[0]).set("markLineData", new Document()).doc());
					legendData.add(label + "ʵ�ʱ�׼" + "��ʱ");
					series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "ʵ�ʱ�׼" + "���").set("data", resourceData[3]).doc());
					legendData.add(label + "ʵ�ʱ�׼" + "���");
					appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, basicAggWorkTimeData, basicAggAmountData,
							(List<Double>) resourceData[0], (List<Double>) resourceData[3]);

					series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "ʵ�ʼӰ�" + "��ʱ").set("stack", label + "ʵ��")
							.set("data", resourceData[1]).set("markLineData", new Document()).doc());
					legendData.add(label + "ʵ�ʼӰ�" + "��ʱ");
					series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "ʵ�ʼӰ�" + "���").set("data", resourceData[4]).doc());
					legendData.add(label + "ʵ�ʼӰ�" + "���");
					appendAggregateData(aggregateType, totalAggWorkTimeData, totalAggAmountData, overTimeAggWorkTimeData,
							overTimeAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
				}
			}
		}

	}

	private void appendAggregateData(String aggregateType, List<Double> totalAggWorkTimeData, List<Double> totalAggAmountData,
			List<Double> aggWorkTimeData, List<Double> aggAmountData, List<Double> resourceWorkTimeData, List<Double> resourceAmountData) {
		if ("�ܼ�".equals(aggregateType)) {
			addAggData(totalAggWorkTimeData, totalAggAmountData, resourceWorkTimeData, resourceAmountData);
		} else if ("��׼�Ӱ�".equals(aggregateType)) {
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
	 * ��ѯ��Դͼ�����ݣ�����List<Double>���ɵ�Array
	 * 
	 * @param collectionName
	 * @param basicQtyName
	 * @param overTimeQtyName
	 * @param match
	 * @param group_id
	 * @param xAxisData
	 * @return Ԫ��1����׼������Ԫ��2���Ӱ�������Ԫ��3�������ϼƣ�Ԫ��4����׼��Ԫ��5���Ӱ��Ԫ��6�����ϼ�.
	 */
	private Object[] getResourceData(String collectionName, String basicQtyName, String overTimeQtyName, Document match, Document group_id,
			List<String> xAxisData) {
		HashMap<String, Double> basicQty = new HashMap<String, Double>();
		HashMap<String, Double> overTimeQty = new HashMap<String, Double>();
		HashMap<String, Double> totalQty = new HashMap<String, Double>();
		HashMap<String, Double> basicAmount = new HashMap<String, Double>();
		HashMap<String, Double> overTimeAmount = new HashMap<String, Double>();
		HashMap<String, Double> totalAmount = new HashMap<String, Double>();
		// ��ѯ��ȡ����
		c(collectionName).aggregate(new JQ("��ѯ-��Դͼ��").set("$basicQty", basicQtyName).set("$overTimeQty", overTimeQtyName)
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
	 * ���ݴ���Ĳ�ѯ�����ݣ���ȡ����������Դid
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
	 * ��ȡ��֯�����¼���֯��������Դid
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
