package com.bizvisionsoft.serviceimpl.renderer;

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

import com.bizvisionsoft.service.model.Equipment;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.bizvisionsoft.serviceimpl.renderer.ColorTheme.BruiColor;
import com.mongodb.client.model.Aggregates;

public class ResourceChartRenderer extends BasicServiceImpl {

	private Date start;
	private Date end;
	private String aggregateType;
	private String showData;
	private String seriesType;
	private List<String> dataType;
	private String dateType;
	private List<Document> input;
	private ArrayList<Document> series;
	private ArrayList<String> legendData;
	private ArrayList<String> xAxisData;
	private Document markLineData;
	private Document group_id;
	private ArrayList<Double> totalPlanAggWorkTimeData;
	private ArrayList<Double> totalPlanAggAmountData;
	private ArrayList<Double> basicPlanAggWorkTimeData;
	private ArrayList<Double> basicPlanAggAmountData;
	private ArrayList<Double> overTimePlanAggWorkTimeData;
	private ArrayList<Double> overTimePlanAggAmountData;
	private ArrayList<Double> totalActualAggWorkTimeData;
	private ArrayList<Double> totalActualAggAmountData;
	private ArrayList<Double> basicActualAggWorkTimeData;
	private ArrayList<Double> basicActualAggAmountData;
	private ArrayList<Double> overTimeActualAggWorkTimeData;
	private ArrayList<Double> overTimeActualAggAmountData;
	private Document match;

	@SuppressWarnings("unchecked")
	public ResourceChartRenderer(Document condition) {
		checkResChartOption(condition);
		Document option = (Document) condition.get("option");
		List<?> dateRange = (List<?>) option.get("dateRange");
		start = (Date) dateRange.get(0);
		end = (Date) dateRange.get(1);
		dateType = option.getString("dateType");
		dataType = (List<String>) option.get("dataType");
		seriesType = option.getString("seriesType");
		showData = option.getString("showData");
		aggregateType = option.getString("aggregateType");
		input = ((List<Document>) condition.get("input"));
	}

	public Document render() {

		series = new ArrayList<Document>();
		legendData = new ArrayList<String>();

		// ������ʱ�乹����ѯ���
		match = new Document("$and",
				Arrays.asList(new Document("id", new Document("$gte", start)), new Document("id", new Document("$lte", end))));

		// ����ʱ�����ͺ���ֹʱ�䣬����group��_id��xAxis����
		initializeXAxis();

		initializeData();

		// ����ϵ�����͹���series
		if ("����".equals(seriesType)) {
			input.forEach(d -> {
				match.append("resource_id", new Document("$in", listResource(d)));
				appendSeries(d.getString("label"));
			});
		} else {
			List<ObjectId> resourceIds = new ArrayList<>();
			input.forEach(d -> resourceIds.addAll(listResource(d)));
			match.append("resource_id", new Document("$in", resourceIds));
			appendSeries("");

		}
		// �����ۼ�ֵ���͹���series
		if ("�ܼ�".equals(aggregateType)) {
			if (dataType.contains("�ƻ�")) {
				appendSummarySeries("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼƼƻ���ʱ", "�ۼƼƻ���ʱ", totalPlanAggWorkTimeData, BruiColor.Light_Blue);
				appendSummarySeries("ͼ��-��Դͼ��-�ۼƽ��", "�ۼƼƻ����", "�ۼƼƻ����", totalPlanAggAmountData, BruiColor.Light_Blue);
			}
			if (dataType.contains("ʵ��")) {
				appendSummarySeries("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼ�ʵ�ʹ�ʱ", "�ۼ�ʵ�ʹ�ʱ", totalActualAggWorkTimeData, BruiColor.Teal);
				appendSummarySeries("ͼ��-��Դͼ��-�ۼƽ��", "�ۼ�ʵ�ʽ��", "�ۼ�ʵ�ʽ��", totalActualAggAmountData, BruiColor.Teal);
			}
		} else if ("��׼�Ӱ�".equals(aggregateType)) {
			if (dataType.contains("�ƻ�")) {
				appendSummarySeries("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼƼƻ���׼��ʱ", "�ۼƼƻ���ʱ", basicPlanAggWorkTimeData, BruiColor.Light_Blue);
				appendSummarySeries("ͼ��-��Դͼ��-�ۼƽ��", "�ۼƼƻ���׼���", "�ۼƼƻ����", basicPlanAggAmountData, BruiColor.Light_Blue);

				appendSummarySeries("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼƼƻ��Ӱ๤ʱ", "�ۼƼƻ���ʱ", overTimePlanAggWorkTimeData, BruiColor.Orange);
				appendSummarySeries("ͼ��-��Դͼ��-�ۼƽ��", "�ۼƼƻ��Ӱ���", "�ۼƼƻ����", overTimePlanAggAmountData, BruiColor.Orange);
			}
			if (dataType.contains("ʵ��")) {
				appendSummarySeries("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼ�ʵ�ʱ�׼��ʱ", "�ۼ�ʵ�ʹ�ʱ", basicActualAggWorkTimeData, BruiColor.Teal);
				appendSummarySeries("ͼ��-��Դͼ��-�ۼƽ��", "�ۼ�ʵ�ʱ�׼���", "�ۼ�ʵ�ʽ��", basicActualAggAmountData, BruiColor.Teal);

				appendSummarySeries("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼ�ʵ�ʼӰ๤ʱ", "�ۼ�ʵ�ʹ�ʱ", overTimeActualAggWorkTimeData, BruiColor.Red);
				appendSummarySeries("ͼ��-��Դͼ��-�ۼƽ��", "�ۼ�ʵ�ʼӰ���", "�ۼ�ʵ�ʽ��", overTimeActualAggAmountData, BruiColor.Red);
			}
		}

		// ������Դͼ��
		JQ jq = new JQ("ͼ��-��Դͼ��").set("title", "").set("legendData", legendData).set("xAxisData", xAxisData).set("series", series);
		return jq.doc();
	}

