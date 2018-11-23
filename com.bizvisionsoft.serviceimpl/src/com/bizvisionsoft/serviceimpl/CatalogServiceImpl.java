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
import com.bizvisionsoft.service.model.EPS;
import com.bizvisionsoft.service.model.Equipment;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
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
		return new Document("dateRange", Arrays.asList(start, end)).append("dateType", "��").append("seriesType", "����")
				.append("dataType", new ArrayList<String>(Arrays.asList("�ƻ�", "ʵ��"))).append("showData", "����")
				.append("aggregateType", "���ۼ�");
	}

	/**
	 * ����Ա��������֯
	 */
	@Override
	public List<Catalog> listResOrgRoot(String userId) {
		List<Bson> pipeline = Arrays.asList(Aggregates.match(new Document("userId", userId)),
				Aggregates.lookup("organization", "org_id", "_id", "org"), Aggregates.unwind("$org"));
		return c("user").aggregate(pipeline).map(d -> (Document) d.get("org")).map(CatalogMapper::org).into(new ArrayList<>());
	}

	@Override
	public List<Catalog> listResOrgStructure(Catalog parent) {
		List<Catalog> result = new ArrayList<Catalog>();
		if (typeEquals(parent, Organization.class)) {
			listSubOrg(parent._id, result);
			listResourceType(parent._id, "user", result);
			listResourceType(parent._id, "equipment", result);
		} else if (typeEquals(parent, ResourceType.class)) {
			ObjectId resourceType_id = parent._id;
			Object org_id = parent.meta.get("org_id");
			listHRResource(org_id, resourceType_id, CatalogMapper::user, result);
			listEQResource(org_id, resourceType_id, CatalogMapper::equipment, result);
		} else if (typeEquals(parent, EPS.class)) {
			listSubEPS(parent._id, result);
			listProject(parent._id, result);
		} else if (typeEquals(parent, Project.class)) {
			// TODO �г��׶�
			// TODO �г���Դ���
		}
		return result;
	}

	@Override
	public long countResOrgStructure(Catalog parent) {
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
		} else if (typeEquals(parent, EPS.class)) {
			count += countSubEPS(parent._id);
			count += countProject(parent._id);
		} else if (typeEquals(parent, Project.class)) {
			// TODO �г���Դ���
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
				.map(CatalogMapper::resourceType).into(into);
	}

	private long countSubOrg(ObjectId org_id) {
		return c("organization").countDocuments(new Document("parent_id", org_id));
	}

	private long countSubEPS(ObjectId eps_id) {
		return c("eps").countDocuments(new Document("parent_id", eps_id));
	}

	private long countProject(ObjectId eps_id) {
		return c("project").countDocuments(new Document("eps_id", eps_id));
	}

	private List<Catalog> listSubOrg(ObjectId org_id, List<Catalog> into) {
		return c("organization").find(new Document("parent_id", org_id)).map(CatalogMapper::org).into(into);
	}

	private List<Catalog> listSubEPS(ObjectId parent_id, List<Catalog> into) {
		return c("eps").find(new Document("parent_id", parent_id)).map(CatalogMapper::eps).into(into);
	}

	private List<Catalog> listProject(ObjectId eps_id, List<Catalog> into) {
		return c("project").find(new Document("eps_id", eps_id)).map(CatalogMapper::project).into(into);
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

	private boolean typeEquals(Catalog c, Class<?> clas) {
		return clas.getName().equals(c.type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Document createResChart(Document condition) {

		checkResChartOption(condition);

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
			markLineData.append("data", "");
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
		List<Double> totalPlanAggWorkTimeData = new ArrayList<Double>();
		List<Double> totalPlanAggAmountData = new ArrayList<Double>();
		List<Double> basicPlanAggWorkTimeData = new ArrayList<Double>();
		List<Double> basicPlanAggAmountData = new ArrayList<Double>();
		List<Double> overTimePlanAggWorkTimeData = new ArrayList<Double>();
		List<Double> overTimePlanAggAmountData = new ArrayList<Double>();

		List<Double> totalActualAggWorkTimeData = new ArrayList<Double>();
		List<Double> totalActualAggAmountData = new ArrayList<Double>();
		List<Double> basicActualAggWorkTimeData = new ArrayList<Double>();
		List<Double> basicActualAggAmountData = new ArrayList<Double>();
		List<Double> overTimeActualAggWorkTimeData = new ArrayList<Double>();
		List<Double> overTimeActualAggAmountData = new ArrayList<Double>();

		List<String> legendData = new ArrayList<String>();
		// ����ϵ�����͹���series
		if ("����".equals(seriesType)) {
			((List<Document>) condition.get("input")).forEach((Document doc) -> {
				match.append("resource_id", new Document("$in", addChildResource(doc)));
				appendSeries(series, dataType, showData, aggregateType, match, group_id, xAxisData, markLineData, totalPlanAggWorkTimeData,
						totalPlanAggAmountData, basicPlanAggWorkTimeData, basicPlanAggAmountData, overTimePlanAggWorkTimeData,
						overTimePlanAggAmountData, totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData,
						basicActualAggAmountData, overTimeActualAggWorkTimeData, overTimeActualAggAmountData, legendData,
						doc.getString("label"));
			});
		} else {
			List<ObjectId> resourceIds = new ArrayList<>();
			((List<Document>) condition.get("input")).forEach((Document doc) -> resourceIds.addAll(addChildResource(doc)));
			match.append("resource_id", new Document("$in", resourceIds));
			appendSeries(series, dataType, showData, aggregateType, match, group_id, xAxisData, markLineData, totalPlanAggWorkTimeData,
					totalPlanAggAmountData, basicPlanAggWorkTimeData, basicPlanAggAmountData, overTimePlanAggWorkTimeData,
					overTimePlanAggAmountData, totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData,
					basicActualAggAmountData, overTimeActualAggWorkTimeData, overTimeActualAggAmountData, legendData, "");

		}
		// �����ۼ�ֵ���͹���series
		if ("�ܼ�".equals(aggregateType)) {
			if (dataType.contains("�ƻ�")) {
				series.add(new JQ("ͼ��-��Դͼ��-�ۼƹ�ʱ").set("name", "�ۼƼƻ���ʱ").set("color1", "rgba(95,184,120,1)")
						.set("color2", "rgba(95,184,120,0.7)").set("color3", "rgba(95,184,120,0)").set("stack", "�ۼƼƻ���ʱ")
						.set("data", totalPlanAggWorkTimeData).doc());
				legendData.add("�ۼƼƻ���ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-�ۼƽ��").set("name", "�ۼƼƻ����").set("color1", "rgba(95,184,120,1)")
						.set("color2", "rgba(95,184,120,0.7)").set("color3", "rgba(95,184,120,0)").set("stack", "�ۼƼƻ����")
						.set("data", totalPlanAggAmountData).doc());
				legendData.add("�ۼƼƻ����");
			}
			if (dataType.contains("ʵ��")) {
				series.add(
						new JQ("ͼ��-��Դͼ��-�ۼƹ�ʱ").set("name", "�ۼ�ʵ�ʹ�ʱ").set("color1", "rgba(0,150,136,1)").set("color2", "rgba(0,150,136,0.7)")
								.set("color3", "rgba(0,150,136,0)").set("stack", "�ۼ�ʵ�ʹ�ʱ").set("data", totalActualAggWorkTimeData).doc());
				legendData.add("�ۼ�ʵ�ʹ�ʱ");
				series.add(
						new JQ("ͼ��-��Դͼ��-�ۼƽ��").set("name", "�ۼ�ʵ�ʽ��").set("color1", "rgba(0,150,136,1)").set("color2", "rgba(0,150,136,0.7)")
								.set("color3", "rgba(0,150,136,0)").set("stack", "�ۼ�ʵ�ʽ��").set("data", totalActualAggAmountData).doc());
				legendData.add("�ۼ�ʵ�ʽ��");
			}
		} else if ("��׼�Ӱ�".equals(aggregateType)) {
			if (dataType.contains("�ƻ�")) {
				series.add(new JQ("ͼ��-��Դͼ��-�ۼƹ�ʱ").set("name", "�ۼƼƻ���׼��ʱ").set("color1", "rgba(95,184,120,1)")
						.set("color2", "rgba(95,184,120,0.7)").set("color3", "rgba(95,184,120,0)").set("stack", "�ۼƼƻ���ʱ")
						.set("data", basicPlanAggWorkTimeData).doc());
				legendData.add("�ۼƼƻ���׼��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-�ۼƽ��").set("name", "�ۼƼƻ���׼���").set("color1", "rgba(95,184,120,1)")
						.set("color2", "rgba(95,184,120,0.7)").set("color3", "rgba(95,184,120,0)").set("stack", "�ۼƼƻ����")
						.set("data", basicPlanAggAmountData).doc());
				legendData.add("�ۼƼƻ���׼���");
				series.add(new JQ("ͼ��-��Դͼ��-�ۼƹ�ʱ").set("name", "�ۼƼƻ��Ӱ๤ʱ").set("color1", "rgba(255,87,34,1)")
						.set("color2", "rgba(255,87,34,0.7)").set("color3", "rgba(255,87,34,0)").set("stack", "�ۼƼƻ���ʱ")
						.set("data", overTimePlanAggWorkTimeData).doc());
				legendData.add("�ۼƼƻ��Ӱ๤ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-�ۼƽ��").set("name", "�ۼƼƻ��Ӱ���").set("color1", "rgba(255,87,34,1)")
						.set("color2", "rgba(255,87,34,0.7)").set("color3", "rgba(255,87,34,0)").set("stack", "�ۼƼƻ����")
						.set("data", overTimePlanAggAmountData).doc());
				legendData.add("�ۼƼƻ��Ӱ���");
			}
			if (dataType.contains("ʵ��")) {
				series.add(new JQ("ͼ��-��Դͼ��-�ۼƹ�ʱ").set("name", "�ۼ�ʵ�ʱ�׼��ʱ").set("color1", "rgba(0,150,136,1)")
						.set("color2", "rgba(0,150,136,0.7)").set("color3", "rgba(0,150,136,0)").set("stack", "�ۼ�ʵ�ʹ�ʱ")
						.set("data", basicActualAggWorkTimeData).doc());
				legendData.add("�ۼ�ʵ�ʱ�׼��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-�ۼƽ��").set("name", "�ۼ�ʵ�ʱ�׼���").set("color1", "rgba(0,150,136,1)")
						.set("color2", "rgba(0,150,136,0.7)").set("color3", "rgba(0,150,136,0)").set("stack", "�ۼ�ʵ�ʽ��")
						.set("data", basicActualAggAmountData).doc());
				legendData.add("�ۼ�ʵ�ʱ�׼���");
				series.add(new JQ("ͼ��-��Դͼ��-�ۼƹ�ʱ").set("name", "�ۼ�ʵ�ʼӰ๤ʱ").set("color1", "rgba(255,184,0,1)")
						.set("color2", "rgba(255,184,0,0.7)").set("color3", "rgba(255,184,0,0)").set("stack", "�ۼ�ʵ�ʹ�ʱ")
						.set("data", overTimeActualAggWorkTimeData).doc());
				legendData.add("�ۼ�ʵ�ʼӰ๤ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-�ۼƽ��").set("name", "�ۼ�ʵ�ʼӰ���").set("color1", "rgba(255,184,0,1)")
						.set("color2", "rgba(255,184,0,0.7)").set("color3", "rgba(255,184,0,0)").set("stack", "�ۼ�ʵ�ʽ��")
						.set("data", overTimeActualAggAmountData).doc());
				legendData.add("�ۼ�ʵ�ʼӰ���");
			}
		}

		// ������Դͼ��
		// TODO legendData��series��˳����Ҫ����
		JQ jq = new JQ("ͼ��-��Դͼ��").set("title", "").set("legendData", legendData).set("xAxisData", xAxisData).set("series", series);
		return jq.doc();
	}

	/**
	 * ���ͼ���������׳�����
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
						throw new ServiceException("���ڷ�Χ���ݲ��Ϸ�");
					}
				} else {
					throw new ServiceException("���ڷ�Χ���ݲ��Ϸ�");
				}
			} else {
				throw new ServiceException("���ڷ�Χ���ʹ���");
			}
		} else {
			throw new ServiceException("ѡ�����ʹ���");
		}
	}

	/**
	 * ����series
	 * 
	 * @param series
	 * @param dataType
	 * @param showData
	 * @param aggregateType
	 * @param match
	 * @param group_id
	 * @param xAxisData
	 * @param markLineData
	 * @param totalPlanAggWorkTimeData
	 * @param totalPlanAggAmountData
	 * @param basicPlanAggWorkTimeData
	 * @param basicPlanAggAmountData
	 * @param overTimePlanAggWorkTimeData
	 * @param overTimePlanAggAmountData
	 * @param totalActualAggWorkTimeData
	 * @param totalActualAggAmountData
	 * @param basicActualAggWorkTimeData
	 * @param basicActualAggAmountData
	 * @param overTimeActualAggWorkTimeData
	 * @param overTimeActualAggAmountData
	 * @param legendData
	 * @param label
	 */
	@SuppressWarnings("unchecked")
	private void appendSeries(List<Document> series, List<String> dataType, String showData, String aggregateType, Document match,
			Document group_id, List<String> xAxisData, Document markLineData, List<Double> totalPlanAggWorkTimeData,
			List<Double> totalPlanAggAmountData, List<Double> basicPlanAggWorkTimeData, List<Double> basicPlanAggAmountData,
			List<Double> overTimePlanAggWorkTimeData, List<Double> overTimePlanAggAmountData, List<Double> totalActualAggWorkTimeData,
			List<Double> totalActualAggAmountData, List<Double> basicActualAggWorkTimeData, List<Double> basicActualAggAmountData,
			List<Double> overTimeActualAggWorkTimeData, List<Double> overTimeActualAggAmountData, List<String> legendData, String label) {
		if (dataType.contains("�ƻ�")) {
			// ��ȡ�ƻ���Դ����
			Object[] resourceData = getResourceData("resourcePlan", "$planBasicQty", "$planOverTimeQty", match, group_id, xAxisData);
			if ("��׼".equals(showData)) {
				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "�ƻ���׼" + "��ʱ").set("stack", label + "�ƻ���ʱ").set("data", resourceData[0])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "�ƻ���׼" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "�ƻ���׼" + "���").set("stack", label + "�ƻ����").set("data", resourceData[3])
						.doc());
				legendData.add(label + "�ƻ���׼" + "���");

				// �����ۼ�ֵ
				appendAggregateData(aggregateType, totalPlanAggWorkTimeData, totalPlanAggAmountData, basicPlanAggWorkTimeData,
						basicPlanAggAmountData, (List<Double>) resourceData[0], (List<Double>) resourceData[3]);
			} else if ("�Ӱ�".equals(showData)) {
				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "�ƻ��Ӱ�" + "��ʱ").set("stack", label + "�ƻ���ʱ").set("data", resourceData[1])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "�ƻ��Ӱ�" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "�ƻ��Ӱ�" + "���").set("stack", label + "�ƻ����").set("data", resourceData[4])
						.doc());
				legendData.add(label + "�ƻ��Ӱ�" + "���");

				// �����ۼ�ֵ
				appendAggregateData(aggregateType, totalPlanAggWorkTimeData, totalPlanAggAmountData, overTimePlanAggWorkTimeData,
						overTimePlanAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
			} else if ("����".equals(showData)) {
				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "�ƻ�" + "��ʱ").set("stack", label + "�ƻ���ʱ").set("data", resourceData[2])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "�ƻ�" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "�ƻ�" + "���").set("stack", label + "�ƻ����").set("data", resourceData[5])
						.doc());
				legendData.add(label + "�ƻ�" + "���");

				// �����ۼ�ֵ
				appendAggregateData(aggregateType, totalPlanAggWorkTimeData, totalPlanAggAmountData, basicPlanAggWorkTimeData,
						basicPlanAggAmountData, (List<Double>) resourceData[0], (List<Double>) resourceData[3]);

				appendAggregateData(aggregateType, totalPlanAggWorkTimeData, totalPlanAggAmountData, overTimePlanAggWorkTimeData,
						overTimePlanAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
			} else {
				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "�ƻ���׼" + "��ʱ").set("stack", label + "�ƻ���ʱ").set("data", resourceData[0])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "�ƻ���׼" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "�ƻ���׼" + "���").set("stack", label + "�ƻ����").set("data", resourceData[3])
						.doc());
				legendData.add(label + "�ƻ���׼" + "���");

				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "�ƻ��Ӱ�" + "��ʱ").set("stack", label + "�ƻ���ʱ").set("data", resourceData[1])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "�ƻ��Ӱ�" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "�ƻ��Ӱ�" + "���").set("stack", label + "�ƻ����").set("data", resourceData[4])
						.doc());
				legendData.add(label + "�ƻ��Ӱ�" + "���");

				// �����ۼ�ֵ
				appendAggregateData(aggregateType, totalPlanAggWorkTimeData, totalPlanAggAmountData, basicPlanAggWorkTimeData,
						basicPlanAggAmountData, (List<Double>) resourceData[0], (List<Double>) resourceData[3]);
				appendAggregateData(aggregateType, totalPlanAggWorkTimeData, totalPlanAggAmountData, overTimePlanAggWorkTimeData,
						overTimePlanAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
			}
		}
		if (dataType.contains("ʵ��")) {
			// ��ȡʵ����Դ����
			Object[] resourceData = getResourceData("resourceActual", "$actualBasicQty", "$actualOverTimeQty", match, group_id, xAxisData);
			if ("��׼".equals(showData)) {
				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "ʵ�ʱ�׼" + "��ʱ").set("stack", label + "ʵ�ʹ�ʱ").set("data", resourceData[0])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "ʵ�ʱ�׼" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "ʵ�ʱ�׼" + "���").set("stack", label + "ʵ�ʽ��").set("data", resourceData[3])
						.doc());
				legendData.add(label + "ʵ�ʱ�׼" + "���");

				// �����ۼ�ֵ
				appendAggregateData(aggregateType, totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData,
						basicActualAggAmountData, (List<Double>) resourceData[0], (List<Double>) resourceData[3]);
			} else if ("�Ӱ�".equals(showData)) {
				if (markLineData.get("data") != null && resourceData[6] != null)
					markLineData.put("data", Arrays.asList(new Document("yAxis", resourceData[6]).append("name", label + "�����")));

				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "ʵ�ʼӰ�" + "��ʱ").set("stack", label + "ʵ�ʹ�ʱ").set("data", resourceData[1])
						.set("markLineData", markLineData).doc());
				legendData.add(label + "ʵ�ʼӰ�" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "ʵ�ʼӰ�" + "���").set("stack", label + "ʵ�ʽ��").set("data", resourceData[4])
						.doc());
				legendData.add(label + "ʵ�ʼӰ�" + "���");

				// �����ۼ�ֵ
				appendAggregateData(aggregateType, totalActualAggWorkTimeData, totalActualAggAmountData, overTimeActualAggWorkTimeData,
						overTimeActualAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
			} else if ("����".equals(showData)) {
				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "ʵ��" + "��ʱ").set("stack", label + "ʵ�ʹ�ʱ").set("data", resourceData[2])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "ʵ��" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "ʵ��" + "���").set("stack", label + "ʵ�ʽ��").set("data", resourceData[5])
						.doc());
				legendData.add(label + "ʵ��" + "���");

				// �����ۼ�ֵ
				appendAggregateData(aggregateType, totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData,
						basicActualAggAmountData, (List<Double>) resourceData[0], (List<Double>) resourceData[3]);

				appendAggregateData(aggregateType, totalActualAggWorkTimeData, totalActualAggAmountData, overTimeActualAggWorkTimeData,
						overTimeActualAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
			} else {
				if (markLineData.get("data") != null && resourceData[6] != null)
					markLineData.put("data", Arrays.asList(new Document("yAxis", resourceData[6]).append("name", label + "�����")));

				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "ʵ�ʱ�׼" + "��ʱ").set("stack", label + "ʵ�ʹ�ʱ").set("data", resourceData[0])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "ʵ�ʱ�׼" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "ʵ�ʱ�׼" + "���").set("stack", label + "ʵ�ʽ��").set("data", resourceData[3])
						.doc());
				legendData.add(label + "ʵ�ʱ�׼" + "���");

				series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "ʵ�ʼӰ�" + "��ʱ").set("stack", label + "ʵ�ʹ�ʱ").set("data", resourceData[1])
						.set("markLineData", markLineData).doc());
				legendData.add(label + "ʵ�ʼӰ�" + "��ʱ");
				series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "ʵ�ʼӰ�" + "���").set("stack", label + "ʵ�ʽ��").set("data", resourceData[4])
						.doc());
				legendData.add(label + "ʵ�ʼӰ�" + "���");
				// �����ۼ�ֵ
				appendAggregateData(aggregateType, totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData,
						basicActualAggAmountData, (List<Double>) resourceData[0], (List<Double>) resourceData[3]);
				appendAggregateData(aggregateType, totalActualAggWorkTimeData, totalActualAggAmountData, overTimeActualAggWorkTimeData,
						overTimeActualAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
			}

		}

	}

	/**
	 * �����ۼ�ֵ���ͣ������ۼ�ֵ
	 * 
	 * @param aggregateType
	 * @param totalAggWorkTimeData
	 * @param totalAggAmountData
	 * @param aggWorkTimeData
	 * @param aggAmountData
	 * @param resourceWorkTimeData
	 * @param resourceAmountData
	 */
	private void appendAggregateData(String aggregateType, List<Double> totalAggWorkTimeData, List<Double> totalAggAmountData,
			List<Double> aggWorkTimeData, List<Double> aggAmountData, List<Double> resourceWorkTimeData, List<Double> resourceAmountData) {
		if ("�ܼ�".equals(aggregateType)) {
			addAggData(totalAggWorkTimeData, totalAggAmountData, resourceWorkTimeData, resourceAmountData);
		} else if ("��׼�Ӱ�".equals(aggregateType)) {
			addAggData(aggWorkTimeData, aggAmountData, resourceWorkTimeData, resourceAmountData);
		}
	}

	/**
	 * ���㹤ʱ�ͽ����ۼ�ֵ
	 * 
	 * @param aggWorkTimeDatas
	 * @param aggAmountDatas
	 * @param resourceWorkTimeData
	 * @param resourceAmountData
	 */
	private void addAggData(List<Double> aggWorkTimeDatas, List<Double> aggAmountDatas, List<Double> resourceWorkTimeData,
			List<Double> resourceAmountData) {
		Double wd = null;
		Double ad = null;
		if (aggWorkTimeDatas.size() > 0) {
			for (int i = 0; i < aggWorkTimeDatas.size(); i++) {
				Double d = resourceWorkTimeData.get(i);
				if (d != null && wd != null)
					wd += d;
				else if (d != null && wd == null)
					wd = d;

				Double aggWorkTimeData = aggWorkTimeDatas.get(i);
				if (aggWorkTimeData != null && wd != null)
					aggWorkTimeData += wd;
				else if (aggWorkTimeData == null && wd != null)
					aggWorkTimeData = wd;

				aggWorkTimeDatas.set(i, aggWorkTimeData);

				d = resourceAmountData.get(i);
				if (d != null && ad != null)
					ad += d;
				else if (d != null && ad == null)
					ad = d;

				Double aggAmountData = aggAmountDatas.get(i);
				if (aggAmountData != null && ad != null)
					aggAmountData += ad;
				else if (aggAmountData == null && ad != null)
					aggAmountData = ad;

				aggAmountDatas.set(i, aggAmountData);
			}
		} else {
			for (Double d : resourceWorkTimeData) {
				if (d != null && wd != null)
					wd += d;
				else if (d != null && wd == null)
					wd = d;
				aggWorkTimeDatas.add(wd);
			}
			for (Double d : resourceAmountData) {
				if (d != null && ad != null)
					ad += d;
				else if (d != null && ad == null)
					ad = d;
				aggAmountDatas.add(ad);
			}
		}
	}

	/**
	 * ��ѯ��Դͼ�����ݣ�������xAxisData��������ֵ
	 * 
	 * @param collectionName
	 * @param basicQtyName
	 * @param overTimeQtyName
	 * @param match
	 * @param group_id
	 * @param xAxisData
	 * @return Ԫ��1����׼������Ԫ��2���Ӱ�������Ԫ��3�������ϼƣ�Ԫ��4����׼��Ԫ��5���Ӱ��Ԫ��6�����ϼƣ�Ԫ��7����׼��ʱ.
	 */
	private Object[] getResourceData(String collectionName, String basicQtyName, String overTimeQtyName, Document match, Document group_id,
			List<String> xAxisData) {
		HashMap<String, Double> basicQty = new HashMap<String, Double>();
		HashMap<String, Double> overTimeQty = new HashMap<String, Double>();
		HashMap<String, Double> totalQty = new HashMap<String, Double>();
		HashMap<String, Double> basicAmount = new HashMap<String, Double>();
		HashMap<String, Double> overTimeAmount = new HashMap<String, Double>();
		HashMap<String, Double> totalAmount = new HashMap<String, Double>();
		List<Double> works = new ArrayList<Double>();
		// ��ѯ��ȡ����
		c(collectionName).aggregate(new JQ("��ѯ-��Դͼ��").set("$basicQty", basicQtyName).set("$overTimeQty", overTimeQtyName)
				.set("match", match).set("group_id", group_id).array()).forEach((Document doc) -> {
					basicQty.put(doc.getString("_id"), doc.getDouble("basicQty"));
					overTimeQty.put(doc.getString("_id"), doc.getDouble("overTimeQty"));
					totalQty.put(doc.getString("_id"), doc.getDouble("totalQty"));
					basicAmount.put(doc.getString("_id"), doc.getDouble("basicAmount"));
					overTimeAmount.put(doc.getString("_id"), doc.getDouble("overTimeAmount"));
					totalAmount.put(doc.getString("_id"), doc.getDouble("totalAmount"));
					works.add(doc.getDouble("basicWorks"));
				});
		List<Double> basicQtys = new ArrayList<Double>();
		List<Double> overTimeQtys = new ArrayList<Double>();
		List<Double> totalQtys = new ArrayList<Double>();
		List<Double> basicAmounts = new ArrayList<Double>();
		List<Double> overTimeAmounts = new ArrayList<Double>();
		List<Double> totalAmounts = new ArrayList<Double>();

		// ����xAxisData��������ֵ
		for (String xAxis : xAxisData) {
			basicQtys.add(basicQty.get(xAxis));
			overTimeQtys.add(overTimeQty.get(xAxis));
			totalQtys.add(totalQty.get(xAxis));

			basicAmounts.add(basicAmount.get(xAxis) == null ? null : (basicAmount.get(xAxis) / 10000));
			overTimeAmounts.add(overTimeAmount.get(xAxis) == null ? null : (overTimeAmount.get(xAxis) / 10000));
			totalAmounts.add(totalAmount.get(xAxis) == null ? null : (totalAmount.get(xAxis) / 10000));
		}

		return new Object[] { basicQtys, overTimeQtys, totalQtys, basicAmounts, overTimeAmounts, totalAmounts,
				Check.isAssigned(works) ? works.get(0) : null };
	}

	/**
	 * ���ݴ���Ĳ�ѯ�����ݣ���ȡ����������Դid
	 * 
	 * @param doc
	 * @return
	 */
	private List<ObjectId> addChildResource(Document doc) {
		List<ObjectId> result = new ArrayList<>();
		String type = doc.getString("type");
		if (ResourceType.class.getName().equals(type)) {
			ObjectId resType_id = doc.getObjectId("_id");
			Object org_id = ((Document) doc.get("meta")).getObjectId("org_id");
			listEQResource(org_id, resType_id, d -> d.getObjectId("_id"), result);
			listHRResource(org_id, resType_id, d -> d.getObjectId("_id"), result);
		} else if (Organization.class.getName().equals(type)) {
			List<Bson> pipe = new ArrayList<>();
			pipe.add(Aggregates.match(new Document("_id", doc.get("_id"))));
			pipe.addAll(new JQ("��ѯ-ͨ��-�¼�����ȡ��-������").set("from", "organization").set("startWith", "$_id").set("connectFromField", "_id")
					.set("connectToField", "parent_id").array());
			pipe.addAll(new JQ("׷��-��֯����Դ").array());

			debugPipeline(pipe);
			c("organization").aggregate(pipe).map(d -> d.getObjectId("_id")).into(result);
		} else if (User.class.getName().equals(type) || Equipment.class.getName().equals(type)) {
			result.add(doc.getObjectId("_id"));
		}
		doc.put("childResourceIds", result);
		return result;
	}

	@Override
	public List<Catalog> listResEPSRoot() {
		return c("eps").find(new Document("parent_id", null)).map(CatalogMapper::eps).into(new ArrayList<>());
	}

}
