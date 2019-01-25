package com.bizvisionsoft.serviceimpl.renderer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
import com.bizvisionsoft.serviceimpl.query.JQ;

public class ProblemChartRender extends BasicServiceImpl {

	private static final int MIN_COUNT_DATAZOOM = 30;

	private List<Document> input;

	private Date from;

	private Date to;

	private String xAxis;// %Y#%Y-%m#%Y-%m-%d#catalog

	private List<String> x0AxisData;

	private String dateFormat;

	private String title;

	public ProblemChartRender() {
	}

	@SuppressWarnings("unchecked")
	public ProblemChartRender(Document condition) {
		Document option = (Document) condition.get("option");
		List<?> dateRange = (List<?>) option.get("dateRange");
		from = (Date) dateRange.get(0);
		to = (Date) dateRange.get(1);
		xAxis = option.getString("xAxis");
		input = (List<Document>) condition.get("input");
		initializeXAxis();
	}

	public static Document renderClassifyCostChart(Document condition) {
		return new ProblemChartRender(condition).renderClassifyCost();
	}

	public static Document renderClassifyProblemChart(Document condition) {
		return new ProblemChartRender(condition).renderClassifyProblem();
	}

	public static Document renderClassifyCauseChart(Document condition) {
		return new ProblemChartRender(condition).renderClassifyCause();
	}

	public static Document renderClassifyDeptChart(Document condition) {
		return new ProblemChartRender(condition).renderClassifyDept();
	}

	public static Document renderCostClassifyByProblemChart() {
		return new ProblemChartRender()._renderCostClassifyByProblemChart();
	}

	public static Document renderCountClassifyByProblemChart() {
		return new ProblemChartRender()._renderCountClassifyByProblemChart();
	}

	public static Document renderCostClassifyByCauseChart() {
		return new ProblemChartRender()._renderCostPie("查询-问题成本-原因权重计算", "问题原因（损失比重）", "causeRelation");
	}

	public static Document renderCostClassifyByDeptChart() {
		return new ProblemChartRender()._renderCostPie("查询-问题成本-按责任部门汇总", "责任部门（损失比重）", "problem");
	}

	public static Document renderCauseProblemChart() {
		return new ProblemChartRender()._renderCauseProblemChart();
	}

	private Document _renderCauseProblemChart() {
		Set<String> cateNames = new HashSet<>();
		List<Document> cNodes = new ArrayList<>();
		List<Document> pNodes = new ArrayList<>();
		List<Document> links = new ArrayList<>();
		c("causeRelation").aggregate(new JQ("查询-问题分类-原因分类-因果关系").array()).forEach((Document d) -> {
			String cName = d.getString("cNode");
			String cata = "因素:" + cName.split("/")[0].trim();
			cateNames.add(cata);
			Document cNode = cNodes.stream().filter(n -> cName.equals(n.get("name"))).findFirst().orElse(null);
			if (cNode == null) {
				cNode = new Document("name", cName)//
						.append("id", new ObjectId().toHexString()).append("draggable", true)//
						.append("category", cata)//
						.append("symbolSize", 20)//
						.append("value", 20);
				cNodes.add(cNode);
			}

			String pName = d.getString("pNode");
			cata = "问题:" + pName.split("/")[0].trim();
			cateNames.add(cata);
			Document pNode = pNodes.stream().filter(n -> pName.equals(n.get("name"))).findFirst().orElse(null);
			if (pNode == null) {
				pNode = new Document("name", pName)//
						.append("id", new ObjectId().toHexString()).append("draggable", true)//
						.append("category", cata)//
						.append("symbolSize", 60)//
						.append("value", 20);
				pNodes.add(pNode);
			}

			links.add(new Document("source", cNode.get("id")).append("target", pNode.get("id")).append("emphasis",
					new Document("label", new Document("show", false))));
		});

		cNodes.addAll(pNodes);
		List<Document> categories = cateNames.stream().sorted().map(s -> new Document("name", s)).collect(Collectors.toList());

		Document chart = new JQ("图表-因果关系图-带箭头").set("标题", "问题因果分析").set("data", cNodes).set("links", links).set("categories", categories)
				.doc();
		debugDocument(chart);
		return chart;
	}

	private Document _renderCountClassifyByProblemChart() {
		List<String> xAxis = getXAxisOfProblem();
		if (xAxis.isEmpty())
			return blankChart();
		String queryName = "查询-问题根分类-各月数量";
		String title = "问题数量";
		return _renderClassifyProblemBarChart(queryName, title, xAxis);
	}

