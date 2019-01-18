package com.bizvisionsoft.serviceimpl.renderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
import com.bizvisionsoft.serviceimpl.query.JQ;

public class ProblemCostChartRender extends BasicServiceImpl {

	private List<Document> input;

	private Date from;

	private Date to;

	private String xAxis;// %Y#%Y-%m#%Y-%m-%d#catalog

	private ArrayList<String> xAxisData;

	private String dateFormat;

	private boolean stack;

	@SuppressWarnings("unchecked")
	public ProblemCostChartRender(Document condition) {
		Document option = (Document) condition.get("option");
		List<?> dateRange = (List<?>) option.get("dateRange");
		from = (Date) dateRange.get(0);
		to = (Date) dateRange.get(1);
		xAxis = option.getString("xAxis");
		input = (List<Document>) condition.get("input");
		stack = "堆叠".equals(option.get("stack"));
		initializeXAxis();
	}

	public static Document renderClassifyCostChart(Document condition) {
		return new ProblemCostChartRender(condition).render();
	}

	private Document render() {
		if (!"catalog".equals(xAxis)) {
			boolean selectRoot = input.size() == 1 && input.get(0).get("_id") == null;
			if (!selectRoot) {
				// 增加筛选条件
				return renderClassifyPeriodChart();
			} else {
				return renderRootPeriodChart();
			}
		}
		return new Document();
	}

	private Document renderRootPeriodChart() {
		List<Bson> pipe = new ArrayList<>();
		pipe.add(new Document("$match", new Document("date", new Document("$lte", to).append("$gte", from))));
		pipe.add(new Document("$project",
				new Document("date", new Document("$dateToString", new Document("date", "$date").append("format", dateFormat)))
						.append("amount", new Document("$subtract", Arrays.asList("$drAmount", "$crAmount")))));
		pipe.add(new Document("$group", new Document("_id", "$date").append("amount", new Document("$sum", "$amount"))));

		debugPipeline(pipe);
		ArrayList<Document> source = c("problemCostItem").aggregate(pipe).into(new ArrayList<>());

		List<Document> series = Arrays.asList(new Document("type", "bar").append("name", "损失金额").append("label",
				new Document("normal", new Document("show", true).append("position", "top"))));

		JQ chart = new JQ("图表-通用-带数据集")//
				.set("数据集", new Document("source", source))//
				.set("x轴名称", "期间")//
				.set("x轴数据", xAxisData)//
				.set("y轴名称", "损失金额（人民币元）").set("系列", series)//
				.set("数据放缩", xAxisData.size() > 100 ? new Document() : false).set("图例", false);
		return chart.doc();
	}

	private Document renderClassifyPeriodChart() {
		List<Bson> pipe = new ArrayList<>();
		List<ObjectId> ids = input.stream().map(doc -> doc.getObjectId("_id")).collect(Collectors.toList());
		pipe.add(new Document("$match", new Document("_id", new Document("$in", ids))));
		new JQ("查询-问题成本分类-按时间").set("起始时间", from).set("终止时间", to).set("日期分组格式", dateFormat).appendTo(pipe);

		pipe.add(new Document("$group", new Document("_id", "$name").append("source",
				new Document("$addToSet", new Document("_id", "$_id").append("amount", "$amount")))));
		ArrayList<Document> dataset = c("classifyProblemLost").aggregate(pipe).into(new ArrayList<>());
		// 构建系列数据集
		ArrayList<Document> series = new ArrayList<Document>();
		for (int i = 0; i < dataset.size(); i++) {
			Document source = dataset.get(i);
			List<?> list = (List<?>) source.get("source");
			Collections.sort(list, (o1, o2) -> ((Document) o1).getString("_id").compareTo(((Document) o2).getString("_id")));

			Document amount = new Document("datasetIndex", i)//
					.append("name", "" + source.get("_id"))//
					.append("xAxisIndex", 0)//
					.append("yAxisIndex", 0)//
					.append("type", "bar")//
					.append("encode", new Document("x", "_id").append("y", "amount"));
			if (stack)
				amount.append("stack", "金额");
			series.add(amount);

		}

		JQ chart = new JQ("图表-通用-带数据集")//
				.set("数据集", dataset)//
				.set("x轴名称", "期间")//
				.set("x轴数据", xAxisData)//
				.set("y轴名称", "损失金额（人民币元）").set("系列", series)//
				.set("数据放缩", xAxisData.size() > 100 ? new Document() : false)//
				.set("图例", new Document());
		return chart.doc();
	}

	private void initializeXAxis() {
		xAxisData = new ArrayList<String>();
		// markLineData = new Document();
		Calendar cal = Calendar.getInstance();
		cal.setTime(from);
		List<Double> agg = new ArrayList<Double>();
		SimpleDateFormat sdf;
		if ("date".equals(xAxis)) {
			dateFormat = "%Y-%m-%d";
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			while (!cal.getTime().after(to)) {
				xAxisData.add(sdf.format(cal.getTime()));
				agg.add(0d);
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
		} else if ("month".equals(xAxis)) {
			dateFormat = "%Y-%m";
			sdf = new SimpleDateFormat("yyyy-MM");
			while (!cal.getTime().after(to)) {
				xAxisData.add(sdf.format(cal.getTime()));
				agg.add(0d);
				cal.add(Calendar.MONTH, 1);
			}
		} else if ("year".equals(xAxis)) {
			dateFormat = "%Y";
			sdf = new SimpleDateFormat("yyyy");
			while (!cal.getTime().after(to)) {
				xAxisData.add(sdf.format(cal.getTime()));
				agg.add(0d);
				cal.add(Calendar.YEAR, 1);
			}
		}
	}

}
