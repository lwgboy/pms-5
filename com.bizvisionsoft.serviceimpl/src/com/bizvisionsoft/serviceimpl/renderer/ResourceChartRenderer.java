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

		// 并根据时间构建查询语句
		match = new Document("$and",
				Arrays.asList(new Document("id", new Document("$gte", start)), new Document("id", new Document("$lte", end))));

		// 根据时间类型和起止时间，构建group的_id和xAxis数据
		initializeXAxis();

		initializeData();

		// 根据系列类型构建series
		if ("并列".equals(seriesType)) {
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
		// 根据累计值类型构建series
		if ("总计".equals(aggregateType)) {
			if (dataType.contains("计划")) {
				appendSummarySeries("图表-资源图表-累计工时", "累计计划工时", "累计计划工时", totalPlanAggWorkTimeData, BruiColor.Light_Blue);
				appendSummarySeries("图表-资源图表-累计金额", "累计计划金额", "累计计划金额", totalPlanAggAmountData, BruiColor.Light_Blue);
			}
			if (dataType.contains("实际")) {
				appendSummarySeries("图表-资源图表-累计工时", "累计实际工时", "累计实际工时", totalActualAggWorkTimeData, BruiColor.Teal);
				appendSummarySeries("图表-资源图表-累计金额", "累计实际金额", "累计实际金额", totalActualAggAmountData, BruiColor.Teal);
			}
		} else if ("标准加班".equals(aggregateType)) {
			if (dataType.contains("计划")) {
				appendSummarySeries("图表-资源图表-累计工时", "累计计划标准工时", "累计计划工时", basicPlanAggWorkTimeData, BruiColor.Light_Blue);
				appendSummarySeries("图表-资源图表-累计金额", "累计计划标准金额", "累计计划金额", basicPlanAggAmountData, BruiColor.Light_Blue);

				appendSummarySeries("图表-资源图表-累计工时", "累计计划加班工时", "累计计划工时", overTimePlanAggWorkTimeData, BruiColor.Orange);
				appendSummarySeries("图表-资源图表-累计金额", "累计计划加班金额", "累计计划金额", overTimePlanAggAmountData, BruiColor.Orange);
			}
			if (dataType.contains("实际")) {
				appendSummarySeries("图表-资源图表-累计工时", "累计实际标准工时", "累计实际工时", basicActualAggWorkTimeData, BruiColor.Teal);
				appendSummarySeries("图表-资源图表-累计金额", "累计实际标准金额", "累计实际金额", basicActualAggAmountData, BruiColor.Teal);

				appendSummarySeries("图表-资源图表-累计工时", "累计实际加班工时", "累计实际工时", overTimeActualAggWorkTimeData, BruiColor.Red);
				appendSummarySeries("图表-资源图表-累计金额", "累计实际加班金额", "累计实际金额", overTimeActualAggAmountData, BruiColor.Red);
			}
		}

		// 生成资源图表
		JQ jq = new JQ("图表-资源图表").set("title", "").set("legendData", legendData).set("xAxisData", xAxisData).set("series", series);
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
		if ("日".equals(dateType)) {
			group_id.append("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$id"));
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
			// TODO 需要根据所选资源的资源日历进行设置
			markLineData.append("data", "");
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
	private void appendSummarySeries(String seriesJQ, String name, String stack, ArrayList<Double> data, BruiColor color) {
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
	private void appendCatalogSeries(String timeChart, String name, String stack, Object markLineData, Object data) {
		series.add(new JQ(timeChart).set("name", name).set("stack", stack).set("data", data).set("markLineData", markLineData).doc());
		legendData.add(name);
	}

	/**
	 * 构建series
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
		String timeChart = "图表-资源图表-工时";
		String amountChart = "图表-资源图表-金额";
		if (dataType.contains("计划")) {
			// 获取计划资源数据
			Object[] res = getResourceData("resourcePlan", "$planBasicQty", "$planOverTimeQty", match, group_id, xAxisData);
			if ("标准".equals(showData)) {
				appendCatalogSeries(timeChart, label + "计划标准工时", label + "计划工时", new Document(), res[0]);
				appendCatalogSeries(amountChart, label + "计划标准金额", label + "计划金额", new Document(), res[3]);

				// 计算累计值
				summary(totalPlanAggWorkTimeData, totalPlanAggAmountData, basicPlanAggWorkTimeData, basicPlanAggAmountData,
						(List<Double>) res[0], (List<Double>) res[3]);
			} else if ("加班".equals(showData)) {
				appendCatalogSeries(timeChart, label + "计划加班工时", label + "计划工时", new Document(), res[1]);
				appendCatalogSeries(amountChart, label + "计划加班金额", label + "计划金额", new Document(), res[4]);

				// 计算累计值
				summary(totalPlanAggWorkTimeData, totalPlanAggAmountData, overTimePlanAggWorkTimeData, overTimePlanAggAmountData,
						(List<Double>) res[1], (List<Double>) res[4]);
			} else if ("汇总".equals(showData)) {
				appendCatalogSeries(timeChart, label + "计划工时", label + "计划工时", new Document(), res[2]);
				appendCatalogSeries(amountChart, label + "计划金额", label + "计划金额", new Document(), res[5]);

				// 计算累计值
				summary(totalPlanAggWorkTimeData, totalPlanAggAmountData, basicPlanAggWorkTimeData, basicPlanAggAmountData,
						(List<Double>) res[0], (List<Double>) res[3]);

				summary(totalPlanAggWorkTimeData, totalPlanAggAmountData, overTimePlanAggWorkTimeData, overTimePlanAggAmountData,
						(List<Double>) res[1], (List<Double>) res[4]);
			} else {
				appendCatalogSeries(timeChart, label + "计划标准工时", label + "计划工时", new Document(), res[0]);
				appendCatalogSeries(amountChart, label + "计划标准金额", label + "计划金额", new Document(), res[3]);
				appendCatalogSeries(timeChart, label + "计划加班工时", label + "计划工时", new Document(), res[1]);
				appendCatalogSeries(amountChart, label + "计划加班金额", label + "计划金额", new Document(), res[4]);

				// 计算累计值
				summary(totalPlanAggWorkTimeData, totalPlanAggAmountData, basicPlanAggWorkTimeData, basicPlanAggAmountData,
						(List<Double>) res[0], (List<Double>) res[3]);
				summary(totalPlanAggWorkTimeData, totalPlanAggAmountData, overTimePlanAggWorkTimeData, overTimePlanAggAmountData,
						(List<Double>) res[1], (List<Double>) res[4]);
			}
		}
		if (dataType.contains("实际")) {
			// 获取实际资源数据
			Object[] res = getResourceData("resourceActual", "$actualBasicQty", "$actualOverTimeQty", match, group_id, xAxisData);
			if ("标准".equals(showData)) {
				appendCatalogSeries(timeChart, label + "实际标准工时", label + "实际工时", new Document(), res[0]);
				appendCatalogSeries(amountChart, label + "实际标准金额", label + "实际金额", new Document(), res[3]);

				// 计算累计值
				summary(totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData, basicActualAggAmountData,
						(List<Double>) res[0], (List<Double>) res[3]);
			} else if ("加班".equals(showData)) {
				if (markLineData.get("data") != null && res[6] != null)
					markLineData.put("data", Arrays.asList(new Document("yAxis", res[6]).append("name", label + "标记线")));

				appendCatalogSeries(timeChart, label + "实际加班工时", label + "实际工时", markLineData, res[1]);
				appendCatalogSeries(amountChart, label + "实际加班金额", label + "实际金额", new Document(), res[4]);

				// 计算累计值
				summary(totalActualAggWorkTimeData, totalActualAggAmountData, overTimeActualAggWorkTimeData, overTimeActualAggAmountData,
						(List<Double>) res[1], (List<Double>) res[4]);
			} else if ("汇总".equals(showData)) {
				appendCatalogSeries(timeChart, label + "实际工时", label + "实际工时", new Document(), res[2]);
				appendCatalogSeries(amountChart, label + "实际金额", label + "实际金额", new Document(), res[5]);

				// 计算累计值
				summary(totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData, basicActualAggAmountData,
						(List<Double>) res[0], (List<Double>) res[3]);

				summary(totalActualAggWorkTimeData, totalActualAggAmountData, overTimeActualAggWorkTimeData, overTimeActualAggAmountData,
						(List<Double>) res[1], (List<Double>) res[4]);
			} else {
				if (markLineData.get("data") != null && res[6] != null)
					markLineData.put("data", Arrays.asList(new Document("yAxis", res[6]).append("name", label + "标记线")));

				appendCatalogSeries(timeChart, label + "实际标准工时", label + "实际工时", new Document(), res[0]);
				appendCatalogSeries(amountChart, label + "实际标准金额", label + "实际金额", new Document(), res[3]);
				appendCatalogSeries(timeChart, label + "实际加班工时", label + "实际工时", markLineData, res[1]);
				appendCatalogSeries(amountChart, label + "实际加班金额", label + "实际金额", new Document(), res[4]);

				// 计算累计值
				summary(totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData, basicActualAggAmountData,
						(List<Double>) res[0], (List<Double>) res[3]);
				summary(totalActualAggWorkTimeData, totalActualAggAmountData, overTimeActualAggWorkTimeData, overTimeActualAggAmountData,
						(List<Double>) res[1], (List<Double>) res[4]);
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

	/**
	 * 查询资源图表数据，并根据xAxisData构建返回值
	 * 
	 * @param collectionName
	 * @param basicQtyName
	 * @param overTimeQtyName
	 * @param match
	 * @param group_id
	 * @param xAxisData
	 * @return 元素1：标准数量，元素2：加班数量，元素3：数量合计，元素4：标准金额，元素5：加班金额，元素6：金额合计，元素7：标准工时.
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
		// 查询获取数据
		c(collectionName).aggregate(new JQ("查询-资源图表").set("$basicQty", basicQtyName).set("$overTimeQty", overTimeQtyName)
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

		// 根据xAxisData构建返回值
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
	 * 根据传入的查询的数据，获取其从属其的资源id
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
			pipe.addAll(new JQ("查询-通用-下级迭代取出-含本级").set("from", "organization").set("startWith", "$_id").set("connectFromField", "_id")
					.set("connectToField", "parent_id").array());
			pipe.addAll(new JQ("追加-组织下资源").array());
			debugPipeline(pipe);
			c("organization").aggregate(pipe).map(d -> d.getObjectId("_id")).into(result);
		} else if (User.class.getName().equals(type) || Equipment.class.getName().equals(type)) {
			result.add(doc.getObjectId("_id"));
		}
		// doc.put("childResourceIds", result);
		return result;
	}

}
