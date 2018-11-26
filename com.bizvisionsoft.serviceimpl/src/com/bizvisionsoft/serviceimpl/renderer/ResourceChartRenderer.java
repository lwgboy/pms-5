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
					// 加载计划数据
					basicPlanTime[idx] = doc.getDouble("basicTime");
					extraPlanTime[idx] = doc.getDouble("extraTime");
					totalPlanTime[idx] = doc.getDouble("sumTime");
					basicPlanAmounts[idx] = doc.getDouble("basicAmount");
					extraPlanAmounts[idx] = doc.getDouble("extraAmount");
					totalPlanAmounts[idx] = doc.getDouble("sumAmount");

					// 计算计划汇总值
					summary(idx, xAxisData.size(), basicPlanAggTime, doc.getDouble("basicTime"));
					summary(idx, xAxisData.size(), extraPlanAggTime, doc.getDouble("extraTime"));
					summary(idx, xAxisData.size(), totalPlanAggTime, doc.getDouble("sumTime"));
					summary(idx, xAxisData.size(), basicPlanAggAmount, doc.getDouble("basicAmount"));
					summary(idx, xAxisData.size(), extraPlanAggAmount, doc.getDouble("extraAmount"));
					summary(idx, xAxisData.size(), totalPlanAggAmount, doc.getDouble("sumAmount"));
				} else {
					// 加载实际数据
					basicActualTime[idx] = doc.getDouble("basicTime");
					extraActualTime[idx] = doc.getDouble("extraTime");
					totalActualTime[idx] = doc.getDouble("sumTime");
					basicActualAmounts[idx] = doc.getDouble("basicAmount");
					extraActualAmounts[idx] = doc.getDouble("extraAmount");
					totalActualAmounts[idx] = doc.getDouble("sumAmount");

					// 计算实际汇总值
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

		// 根据时间类型、系列类型、计划/实际，构建group
		initializeGroup();
		// 根据时间类型和起止时间，构建xAxis，并初始化累计值
		initializeXAxis();
		// 根据累计类型，构建Y轴
		initializeYAxis();
		// 初始化汇总字段，并构建match
		initializeMatch();

		// 根据系列类型构建series
		appendSeries();

		// 生成资源图表
		JQ jq = new JQ("图表-资源图表").set("title", "").set("legendData", legendData).set("xAxisData", xAxisData).set("yAxis", yAxis)
				.set("series", series);
		return jq.doc();
	}

	/**
	 * 加载Series
	 * 
	 * @param resData
	 */
	private void appendSeries() {
		Map<String, ResourceData> resData = query();
		for (String key : resData.keySet()) {
			String timeChart = "图表-资源图表-工时";
			String amountChart = "图表-资源图表-金额";
			ResourceData r = resData.get(key);
			if (dataType.contains("计划")) {
				// 获取计划资源数据
				if ("标准".equals(showData)) {
					appendCatalog(timeChart, key + "计划标准工时", key + "计划工时", r.basicPlanTime());
					appendCatalog(amountChart, key + "计划标准金额", key + "计划金额", r.basicPlanAmounts());
				} else if ("加班".equals(showData)) {
					appendCatalog(timeChart, key + "计划加班工时", key + "计划工时", r.extraPlanTime());
					appendCatalog(amountChart, key + "计划加班金额", key + "计划金额", r.extraPlanAmounts());
				} else if ("汇总".equals(showData)) {
					appendCatalog(timeChart, key + "计划工时", key + "计划工时", r.totalPlanTime());
					appendCatalog(amountChart, key + "计划金额", key + "计划金额", r.totalPlanAmounts());
				} else {
					appendCatalog(timeChart, key + "计划标准工时", key + "计划工时", r.basicPlanTime());
					appendCatalog(amountChart, key + "计划标准金额", key + "计划金额", r.basicPlanAmounts());
					appendCatalog(timeChart, key + "计划加班工时", key + "计划工时", r.extraPlanTime());
					appendCatalog(amountChart, key + "计划加班金额", key + "计划金额", r.extraPlanAmounts());
				}
			}
			if (dataType.contains("实际")) {
				// 获取实际资源数据
				if ("标准".equals(showData)) {
					appendCatalog(timeChart, key + "实际标准工时", key + "实际工时", r.basicActualTime());
					appendCatalog(amountChart, key + "实际标准金额", key + "实际金额", r.basicActualAmounts());
				} else if ("加班".equals(showData)) {
					appendCatalog(timeChart, key + "实际加班工时", key + "实际工时", r.extraActualTime());
					appendCatalog(amountChart, key + "实际加班金额", key + "实际金额", r.extraActualAmounts());
				} else if ("汇总".equals(showData)) {
					appendCatalog(timeChart, key + "实际工时", key + "实际工时", r.totalActualTime());
					appendCatalog(amountChart, key + "实际金额", key + "实际金额", r.totalActualAmounts());
				} else {
					appendCatalog(timeChart, key + "实际标准工时", key + "实际工时", r.basicActualTime());
					appendCatalog(amountChart, key + "实际标准金额", key + "实际金额", r.basicActualAmounts());
					appendCatalog(timeChart, key + "实际加班工时", key + "实际工时", r.extraActualTime());
					appendCatalog(amountChart, key + "实际加班金额", key + "实际金额", r.extraActualAmounts());

				}

			}
		}

		// 根据累计值类型构建series
		if ("总计".equals(aggregateType)) {
			if (dataType.contains("计划")) {
				appendSummary("图表-资源图表-累计工时", "累计计划工时", "累计计划工时", list(totalPlanAggTime), BruiColor.Light_Blue);
				appendSummary("图表-资源图表-累计金额", "累计计划金额", "累计计划金额", list(totalPlanAggAmount), BruiColor.Light_Blue);
			}
			if (dataType.contains("实际")) {
				appendSummary("图表-资源图表-累计工时", "累计实际工时", "累计实际工时", list(totalActualAggTime), BruiColor.Teal);
				appendSummary("图表-资源图表-累计金额", "累计实际金额", "累计实际金额", list(totalActualAggAmount), BruiColor.Teal);
			}
		} else if ("标准加班".equals(aggregateType)) {
			if (dataType.contains("计划")) {
				appendSummary("图表-资源图表-累计工时", "累计计划标准工时", "累计计划工时", list(basicPlanAggTime), BruiColor.Light_Blue);
				appendSummary("图表-资源图表-累计金额", "累计计划标准金额", "累计计划金额", list(basicPlanAggAmount), BruiColor.Light_Blue);

				appendSummary("图表-资源图表-累计工时", "累计计划加班工时", "累计计划工时", list(extraPlanAggTime), BruiColor.Orange);
				appendSummary("图表-资源图表-累计金额", "累计计划加班金额", "累计计划金额", list(extraPlanAggAmount), BruiColor.Orange);
			}
			if (dataType.contains("实际")) {
				appendSummary("图表-资源图表-累计工时", "累计实际标准工时", "累计实际工时", list(basicActualAggTime), BruiColor.Teal);
				appendSummary("图表-资源图表-累计金额", "累计实际标准金额", "累计实际金额", list(basicActualAggAmount), BruiColor.Teal);

				appendSummary("图表-资源图表-累计工时", "累计实际加班工时", "累计实际工时", list(extraActualAggTime), BruiColor.Red);
				appendSummary("图表-资源图表-累计金额", "累计实际加班金额", "累计实际金额", list(extraActualAggAmount), BruiColor.Red);
			}
		}
	}

	/**
	 * 构建查询
	 */
	private void initializeMatch() {
		match = new Document("id", new Document("$gte", start).append("$lte", end));
		List<String> typeData = new ArrayList<>();
		if (dataType.contains("计划")) {
			typeData.add("plan");
		}
		if (dataType.contains("实际")) {
			typeData.add("actual");
		}
		match.append("type", new Document("$in", typeData));
	}

	/**
	 * 构建分组
	 */
	private void initializeGroup() {
		Document groupId = new Document();
		if ("日".equals(dateType)) {
			groupId.append("id", new Document("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$id")));
		} else if ("月".equals(dateType)) {
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
	 * 根据时间类型和起止时间，构建xAxis，并初始化累计值
	 */
	private void initializeXAxis() {
		xAxisData = new ArrayList<String>();
		// markLineData = new Document();
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		List<Double> agg = new ArrayList<Double>();
		SimpleDateFormat sdf;
		if ("日".equals(dateType)) {
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				agg.add(0d);
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
			// TODO 需要根据所选资源的资源日历进行设置
			// markLineData.append("data", "");
		} else if ("月".equals(dateType)) {
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
	 * 加载Y轴配置
	 */
	private void initializeYAxis() {
		yAxis = new ArrayList<>();
		// 加载工时Y轴
		yAxis.add(new Document("type", "value").append("name", "工时（小时）").append("axisLabel", new Document("formatter", "{value}")));
		// 加载金额Y轴
		yAxis.add(new Document("type", "value").append("name", "金额（元）").append("axisLabel", new Document("formatter", "{value}"))
				.append("gridIndex", 1));

		if (!"不累计".equals(aggregateType)) {
			// 加载累计工时Y轴
			yAxis.add(new Document("type", "value").append("name", "累计工时（小时）").append("axisLabel", new Document("formatter", "{value}")));
			// 加载累计金额Y轴
			yAxis.add(new Document("type", "value").append("name", "累计金额（元）").append("axisLabel", new Document("formatter", "{value}"))
					.append("gridIndex", 1));
		}
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
	 * 添加累计系列
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
	 * 添加节点系列
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

	// 查询获取数据
	private Map<String, ResourceData> query() {
		Map<String, ResourceData> resDataMap = new HashMap<String, ResourceData>();

		if ("并列".equals(seriesType)) {
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
		List<Bson> pipeline = new JQ("视图-资源计划和实际用量").array();
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
