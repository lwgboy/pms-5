package com.bizvisionsoft.serviceimpl.renderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.bizvisionsoft.serviceimpl.renderer.ColorTheme.BruiColor;

public class ResourceChartRenderer extends BasicServiceImpl {

	private class ResourceData {

		private Double[] basicPlanTime;

		private Double[] extraPlanTime;

		private Double[] totalPlanTime;

		private Double[] basicPlanAmounts;

		private Double[] extraPlanAmounts;

		private Double[] totalPlanAmounts;

		List<Double> basicPlanTime() {
			return list(basicPlanTime);
		}

		List<Double> extraPlanTime() {
			return list(extraPlanTime);
		}

		List<Double> totalPlanTime() {
			return list(totalPlanTime);
		}

		List<Double> basicPlanAmounts() {
			return list(basicPlanAmounts);
		}

		List<Double> extraPlanAmounts() {
			return list(extraPlanAmounts);
		}

		List<Double> totalPlanAmounts() {
			return list(totalPlanAmounts);
		}

		private Double[] basicActualTime;

		private Double[] extraActualTime;

		private Double[] totalActualTime;

		private Double[] basicActualAmounts;

		private Double[] extraActualAmounts;

		private Double[] totalActualAmounts;

		List<Double> basicActualTime() {
			return list(basicActualTime);
		}

		List<Double> extraActualTime() {
			return list(extraActualTime);
		}

		List<Double> totalActualTime() {
			return list(totalActualTime);
		}

		List<Double> basicActualAmounts() {
			return list(basicActualAmounts);
		}

		List<Double> extraActualAmounts() {
			return list(extraActualAmounts);
		}

		List<Double> totalActualAmounts() {
			return list(totalActualAmounts);
		}

		ResourceData(int xAxisCount) {
			basicPlanTime = new Double[xAxisCount];
			extraPlanTime = new Double[xAxisCount];
			totalPlanTime = new Double[xAxisCount];
			basicPlanAmounts = new Double[xAxisCount];
			extraPlanAmounts = new Double[xAxisCount];
			totalPlanAmounts = new Double[xAxisCount];
			basicActualTime = new Double[xAxisCount];
			extraActualTime = new Double[xAxisCount];
			totalActualTime = new Double[xAxisCount];
			basicActualAmounts = new Double[xAxisCount];
			extraActualAmounts = new Double[xAxisCount];
			totalActualAmounts = new Double[xAxisCount];
		}

