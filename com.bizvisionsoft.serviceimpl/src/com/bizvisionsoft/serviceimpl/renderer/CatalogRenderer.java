package com.bizvisionsoft.serviceimpl.renderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.serviceimpl.query.JQ;

public class CatalogRenderer {

	private String dateType;

	private List<String> dataType;

	private String showData;

	private String aggregateType;

	private String seriesType;

	private List<Document> input;

	private Document group_id;

	private Date start;

	private Date end;

	private List<String> xAxisData;

	private List<Document> series;

	private List<String> legendData;

	private Document markLineData;

	private String[][] color = new String[][] { { "rgba(95,184,120,1)", "rgba(95,184,120,0.7)", "rgba(95,184,120,0)" },
			{ "rgba(0,150,136,1)", "rgba(0,150,136,0.7)", "rgba(0,150,136,0)" },
			{ "rgba(255,87,34,1)", "rgba(255,87,34,0.7)", "rgba(255,87,34,0)" },
			{ "rgba(255,184,0,1)", "rgba(255,184,0,0.7)", "rgba(255,184,0,0)" } };

	@SuppressWarnings("unchecked")
	public CatalogRenderer(Document option) {
		List<?> dateRange = (List<?>) option.get("dateRange");
		dateType = option.getString("dateType");
		showData = option.getString("showData");
		aggregateType = option.getString("aggregateType");

		seriesType = option.getString("seriesType");
		dataType = (List<String>) option.get("dataType");

		start = (Date) dateRange.get(0);
		end = (Date) dateRange.get(1);

	}

	public String getSeriesType() {
		return seriesType;
	}

	public List<String> getDataType() {
		return dataType;
	}

	private Document getMarkLineData() {
		if (markLineData == null) {
			markLineData = new Document();
			if ("日".equals(dateType)) {
				markLineData.append("data", "");
			}
		}
		return markLineData;
	}

	public List<Document> getSeries() {
		return series;
	}

	public List<String> getLegendData() {
		return legendData;
	}

