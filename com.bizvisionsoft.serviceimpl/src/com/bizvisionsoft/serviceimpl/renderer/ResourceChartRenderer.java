package com.bizvisionsoft.serviceimpl.renderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.common.JQ;
import com.bizvisionsoft.service.tools.ColorTheme;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.ColorTheme.BruiColor;
import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;

public class ResourceChartRenderer extends BasicServiceImpl {

	private static final String Chart = "ͼ��-��Դͼ��";

	private static final String TimeChart = "ͼ��-��Դͼ��-��ʱ";

	private static final String AmountChart = "ͼ��-��Դͼ��-���";

	private static final String TimeSummaryChart = "ͼ��-��Դͼ��-�ۼƹ�ʱ";

	private static final String AmountSummaryChart = "ͼ��-��Դͼ��-�ۼƽ��";

	private static final String PlanTime = "�ƻ���ʱ";

	private static final String PlanAmount = "�ƻ����";

	private static final String ActualTime = "ʵ�ʹ�ʱ";

	private static final String ActualAmount = "ʵ�ʽ��";

	private static final String Basic = "����׼��";
	private static final String Extra = "���Ӱࣩ";
	private static final String Prefix_Sum = "�ۼ�";

	private class ResourceData {

		private double[] basicPlanTime;

		private double[] extraPlanTime;

		private double[] totalPlanTime;

		private double[] basicPlanAmount;

		private double[] extraPlanAmount;

		private double[] totalPlanAmount;

		private double[] basicActualTime;

		private double[] extraActualTime;

		private double[] totalActualTime;

		private double[] basicActualAmount;

		private double[] extraActualAmount;

		private double[] totalActualAmount;

		ResourceData(int xAxisCount) {
			basicPlanTime = new double[xAxisCount];
			extraPlanTime = new double[xAxisCount];
			totalPlanTime = new double[xAxisCount];
			basicPlanAmount = new double[xAxisCount];
			extraPlanAmount = new double[xAxisCount];
			totalPlanAmount = new double[xAxisCount];
			basicActualTime = new double[xAxisCount];
			extraActualTime = new double[xAxisCount];
			totalActualTime = new double[xAxisCount];
			basicActualAmount = new double[xAxisCount];
			extraActualAmount = new double[xAxisCount];
			totalActualAmount = new double[xAxisCount];
		}

		void build(Document doc) {
			int idx = xAxisData.indexOf(((Document) doc.get("_id")).getString("id"));
			if (idx != -1) {
				double basicTime = Optional.ofNullable(doc.getDouble("basicTime")).orElse(0d);
				double extraTime = Optional.ofNullable(doc.getDouble("extraTime")).orElse(0d);
				double sumTime = Optional.ofNullable(doc.getDouble("sumTime")).orElse(0d);
				double basicAmount = Optional.ofNullable(doc.getDouble("basicAmount")).orElse(0d);
				double extraAmount = Optional.ofNullable(doc.getDouble("extraAmount")).orElse(0d);
				double sumAmount = Optional.ofNullable(doc.getDouble("sumAmount")).orElse(0d);

				if ("plan".equals(((Document) doc.get("_id")).getString("type"))) {
					// ���ؼƻ�����
					basicPlanTime[idx] = basicTime;
					extraPlanTime[idx] = extraTime;
					totalPlanTime[idx] = sumTime;
					basicPlanAmount[idx] = basicAmount;
					extraPlanAmount[idx] = extraAmount;
					totalPlanAmount[idx] = sumAmount;

					// ����ƻ�����ֵ
					summary(idx, xAxisData.size(), basicPlanAggTime, basicTime);
					summary(idx, xAxisData.size(), extraPlanAggTime, extraTime);
					summary(idx, xAxisData.size(), totalPlanAggTime, sumTime);
					summary(idx, xAxisData.size(), basicPlanAggAmount, basicAmount);
					summary(idx, xAxisData.size(), extraPlanAggAmount, extraAmount);
					summary(idx, xAxisData.size(), totalPlanAggAmount, sumAmount);
				} else {
					// ����ʵ������
					basicActualTime[idx] = basicTime;
					extraActualTime[idx] = extraTime;
					totalActualTime[idx] = sumTime;
					basicActualAmount[idx] = basicAmount;
					extraActualAmount[idx] = extraAmount;
					totalActualAmount[idx] = sumAmount;

					// ����ʵ�ʻ���ֵ
					summary(idx, xAxisData.size(), basicActualAggTime, basicTime);
					summary(idx, xAxisData.size(), extraActualAggTime, extraTime);
					summary(idx, xAxisData.size(), totalActualAggTime, sumTime);
					summary(idx, xAxisData.size(), basicActualAggAmount, basicAmount);
					summary(idx, xAxisData.size(), extraActualAggAmount, extraAmount);
					summary(idx, xAxisData.size(), totalActualAggAmount, sumAmount);
				}
			}
			// Double bw = doc.getDouble("basicWorks");
			// if (bw != null) {
			// basicWorks = bw.doubleValue();
			// }
		}
	}

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
	private List<Document> yAxis;
	// private Document markLineData;
	private Document group;
	private Document match;
	private double[] totalPlanAggTime;
	private double[] totalPlanAggAmount;
	private double[] basicPlanAggTime;
	private double[] basicPlanAggAmount;
	private double[] extraPlanAggTime;
	private double[] extraPlanAggAmount;
	private double[] totalActualAggTime;
	private double[] totalActualAggAmount;
	private double[] basicActualAggTime;
	private double[] basicActualAggAmount;
	private double[] extraActualAggTime;
	private double[] extraActualAggAmount;