		void build(Document doc) {
			int idx = xAxisData.indexOf(((Document) doc.get("_id")).getString("id"));
			if (idx != -1) {
				if ("plan".equals(((Document) doc.get("_id")).getString("type"))) {
					// ���ؼƻ�����
					basicPlanTime[idx] = doc.getDouble("basicTime");
					extraPlanTime[idx] = doc.getDouble("extraTime");
					totalPlanTime[idx] = doc.getDouble("sumTime");
					basicPlanAmounts[idx] = doc.getDouble("basicAmount");
					extraPlanAmounts[idx] = doc.getDouble("extraAmount");
					totalPlanAmounts[idx] = doc.getDouble("sumAmount");

					// ����ƻ�����ֵ
					summary(idx, xAxisData.size(), basicPlanAggTime, doc.getDouble("basicTime"));
					summary(idx, xAxisData.size(), extraPlanAggTime, doc.getDouble("extraTime"));
					summary(idx, xAxisData.size(), totalPlanAggTime, doc.getDouble("sumTime"));
					summary(idx, xAxisData.size(), basicPlanAggAmount, doc.getDouble("basicAmount"));
					summary(idx, xAxisData.size(), extraPlanAggAmount, doc.getDouble("extraAmount"));
					summary(idx, xAxisData.size(), totalPlanAggAmount, doc.getDouble("sumAmount"));
				} else {
					// ����ʵ������
					basicActualTime[idx] = doc.getDouble("basicTime");
					extraActualTime[idx] = doc.getDouble("extraTime");
					totalActualTime[idx] = doc.getDouble("sumTime");
					basicActualAmounts[idx] = doc.getDouble("basicAmount");
					extraActualAmounts[idx] = doc.getDouble("extraAmount");
					totalActualAmounts[idx] = doc.getDouble("sumAmount");

					// ����ʵ�ʻ���ֵ
					summary(idx, xAxisData.size(), basicActualAggTime, doc.getDouble("basicTime"));
					summary(idx, xAxisData.size(), extraActualAggTime, doc.getDouble("extraTime"));
					summary(idx, xAxisData.size(), totalActualAggTime, doc.getDouble("sumTime"));
					summary(idx, xAxisData.size(), basicActualAggAmount, doc.getDouble("basicAmount"));
					summary(idx, xAxisData.size(), extraActualAggAmount, doc.getDouble("extraAmount"));
					summary(idx, xAxisData.size(), totalActualAggAmount, doc.getDouble("sumAmount"));
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
	private Double[] totalPlanAggTime;
	private Double[] totalPlanAggAmount;
	private Double[] basicPlanAggTime;
	private Double[] basicPlanAggAmount;
	private Double[] extraPlanAggTime;
	private Double[] extraPlanAggAmount;
	private Double[] totalActualAggTime;
	private Double[] totalActualAggAmount;
	private Double[] basicActualAggTime;
	private Double[] basicActualAggAmount;
	private Double[] extraActualAggTime;
	private Double[] extraActualAggAmount;

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

		// ����ʱ�����͡�ϵ�����͡��ƻ�/ʵ�ʣ�����group
		initializeGroup();
		// ����ʱ�����ͺ���ֹʱ�䣬����xAxis������ʼ���ۼ�ֵ
		initializeXAxis();
		// �����ۼ����ͣ�����Y��
		initializeYAxis();
		// ��ʼ�������ֶΣ�������match
		initializeMatch();

		// ����ϵ�����͹���series
		appendSeries();

		// ������Դͼ��
		JQ jq = new JQ("ͼ��-��Դͼ��").set("title", "").set("legendData", legendData).set("xAxisData", xAxisData).set("yAxis", yAxis)
				.set("series", series);
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
			String timeChart = "ͼ��-��Դͼ��-��ʱ";
			String amountChart = "ͼ��-��Դͼ��-���";
			ResourceData r = resData.get(key);
			if (dataType.contains("�ƻ�")) {
				// ��ȡ�ƻ���Դ����
				if ("��׼".equals(showData)) {
					appendCatalog(timeChart, key + "�ƻ���׼��ʱ", key + "�ƻ���ʱ", r.basicPlanTime());
					appendCatalog(amountChart, key + "�ƻ���׼���", key + "�ƻ����", r.basicPlanAmounts());
				} else if ("�Ӱ�".equals(showData)) {
					appendCatalog(timeChart, key + "�ƻ��Ӱ๤ʱ", key + "�ƻ���ʱ", r.extraPlanTime());
					appendCatalog(amountChart, key + "�ƻ��Ӱ���", key + "�ƻ����", r.extraPlanAmounts());
				} else if ("����".equals(showData)) {
					appendCatalog(timeChart, key + "�ƻ���ʱ", key + "�ƻ���ʱ", r.totalPlanTime());
					appendCatalog(amountChart, key + "�ƻ����", key + "�ƻ����", r.totalPlanAmounts());
				} else {
					appendCatalog(timeChart, key + "�ƻ���׼��ʱ", key + "�ƻ���ʱ", r.basicPlanTime());
					appendCatalog(amountChart, key + "�ƻ���׼���", key + "�ƻ����", r.basicPlanAmounts());
					appendCatalog(timeChart, key + "�ƻ��Ӱ๤ʱ", key + "�ƻ���ʱ", r.extraPlanTime());
					appendCatalog(amountChart, key + "�ƻ��Ӱ���", key + "�ƻ����", r.extraPlanAmounts());
				}
			}
			if (dataType.contains("ʵ��")) {
				// ��ȡʵ����Դ����
				if ("��׼".equals(showData)) {
					appendCatalog(timeChart, key + "ʵ�ʱ�׼��ʱ", key + "ʵ�ʹ�ʱ", r.basicActualTime());
					appendCatalog(amountChart, key + "ʵ�ʱ�׼���", key + "ʵ�ʽ��", r.basicActualAmounts());
				} else if ("�Ӱ�".equals(showData)) {
					appendCatalog(timeChart, key + "ʵ�ʼӰ๤ʱ", key + "ʵ�ʹ�ʱ", r.extraActualTime());
					appendCatalog(amountChart, key + "ʵ�ʼӰ���", key + "ʵ�ʽ��", r.extraActualAmounts());
				} else if ("����".equals(showData)) {
					appendCatalog(timeChart, key + "ʵ�ʹ�ʱ", key + "ʵ�ʹ�ʱ", r.totalActualTime());
					appendCatalog(amountChart, key + "ʵ�ʽ��", key + "ʵ�ʽ��", r.totalActualAmounts());
				} else {
					appendCatalog(timeChart, key + "ʵ�ʱ�׼��ʱ", key + "ʵ�ʹ�ʱ", r.basicActualTime());
					appendCatalog(amountChart, key + "ʵ�ʱ�׼���", key + "ʵ�ʽ��", r.basicActualAmounts());
					appendCatalog(timeChart, key + "ʵ�ʼӰ๤ʱ", key + "ʵ�ʹ�ʱ", r.extraActualTime());
					appendCatalog(amountChart, key + "ʵ�ʼӰ���", key + "ʵ�ʽ��", r.extraActualAmounts());

				}

			}
		}

		// �����ۼ�ֵ���͹���series
		if ("�ܼ�".equals(aggregateType)) {
			if (dataType.contains("�ƻ�")) {
				appendSummary("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼƼƻ���ʱ", "�ۼƼƻ���ʱ", list(totalPlanAggTime), BruiColor.Light_Blue);
				appendSummary("ͼ��-��Դͼ��-�ۼƽ��", "�ۼƼƻ����", "�ۼƼƻ����", list(totalPlanAggAmount), BruiColor.Light_Blue);
			}
			if (dataType.contains("ʵ��")) {
				appendSummary("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼ�ʵ�ʹ�ʱ", "�ۼ�ʵ�ʹ�ʱ", list(totalActualAggTime), BruiColor.Teal);
				appendSummary("ͼ��-��Դͼ��-�ۼƽ��", "�ۼ�ʵ�ʽ��", "�ۼ�ʵ�ʽ��", list(totalActualAggAmount), BruiColor.Teal);
			}
		} else if ("��׼�Ӱ�".equals(aggregateType)) {
			if (dataType.contains("�ƻ�")) {
				appendSummary("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼƼƻ���׼��ʱ", "�ۼƼƻ���ʱ", list(basicPlanAggTime), BruiColor.Light_Blue);
				appendSummary("ͼ��-��Դͼ��-�ۼƽ��", "�ۼƼƻ���׼���", "�ۼƼƻ����", list(basicPlanAggAmount), BruiColor.Light_Blue);

				appendSummary("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼƼƻ��Ӱ๤ʱ", "�ۼƼƻ���ʱ", list(extraPlanAggTime), BruiColor.Orange);
				appendSummary("ͼ��-��Դͼ��-�ۼƽ��", "�ۼƼƻ��Ӱ���", "�ۼƼƻ����", list(extraPlanAggAmount), BruiColor.Orange);
			}
			if (dataType.contains("ʵ��")) {
				appendSummary("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼ�ʵ�ʱ�׼��ʱ", "�ۼ�ʵ�ʹ�ʱ", list(basicActualAggTime), BruiColor.Teal);
				appendSummary("ͼ��-��Դͼ��-�ۼƽ��", "�ۼ�ʵ�ʱ�׼���", "�ۼ�ʵ�ʽ��", list(basicActualAggAmount), BruiColor.Teal);

				appendSummary("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼ�ʵ�ʼӰ๤ʱ", "�ۼ�ʵ�ʹ�ʱ", list(extraActualAggTime), BruiColor.Red);
				appendSummary("ͼ��-��Դͼ��-�ۼƽ��", "�ۼ�ʵ�ʼӰ���", "�ۼ�ʵ�ʽ��", list(extraActualAggAmount), BruiColor.Red);
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
		totalPlanAggTime = agg.toArray(new Double[0]);
		totalPlanAggAmount = agg.toArray(new Double[0]);
		basicPlanAggTime = agg.toArray(new Double[0]);
		basicPlanAggAmount = agg.toArray(new Double[0]);
		extraPlanAggTime = agg.toArray(new Double[0]);
		extraPlanAggAmount = agg.toArray(new Double[0]);

		totalActualAggTime = agg.toArray(new Double[0]);
		totalActualAggAmount = agg.toArray(new Double[0]);
		basicActualAggTime = agg.toArray(new Double[0]);
		basicActualAggAmount = agg.toArray(new Double[0]);
		extraActualAggTime = agg.toArray(new Double[0]);
		extraActualAggAmount = agg.toArray(new Double[0]);
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

	/**
	 * ����ۼ�ϵ��
	 * 
	 * @param seriesJQ
	 * @param name
	 * @param stack
	 * @param data
	 * @param color
	 */
	private void appendSummary(String seriesJQ, String name, String stack, List<Double> data, BruiColor color) {
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
	private void appendCatalog(String timeChart, String name, String stack, List<Double> data) {
		series.add(new JQ(timeChart).set("name", name).set("stack", stack).set("data", data).doc());
		legendData.add(name);
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
		List<Bson> pipeline = new JQ("��ͼ-��Դ�ƻ���ʵ������").array();
		pipeline.add(new Document("$match", match));
		pipeline.add(new Document("$group", group));
		pipeline.add(new Document("$sort", new Document("_id.resName", 1).append("_id.id", 1).append("_id.type", 1)));
		c("resourceType").aggregate(pipeline).forEach((Document d) -> {
			resData.build(d);
		});
		return resData;
	}

	private List<Double> list(Double[] arr) {
		return Arrays.asList(arr).stream().map(d -> d == null ? 0d : d).collect(Collectors.toList());
	}

	private void summary(int idx, int xAxisCount, Double[] Agg, Double value) {
		for (int i = idx; i < xAxisCount; i++) {
			Agg[i] += value;
		}
	}
}