	public Document getGroup_id() {
		if (group_id == null) {
			group_id = new Document();
			if ("日".equals(dateType)) {
				group_id.append("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$id"));
			} else if ("月".equals(dateType)) {
				group_id.append("$dateToString", new Document("format", "%Y-%m").append("date", "$id"));
			} else {
				group_id.append("$dateToString", new Document("format", "%Y").append("date", "$id"));
			}
		}
		return group_id;
	}

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}

	public List<String> getxAxisData() {
		if (xAxisData == null) {
			xAxisData = new ArrayList<String>();
			SimpleDateFormat sdf;
			Calendar cal = Calendar.getInstance();
			cal.setTime(start);
			if ("日".equals(dateType)) {
				sdf = new SimpleDateFormat("yyyy-MM-dd");
				while (!cal.getTime().after(end)) {
					xAxisData.add(sdf.format(cal.getTime()));
					cal.add(Calendar.DAY_OF_MONTH, 1);
				}
			} else if ("月".equals(dateType)) {
				sdf = new SimpleDateFormat("yyyy-MM");
				while (!cal.getTime().after(end)) {
					xAxisData.add(sdf.format(cal.getTime()));
					cal.add(Calendar.MONTH, 1);
				}
			} else {
				sdf = new SimpleDateFormat("yyyy");
				while (!cal.getTime().after(end)) {
					xAxisData.add(sdf.format(cal.getTime()));
					cal.add(Calendar.YEAR, 1);
				}
			}
		}
		return xAxisData;
	}

	public void setInput(List<Document> input) {
		this.input = input;
	}

	@SuppressWarnings("unchecked")
	public void loadSeries() {
		series = new ArrayList<Document>();
		legendData = new ArrayList<String>();

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

		input.forEach((Document doc) -> {
			String label = doc.getString("label");
			if (dataType.contains("计划")) {
				Object plan = doc.get("plan");
				if (plan instanceof Document) {
					appendWorkTimeSeries(label + "计划", (Document) plan, new Document());
					appendAmountSeries(label + "计划", (Document) plan);
					if ("总计".equals(aggregateType)) {
						calculationAggregateData(totalPlanAggWorkTimeData, totalPlanAggAmountData,
								(List<Double>) ((Document) plan).get("totalQty"), (List<Double>) ((Document) plan).get("totalAmount"));
					} else if ("标准加班".equals(aggregateType)) {
						calculationAggregateData(basicPlanAggWorkTimeData, basicPlanAggAmountData,
								(List<Double>) ((Document) plan).get("basicQty"), (List<Double>) ((Document) plan).get("basicAmount"));
						calculationAggregateData(overTimePlanAggWorkTimeData, overTimePlanAggAmountData,
								(List<Double>) ((Document) plan).get("overTimeQty"),
								(List<Double>) ((Document) plan).get("overTimeAmount"));
					}
				}
			}
			if (dataType.contains("实际")) {
				Object actual = doc.get("actual");
				Object works = doc.get("works");
				if (actual instanceof Document) {
					if (getMarkLineData().get("data") != null && works != null)
						getMarkLineData().put("data", Arrays.asList(new Document("yAxis", works).append("name", label + "标记线")));

					appendWorkTimeSeries(label + "实际", (Document) actual, getMarkLineData());
					appendAmountSeries(label + "实际", (Document) actual);
					if ("总计".equals(aggregateType)) {
						calculationAggregateData(totalActualAggWorkTimeData, totalActualAggAmountData,
								(List<Double>) ((Document) actual).get("totalQty"), (List<Double>) ((Document) actual).get("totalAmount"));
					} else if ("标准加班".equals(aggregateType)) {
						calculationAggregateData(basicActualAggWorkTimeData, basicActualAggAmountData,
								(List<Double>) ((Document) actual).get("basicQty"), (List<Double>) ((Document) actual).get("basicAmount"));
						calculationAggregateData(overTimeActualAggWorkTimeData, overTimeActualAggAmountData,
								(List<Double>) ((Document) actual).get("overTimeQty"),
								(List<Double>) ((Document) actual).get("overTimeAmount"));
					}
				}
			}
		});

		if ("总计".equals(aggregateType)) {
			if (dataType.contains("计划")) {
				appendAggregateWorkTimeSeries("计划", "计划", totalPlanAggWorkTimeData, color[0]);
				appendAggregateAmountSeries("计划", "计划", totalPlanAggAmountData, color[0]);
			}
			if (dataType.contains("实际")) {
				appendAggregateWorkTimeSeries("实际", "实际", totalPlanAggWorkTimeData, color[1]);
				appendAggregateAmountSeries("实际", "实际", totalPlanAggAmountData, color[1]);
			}
		} else if ("标准加班".equals(aggregateType)) {
			if (dataType.contains("计划")) {
				appendAggregateWorkTimeSeries("计划标准", "计划", basicPlanAggWorkTimeData, color[0]);
				appendAggregateWorkTimeSeries("计划加班", "计划", overTimePlanAggWorkTimeData, color[2]);
				appendAggregateAmountSeries("计划标准", "计划", basicPlanAggAmountData, color[0]);
				appendAggregateAmountSeries("计划加班", "计划", overTimePlanAggAmountData, color[2]);
			}
			if (dataType.contains("实际")) {
				appendAggregateWorkTimeSeries("实际标准", "实际", basicActualAggWorkTimeData, color[1]);
				appendAggregateWorkTimeSeries("实际加班", "实际", overTimeActualAggWorkTimeData, color[3]);
				appendAggregateAmountSeries("实际标准", "实际", basicActualAggAmountData, color[1]);
				appendAggregateAmountSeries("实际加班", "实际", overTimeActualAggAmountData, color[3]);
			}
		}
	}

	private void appendAggregateWorkTimeSeries(String name, String stack, List<Double> data, String[] color) {
		series.add(new JQ("图表-资源图表-累计工时").set("name", "累计" + name + "工时").set("color1", color[0]).set("color2", color[1])
				.set("color3", color[2]).set("stack", "累计" + stack + "工时").set("data", data).doc());
		legendData.add("累计" + name + "工时");
	}

	private void appendAggregateAmountSeries(String name, String stack, List<Double> data, String[] color) {
		series.add(new JQ("图表-资源图表-累计金额").set("name", "累计" + name + "金额").set("color1", color[0]).set("color2", color[1])
				.set("color3", color[2]).set("stack", "累计" + stack + "金额").set("data", data).doc());
		legendData.add("累计" + name + "金额");
	}

	/**
	 * 计算工时和金额的累计值
	 * 
	 * @param aggWorkTimeDatas
	 * @param aggAmountDatas
	 * @param resourceWorkTimeData
	 * @param resourceAmountData
	 */
	private void calculationAggregateData(List<Double> aggWorkTimeDatas, List<Double> aggAmountDatas, List<Double> resourceWorkTimeData,
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

	private void appendWorkTimeSeries(String label, Document doc, Document markLineData) {
		if ("标准".equals(showData)) {
			series.add(new JQ("图表-资源图表-工时").set("name", label + "标准" + "工时").set("stack", label + "工时").set("data", doc.get("basicQty"))
					.set("markLineData", new Document()).doc());
			legendData.add(label + "标准" + "工时");
		} else if ("加班".equals(showData)) {
			series.add(new JQ("图表-资源图表-工时").set("name", label + "加班" + "工时").set("stack", label + "工时").set("data", doc.get("overTimeQty"))
					.set("markLineData", markLineData).doc());
			legendData.add(label + "加班" + "工时");
		} else if ("汇总".equals(showData)) {
			series.add(new JQ("图表-资源图表-工时").set("name", label + "" + "工时").set("stack", label + "工时").set("data", doc.get("totalQty"))
					.set("markLineData", new Document()).doc());
			legendData.add(label + "" + "工时");
		} else {
			series.add(new JQ("图表-资源图表-工时").set("name", label + "标准" + "工时").set("stack", label + "工时").set("data", doc.get("basicQty"))
					.set("markLineData", new Document()).doc());
			legendData.add(label + "标准" + "工时");

			series.add(new JQ("图表-资源图表-工时").set("name", label + "加班" + "工时").set("stack", label + "工时").set("data", doc.get("overTimeQty"))
					.set("markLineData", markLineData).doc());
			legendData.add(label + "加班" + "工时");
		}
	}

	private void appendAmountSeries(String label, Document doc) {
		if ("标准".equals(showData)) {
			series.add(new JQ("图表-资源图表-金额").set("name", label + "标准" + "金额").set("stack", label + "金额").set("data", doc.get("basicAmount"))
					.doc());
			legendData.add(label + "标准" + "金额");
		} else if ("加班".equals(showData)) {
			series.add(new JQ("图表-资源图表-金额").set("name", label + "加班" + "金额").set("stack", label + "金额")
					.set("data", doc.get("overTimeAmount")).doc());
			legendData.add(label + "加班" + "金额");
		} else if ("汇总".equals(showData)) {
			series.add(new JQ("图表-资源图表-金额").set("name", label + "" + "金额").set("stack", label + "金额").set("data", doc.get("totalAmount"))
					.doc());
			legendData.add(label + "" + "金额");
		} else {
			series.add(new JQ("图表-资源图表-金额").set("name", label + "标准" + "金额").set("stack", label + "金额").set("data", doc.get("basicAmount"))
					.doc());
			legendData.add(label + "标准" + "金额");

			series.add(new JQ("图表-资源图表-金额").set("name", label + "加班" + "金额").set("stack", label + "金额")
					.set("data", doc.get("overTimeAmount")).doc());
			legendData.add(label + "加班" + "金额");
		}
	}

	public Document render(List<Document> input) {
		this.input = input;
		loadSeries();
		JQ jq = new JQ("图表-资源图表").set("title", "").set("legendData", getLegendData()).set("xAxisData", getxAxisData()).set("series",
				getSeries());
		return jq.doc();
	}

}