	private Document _renderCostClassifyByProblemChart() {
		List<String> xAxis = getXAxisOfProblemCost();
		if (xAxis.isEmpty())
			return blankChart();
		String queryName = "查询-问题根分类-各月成本";
		String title = "问题损失";
		return _renderClassifyProblemBarChart(queryName, title, xAxis);
	}

	private Document _renderClassifyProblemBarChart(String queryName, String title, List<String> xAxis) {
		List<Document> dataset = new ArrayList<>();
		List<Document> series = new ArrayList<>();

		ArrayList<Document> result = c("classifyProblem").aggregate(new JQ(queryName).array()).into(new ArrayList<>());
		for (int i = 0; i < result.size(); i++) {
			Document data = result.get(i);
			dataset.add(data);
			String name = data.getString("_id");

			Document s = new Document("datasetIndex", dataset.size() - 1)//
					.append("name", name)//
					.append("xAxisIndex", 0).append("yAxisIndex", 0)//
					.append("encode", new Document("x", "_id").append("y", "value"))//
					.append("type", "bar").append("label", new Document("normal", new Document("show", true).append("position", "top")));//
			series.add(s);

			List<Document> momSource = getMoMData((List<?>) data.get("source"), xAxis, -1);
			if (!momSource.isEmpty()) {
				Document mData = new Document("_id", name).append("source", momSource);
				dataset.add(mData);

				s = new Document("datasetIndex", dataset.size() - 1)//
						.append("name", name)//
						.append("xAxisIndex", 0).append("yAxisIndex", 1)//
						.append("encode", new Document("x", "_id").append("y", "value"))//
						.append("type", "line").append("label", new Document("normal",
								new Document("show", false).append("position", "top").append("formatter", "{@value}%")));//
				series.add(s);
			}
		}

		return new JQ("图表-通用-带数据集-紧凑布局-2y轴")//
				.set("标题", title)//
				.set("数据集", dataset)//
				.set("x轴数据", xAxis)//
				.set("y1轴名称", "金额")//
				.set("y2轴名称", "环比减少(%)")//
				.set("系列", series)//
				.doc();
	}

	private List<Document> getMoMData(List<?> list, List<String> xAxis, int direction) {
		Map<String, Double> map = new HashMap<String, Double>();
		list.forEach(o -> map.put(((Document) o).getString("_id"), ((Document) o).getDouble("value")));
		Calendar cal = Calendar.getInstance();

		List<Document> result = new ArrayList<>();
		xAxis.forEach(date -> {
			try {
				double cValue = Optional.ofNullable(map.get(date)).orElse(0d);
				cal.setTime(new SimpleDateFormat("yyyy-MM").parse(date));
				cal.add(Calendar.MONTH, -1);
				String pDate = new SimpleDateFormat("yyyy-MM").format(cal.getTime());
				double pValue = Optional.ofNullable(map.get(pDate)).orElse(0d);
				if (pValue != 0) {
					result.add(new Document("_id", date).append("value",
							direction * (double) Math.round((cValue - pValue) / pValue * 10000) / 100));
				}
			} catch (ParseException e) {
			}
		});
		return result;
	}

	private Document _renderCostPie(String queryName, String title, String col) {
		ArrayList<Document> source = c(col).aggregate(new JQ(queryName).array()).into(new ArrayList<>());
		if (source.isEmpty())
			return blankChart();

		List<Document> series = Arrays.asList(//
				new Document("type", "pie")//
						.append("radius", 100)
						.append("label", new Document("normal", new Document("show", true).append("formatter", "{b}\n{d}%")))
						.append("encode", new Document("itemName", "_id").append("value", "amount")));//
		Document doc = new JQ("图表-通用-带数据集-紧凑布局-无轴")//
				.set("标题", title)//
				.set("数据集", new Document("source", source))//
				.set("系列", series)//
				.doc();
		return doc;
	}

	private Document renderClassifyDept() {
		List<Bson> pipe = new ArrayList<>();
		List<ObjectId> ids = input.stream().map(doc -> doc.getObjectId("_id")).collect(Collectors.toList());
		pipe.add(new Document("$match", new Document("_id", new Document("$in", ids))));
		new JQ("查询-问题成本-责任部门查问题").appendTo(pipe);
		new JQ("查询-问题成本-问题查成本").set("起始时间", from).set("终止时间", to).set("日期分组格式", dateFormat).appendTo(pipe);
		ArrayList<Document> dataset = c("organization").aggregate(pipe).into(new ArrayList<>());
		return buildDetailChart(dataset);
	}

