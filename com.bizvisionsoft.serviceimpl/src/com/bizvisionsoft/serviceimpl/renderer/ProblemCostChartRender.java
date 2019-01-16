package com.bizvisionsoft.serviceimpl.renderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
import com.bizvisionsoft.serviceimpl.query.JQ;

public class ProblemCostChartRender extends BasicServiceImpl {

	private List<Document> input;

	private Date from;

	private Date to;

	private String xAxis;// year,month,catalog

	private ArrayList<String> xAxisData;

	@SuppressWarnings("unchecked")
	public ProblemCostChartRender(Document condition) {
		Document option = (Document) condition.get("option");
		List<?> dateRange = (List<?>) option.get("dateRange");
		from = (Date) dateRange.get(0);
		to = (Date) dateRange.get(1);
		xAxis = option.getString("xAxis");
		input = (List<Document>) condition.get("input");
		initializeXAxis();
	}

	public static Document renderClassifyCostChart(Document condition) {
		return new ProblemCostChartRender(condition).render();
	}

	private Document render() {
		if (!"catalog".equals(xAxis)) {
			List<Bson> pipe = new JQ("查询-问题个数和成本-按时间").set("from", from).set("to", to).set("dateGroup", "$" + xAxis).array();
			ArrayList<Document> source = c("problemCostItem").aggregate(pipe).into(new ArrayList<>());
			return new JQ("图表-问题个数和成本-按时间").set("datasetSource", source).set("xAxisData", xAxisData).doc();
		}
		return new Document();
	}

	private void initializeXAxis() {
		xAxisData = new ArrayList<String>();
		// markLineData = new Document();
		Calendar cal = Calendar.getInstance();
		cal.setTime(from);
		List<Double> agg = new ArrayList<Double>();
		SimpleDateFormat sdf;
		if ("date".equals(xAxis)) {
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			while (!cal.getTime().after(to)) {
				xAxisData.add(sdf.format(cal.getTime()));
				agg.add(0d);
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
		} else if ("month".equals(xAxis)) {
			sdf = new SimpleDateFormat("yyyy-MM");
			while (!cal.getTime().after(to)) {
				xAxisData.add(sdf.format(cal.getTime()));
				agg.add(0d);
				cal.add(Calendar.MONTH, 1);
			}
		} else if ("year".equals(xAxis)) {
			sdf = new SimpleDateFormat("yyyy");
			while (!cal.getTime().after(to)) {
				xAxisData.add(sdf.format(cal.getTime()));
				agg.add(0d);
				cal.add(Calendar.YEAR, 1);
			}
		}
	}

}
