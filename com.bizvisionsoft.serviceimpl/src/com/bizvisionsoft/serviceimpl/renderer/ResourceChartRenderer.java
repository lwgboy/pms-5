package com.bizvisionsoft.serviceimpl.renderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.model.EPS;
import com.bizvisionsoft.service.model.Equipment;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.bizvisionsoft.serviceimpl.renderer.ColorTheme.BruiColor;
import com.mongodb.client.model.Aggregates;

public class ResourceChartRenderer extends BasicServiceImpl {

	private class ResourceData {

		private Double[] basicTime;

		List<Double> basicTime() {
			return list(basicTime);
		}

		private Double[] extraTime;

		List<Double> extraTime() {
			return list(extraTime);
		}

		private Double[] totalTime;

		List<Double> totalTime() {
			return list(totalTime);
		}

		private Double[] basicAmounts;

		List<Double> basicAmounts() {
			return list(basicAmounts);
		}

		private Double[] extraAmounts;

		List<Double> extraAmounts() {
			return list(extraAmounts);
		}

		private Double[] totalAmounts;

		List<Double> totalAmounts() {
			return list(totalAmounts);
		}

		// private Double basicWorks;

		private List<Double> list(Double[] arr) {
			return Arrays.asList(arr).stream().map(d -> d == null ? 0d : d).collect(Collectors.toList());
		}

		ResourceData(int xAxisCount) {
			basicTime = new Double[xAxisCount];
			extraTime = new Double[xAxisCount];
			totalTime = new Double[xAxisCount];
			basicAmounts = new Double[xAxisCount];
			extraAmounts = new Double[xAxisCount];
			totalAmounts = new Double[xAxisCount];
		}

