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

import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
import com.bizvisionsoft.serviceimpl.ProblemServiceImpl;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.hankcs.hanlp.HanLP;
import com.mongodb.client.model.Aggregates;

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

	public static Document renderAnlysisChart(Document condition) {
		return new ProblemChartRender()._renderAnlysisChart(condition);
	}

	public static Document renderCostClassifyByProblemChart() {
		return new ProblemChartRender()._renderCostClassifyByProblemChart();
	}

	public static Document renderCountClassifyByProblemChart() {
		return new ProblemChartRender()._renderCountClassifyByProblemChart();
	}

	public static Document renderCostClassifyByCauseChart() {
		return new ProblemChartRender()._renderCostPie("��ѯ-����ɱ�-ԭ��Ȩ�ؼ���", "����ԭ����ʧ���أ�", "causeRelation");
	}

	public static Document renderCostClassifyByDeptChart() {
		return new ProblemChartRender()._renderCostPie("��ѯ-����ɱ�-�����β��Ż���", "���β��ţ���ʧ���أ�", "problem");
	}

	public static Document renderCauseProblemChart() {
		return new ProblemChartRender()._renderCauseProblemChart();
	}

	@SuppressWarnings("unchecked")
	private Document _renderAnlysisChart(Document condition) {
		List<Document> categories = new ArrayList<>();
		List<Document> nodes = new ArrayList<>();
		List<Document> links = new ArrayList<>();

		Document option = (Document) condition.get("option");
		String rootId = option.getObjectId("problem_id").toHexString();
		String rootTitle = option.getString("keyword");

		// ��1��������ڵ�
		String cata = rootTitle;
		Document node = new Document("name", rootTitle)//
				.append("id", rootId)//
				.append("draggable", true)//
				.append("category", cata)//
				.append("symbolSize", 80)//
				.append("symbol", "circle")//
				.append("itemStyle", new Document("opacity", 0.8).append("shadowColor", "rgba(0, 0, 0, 0.5)").append("shadowBlur", 10));//
		categories.add(new Document("name", cata));
		nodes.add(node);

		// 0. �ֽ�ؼ���
		List<String> keyword = HanLP.extractKeyword(rootTitle, 15);

		// �޶���������
		List<ObjectId> catalog_ids = ((List<Document>) condition.get("input")).stream().map(d -> d.getObjectId("_id"))
				.collect(Collectors.toList());

		// 1. �������Ƶ�����; TODO ʹ���������
		List<ObjectId> similarProblems = new ArrayList<>();
		c("problem").aggregate(Arrays.asList(//
				Aggregates.match(new Document("status", "�ѹر�").append("classifyProblem._ids", new Document("$in", catalog_ids)))//
		)).forEach((Document d) -> {
			if (HanLP.extractKeyword(d.getString("name"), 15).stream().anyMatch(keyword::contains)) {
				// ��2��������������Ŀ¼
				String probCata = d.getString("name");
				categories.add(new Document("name", probCata));

				ObjectId parentId = d.getObjectId("_id");
				String subid = parentId.toHexString();
				Document subnode = new Document("name", probCata).append("id", subid)//
						.append("draggable", true).append("category", probCata).append("symbolSize", 50);//
				nodes.add(subnode);

				Document sublink = new Document("source", subid).append("target", rootId)//
						.append("label", new Document("show", true).append("formatter", "function(d){return '����';}"))//
						.append("emphasis", new Document("label", new Document("show", false)));
				links.add(sublink);

				// 2. �������Ƶ��������ԭ��
				appendCauseNodesLinks(nodes, links, parentId, "����", "�������-����", probCata);
				appendCauseNodesLinks(nodes, links, parentId, "����", "�������-����", probCata);

				similarProblems.add(d.getObjectId("_id"));
			}
		});
		// ��3��. �������Ƶ��������ERA,ICA,PCA
		String[] stage = {"era","ica","pca"};
		String[] name = {"ERA","ICA","PCA"};
		for (int i = 0; i < name.length; i++) {
			ArrayList<Document> actions = c("problemAction").aggregate(Arrays.asList(
					Aggregates.match(new Document("stage",stage[i]).append("problem_id", new Document("$in",similarProblems))),
					Aggregates.sort(new Document("problem_id",1).append("index", 1))
					)).into(new ArrayList<>());
			if(!actions.isEmpty()) {
				String actionCata = name[i];
				categories.add(new Document("name", actionCata));
				//��3.1�������ж����ڵ�
				String stageId = new ObjectId().toHexString();
				Document subnode = new Document("name", name[i]).append("id", stageId)//
						.append("label", new Document("position","inside")).append("draggable", true).append("category", actionCata).append("symbolSize", 36);//
				nodes.add(subnode);

				Document sublink = new Document("source", stageId).append("target", rootId)//
						.append("emphasis", new Document("label", new Document("show", false)));
				links.add(sublink);
				//��3.2�������ж��ڵ���¼��ڵ�
				actions.forEach(d->{
					String actionId = d.getObjectId("_id").toHexString();
					Document action = new Document("name", d.getString("action")).append("id", actionId)//
							.append("draggable", true).append("category", actionCata).append("symbolSize", 10);//
					nodes.add(action);

					Document actionLink = new Document("source", actionId).append("target", stageId)//
							.append("emphasis", new Document("label", new Document("show", false)));
					links.add(actionLink);
				});
			}
		}
		
		
		
		// ��4��. ���ݹؼ��� ���Ҿ����
		categories.add(new Document("name", "����"));
		c("d8Exp").aggregate(Arrays.asList(// TODO ����ʱдkeyword
				Aggregates.match(new Document("keyword", new Document("$in", keyword))))).forEach((Document exp) -> {
					String id = exp.getObjectId("_id").toHexString();
					Document subnode = new Document("name", exp.getString("name")).append("id", id)//
							.append("symbol", "path://M165,30c-2.762,0-5,2.239-5,5v150c0,2.762-2.238,5-5,5H45c-8.27,0-15-6.73-15-15s6.73-15,15-15h90\r\n" + 
									"		c8.27,0,15-6.73,15-15V25c0-8.27-6.73-15-15-15H35c-8.27,0-15,6.73-15,15v150c0,13.779,11.22,25,25,25h110c8.27,0,15-6.73,15-15V35\r\n" + 
									"		C170,32.239,167.762,30,165,30z")
							.append("draggable", true).append("category", "����").append("symbolSize", 16);//
					nodes.add(subnode);

					Document sublink = new Document("source", id).append("target", rootId)//
							.append("label", new Document("show", true).append("formatter", "function(d){return '����';}"))//
							.append("emphasis", new Document("label", new Document("show", false)));
					links.add(sublink);

				});
		
		if(nodes.size()==1) {
			return blankChart();
		}
		Document chart = new JQ("ͼ��-�����ϵͼ-����ͷ-���Ӿ�").set("����", "").set("data", nodes).set("links", links).set("categories", categories)
				.doc();
		debugDocument(chart);
		return chart;
	}

	private void appendCauseNodesLinks(List<Document> nodes, List<Document> links, ObjectId sProb_id, String name, String type,
			String cata) {
		String makeId = new ObjectId().toHexString();
		Document node = new Document("name", "?").append("id", makeId)//
				.append("label", new Document("position","inside"))
				.append("draggable", true).append("category", cata).append("symbolSize", 36);//
		nodes.add(node);
		Document link = new Document("source", makeId).append("target", sProb_id.toHexString())//
				.append("label", new Document("show", true).append("formatter", "function(d){return '"+name+"';}"))//
				.append("emphasis", new Document("label", new Document("show", false)));
		links.add(link);

		c("causeRelation").find(new Document("problem_id", sProb_id).append("type", type)).forEach((Document doc) -> {
			String id = doc.getObjectId("_id").toHexString();
			Document item = new Document("id", id)//
					.append("name", doc.get("name"))//
					.append("draggable", true)//
					.append("category", cata)//
					.append("symbolSize", 5 * doc.getInteger("weight", 1))//
					.append("value", 100 * doc.getDouble("probability"));
			nodes.add(item);

			ObjectId parentId = doc.getObjectId("parent_id");
			if (parentId == null) {
				links.add(new Document("source", id).append("target", makeId)//
						.append("label",
								new Document("show", true).append("formatter", "function(d){return '" + doc.getString("subject") + "';}"))//
						.append("emphasis", new Document("label", new Document("show", false))));
			} else {
				links.add(new Document("source", id).append("target", parentId.toHexString()).append("emphasis",
						new Document("label", new Document("show", false))));
			}
		});
	}

	private Document _renderCauseProblemChart() {
		List<Document> categories = new ArrayList<>();
		List<Document> nodes = c("problem").aggregate(new JQ("��ѯ-����Ȩ��").array()).map((Document d) -> {
			String name = d.getString("name");
			String cata = "����:" + name.split("/")[0].trim();
			categories.add(new Document("name", cata));
			double value = Check.instanceOf(d.get("value"), Number.class).map(n -> n.doubleValue()).orElse(0d);
			return new Document("name", name)//
					.append("id", d.getObjectId("_id").toHexString())//
					.append("draggable", true)//
					.append("category", cata)//
					.append("symbolSize", 100 * value)//
					.append("symbol", "circle")//
					.append("itemStyle", new Document("opacity", 0.8).append("shadowColor", "rgba(0, 0, 0, 0.5)").append("shadowBlur", 10))//
					.append("value", 100 * value);
		}).into(new ArrayList<>());

		c("problem").aggregate(new JQ("��ѯ-ԭ��Ȩ��").array()).map((Document d) -> {
			String name = d.getString("name");
			String cata = "ԭ��:" + name.split("/")[0].trim();
			categories.add(new Document("name", cata));
			double value = Check.instanceOf(d.get("value"), Number.class).map(n -> n.doubleValue()).orElse(0d);
			return new Document("name", name)//
					.append("id", d.getObjectId("_id").toHexString())//
					.append("draggable", true)//
					.append("category", cata)//
					.append("symbolSize", value / 100)//
					.append("value", value / 100);
		}).into(nodes);

		List<Document> links = c("problem").aggregate(new JQ("��ѯ-���������ϵ").array())
				.map((Document d) -> new Document("source", d.getObjectId("_id").toHexString())
						.append("target", d.getObjectId("pid").toHexString())
						.append("emphasis", new Document("label", new Document("show", false))))
				.into(new ArrayList<>());

		Document chart = new JQ("ͼ��-�����ϵͼ-����ͷ").set("����", "�����������").set("data", nodes).set("links", links).set("categories", categories)
				.doc();
		debugDocument(chart);
		return chart;
	}

	private Document _renderCountClassifyByProblemChart() {
		List<String> xAxis = getXAxisOfProblem();
		if (xAxis.isEmpty())
			return blankChart();
		String queryName = "��ѯ-���������-��������";
		String title = "��������";
		return _renderClassifyProblemBarChart(queryName, title, xAxis);
	}

	private Document _renderCostClassifyByProblemChart() {
		List<String> xAxis = getXAxisOfProblemCost();
		if (xAxis.isEmpty())
			return blankChart();
		String queryName = "��ѯ-���������-���³ɱ�";
		String title = "������ʧ";
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
		if (source.isEmpty())
			return blankChart();

		List<Document> series = Arrays.asList(//
				new Document("type", "pie")//
						.append("radius", 100)
						.append("label", new Document("normal", new Document("show", true).append("formatter", "{b}\n{d}%")))
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
		if (source.isEmpty())
			return blankChart();

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
		if (dataset.isEmpty())
			return blankChart();

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
						new Document("$group", new Document("_id", null).append("from", new Document("$min", "$issueDate")).append("to",
								new Document("$max", "$issueDate")))))
				.first();
		if (date == null)
			return new ArrayList<>();
		Date from = date.getDate("from");
		Date to = date.getDate("to");
		return getXAxisData("month", from, to);
	}

}