	private String domain;

	@SuppressWarnings("unchecked")
	public ResourceChartRenderer(Document condition, String domain) {
		this.domain = domain;
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

		// ����ʱ�����͡�ϵ�����͡��ƻ�/ʵ�ʣ�����group
		initializeGroup();
		// ����ʱ�����ͺ���ֹʱ�䣬����xAxis������ʼ���ۼ�ֵ
		initializeXAxis();
		// �����ۼ����ͣ�����Y��
		initializeYAxis();
		// ����match
		initializeMatch();

		// ����ϵ�����͹���series
		appendSeries();

		// ������Դͼ��
		JQ jq = Domain.getJQ(domain, Chart).set("title", "").set("legendData", legendData).set("xAxisData", xAxisData).set("yAxis", yAxis).set("series",
				series);
		return jq.doc();
	}

	/**
	 * ����Series
	 * 
	 * @param resData
	 */
	private void appendSeries() {

		Map<String, ResourceData> resData = query();
		for (String key : resData.keySet()) {

			ResourceData r = resData.get(key);
			if (dataType.contains("�ƻ�")) {
				// ��ȡ�ƻ���Դ����
				if ("��׼".equals(showData)) {
					appendCatalog(TimeChart, key, Basic, PlanTime, r.basicPlanTime);
					appendCatalog(AmountChart, key, Basic, PlanAmount, r.basicPlanAmount);
				} else if ("�Ӱ�".equals(showData)) {
					appendCatalog(TimeChart, key, Extra, PlanTime, r.extraPlanTime);
					appendCatalog(AmountChart, key, Extra, PlanAmount, r.extraPlanAmount);
				} else if ("����".equals(showData)) {
					appendCatalog(TimeChart, key, "", PlanTime, r.totalPlanTime);
					appendCatalog(AmountChart, key, "", PlanAmount, r.totalPlanAmount);
				} else if ("����".equals(showData)) {
					appendCatalog(TimeChart, key, Basic, PlanTime, r.basicPlanTime);
					appendCatalog(AmountChart, key, Basic, PlanAmount, r.basicPlanAmount);
					appendCatalog(TimeChart, key, Extra, PlanTime, r.extraPlanTime);
					appendCatalog(AmountChart, key, Extra, PlanAmount, r.extraPlanAmount);
				}
			}
			if (dataType.contains("ʵ��")) {
				// ��ȡʵ����Դ����
				if ("��׼".equals(showData)) {
					appendCatalog(TimeChart, key, Basic, ActualTime, r.basicActualTime);
					appendCatalog(AmountChart, key, Basic, ActualAmount, r.basicActualAmount);
				} else if ("�Ӱ�".equals(showData)) {
					appendCatalog(TimeChart, key, Extra, ActualTime, r.extraActualTime);
					appendCatalog(AmountChart, key, Extra, ActualAmount, r.extraActualAmount);
				} else if ("����".equals(showData)) {
					appendCatalog(TimeChart, key, "", ActualTime, r.totalActualTime);
					appendCatalog(AmountChart, key, "", ActualAmount, r.totalActualAmount);
				} else if ("����".equals(showData)) {
					appendCatalog(TimeChart, key, Basic, ActualTime, r.basicActualTime);
					appendCatalog(AmountChart, key, Basic, ActualAmount, r.basicActualAmount);
					appendCatalog(TimeChart, key, Extra, ActualTime, r.extraActualTime);
					appendCatalog(AmountChart, key, Extra, ActualAmount, r.extraActualAmount);
				}
			}
		}

		// �����ۼ�ֵ���͹���series
		if ("�ܼ�".equals(aggregateType)) {
			if (dataType.contains("�ƻ�")) {
				appendSummary(TimeSummaryChart, "", PlanTime, totalPlanAggTime, BruiColor.Light_Blue);
				appendSummary(AmountSummaryChart, "", PlanAmount, totalPlanAggAmount, BruiColor.Light_Blue);
			}
			if (dataType.contains("ʵ��")) {
				appendSummary(TimeSummaryChart, "", ActualTime, totalActualAggTime, BruiColor.Teal);
				appendSummary(AmountSummaryChart, "", ActualAmount, totalActualAggAmount, BruiColor.Teal);
			}
		} else if ("��׼�Ӱ�".equals(aggregateType)) {
			if (dataType.contains("�ƻ�")) {
				appendSummary(TimeSummaryChart, Basic, PlanTime, basicPlanAggTime, BruiColor.Light_Blue);
				appendSummary(AmountSummaryChart, Basic, PlanAmount, basicPlanAggAmount, BruiColor.Light_Blue);

				appendSummary(TimeSummaryChart, Extra, PlanTime, extraPlanAggTime, BruiColor.Orange);
				appendSummary(AmountSummaryChart, Extra, PlanAmount, extraPlanAggAmount, BruiColor.Orange);
			}
			if (dataType.contains("ʵ��")) {
				appendSummary(TimeSummaryChart, Basic, ActualTime, basicActualAggTime, BruiColor.Teal);
				appendSummary(AmountSummaryChart, Basic, ActualAmount, basicActualAggAmount, BruiColor.Teal);

				appendSummary(TimeSummaryChart, Extra, ActualTime, extraActualAggTime, BruiColor.Red);
				appendSummary(AmountSummaryChart, Extra, ActualAmount, extraActualAggAmount, BruiColor.Red);
			}
		}
	}