	private Document renderClassifyCause() {
		if (!isSelectRoot()) {
			// 增加筛选条件
			return renderClassifyCauseDetailChart();
		} else {
			return renderRootChart();
		}
	}

	private Document renderClassifyCost() {
		if (!isSelectRoot()) {
			// 增加筛选条件
			return renderClassifyCostDetailChart();
		} else {
			return renderRootChart();
		}
	}

	private Document renderClassifyProblem() {
		if (!isSelectRoot()) {
			// 增加筛选条件
			return renderClassifyProblemDetailChart();
		} else {
			return renderRootChart();
		}
	}

	private Document renderRootChart() {
		List<Bson> pipe = new ArrayList<>();
		pipe.add(new Document("$match", new Document("date", new Document("$lte", to).append("$gte", from))));
		pipe.add(new Document("$project",
				new Document("date", new Document("$dateToString", new Document("date", "$date").append("format", dateFormat)))
						.append("amount", new Document("$subtract", Arrays.asList("$drAmount", "$crAmount")))));
		pipe.add(new Document("$group", new Document("_id", "$date").append("amount", new Document("$sum", "$amount"))));

		ArrayList<Document> source = c("problemCostItem").aggregate(pipe).into(new ArrayList<>());
		if (source.isEmpty())
			return blankChart();

		List<Document> series = Arrays.asList(new Document("type", "bar")//
				.append("name", "损失金额").append("label", new Document("normal", new Document("show", true).append("position", "top")))//
		);

		JQ chart = new JQ("图表-通用-带数据集")//
				.set("标题", title)//
				.set("数据集", new Document("source", source))//
				.set("x轴名称", "期间")//
				.set("x轴数据", x0AxisData)//
				.set("y轴名称", "损失金额（人民币元）").set("系列", series)//
				.set("数据放缩", x0AxisData.size() > MIN_COUNT_DATAZOOM ? new Document() : false)//
				.set("图例", false);
		return chart.doc();
	}

	private Document renderClassifyCostDetailChart() {
		List<Bson> pipe = new ArrayList<>();
		List<ObjectId> ids = input.stream().map(doc -> doc.getObjectId("_id")).collect(Collectors.toList());
		pipe.add(new Document("$match", new Document("_id", new Document("$in", ids))));
		new JQ("查询-问题成本-按时间和成本分类").set("起始时间", from).set("终止时间", to).set("日期分组格式", dateFormat).appendTo(pipe);
		ArrayList<Document> dataset = c("classifyProblemLost").aggregate(pipe).into(new ArrayList<>());
		return buildDetailChart(dataset);
	}

	private Document renderClassifyProblemDetailChart() {
		List<Bson> pipe = new ArrayList<>();
		List<ObjectId> ids = input.stream().map(doc -> doc.getObjectId("_id")).collect(Collectors.toList());
		pipe.add(new Document("$match", new Document("_id", new Document("$in", ids))));
		new JQ("查询-问题成本-问题分类查问题").appendTo(pipe);
		new JQ("查询-问题成本-问题查成本").set("起始时间", from).set("终止时间", to).set("日期分组格式", dateFormat).appendTo(pipe);
		ArrayList<Document> dataset = c("classifyProblem").aggregate(pipe).into(new ArrayList<>());
		return buildDetailChart(dataset);
	}

	private Document renderClassifyCauseDetailChart() {
		List<Bson> pipe = new ArrayList<>();
		List<ObjectId> ids = input.stream().map(doc -> doc.getObjectId("_id")).collect(Collectors.toList());
		pipe.add(new Document("$match", new Document("_id", new Document("$in", ids))));
		new JQ("查询-问题成本-原因分类查问题").appendTo(pipe);
		new JQ("查询-问题成本-问题查成本").set("起始时间", from).set("终止时间", to).set("日期分组格式", dateFormat).appendTo(pipe);
		ArrayList<Document> dataset = c("classifyCause").aggregate(pipe).into(new ArrayList<>());
		return buildDetailChart(dataset);

	}