	private void initializeData() {
		totalPlanAggWorkTimeData = new ArrayList<Double>();
		totalPlanAggAmountData = new ArrayList<Double>();
		basicPlanAggWorkTimeData = new ArrayList<Double>();
		basicPlanAggAmountData = new ArrayList<Double>();
		overTimePlanAggWorkTimeData = new ArrayList<Double>();
		overTimePlanAggAmountData = new ArrayList<Double>();

		totalActualAggWorkTimeData = new ArrayList<Double>();
		totalActualAggAmountData = new ArrayList<Double>();
		basicActualAggWorkTimeData = new ArrayList<Double>();
		basicActualAggAmountData = new ArrayList<Double>();
		overTimeActualAggWorkTimeData = new ArrayList<Double>();
		overTimeActualAggAmountData = new ArrayList<Double>();
	}

	private void initializeXAxis() {
		xAxisData = new ArrayList<String>();
		markLineData = new Document();
		group_id = new Document();

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
	 * ����ۼ�ϵ��
	 * 
	 * @param seriesJQ
	 * @param name
	 * @param stack
	 * @param data
	 * @param color
	 */
	private void appendSummarySeries(String seriesJQ, String name, String stack, ArrayList<Double> data, BruiColor color) {
		Document doc = new JQ(seriesJQ).set("name", name).set("color1", ColorTheme.getHtmlColor(color.getRgba(0xff)))
				.set("color2", ColorTheme.getHtmlColor(color.getRgba(0x66))).set("color3", ColorTheme.getHtmlColor(color.getRgba(0)))
				.set("stack", stack).set("data", data).doc();
		series.add(doc);
		legendData.add(name);
	}

	/**
	 * ��ӽڵ�ϵ��
	 * 
	 * @param timeChart
	 * @param name
	 * @param stack
	 * @param markLineData
	 * @param data
	 */
	private void appendCatalogSeries(String timeChart, String name, String stack, Object markLineData, Object data) {
		series.add(new JQ(timeChart).set("name", name).set("stack", stack).set("data", data).set("markLineData", markLineData).doc());
		legendData.add(name);
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
	private void appendSeries(String label) {
		String timeChart = "ͼ��-��Դͼ��-��ʱ";
		String amountChart = "ͼ��-��Դͼ��-���";
		if (dataType.contains("�ƻ�")) {
			// ��ȡ�ƻ���Դ����
			Object[] res = getResourceData("resourcePlan", "$planBasicQty", "$planOverTimeQty", match, group_id, xAxisData);
			if ("��׼".equals(showData)) {
				appendCatalogSeries(timeChart, label + "�ƻ���׼��ʱ", label + "�ƻ���ʱ", new Document(), res[0]);
				appendCatalogSeries(amountChart, label + "�ƻ���׼���", label + "�ƻ����", new Document(), res[3]);

				// �����ۼ�ֵ
				summary(totalPlanAggWorkTimeData, totalPlanAggAmountData, basicPlanAggWorkTimeData, basicPlanAggAmountData,
						(List<Double>) res[0], (List<Double>) res[3]);
			} else if ("�Ӱ�".equals(showData)) {
				appendCatalogSeries(timeChart, label + "�ƻ��Ӱ๤ʱ", label + "�ƻ���ʱ", new Document(), res[1]);
				appendCatalogSeries(amountChart, label + "�ƻ��Ӱ���", label + "�ƻ����", new Document(), res[4]);

				// �����ۼ�ֵ
				summary(totalPlanAggWorkTimeData, totalPlanAggAmountData, overTimePlanAggWorkTimeData, overTimePlanAggAmountData,
						(List<Double>) res[1], (List<Double>) res[4]);
			} else if ("����".equals(showData)) {
				appendCatalogSeries(timeChart, label + "�ƻ���ʱ", label + "�ƻ���ʱ", new Document(), res[2]);
				appendCatalogSeries(amountChart, label + "�ƻ����", label + "�ƻ����", new Document(), res[5]);

				// �����ۼ�ֵ
				summary(totalPlanAggWorkTimeData, totalPlanAggAmountData, basicPlanAggWorkTimeData, basicPlanAggAmountData,
						(List<Double>) res[0], (List<Double>) res[3]);

				summary(totalPlanAggWorkTimeData, totalPlanAggAmountData, overTimePlanAggWorkTimeData, overTimePlanAggAmountData,
						(List<Double>) res[1], (List<Double>) res[4]);
			} else {
				appendCatalogSeries(timeChart, label + "�ƻ���׼��ʱ", label + "�ƻ���ʱ", new Document(), res[0]);
				appendCatalogSeries(amountChart, label + "�ƻ���׼���", label + "�ƻ����", new Document(), res[3]);
				appendCatalogSeries(timeChart, label + "�ƻ��Ӱ๤ʱ", label + "�ƻ���ʱ", new Document(), res[1]);
				appendCatalogSeries(amountChart, label + "�ƻ��Ӱ���", label + "�ƻ����", new Document(), res[4]);

				// �����ۼ�ֵ
				summary(totalPlanAggWorkTimeData, totalPlanAggAmountData, basicPlanAggWorkTimeData, basicPlanAggAmountData,
						(List<Double>) res[0], (List<Double>) res[3]);
				summary(totalPlanAggWorkTimeData, totalPlanAggAmountData, overTimePlanAggWorkTimeData, overTimePlanAggAmountData,
						(List<Double>) res[1], (List<Double>) res[4]);
			}
		}
		if (dataType.contains("ʵ��")) {
			// ��ȡʵ����Դ����
			Object[] res = getResourceData("resourceActual", "$actualBasicQty", "$actualOverTimeQty", match, group_id, xAxisData);
			if ("��׼".equals(showData)) {
				appendCatalogSeries(timeChart, label + "ʵ�ʱ�׼��ʱ", label + "ʵ�ʹ�ʱ", new Document(), res[0]);
				appendCatalogSeries(amountChart, label + "ʵ�ʱ�׼���", label + "ʵ�ʽ��", new Document(), res[3]);

				// �����ۼ�ֵ
				summary(totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData, basicActualAggAmountData,
						(List<Double>) res[0], (List<Double>) res[3]);
			} else if ("�Ӱ�".equals(showData)) {
				if (markLineData.get("data") != null && res[6] != null)
					markLineData.put("data", Arrays.asList(new Document("yAxis", res[6]).append("name", label + "�����")));

				appendCatalogSeries(timeChart, label + "ʵ�ʼӰ๤ʱ", label + "ʵ�ʹ�ʱ", markLineData, res[1]);
				appendCatalogSeries(amountChart, label + "ʵ�ʼӰ���", label + "ʵ�ʽ��", new Document(), res[4]);

				// �����ۼ�ֵ
				summary(totalActualAggWorkTimeData, totalActualAggAmountData, overTimeActualAggWorkTimeData, overTimeActualAggAmountData,
						(List<Double>) res[1], (List<Double>) res[4]);
			} else if ("����".equals(showData)) {
				appendCatalogSeries(timeChart, label + "ʵ�ʹ�ʱ", label + "ʵ�ʹ�ʱ", new Document(), res[2]);
				appendCatalogSeries(amountChart, label + "ʵ�ʽ��", label + "ʵ�ʽ��", new Document(), res[5]);

				// �����ۼ�ֵ
				summary(totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData, basicActualAggAmountData,
						(List<Double>) res[0], (List<Double>) res[3]);

				summary(totalActualAggWorkTimeData, totalActualAggAmountData, overTimeActualAggWorkTimeData, overTimeActualAggAmountData,
						(List<Double>) res[1], (List<Double>) res[4]);
			} else {
				if (markLineData.get("data") != null && res[6] != null)
					markLineData.put("data", Arrays.asList(new Document("yAxis", res[6]).append("name", label + "�����")));

				appendCatalogSeries(timeChart, label + "ʵ�ʱ�׼��ʱ", label + "ʵ�ʹ�ʱ", new Document(), res[0]);
				appendCatalogSeries(amountChart, label + "ʵ�ʱ�׼���", label + "ʵ�ʽ��", new Document(), res[3]);
				appendCatalogSeries(timeChart, label + "ʵ�ʼӰ๤ʱ", label + "ʵ�ʹ�ʱ", markLineData, res[1]);
				appendCatalogSeries(amountChart, label + "ʵ�ʼӰ���", label + "ʵ�ʽ��", new Document(), res[4]);

				// �����ۼ�ֵ
				summary(totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData, basicActualAggAmountData,
						(List<Double>) res[0], (List<Double>) res[3]);
				summary(totalActualAggWorkTimeData, totalActualAggAmountData, overTimeActualAggWorkTimeData, overTimeActualAggAmountData,
						(List<Double>) res[1], (List<Double>) res[4]);
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
	private void summary(List<Double> totalAggWorkTimeData, List<Double> totalAggAmountData, List<Double> aggWorkTimeData,
			List<Double> aggAmountData, List<Double> resourceWorkTimeData, List<Double> resourceAmountData) {
		if ("�ܼ�".equals(aggregateType)) {
			summary(totalAggWorkTimeData, totalAggAmountData, resourceWorkTimeData, resourceAmountData);
		} else if ("��׼�Ӱ�".equals(aggregateType)) {
			summary(aggWorkTimeData, aggAmountData, resourceWorkTimeData, resourceAmountData);
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
	private void summary(List<Double> aggWorkTimeDatas, List<Double> aggAmountDatas, List<Double> resourceWorkTimeData,
			List<Double> resourceAmountData) {
		double wd = 0d;
		double ad = 0d;
		if (aggWorkTimeDatas.size() > 0) {
			for (int i = 0; i < aggWorkTimeDatas.size(); i++) {
				wd += Optional.ofNullable(resourceWorkTimeData.get(i)).orElse(0d);
				double aggWorkTimeData = wd + Optional.ofNullable(aggWorkTimeDatas.get(i)).orElse(0d);
				aggWorkTimeDatas.set(i, aggWorkTimeData);

				ad += Optional.ofNullable(resourceAmountData.get(i)).orElse(0d);
				double aggAmountData = ad + Optional.ofNullable(aggAmountDatas.get(i)).orElse(0d);
				aggAmountDatas.set(i, aggAmountData);
			}
		} else {
			for (Double d : resourceWorkTimeData) {
				if (d != null)
					wd += d;
				aggWorkTimeDatas.add(wd);
			}
			for (Double d : resourceAmountData) {
				if (d != null)
					ad += d;
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
	private List<ObjectId> listResource(Document doc) {
		List<ObjectId> result = new ArrayList<>();
		String type = doc.getString("type");
		if (ResourceType.class.getName().equals(type)) {
			Document condition = new Document("org_id", ((Document) doc.get("meta")).get("org_id")).append("resourceType_id",
					doc.get("_id"));
			c("user").find(condition).map(d -> d.getObjectId("_id")).into(result);
			c("equipment").find(condition).map(d -> d.getObjectId("_id")).into(result);
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
		// doc.put("childResourceIds", result);
		return result;
	}

}