	/**
	 * ������ѯ
	 */
	private void initializeMatch() {
		match = new Document("id", new Document("$gte", start).append("$lte", end));
		List<String> typeData = new ArrayList<>();
		if (dataType.contains("�ƻ�")) {
			typeData.add("plan");
		}
		if (dataType.contains("ʵ��")) {
			typeData.add("actual");
		}
		match.append("type", new Document("$in", typeData));
	}

	/**
	 * ��������
	 */
	private void initializeGroup() {
		Document groupId = new Document();
		if ("��".equals(dateType)) {
			groupId.append("id", new Document("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$id")));
		} else if ("��".equals(dateType)) {
			groupId.append("id", new Document("$dateToString", new Document("format", "%Y-%m").append("date", "$id")));
		} else {
			groupId.append("id", new Document("$dateToString", new Document("format", "%Y").append("date", "$id")));
		}
		groupId.append("type", "$type");

		group = new Document("_id", groupId).append("basicTime", new Document("$sum", "$basicTime"))
				.append("basicAmount", new Document("$sum", "$basicAmount")).append("extraTime", new Document("$sum", "$extraTime"))
				.append("extraAmount", new Document("$sum", "$extraAmount")).append("sumTime", new Document("$sum", "$sumTime"))
				.append("sumAmount", new Document("$sum", "$sumAmount"));

	}

	/**
	 * ����ʱ�����ͺ���ֹʱ�䣬����xAxis������ʼ���ۼ�ֵ
	 */
	private void initializeXAxis() {
		xAxisData = new ArrayList<String>();
		// markLineData = new Document();
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		List<Double> agg = new ArrayList<Double>();
		SimpleDateFormat sdf;
		if ("��".equals(dateType)) {
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				agg.add(0d);
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
			// TODO ��Ҫ������ѡ��Դ����Դ������������
			// markLineData.append("data", "");
		} else if ("��".equals(dateType)) {
			sdf = new SimpleDateFormat("yyyy-MM");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				agg.add(0d);
				cal.add(Calendar.MONTH, 1);
			}
		} else {
			sdf = new SimpleDateFormat("yyyy");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				agg.add(0d);
				cal.add(Calendar.YEAR, 1);
			}
		}
		totalPlanAggTime = Formatter.toArray(agg);
		totalPlanAggAmount = Formatter.toArray(agg);
		basicPlanAggTime = Formatter.toArray(agg);
		basicPlanAggAmount = Formatter.toArray(agg);
		extraPlanAggTime = Formatter.toArray(agg);
		extraPlanAggAmount = Formatter.toArray(agg);

		totalActualAggTime = Formatter.toArray(agg);
		totalActualAggAmount = Formatter.toArray(agg);
		basicActualAggTime = Formatter.toArray(agg);
		basicActualAggAmount = Formatter.toArray(agg);
		extraActualAggTime = Formatter.toArray(agg);
		extraActualAggAmount = Formatter.toArray(agg);
	}

	/**
	 * ����Y������
	 */
	private void initializeYAxis() {
		yAxis = new ArrayList<>();
		// ���ع�ʱY��
		yAxis.add(new Document("type", "value").append("name", "��ʱ��Сʱ��").append("axisLabel", new Document("formatter", "{value}")));
		// ���ؽ��Y��
		yAxis.add(new Document("type", "value").append("name", "��Ԫ��").append("axisLabel", new Document("formatter", "{value}"))
				.append("gridIndex", 1));

		if (!"���ۼ�".equals(aggregateType)) {
			// �����ۼƹ�ʱY��
			yAxis.add(new Document("type", "value").append("name", "�ۼƹ�ʱ��Сʱ��").append("axisLabel", new Document("formatter", "{value}")));
			// �����ۼƽ��Y��
			yAxis.add(new Document("type", "value").append("name", "�ۼƽ�Ԫ��").append("axisLabel", new Document("formatter", "{value}"))
					.append("gridIndex", 1));
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

	private void appendSummary(String seriesJQ, String postfix, String stack, double[] ds, BruiColor color) {
		List<Double> data = Formatter.toList(ds);
		Document doc = Domain.getJQ(domain, seriesJQ).set("name", Prefix_Sum + stack + postfix)
				.set("color1", ColorTheme.getHtmlColor(color.getRgba(0xff))).set("color2", ColorTheme.getHtmlColor(color.getRgba(0x66)))
				.set("color3", ColorTheme.getHtmlColor(color.getRgba(0))).set("stack", Prefix_Sum + stack).set("data", data).doc();
		series.add(doc);
		legendData.add(Prefix_Sum + stack + postfix);
	}

	/**
	 * ��ӽڵ�ϵ��
	 * 
	 * @param TimeChart
	 * @param name
	 * @param stack
	 * @param markLineData
	 * @param data
	 */
	private void appendCatalog(String timeChart, String seriesName, String postfix, String stack, double[] ds) {
		List<Double> data = Formatter.toList(ds);
		series.add(Domain.getJQ(domain, timeChart).set("name", seriesName + stack + postfix).set("stack", seriesName + stack).set("data", data).doc());
		legendData.add(stack + postfix);
	}

	// ��ѯ��ȡ����
	private Map<String, ResourceData> query() {
		Map<String, ResourceData> resDataMap = new HashMap<String, ResourceData>();
		if ("����".equals(seriesType)) {
			input.forEach(d -> {
				Document inputMatch = new Document(match);
				inputMatch.putAll((Document) d.get("match"));
				resDataMap.put(d.getString("label"), query(inputMatch));
			});
		} else {
			List<Object> list = new ArrayList<Object>();
			input.forEach(d -> {
				list.add(d.get("match"));
			});
			match.append("$or", list);
			resDataMap.put("", query(match));
		}
		return resDataMap;
	}

	private ResourceData query(Document match) {
		ResourceData resData = new ResourceData(xAxisData.size());
		List<Bson> pipeline = Domain.getJQ(domain, "��ͼ-��Դ�ƻ���ʵ������").array();
		pipeline.add(new Document("$match", match));
		pipeline.add(new Document("$group", group));
		pipeline.add(new Document("$sort", new Document("_id.resName", 1).append("_id.id", 1).append("_id.type", 1)));
		c("resourceType", domain).aggregate(pipeline).forEach((Document d) -> {
			resData.build(d);
		});
		return resData;
	}

	private void summary(int idx, int xAxisCount, double[] Agg, double value) {
		for (int i = idx; i < xAxisCount; i++) {
			Agg[i] += value;
		}
	}
}