		void build(Document doc) {
			int idx = xAxisData.indexOf(doc.getString("_id"));
			if (idx != -1) {
				basicTime[idx] = doc.getDouble("basicQty");
				extraTime[idx] = doc.getDouble("overTimeQty");
				totalTime[idx] = doc.getDouble("totalQty");
				basicAmounts[idx] = doc.getDouble("basicAmount");
				extraAmounts[idx] = doc.getDouble("overTimeAmount");
				totalAmounts[idx] = doc.getDouble("totalAmount");
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
	// private Document markLineData;
	private Document group_id;
	private ArrayList<Double> totalPlanAggTime;
	private ArrayList<Double> totalPlanAggAmount;
	private ArrayList<Double> basicPlanAggTime;
	private ArrayList<Double> basicPlanAggAmount;
	private ArrayList<Double> extraPlanAggTime;
	private ArrayList<Double> extraPlanAggAmount;
	private ArrayList<Double> totalActualAggTime;
	private ArrayList<Double> totalActualAggAmount;
	private ArrayList<Double> basicActualAggTime;
	private ArrayList<Double> basicActualAggAmount;
	private ArrayList<Double> extraActualAggTime;
	private ArrayList<Double> extraActualAggAmount;
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

		// 根据时间类型和起止时间，构建group的_id和xAxis数据
		initializeXAxis();

		initializeData();

		// 根据系列类型构建series
		if ("并列".equals(seriesType)) {
			input.forEach(d -> {
				appendSeries(d.getString("label"));
			});
		} else {
			appendSeries("");

		}
		// 根据累计值类型构建series
		if ("总计".equals(aggregateType)) {
			if (dataType.contains("计划")) {
				appendSummary("图表-资源图表-累计工时", "累计计划工时", "累计计划工时", totalPlanAggTime, BruiColor.Light_Blue);
				appendSummary("图表-资源图表-累计金额", "累计计划金额", "累计计划金额", totalPlanAggAmount, BruiColor.Light_Blue);
			}
			if (dataType.contains("实际")) {
				appendSummary("图表-资源图表-累计工时", "累计实际工时", "累计实际工时", totalActualAggTime, BruiColor.Teal);
				appendSummary("图表-资源图表-累计金额", "累计实际金额", "累计实际金额", totalActualAggAmount, BruiColor.Teal);
			}
		} else if ("标准加班".equals(aggregateType)) {
			if (dataType.contains("计划")) {
				appendSummary("图表-资源图表-累计工时", "累计计划标准工时", "累计计划工时", basicPlanAggTime, BruiColor.Light_Blue);
				appendSummary("图表-资源图表-累计金额", "累计计划标准金额", "累计计划金额", basicPlanAggAmount, BruiColor.Light_Blue);

				appendSummary("图表-资源图表-累计工时", "累计计划加班工时", "累计计划工时", extraPlanAggTime, BruiColor.Orange);
				appendSummary("图表-资源图表-累计金额", "累计计划加班金额", "累计计划金额", extraPlanAggAmount, BruiColor.Orange);
			}
			if (dataType.contains("实际")) {
				appendSummary("图表-资源图表-累计工时", "累计实际标准工时", "累计实际工时", basicActualAggTime, BruiColor.Teal);
				appendSummary("图表-资源图表-累计金额", "累计实际标准金额", "累计实际金额", basicActualAggAmount, BruiColor.Teal);

				appendSummary("图表-资源图表-累计工时", "累计实际加班工时", "累计实际工时", extraActualAggTime, BruiColor.Red);
				appendSummary("图表-资源图表-累计金额", "累计实际加班金额", "累计实际金额", extraActualAggAmount, BruiColor.Red);
			}
		}

		// 生成资源图表
		JQ jq = new JQ("图表-资源图表").set("title", "").set("legendData", legendData).set("xAxisData", xAxisData).set("series", series);
		return jq.doc();
	}

	private void initializeData() {
		totalPlanAggTime = new ArrayList<Double>();
		totalPlanAggAmount = new ArrayList<Double>();
		basicPlanAggTime = new ArrayList<Double>();
		basicPlanAggAmount = new ArrayList<Double>();
		extraPlanAggTime = new ArrayList<Double>();
		extraPlanAggAmount = new ArrayList<Double>();

		totalActualAggTime = new ArrayList<Double>();
		totalActualAggAmount = new ArrayList<Double>();
		basicActualAggTime = new ArrayList<Double>();
		basicActualAggAmount = new ArrayList<Double>();
		extraActualAggTime = new ArrayList<Double>();
		extraActualAggAmount = new ArrayList<Double>();
	}

	private void initializeXAxis() {
		xAxisData = new ArrayList<String>();
		// markLineData = new Document();
		group_id = new Document();

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
			// TODO 需要根据所选资源的资源日历进行设置
			// markLineData.append("data", "");
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

	private void appendSeries(String label) {
		String timeChart = "图表-资源图表-工时";
		String amountChart = "图表-资源图表-金额";
		if (dataType.contains("计划")) {
			// 获取计划资源数据
			ResourceData r = query("resourcePlan", "$planBasicQty", "$planOverTimeQty");
			if ("标准".equals(showData)) {
				appendCatalog(timeChart, label + "计划标准工时", label + "计划工时", r.basicTime());
				appendCatalog(amountChart, label + "计划标准金额", label + "计划金额", r.basicAmounts());

				// 计算累计值
				summary(totalPlanAggTime, totalPlanAggAmount, basicPlanAggTime, basicPlanAggAmount, r.basicTime(), r.basicAmounts());
			} else if ("加班".equals(showData)) {
				appendCatalog(timeChart, label + "计划加班工时", label + "计划工时", r.extraTime());
				appendCatalog(amountChart, label + "计划加班金额", label + "计划金额", r.extraAmounts());

				// 计算累计值
				summary(totalPlanAggTime, totalPlanAggAmount, extraPlanAggTime, extraPlanAggAmount, r.extraTime(), r.extraAmounts());
			} else if ("汇总".equals(showData)) {
				appendCatalog(timeChart, label + "计划工时", label + "计划工时", r.list(r.totalTime));
				appendCatalog(amountChart, label + "计划金额", label + "计划金额", r.list(r.totalAmounts));

				// 计算累计值
				summary(totalPlanAggTime, totalPlanAggAmount, basicPlanAggTime, basicPlanAggAmount, r.basicTime(), r.basicAmounts());

				summary(totalPlanAggTime, totalPlanAggAmount, extraPlanAggTime, extraPlanAggAmount, r.extraTime(), r.extraAmounts());
			} else {
				appendCatalog(timeChart, label + "计划标准工时", label + "计划工时", r.basicTime());
				appendCatalog(amountChart, label + "计划标准金额", label + "计划金额", r.basicAmounts());
				appendCatalog(timeChart, label + "计划加班工时", label + "计划工时", r.extraTime());
				appendCatalog(amountChart, label + "计划加班金额", label + "计划金额", r.extraAmounts());

				// 计算累计值
				summary(totalPlanAggTime, totalPlanAggAmount, basicPlanAggTime, basicPlanAggAmount, r.basicTime(), r.basicAmounts());
				summary(totalPlanAggTime, totalPlanAggAmount, extraPlanAggTime, extraPlanAggAmount, r.extraTime(), r.extraAmounts());
			}
		}
		if (dataType.contains("实际")) {
			// 获取实际资源数据
			ResourceData r = query("resourceActual", "$actualBasicQty", "$actualOverTimeQty");
			if ("标准".equals(showData)) {
				appendCatalog(timeChart, label + "实际标准工时", label + "实际工时", r.basicTime());
				appendCatalog(amountChart, label + "实际标准金额", label + "实际金额", r.basicAmounts());

				// 计算累计值
				summary(totalActualAggTime, totalActualAggAmount, basicActualAggTime, basicActualAggAmount, r.basicTime(),
						r.basicAmounts());
			} else if ("加班".equals(showData)) {
				// if (markLineData.get("data") != null && r.basicWorks != null)
				// markLineData.put("data", Arrays.asList(new Document("yAxis",
				// r.basicWorks).append("name", catalogLabel + "标记线")));

				appendCatalog(timeChart, label + "实际加班工时", label + "实际工时", r.extraTime());
				appendCatalog(amountChart, label + "实际加班金额", label + "实际金额", r.extraAmounts());

				// 计算累计值
				summary(totalActualAggTime, totalActualAggAmount, extraActualAggTime, extraActualAggAmount, r.extraTime(),
						r.extraAmounts());
			} else if ("汇总".equals(showData)) {
				appendCatalog(timeChart, label + "实际工时", label + "实际工时", r.totalTime());
				appendCatalog(amountChart, label + "实际金额", label + "实际金额", r.totalAmounts());

				// 计算累计值
				summary(totalActualAggTime, totalActualAggAmount, basicActualAggTime, basicActualAggAmount, r.basicTime(),
						r.basicAmounts());

				summary(totalActualAggTime, totalActualAggAmount, extraActualAggTime, extraActualAggAmount, r.extraTime(),
						r.extraAmounts());
			} else {
				// if (markLineData.get("data") != null && r.basicWorks != null)
				// markLineData.put("data", Arrays.asList(new Document("yAxis",
				// r.basicWorks).append("name", catalogLabel + "标记线")));

				appendCatalog(timeChart, label + "实际标准工时", label + "实际工时", r.basicTime());
				appendCatalog(amountChart, label + "实际标准金额", label + "实际金额", r.basicAmounts());
				appendCatalog(timeChart, label + "实际加班工时", label + "实际工时", r.extraTime());
				appendCatalog(amountChart, label + "实际加班金额", label + "实际金额", r.extraAmounts());

				// 计算累计值
				summary(totalActualAggTime, totalActualAggAmount, basicActualAggTime, basicActualAggAmount, r.basicTime(),
						r.basicAmounts());
				summary(totalActualAggTime, totalActualAggAmount, extraActualAggTime, extraActualAggAmount, r.extraTime(),
						r.extraAmounts());
			}

		}

	}

	/**
	 * 根据累计值类型，计算累计值
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
		if ("总计".equals(aggregateType)) {
			summary(totalAggWorkTimeData, totalAggAmountData, resourceWorkTimeData, resourceAmountData);
		} else if ("标准加班".equals(aggregateType)) {
			summary(aggWorkTimeData, aggAmountData, resourceWorkTimeData, resourceAmountData);
		}
	}

	/**
	 * 计算工时和金额的累计值
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

	private ResourceData query(String col, String basicQtyName, String overTimeQtyName) {
		ResourceData resData = new ResourceData(xAxisData.size());
		// 查询获取数据
		buildMatchCondition();
		c(col).aggregate(new JQ("查询-资源图表").set("$basicQty", basicQtyName).set("$overTimeQty", overTimeQtyName).set("match", match)
				.set("group_id", group_id).array()).forEach((Document d) -> resData.build(d));
		return resData;
	}

	private void buildMatchCondition() {
		// 并根据时间构建查询语句
		match = new Document("id", new Document("$gte", start).append("$lte", end));
		List<ObjectId> resourceIds = new ArrayList<>();
		List<ObjectId> workIds = new ArrayList<>();

		input.forEach(doc -> {
			String type = doc.getString("type");
			if (ResourceType.class.getName().equals(type)) {
				Document meta = ((Document) doc.get("meta"));
				Object resTypeId = doc.get("_id");
				Object _id = meta.get("org_id");
				if (_id != null) {// 选取的组织下级的类型
					Document condition = new Document("org_id", _id).append("resourceType_id", resTypeId);
					c("user").find(condition).map(d -> d.getObjectId("_id")).into(resourceIds);
					c("equipment").find(condition).map(d -> d.getObjectId("_id")).into(resourceIds);
				} else {
					List<Bson> pipe = filterWork(meta);
					pipe.add(Aggregates.match(new Document("resTypeId", resTypeId)));
					c("work").aggregate(pipe).map(d -> d.getObjectId("work_id")).into(workIds);
				}
			} else if (Organization.class.getName().equals(type)) {
				List<Bson> pipe = new ArrayList<>();
				pipe.add(Aggregates.match(new Document("_id", doc.get("_id"))));
				pipe.addAll(new JQ("查询-通用-下级迭代取出-含本级").set("from", "organization").set("startWith", "$_id").set("connectFromField", "_id")
						.set("connectToField", "parent_id").array());
				pipe.addAll(new JQ("追加-组织下资源").array());
				debugPipeline(pipe);
				c("organization").aggregate(pipe).map(d -> d.getObjectId("_id")).into(resourceIds);
			} else if (User.class.getName().equals(type)//
					|| Equipment.class.getName().equals(type)//
					|| (ResourceType.class.getName() + ".TypedResource").equals(type)) {
				Document meta = (Document) doc.get("meta");
				List<Bson> pipe = filterWork(meta);
				if (!pipe.isEmpty())
					c("work").aggregate(pipe).map(d -> d.getObjectId("work_id")).into(workIds);
				resourceIds.add(doc.getObjectId("_id"));
			} else if (EPS.class.getName().equals(type)) {

			} else if (Project.class.getName().equals(type)) {

			}
		});
		if (!workIds.isEmpty())
			match.append("work_id", new Document("$in", workIds));
		if (!resourceIds.isEmpty())
			match.append("resource_id", new Document("$in", resourceIds));
	}

	private List<Bson> filterWork(Document meta) {
		Object _id;
		List<Bson> pipe = new ArrayList<>();
		_id = meta.get("project_id");
		if (_id != null) {// 选取的是项目下级的类型
			pipe.add(Aggregates.match(new Document("project_id", _id)));
			pipe.addAll(new JQ("查询-工作-资源").array());
		} else {
			_id = meta.get("stage_id");
			if (_id != null) {// 选取的是阶段下级的类型
				pipe.add(Aggregates.match(new Document("_id", _id)));
				pipe.addAll(new JQ("查询-通用-下级迭代取出-含本级").set("from", "work").set("startWith", "$_id").set("connectFromField", "_id")
						.set("connectToField", "parent_id").array());
				pipe.addAll(new JQ("查询-工作-资源").array());
			}
		}
		return pipe;
	}

}
