package com.bizvisionsoft.serviceimpl.renderer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
import com.bizvisionsoft.serviceimpl.query.JQ;

public class ProblemCostChartRender extends BasicServiceImpl {

	private static final int MIN_COUNT_DATAZOOM = 30;

	private List<Document> input;

	private Date from;

	private Date to;

	private String xAxis;// %Y#%Y-%m#%Y-%m-%d#catalog

	private List<String> x0AxisData;

	private String dateFormat;

	private String title;

	public ProblemCostChartRender() {
	}

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
		return new ProblemCostChartRender(condition).renderClassifyCost();
	}

	public static Document renderClassifyProblemChart(Document condition) {
		return new ProblemCostChartRender(condition).renderClassifyProblem();
	}

	public static Document renderClassifyCauseChart(Document condition) {
		return new ProblemCostChartRender(condition).renderClassifyCause();
	}

	public static Document renderClassifyDeptChart(Document condition) {
		return new ProblemCostChartRender(condition).renderClassifyDept();
	}

	public static Document renderCostClassifyByProblemChart() {
		return new ProblemCostChartRender()._renderCostClassifyByProblemChart();
	}

	public static Document renderCountClassifyByProblemChart() {
		return new ProblemCostChartRender()._renderCountClassifyByProblemChart();
	}

	public static Document renderCostClassifyByCauseChart() {
		return new ProblemCostChartRender()._renderCostPie("��ѯ-����ɱ�-ԭ��Ȩ�ؼ���", "����ԭ����ʧ���أ�", "causeRelation");
	}

	public static Document renderCostClassifyByDeptChart() {
		return new ProblemCostChartRender()._renderCostPie("��ѯ-����ɱ�-�����β��Ż���", "���β��ţ���ʧ���أ�", "problem");
	}

	private Document _renderCountClassifyByProblemChart() {
		String queryName = "��ѯ-���������-��������";
		String title = "��������";
		List<String> xAxis = getXAxisOfProblem();
		return _renderClassifyProblemBarChart(queryName, title, xAxis);
	}

	private Document _renderCostClassifyByProblemChart() {
		String queryName = "��ѯ-���������-���³ɱ�";
		String title = "������ʧ";
		List<String> xAxis = getXAxisOfProblemCost();
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

			Document s = new Document("datasetIndex", 2 * i)//
					.append("name", name)//
					.append("xAxisIndex", 0).append("yAxisIndex", 0)//					
					.append("encode", new Document("x", "_id").append("y", "value"))//
					.append("type", "bar").append("label", new Document("normal", new Document("show", true).append("position", "top")));//
			series.add(s);

			Document mData = new Document("_id", name).append("source", getMoMData((List<?>) data.get("source"), xAxis, -1));
			dataset.add(mData);

			s = new Document("datasetIndex", 2 * i + 1)//
					.append("name", name)//
					.append("xAxisIndex", 0).append("yAxisIndex", 1)//
					.append("encode", new Document("x", "_id").append("y", "value"))//
					.append("type", "line").append("label",
							new Document("normal", new Document("show", false).append("position", "top").append("formatter", "{@value}%")));//
			series.add(s);
		}

		return new JQ("ͼ��-ͨ��-�����ݼ�-���ղ���-2y��")//
				.set("����", title)//
				.set("���ݼ�", dataset)//
				.set("x������", xAxis)//
				.set("y1������", "���")//
				.set("y2������", "���ȼ���(%)")//
				.set("ϵ��", series)//
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
		List<Document> series = Arrays.asList(//
				new Document("type", "pie")//
						.append("radius", "60%").append("label", new Document("normal", new Document("show",true).append("formatter", "{b}\n{d}%")))
						.append("encode", new Document("itemName", "_id").append("value", "amount")));//
		Document doc = new JQ("ͼ��-ͨ��-�����ݼ�-���ղ���-����")//
				.set("����", title)//
				.set("���ݼ�", new Document("source", source))//
				.set("ϵ��", series)//
				.doc();
		return doc;
	}

	private Document renderClassifyDept() {
		List<Bson> pipe = new ArrayList<>();
		List<ObjectId> ids = input.stream().map(doc -> doc.getObjectId("_id")).collect(Collectors.toList());
		pipe.add(new Document("$match", new Document("_id", new Document("$in", ids))));
		new JQ("��ѯ-����ɱ�-���β��Ų�����").appendTo(pipe);
		new JQ("��ѯ-����ɱ�-�����ɱ�").set("��ʼʱ��", from).set("��ֹʱ��", to).set("���ڷ����ʽ", dateFormat).appendTo(pipe);
		ArrayList<Document> dataset = c("organization").aggregate(pipe).into(new ArrayList<>());
		return buildDetailChart(dataset);
	}

	private Document renderClassifyCause() {
		if (!isSelectRoot()) {
			// ����ɸѡ����
			return renderClassifyCauseDetailChart();
		} else {
			return renderRootChart();
		}
	}

	private Document renderClassifyCost() {
		if (!isSelectRoot()) {
			// ����ɸѡ����
			return renderClassifyCostDetailChart();
		} else {
			return renderRootChart();
		}
	}

	private Document renderClassifyProblem() {
		if (!isSelectRoot()) {
			// ����ɸѡ����
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

		List<Document> series = Arrays.asList(new Document("type", "bar")//
				.append("name", "��ʧ���").append("label", new Document("normal", new Document("show", true).append("position", "top")))//
		);

		JQ chart = new JQ("ͼ��-ͨ��-�����ݼ�")//
				.set("����", title)//
				.set("���ݼ�", new Document("source", source))//
				.set("x������", "�ڼ�")//
				.set("x������", x0AxisData)//
				.set("y������", "��ʧ�������Ԫ��").set("ϵ��", series)//
				.set("���ݷ���", x0AxisData.size() > MIN_COUNT_DATAZOOM ? new Document() : false)//
				.set("ͼ��", false);
		return chart.doc();
	}

	private Document renderClassifyCostDetailChart() {
		List<Bson> pipe = new ArrayList<>();
		List<ObjectId> ids = input.stream().map(doc -> doc.getObjectId("_id")).collect(Collectors.toList());
		pipe.add(new Document("$match", new Document("_id", new Document("$in", ids))));
		new JQ("��ѯ-����ɱ�-��ʱ��ͳɱ�����").set("��ʼʱ��", from).set("��ֹʱ��", to).set("���ڷ����ʽ", dateFormat).appendTo(pipe);
		ArrayList<Document> dataset = c("classifyProblemLost").aggregate(pipe).into(new ArrayList<>());
		return buildDetailChart(dataset);
	}

	private Document renderClassifyProblemDetailChart() {
		List<Bson> pipe = new ArrayList<>();
		List<ObjectId> ids = input.stream().map(doc -> doc.getObjectId("_id")).collect(Collectors.toList());
		pipe.add(new Document("$match", new Document("_id", new Document("$in", ids))));
		new JQ("��ѯ-����ɱ�-������������").appendTo(pipe);
		new JQ("��ѯ-����ɱ�-�����ɱ�").set("��ʼʱ��", from).set("��ֹʱ��", to).set("���ڷ����ʽ", dateFormat).appendTo(pipe);
		ArrayList<Document> dataset = c("classifyProblem").aggregate(pipe).into(new ArrayList<>());
		return buildDetailChart(dataset);
	}

	private Document renderClassifyCauseDetailChart() {
		List<Bson> pipe = new ArrayList<>();
		List<ObjectId> ids = input.stream().map(doc -> doc.getObjectId("_id")).collect(Collectors.toList());
		pipe.add(new Document("$match", new Document("_id", new Document("$in", ids))));
		new JQ("��ѯ-����ɱ�-ԭ����������").appendTo(pipe);
		new JQ("��ѯ-����ɱ�-�����ɱ�").set("��ʼʱ��", from).set("��ֹʱ��", to).set("���ڷ����ʽ", dateFormat).appendTo(pipe);
		ArrayList<Document> dataset = c("classifyCause").aggregate(pipe).into(new ArrayList<>());
		return buildDetailChart(dataset);

	}

	private Document buildDetailChart(ArrayList<Document> dataset) {

		// ����ϵ�����ݼ�
		List<Document> summary = new ArrayList<>();

		ArrayList<Document> series = new ArrayList<Document>();
		buildSeriesAndSummary(dataset, summary, series);

		// ׷���ܼ�ϵ��
		if (!summary.isEmpty()) {
			dataset.add(new Document("source", summary));// �ܼ�
			series.add(new Document("datasetIndex", dataset.size() - 1)//
					.append("type", "bar")//
					.append("barWidth", "20%")//
					.append("xAxisIndex", 1).append("yAxisIndex", 1)//
					.append("encode", new Document("x", "_id").append("y", "amount"))
					.append("label", new Document("normal", new Document("show", true).append("position", "top"))));

			// �����ܼ�ϵ�е�x��

			// ׷�ӱ�ͼ
			series.add(new Document("datasetIndex", dataset.size() - 1)//
					.append("name", "����ռ��")//
					.append("type", "pie")//
					.append("center", Arrays.asList("80%", "25%")).append("radius", "30%")
					.append("label", new Document("normal", new Document("formatter", "{b}\n{d}%"))));
		}

		JQ jq = new JQ("ͼ��-ͨ��-��������Ŀ")//
				.set("����", title)//
				.set("���ݼ�", dataset)//
				.set("x������", "�ڼ�")//
				.set("x������", x0AxisData)//
				.set("y������", "��ʧ�������Ԫ��")//
				.set("ϵ��", series)//
				.set("���ݷ���", x0AxisData.size() > MIN_COUNT_DATAZOOM ? new Document("type", "slider").append("bottom", 40) : false)//
				.set("ͼ��", new Document("bottom", 10).append("type", "scroll"));
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
			title = Formatter.getString(from, "yyyy��M��d��") + "��" + Formatter.getString(to, "yyyy��M��d��") + "�ڼ� ������ʧ����ͳ��";
		} else if ("month".equals(xAxis)) {
			dateFormat = "%Y-%m";
			title = Formatter.getString(from, "yyyy��M��") + "��" + Formatter.getString(to, "yyyy��M��") + "�ڼ� ������ʧ����ͳ��";
		} else if ("year".equals(xAxis)) {
			dateFormat = "%Y";
			title = Formatter.getString(from, "yyyy��") + "��" + Formatter.getString(to, "yyyy��") + "�ڼ� ������ʧ����ͳ��";
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
				Arrays.asList(new Document("$match", new Document("status", new Document("$in", Arrays.asList("�Ѵ���", "�����", "�ѹر�")))),
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