	private Document buildDetailChart(ArrayList<Document> dataset) {
		if (dataset.isEmpty())
			return blankChart();

		// 构建系列数据集
		List<Document> summary = new ArrayList<>();

		ArrayList<Document> series = new ArrayList<Document>();
		buildSeriesAndSummary(dataset, summary, series);

		// 追加总计系列
		if (!summary.isEmpty()) {
			dataset.add(new Document("source", summary));// 总计
			series.add(new Document("datasetIndex", dataset.size() - 1)//
					.append("type", "bar")//
					.append("barWidth", "20%")//
					.append("xAxisIndex", 1).append("yAxisIndex", 1)//
					.append("encode", new Document("x", "_id").append("y", "amount"))
					.append("label", new Document("normal", new Document("show", true).append("position", "top"))));

			// 构造总计系列的x轴

			// 追加饼图
			series.add(new Document("datasetIndex", dataset.size() - 1)//
					.append("name", "分类占比")//
					.append("type", "pie")//
					.append("center", Arrays.asList("80%", "25%")).append("radius", "30%")
					.append("label", new Document("normal", new Document("formatter", "{b}\n{d}%"))));
		}

		JQ jq = new JQ("图表-通用-上下两栏目")//
				.set("标题", title)//
				.set("数据集", dataset)//
				.set("x轴名称", "期间")//
				.set("x轴数据", x0AxisData)//
				.set("y轴名称", "损失金额（人民币元）")//
				.set("系列", series)//
				.set("数据放缩", x0AxisData.size() > MIN_COUNT_DATAZOOM ? new Document("type", "slider").append("bottom", 40) : false)//
				.set("图例", new Document("bottom", 10).append("type", "scroll"));
		Document doc = jq.doc();
		debugDocument(doc);
		return doc;
	}

	private void buildSeriesAndSummary(List<Document> dataset, List<Document> summary, List<Document> series) {
		for (int i = 0; i < dataset.size(); i++) {
			Document source = dataset.get(i);
			String name = source.getString("_id");
			summary.add(new Document("_id", name).append("amount", source.get("amount")));

			List<?> list = (List<?>) source.get("source");
			Collections.sort(list, (o1, o2) -> ((Document) o1).getString("_id").compareTo(((Document) o2).getString("_id")));

			Document amount = new Document("datasetIndex", i)//
					.append("name", name)//
					.append("type", "bar")//
					.append("xAxisIndex", 0).append("yAxisIndex", 0)//
					.append("encode", new Document("x", "_id").append("y", "amount"))
					.append("label", new Document("normal", new Document("show", true).append("position", "top")));
			series.add(amount);
		}
	}

	private void initializeXAxis() {
		x0AxisData = getXAxisData(xAxis, from, to);
		// markLineData = new Document();
		Calendar cal = Calendar.getInstance();
		cal.setTime(from);
		if ("date".equals(xAxis)) {
			dateFormat = "%Y-%m-%d";
			title = Formatter.getString(from, "yyyy年M月d日") + "至" + Formatter.getString(to, "yyyy年M月d日") + "期间 问题损失分类统计";
		} else if ("month".equals(xAxis)) {
			dateFormat = "%Y-%m";
			title = Formatter.getString(from, "yyyy年M月") + "至" + Formatter.getString(to, "yyyy年M月") + "期间 问题损失分类统计";
		} else if ("year".equals(xAxis)) {
			dateFormat = "%Y";
			title = Formatter.getString(from, "yyyy年") + "至" + Formatter.getString(to, "yyyy年") + "期间 问题损失分类统计";
		}
	}

	private boolean isSelectRoot() {
		return input.size() == 1 && input.get(0).get("_id") == null;
	}

	private List<String> getXAxisOfProblemCost() {
		Document date = c("problemCostItem").aggregate(Arrays.asList(new Document("$group",
				new Document("_id", null).append("from", new Document("$min", "$date")).append("to", new Document("$max", "$date")))))
				.first();

		if (date == null)
			return new ArrayList<>();
		Date from = date.getDate("from");
		Date to = date.getDate("to");
		return getXAxisData("month", from, to);
	}

	private List<String> getXAxisOfProblem() {
		Document date = c("problem").aggregate(
				Arrays.asList(new Document("$match", new Document("status", new Document("$in", Arrays.asList("已创建", "解决中", "已关闭")))),
						new Document("$group", new Document("_id", null).append("from", new Document("$min", "$creationInfo.date"))
								.append("to", new Document("$max", "$creationInfo.date")))))
				.first();
		if (date == null)
			return new ArrayList<>();
		Date from = date.getDate("from");
		Date to = date.getDate("to");
		return getXAxisData("month", from, to);
	}

}
