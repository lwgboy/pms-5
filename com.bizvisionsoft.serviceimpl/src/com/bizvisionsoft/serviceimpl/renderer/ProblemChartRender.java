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

import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.common.JQ;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
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

	private String domain;
	
	public ProblemChartRender(String domain) {
		this.domain = domain;
	}

	@SuppressWarnings("unchecked")
	public ProblemChartRender(Document condition, String domain) {
		this.domain = domain;
		Document option = (Document) condition.get("option");
		List<?> dateRange = (List<?>) option.get("dateRange");
		from = (Date) dateRange.get(0);
		to = (Date) dateRange.get(1);
		xAxis = option.getString("xAxis");
		input = (List<Document>) condition.get("input");
		initializeXAxis();
	}

	public static Document renderClassifyCostChart(Document condition, String domain) {
		return new ProblemChartRender(condition, domain).renderClassifyCost();
	}

	public static Document renderClassifyProblemChart(Document condition, String domain) {
		return new ProblemChartRender(condition, domain).renderClassifyProblem();
	}

	public static Document renderClassifyCauseChart(Document condition, String domain) {
		return new ProblemChartRender(condition, domain).renderClassifyCause();
	}

	public static Document renderClassifyDeptChart(Document condition, String domain) {
		return new ProblemChartRender(condition, domain).renderClassifyDept();
	}

	public static Document renderAnlysisChart(Document condition, String domain) {
		return new ProblemChartRender(domain)._renderAnlysisChart(condition);
	}

	public static Document renderCostClassifyByProblemChart(String domain) {
		return new ProblemChartRender(domain)._renderCostClassifyByProblemChart();
	}

	public static Document renderCountClassifyByProblemChart(String domain) {
		return new ProblemChartRender(domain)._renderCountClassifyByProblemChart();
	}

	public static Document renderCostClassifyByCauseChart(String domain) {
		return new ProblemChartRender(domain)._renderCostPie("��ѯ-����ɱ�-ԭ��Ȩ�ؼ���", "����ԭ����ʧ���أ�", "causeRelation");
	}

	public static Document renderCostClassifyByDeptChart(String domain) {
		return new ProblemChartRender(domain)._renderCostPie("��ѯ-����ɱ�-�����β��Ż���", "���β��ţ���ʧ���أ�", "problem");
	}

	public static Document renderCauseProblemChart(String domain) {
		return new ProblemChartRender(domain)._renderCauseProblemChart();
	}

	public static Document renderCountClassifyByProblemChartDate(String domain) {
		return new ProblemChartRender(domain)._renderCountClassifyByProblemChartDate();
	}
	public static Document renderCountcreateCountOrganizationChart(Document condition,String domain) {
		return new ProblemChartRender(domain)._createResOrganizationChart(condition,domain);
	}
	public static Document renderCountcreateCountClassifyProblemChart(Document condition,String domain) {
		return new ProblemChartRender(domain)._createResclassifyProblemChart(condition,domain);
	}
	public static Document renderCountcreateCountClassifyProblemsChart(Document condition,String domain) {
		return new ProblemChartRender(domain)._createResclassifyProblemsChart(condition,domain);
	}
	public static Document renderCountcreateCountOrganizationDeptsChart(Document condition,String domain) {
		return new ProblemChartRender(domain)._createResOrganizationDeptsChart(condition,domain);
	}
	public static Document renderCountcreateCountClassifyProblemXianXinChart(Document condition,String domain) {
		return new ProblemChartRender(domain)._createResclassifyProblemXianXinChart(condition,domain);
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
				.append("mType", "root")//
				.append("draggable", true)//
				.append("category", cata)//
				.append("symbolSize", 80)//
				.append("symbol", "circle")//
				.append("itemStyle", new Document("opacity", 0.8).append("shadowColor", "rgba(0, 0, 0, 0.5)")
						.append("shadowBlur", 20));//
		categories.add(new Document("name", cata));

		nodes.add(node);

		// 0. �ֽ�ؼ���
		List<String> keyword = HanLP.extractKeyword(rootTitle, 15);

		// �޶���������
		List<ObjectId> catalog_ids = ((List<Document>) condition.get("input")).stream().map(d -> d.getObjectId("_id"))
				.collect(Collectors.toList());

		// 1. �������Ƶ�����; TODO ʹ���������
		List<ObjectId> similarProblems = new ArrayList<>();
		c("problem", domain).aggregate(Arrays.asList(//
				Aggregates.unwind("$classifyProblems"),
				Aggregates.match(
						new Document("status", "�ѹر�").append("classifyProblems._ids", new Document("$in", catalog_ids))),
				new Document("$group",new Document().append("_id", null).append("p_id", new Document("$addToSet","$_id"))),
				Aggregates.unwind("$p_id"),
				Aggregates.lookup("problem", "p_id", "_id", "newproblem"),
				Aggregates.unwind("$newproblem"),
				new Document().append("$replaceRoot", new Document().append("newRoot","$newproblem"))
		)).forEach((Document d) -> {
			// Modify by zjc 20190318 �����������ų������������У��������Ϊ����ָ���������쳣
			if (HanLP.extractKeyword(d.getString("name"), 15).stream().anyMatch(keyword::contains)
					&& !d.getObjectId("_id").toHexString().equals(rootId)) {
				// ��2��������������Ŀ¼
				String probCata = d.getString("name");
				categories.add(new Document("name", probCata));

				ObjectId parentId = d.getObjectId("_id");
				String subid = parentId.toHexString();
				Document subnode = new Document("name", probCata)//
						.append("id", subid)//
						.append("mType", "problem")//
						.append("draggable", true)//
						.append("category", probCata)//
						.append("symbolSize", 50);//
				nodes.add(subnode);

				links.add(createGraphicLink(subid, rootId, "����"));

				// 2. �������Ƶ��������ԭ��
				appendCauseNodesLinks(nodes, links, parentId, "����", "�������-����", probCata);
				appendCauseNodesLinks(nodes, links, parentId, "����", "�������-����", probCata);

				similarProblems.add(d.getObjectId("_id"));
			}
		});
		// ��3��. �������Ƶ��������ERA,ICA,PCA
		String[] stage = { "era", "ica", "pca" };
		String[] name = { "ERA", "ICA", "PCA" };
		for (int i = 0; i < name.length; i++) {
			ArrayList<Document> actions = c("problemAction", domain).aggregate(Arrays.asList(
					Aggregates.match(
							new Document("stage", stage[i]).append("problem_id", new Document("$in", similarProblems))),
					Aggregates.sort(new Document("problem_id", 1).append("index", 1)))).into(new ArrayList<>());
			if (!actions.isEmpty()) {
				String actionCata = name[i];
				categories.add(new Document("name", actionCata));
				// ��3.1�������ж����ڵ�
				String stageId = new ObjectId().toHexString();
				Document subnode = new Document("name", name[i])//
						.append("id", stageId)//
						.append("label", new Document("position", "inside")).append("draggable", true)
						.append("category", actionCata).append("symbolSize", 36);//
				nodes.add(subnode);

				links.add(createGraphicLink(stageId, rootId, "�ж�"));
				// ��3.2�������ж��ڵ���¼��ڵ�
				actions.forEach(d -> {
					String actionId = d.getObjectId("_id").toHexString();
					Document action = new Document("name", d.getString("action"))//
							.append("id", actionId)//
							.append("mType", "problemAction")//
							.append("draggable", true)//
							.append("category", actionCata)//
							.append("symbolSize", 10);//
					nodes.add(action);

					links.add(createGraphicLink(actionId, stageId));
				});
			}
		}

		// ��4��. ���ݹؼ��� ���Ҿ����
		categories.add(new Document("name", "����"));
		c("d8Exp", domain).aggregate(Arrays.asList(// TODO ����ʱдkeyword
				Aggregates.match(new Document("keyword", new Document("$in", keyword))))).forEach((Document exp) -> {
					String id = exp.getObjectId("_id").toHexString();
					Document subnode = new Document("name", exp.getString("name"))//
							.append("id", id)//
							.append("mType", "d8EXP")//
							.append("symbol",
									"path://M165,30c-2.762,0-5,2.239-5,5v150c0,2.762-2.238,5-5,5H45c-8.27,0-15-6.73-15-15s6.73-15,15-15h90\r\nc8.27,0,15-6.73,15-15V25c0-8.27-6.73-15-15-15H35c-8.27,0-15,6.73-15,15v150c0,13.779,11.22,25,25,25h110c8.27,0,15-6.73,15-15V35\r\nC170,32.239,167.762,30,165,30z")
							.append("draggable", true)//
							.append("category", "����")//
							.append("symbolSize", Arrays.asList(20, 23));//
					nodes.add(subnode);

					links.add(createGraphicLink(id, rootId, "����"));
				});

		if (nodes.size() == 1) {
			return blankChart(domain);
		}
		Document chart = Domain.getJQ(domain, "ͼ��-�����ϵͼ-����ͷ-���Ӿ�").set("����", "").set("data", nodes).set("links", links)
				.set("categories", categories).doc();
		debugDocument(chart);
		return chart;
	}

	private void appendCauseNodesLinks(List<Document> nodes, List<Document> links, ObjectId sProb_id, String name,
			String type, String cata) {
		String makeId = new ObjectId().toHexString();
		nodes.add(new Document("name", "")//
				.append("id", makeId)//
				.append("symbol",
						"path://M100.097,199.785l-86.416-49.893V50.108l86.416-49.893l86.417,49.893v99.785L100.097,199.785z")
				.append("label", new Document("position", "inside")).append("draggable", true).append("category", cata)
				.append("symbolSize", Arrays.asList(20, 23)));//
		links.add(createGraphicLink(makeId, sProb_id.toHexString(), name));

		c("causeRelation", domain).find(new Document("problem_id", sProb_id).append("type", type))
				.forEach((Document doc) -> {
					String id = doc.getObjectId("_id").toHexString();
					nodes.add(new Document("id", id)//
							.append("name", doc.get("name"))//
							.append("draggable", true)//
							.append("mType", "causeRelation")//
							.append("category", cata)//
							.append("symbolSize", 5 * doc.getInteger("weight", 1))//
							.append("value", 100 * doc.getDouble("probability")));

					ObjectId parentId = doc.getObjectId("parent_id");
					if (parentId == null) {
						links.add(createGraphicLink(id, makeId, doc.getString("subject")));
					} else {
						links.add(createGraphicLink(id, parentId.toHexString()));
					}
				});
	}

	private Document createGraphicLink(String src, String tgt, String text) {
		return new Document("source", src)//
				.append("target", tgt)//
				.append("label", new Document("show", true)//
						.append("formatter", "function(d){return '" + text + "';}"))//
				.append("emphasis", new Document("label", new Document("show", false)));
	}

	private Document createGraphicLink(String src, String tgt) {
		return new Document("source", src).append("target", tgt)//
				.append("emphasis", new Document("label", new Document("show", false)));
	}

	@SuppressWarnings("unchecked")
	private Document _renderCauseProblemChart() {
		List<Document> categories = new ArrayList<>();
		List<Document> nodes = c("problem", domain).aggregate(Domain.getJQ(domain, "��ѯ-����Ȩ��").array())
				.map((Document d) -> {
//					String name = ((List<String>) d.get("name")).get(0);
					String name = (String) d.get("name");
					String cata = "����:" + name.split("/")[0].trim();
					categories.add(new Document("name", cata));
					double value = Check.instanceOf(d.get("value"), Number.class).map(n -> n.doubleValue()).orElse(0d);
//					ObjectId _id = ((List<ObjectId>) d.get("_id")).get(0);
					ObjectId _id = (ObjectId)d.get("_id");
					return new Document("name", name)//
							.append("id", _id.toHexString())//
							.append("draggable", true)//
							.append("category", cata)//
							.append("symbolSize", 100 * value)//
							.append("symbol", "circle")//
							.append("itemStyle",
									new Document("opacity", 0.8).append("shadowColor", "rgba(0, 0, 0, 0.5)")
											.append("shadowBlur", 10))//
							.append("value", 100 * value);
				}).into(new ArrayList<>());

		c("problem", domain).aggregate(Domain.getJQ(domain, "��ѯ-ԭ��Ȩ��").array()).map((Document d) -> {
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

		List<Document> links = c("problem", domain).aggregate(Domain.getJQ(domain, "��ѯ-���������ϵ").array())
				.map((Document d) -> createGraphicLink(d.getObjectId("_id").toHexString(),
//						((List<ObjectId>) d.get("pid")).get(0).toHexString()))
						((ObjectId) d.get("pid")).toHexString()))
				.into(new ArrayList<>());

		Document chart = Domain.getJQ(domain, "ͼ��-�����ϵͼ-����ͷ").set("����", "�����������").set("data", nodes).set("links", links)
				.set("categories", categories).doc();
		debugDocument(chart);
		return chart;
	}

	private Document _renderCountClassifyByProblemChart() {
		List<String> xAxis = getXAxisOfProblem();
		if (xAxis.isEmpty())
			return blankChart(domain);
		String queryName = "��ѯ-���������-��������";
		String title = "��������";
		return _renderClassifyProblemBarChart(queryName, title, xAxis);
	}

	private Document _renderCostClassifyByProblemChart() {
		List<String> xAxis = getXAxisOfProblemCost();
		if (xAxis.isEmpty())
			return blankChart(domain);
		String queryName = "��ѯ-���������-���³ɱ�";
		String title = "������ʧ";
		return _renderClassifyProblemBarChart(queryName, title, xAxis);
	}

	private Document _renderClassifyProblemBarChart(String queryName, String title, List<String> xAxis) {
		List<Document> dataset = new ArrayList<>();
		List<Document> series = new ArrayList<>();

		ArrayList<Document> result = c("classifyProblem", domain).aggregate(Domain.getJQ(domain, queryName).array())
				.into(new ArrayList<>());
		for (int i = 0; i < result.size(); i++) {
			Document data = result.get(i);
			dataset.add(data);
			String name = data.getString("_id");

			Document s = new Document("datasetIndex", dataset.size() - 1)//
					.append("name", name)//
					.append("xAxisIndex", 0).append("yAxisIndex", 0)//
					.append("encode", new Document("x", "_id").append("y", "value"))//
					.append("type", "bar")
					.append("label", new Document("normal", new Document("show", true).append("position", "top")));//
			series.add(s);

			List<Document> momSource = getMoMData((List<?>) data.get("source"), xAxis, -1);
			if (!momSource.isEmpty()) {
				Document mData = new Document("_id", name).append("source", momSource);
				dataset.add(mData);

				s = new Document("datasetIndex", dataset.size() - 1)//
						.append("name", name)//
						.append("xAxisIndex", 0).append("yAxisIndex", 1)//
						.append("encode", new Document("x", "_id").append("y", "value"))//
						.append("type", "line").append("label", new Document("normal", new Document("show", false)
								.append("position", "top").append("formatter", "{@value}%")));//
				series.add(s);
			}
		}

		return Domain.getJQ(domain, "ͼ��-ͨ��-�����ݼ�-���ղ���-2y��")//
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
		ArrayList<Document> source = c(col, domain).aggregate(Domain.getJQ(domain, queryName).array())
				.into(new ArrayList<>());
		if (source.isEmpty())
			return blankChart(domain);

		List<Document> series = Arrays.asList(//
				new Document("type", "pie")//
						.append("radius", 100)
						.append("label",
								new Document("normal", new Document("show", true).append("formatter", "{b}\n{d}%")))
						.append("encode", new Document("itemName", "_id").append("value", "amount")));//
		Document doc = Domain.getJQ(domain, "ͼ��-ͨ��-�����ݼ�-���ղ���-����")//
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
		Domain.getJQ(domain, "��ѯ-����ɱ�-���β��Ų�����").appendTo(pipe);
		Domain.getJQ(domain, "��ѯ-����ɱ�-�����ɱ�").set("��ʼʱ��", from).set("��ֹʱ��", to).set("���ڷ����ʽ", dateFormat)
				.appendTo(pipe);
		ArrayList<Document> dataset = c("organization", domain).aggregate(pipe).into(new ArrayList<>());
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
				new Document("date",
						new Document("$dateToString", new Document("date", "$date").append("format", dateFormat)))
								.append("amount", new Document("$subtract", Arrays.asList("$drAmount", "$crAmount")))));
		pipe.add(
				new Document("$group", new Document("_id", "$date").append("amount", new Document("$sum", "$amount"))));

		ArrayList<Document> source = c("problemCostItem", domain).aggregate(pipe).into(new ArrayList<>());
		if (source.isEmpty())
			return blankChart(domain);

		List<Document> series = Arrays.asList(new Document("type", "bar")//
				.append("name", "��ʧ���")
				.append("label", new Document("normal", new Document("show", true).append("position", "top")))//
		);

		JQ chart = Domain.getJQ(domain, "ͼ��-ͨ��-�����ݼ�")//
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
		Domain.getJQ(domain, "��ѯ-����ɱ�-��ʱ��ͳɱ�����").set("��ʼʱ��", from).set("��ֹʱ��", to).set("���ڷ����ʽ", dateFormat)
				.appendTo(pipe);
		ArrayList<Document> dataset = c("classifyProblemLost", domain).aggregate(pipe).into(new ArrayList<>());
		return buildDetailChart(dataset);
	}

	private Document renderClassifyProblemDetailChart() {
		List<Bson> pipe = new ArrayList<>();
		List<ObjectId> ids = input.stream().map(doc -> doc.getObjectId("_id")).collect(Collectors.toList());
		pipe.add(new Document("$match", new Document("_id", new Document("$in", ids))));
		Domain.getJQ(domain, "��ѯ-����ɱ�-������������").appendTo(pipe);
		Domain.getJQ(domain, "��ѯ-����ɱ�-�����ɱ�").set("��ʼʱ��", from).set("��ֹʱ��", to).set("���ڷ����ʽ", dateFormat)
				.appendTo(pipe);
		ArrayList<Document> dataset = c("classifyProblem", domain).aggregate(pipe).into(new ArrayList<>());
		return buildDetailChart(dataset);
	}

	private Document renderClassifyCauseDetailChart() {
		List<Bson> pipe = new ArrayList<>();
		List<ObjectId> ids = input.stream().map(doc -> doc.getObjectId("_id")).collect(Collectors.toList());
		pipe.add(new Document("$match", new Document("_id", new Document("$in", ids))));
		Domain.getJQ(domain, "��ѯ-����ɱ�-ԭ����������").appendTo(pipe);
		Domain.getJQ(domain, "��ѯ-����ɱ�-�����ɱ�").set("��ʼʱ��", from).set("��ֹʱ��", to).set("���ڷ����ʽ", dateFormat)
				.appendTo(pipe);
		ArrayList<Document> dataset = c("classifyCause", domain).aggregate(pipe).into(new ArrayList<>());
		return buildDetailChart(dataset);

	}

	private Document buildDetailChart(ArrayList<Document> dataset) {
		if (dataset.isEmpty())
			return blankChart(domain);

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

		JQ jq = Domain.getJQ(domain, "ͼ��-ͨ��-��������Ŀ")//
				.set("����", title)//
				.set("���ݼ�", dataset)//
				.set("x������", "�ڼ�")//
				.set("x������", x0AxisData)//
				.set("y������", "��ʧ�������Ԫ��")//
				.set("ϵ��", series)//
				.set("���ݷ���",
						x0AxisData.size() > MIN_COUNT_DATAZOOM ? new Document("type", "slider").append("bottom", 40)
								: false)//
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
			Collections.sort(list,
					(o1, o2) -> ((Document) o1).getString("_id").compareTo(((Document) o2).getString("_id")));

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
		Document date = c("problemCostItem", domain)
				.aggregate(Arrays.asList(new Document("$group", new Document("_id", null)
						.append("from", new Document("$min", "$date")).append("to", new Document("$max", "$date")))))
				.first();

		if (date == null)
			return new ArrayList<>();
		Date from = date.getDate("from");
		Date to = date.getDate("to");
		return getXAxisData("month", from, to);
	}

	private List<String> getXAxisOfProblem() {
		Document date = c("problem", domain).aggregate(Arrays.asList(
				new Document("$match", new Document("status", new Document("$in", Arrays.asList("�Ѵ���", "�����", "�ѹر�")))),
				new Document("$group", new Document("_id", null).append("from", new Document("$min", "$issueDate"))
						.append("to", new Document("$max", "$issueDate")))))
				.first();
		if (date == null)
			return new ArrayList<>();
		Date from = date.getDate("from");
		Date to = date.getDate("to");
		return getXAxisData("month", from, to);
	}

	private ArrayList<String> getXAxisData(String xAxis, Date from, Date to) {
		ArrayList<String> xAxisData = new ArrayList<String>();
		SimpleDateFormat sdf;
		if ("date".equals(xAxis)) {
			Calendar cal = Calendar.getInstance();
			from = Formatter.getStartOfDay(from);
			to = Formatter.getEndOfDay(to);
			cal.setTime(from);
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			while (!cal.getTime().after(to)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
		} else if ("month".equals(xAxis)) {
			Calendar cal = Calendar.getInstance();
			from = Formatter.getStartOfMonth(from);
			to = Formatter.getEndOfMonth(to);
			cal.setTime(from);
			sdf = new SimpleDateFormat("yyyy-MM");
			while (!cal.getTime().after(to)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.MONTH, 1);
			}
		} else if ("year".equals(xAxis)) {
			Calendar cal = Calendar.getInstance();
			from = Formatter.getStartOfYear(from);
			to = Formatter.getEndOfYear(to);
			cal.setTime(from);
			sdf = new SimpleDateFormat("yyyy");
			while (!cal.getTime().after(to)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.YEAR, 1);
			}
		}
		return xAxisData;
	}
	
	private Document _renderCountClassifyByProblemChartDate() {
		List<String> xAxis = getXAxisOfProblem();
		if (xAxis.isEmpty())
			return blankChart(domain);
		String queryName = "��ѯ-���������-���ż�ʱ���ڽ���";
		String title = "���Ű��ڽ���";
		return _renderClassifyProblemBarChartDate(queryName, title, xAxis);
	}
	
	private Document _renderClassifyProblemBarChartDate(String queryName, String title, List<String> xAxis) {
		List<Document> dataset = new ArrayList<>();
		List<Document> series = new ArrayList<>();
		ArrayList<Document> result = new ArrayList<Document>();
				c("problemAction", domain).aggregate(Domain.getJQ(domain, queryName).array()).into(new ArrayList<>()).forEach((Document d) -> {
					List<?> source=(List<?>)d.get("source");
					List<Document> DocumentList =new ArrayList<Document>();
					for(int i=0;i<source.size();i++) {
						Document document=(Document)source.get(i);
						document.append("value",Double.parseDouble(String.format("%.1f", (Double)document.get("value")).toString()));
						DocumentList.add(document);
					}
					d.append("source", DocumentList);
					result.add(d);
				});
		for (int i = 0; i < result.size(); i++) {
			Document data = result.get(i);
			dataset.add(data);
			String name = data.getString("_id");
			Document s = new Document("datasetIndex", dataset.size() - 1)//
					.append("name", name)//
					.append("xAxisIndex", 0).append("yAxisIndex", 0)//
					.append("encode", new Document("x", "_id").append("y", "value"))//
					.append("type", "bar")
					.append("label", new Document("normal", new Document("show", true).append("position", "top")));//
			series.add(s);
			List<Document> momSource = getMoMDate((List<?>) data.get("source"), xAxis, -1);
			if (!momSource.isEmpty()) {
				Document mData = new Document("_id", name).append("source", momSource);
				dataset.add(mData);
				s = new Document("datasetIndex", dataset.size() - 1)//
						.append("name", name)//
						.append("xAxisIndex", 0).append("yAxisIndex", 1)//
						.append("encode", new Document("x", "_id").append("y", "value"))//
						.append("type", "line").append("label", new Document("normal", new Document("show", false)
								.append("position", "top").append("formatter", "{@value}%")));//
				series.add(s);
			}
		}
		return Domain.getJQ(domain, "ͼ��-ͨ��-�����ݼ�-���ղ���-2y��")//
				.set("����", title)//
				.set("���ݼ�", dataset)//
				.set("x������", xAxis)//
				.set("y1������", "����")//
				.set("y2������", "���ȼ���(%)")//
				.set("ϵ��", series)//
				.doc();
	}
	
	private List<Document> getMoMDate(List<?> list, List<String> xAxis, int direction) {
		Map<String, Double> map = new HashMap<String, Double>();
		list.forEach(o -> map.put(((Document) o).getString("_id"), ((Document) o).getDouble("value")));
		Calendar cal = Calendar.getInstance();
		double [] cValue = {0};
		List<Document> result = new ArrayList<>();
		xAxis.forEach(date -> {
			try {
				cal.setTime(new SimpleDateFormat("yyyy-MM").parse(date));
				cal.add(Calendar.MONTH, -1);
				double pValue = Optional.ofNullable(map.get(date)).orElse(0d);
				if (pValue != 0) {
					if(cValue[0]==0) {
						result.add(new Document("_id", date).append("value",100));
					}else {
						result.add(new Document("_id", date).append("value",
								(double) Math.round((100/(cValue[0]/pValue))-100)));
					}
					cValue[0]=pValue;	
				}
			} catch (ParseException e) {
			}
		});
		return result;
	}
	
	public Document _createResOrganizationChart(Document condition,String domain) {
		this.domain=domain;
		List<String> xAxis = getXAxisOrganizationChart();
		if (xAxis.isEmpty())
			return blankChart(domain);
		String queryName = "��ѯ-���������-�ͻ�Ͷ���嵥";
		String title = "�ͻ�Ͷ���嵥";
		return _renderOrganizationBarChart(queryName, title, xAxis,"organization");
	}
	
	public Document _createResclassifyProblemChart(Document condition,String domain) {
		this.domain=domain;
		List<String> xAxis = getXAxisOrganizationChart();
		if (xAxis.isEmpty())
			return blankChart(domain);
		String queryName = "��ѯ-���������-�ͻ�Ͷ�߲�������";
		String title = "Ͷ�߲�������";
		return _renderOrganizationBarChart(queryName, title, xAxis,"classifyProblem");
	}
	//��ȡʱ��   
	private List<String> getXAxisOrganizationChart() {
        Calendar currCal=Calendar.getInstance();  
        int currentYear = currCal.get(Calendar.YEAR);
		//��ȡ��Сʱ��
		Date from =getYearFirst(currentYear);
		//��ȡ���ʱ��
		Date to = getYearLast(currentYear);
		//��ʱ���Ϊ�������� ���� list ����  ʱ������
		return getXAxisData("month", from, to);
	}
	
	public static Date getYearFirst(int year) {
		Calendar calendar=Calendar.getInstance();
		calendar.clear();
		calendar.set(Calendar.YEAR, year);
		Date CurrYearFirst=calendar.getTime();
		return CurrYearFirst;
	}
	
	public static Date getYearLast(int year) {
		Calendar calendar=Calendar.getInstance();
		calendar.clear();
		calendar.set(Calendar.YEAR, year);
		calendar.roll(Calendar.DAY_OF_YEAR, -1);
		Date CurrYearFirst=calendar.getTime();
		return CurrYearFirst;
	}

	private Document _renderOrganizationBarChart(String queryName, String title, List<String> xAxis,String From) {
		List<Document> dataset = new ArrayList<>();
		List<Document> series = new ArrayList<>();
		ArrayList<String> legendData=new ArrayList<>();
		ArrayList<Document> result =c(From, domain).aggregate(Domain.getJQ(domain, queryName).set("anddate", "2019").array()).into(new ArrayList<>());
		double[] DoubleLists = {0,0,0,0,0,0,0,0,0,0,0,0};
		for (int i = 0; i < result.size(); i++) {
			Document data = result.get(i);
			dataset.add(data);
			String name = data.getString("_id");
			legendData.add(name);
			double[] Doubles = getDatas((List<?>) data.get("source"), xAxis, -1);
			for (int j = 0; j < Doubles.length; j++) {
				DoubleLists[j]=DoubleLists[j]+Doubles[j];
			}
			List<Double> datas = Formatter.toList(Doubles);
			series.add(Domain.getJQ(domain, "ͼ��-��Դͼ��-�ͻ�Ͷ���嵥").set("name", name).set("stack", "����").set("data", datas).doc());
		}
		List<Double> dataLists = Formatter.toList(DoubleLists);
		Document s = new Document("datasetIndex", dataset.size() - 1)
				.append("name", "�ϼ�")
				.append("xAxisIndex", 0).append("yAxisIndex", 1)
				.append("type", "line").append("data",dataLists);
		series.add(s);
		JQ jq = Domain.getJQ(domain, "ͼ��-�ͻ�-Ͷ���嵥")
				.set("title", title)
				.set("legendData", legendData)
				.set("xAxisData", xAxis)
				.set("yAxis", "����")
				.set("yAxis2", "�ϼ�")
				.set("series",series);
		return jq.doc();
	}
	
	private double [] getDatas(List<?> list, List<String> xAxis, int direction) {
		Map<String, Double> map = new HashMap<String, Double>();
		list.forEach(o ->map.put(((Document) o).getString("_id"), 
				Double.parseDouble(((Document) o).getInteger("value").toString())));
		Calendar cal = Calendar.getInstance();
		double [] cValue = new double[12];
		for (int i = 0; i < xAxis.size(); i++) {
			try {
				cal.setTime(new SimpleDateFormat("yyyy-MM").parse(xAxis.get(i)));
				cal.add(Calendar.MONTH, -1);
				double pValue = Optional.ofNullable(map.get(xAxis.get(i))).orElse(0d);
				cValue[i]=pValue;
			} catch (ParseException e) {
			}
		}
			
		return cValue;
	}
	
	public Document _createResclassifyProblemsChart(Document condition,String domain) {
		this.domain=domain;
		String queryName = "��ѯ-���������-�ͻ�Ͷ�߲�������-��Դ";
		String title = "Ͷ�߲�������ϼ�";
		return _renderClassifyProblemsBarChart(queryName, title,"classifyProblem","2019-01-24T00:00:00.000Z");
	}
	
	private Document _renderClassifyProblemsBarChart(String queryName, String title,String From,String anddate) {
		List<Document> dataset = new ArrayList<>();
		List<Document> series = new ArrayList<>();
		List<String> xAxis=new ArrayList<>();
		ArrayList<Document> result =c(From, domain).aggregate(Domain.getJQ(domain, queryName).set("anddate", anddate).array()).into(new ArrayList<>());
		double DoubleListsSum=0;
		double[] DoubleLists = {0,0,0,0,0,0,0};
		for (int i = 0; i < result.size(); i++) {
			Document data = result.get(i);
			dataset.add(data); 
			String name = data.getString("name"); 
			xAxis.add(name);
			DoubleLists[i]=data.getInteger("count");
			DoubleListsSum+=data.getInteger("count");
		}
		List<Double> dataLists = Formatter.toList(DoubleLists);
		Document s = new Document()
				.append("name", "����") 
				.append("type", "bar").append("data",dataLists);
		series.add(s);
		for (int i = 0; i < DoubleLists.length; i++) {
			 if(i==DoubleLists.length-1){
				DoubleLists[i]=100;
			}else if(i!=0) {
				DoubleLists[i]= (double) ((Math.round(((DoubleLists[i]/DoubleListsSum*100)+DoubleLists[i-1]) * 100)) / 100.0);
			}else {
				DoubleLists[i]= (double) ((Math.round(((DoubleLists[i]/DoubleListsSum*100)) * 100)) / 100.0);
			}
		}
		dataLists = Formatter.toList(DoubleLists);
		s = new Document("datasetIndex", dataset.size() - 1)
				.append("name", "����")
				.append("xAxisIndex", 0).append("yAxisIndex", 1)
				.append("type", "line").append("data",dataLists);
		series.add(s);
		return Domain.getJQ(domain, "ͼ��-ͨ��-�����ݼ�-���ղ���-2y��")
				.set("����", title)
				.set("���ݼ�", dataset)
				.set("x������", xAxis)
				.set("y1������", "����")
				.set("y2������", "����ռ��")
				.set("ϵ��", series)
				.doc();
	}
	
	private Document _renderOrganizationDeptsChart(String queryName, String title,String From,String anddate) {
		List<Document> dataset = new ArrayList<>();
		List<Document> series = new ArrayList<>();
		List<String> xAxis=new ArrayList<>();
		ArrayList<Document> result =c(From, domain).aggregate(Domain.getJQ(domain, queryName)
				.set("anddate", anddate).array()).into(new ArrayList<>());
		double[] DoubleLists = {0,0,0,0,0,0,0};
		for (int i = 0; i < result.size(); i++) {
			Document data = result.get(i);
			dataset.add(data); 
			String name = data.getString("name"); 
			xAxis.add(name);
			DoubleLists[i]=data.getInteger("count");
		}
		List<Double> dataLists = Formatter.toList(DoubleLists);
		Document s = new Document().append("name", "����").append("type", "bar").append("data",dataLists);
		series.add(s);
		return Domain.getJQ(domain, "ͼ��-ͨ��-�����ݼ�-���ղ���-2y��")
				.set("����", title)
				.set("���ݼ�", dataset)
				.set("x������", xAxis)
				.set("y1������", "����")
				.set("y2������", "")
				.set("ϵ��", series)
				.doc();
	}
	
	public Document _createResclassifyProblemXianXinChart(Document condition,String domain) {
		this.domain=domain;
		List<String> xAxis = getXAxisOrganizationChart();
		if (xAxis.isEmpty())
			return blankChart(domain);
		String queryName = "��ѯ-���������-�ͻ�Ͷ�߲�������";
		String title = "�ͻ�Ͷ�߲�����������ͼ";
		return _renderClassifyProblemXianXinBarChart(queryName, xAxis,title,"classifyProblem");
	}
	
	private Document _renderClassifyProblemXianXinBarChart(String queryName, List<String> xAxis,String title,String From) {
		List<Document> dataset = new ArrayList<>();
		List<Document> series = new ArrayList<>();
		ArrayList<Document> result =c(From, domain).aggregate(Domain.getJQ(domain, queryName).set("anddate", "2019").array()).into(new ArrayList<>());
		for (int i = 0; i < result.size(); i++) {
			Document data = result.get(i);
			dataset.add(data);
			String name = data.getString("_id");
			List<Document> momSource =  new ArrayList<>();
			List<?> list=(List<?>)data.get("source");
			Map<String, Double> map = new HashMap<String, Double>();
			list.forEach(o ->map.put(((Document) o).getString("_id"), 
					Double.parseDouble(((Document) o).getInteger("value").toString())));
			xAxis.forEach(date -> {
					double pValue = Optional.ofNullable(map.get(date)).orElse(0d);
					momSource.add(new Document("_id", date).append("value",pValue));
			});
			if (!momSource.isEmpty()) {
				Document mData = new Document("_id", name).append("source", momSource);
				dataset.add(mData);
				Document s = new Document("datasetIndex", dataset.size() - 1)
						.append("name", name)
						.append("xAxisIndex", 0).append("yAxisIndex", 1)
						.append("encode", new Document("x", "_id").append("y", "value"))
						.append("type", "line").append("label", new Document("normal", new Document("show", false)
								.append("position", "top").append("formatter", "{@value}%")));
				series.add(s);
			}
		}
		return Domain.getJQ(domain, "ͼ��-ͨ��-�����ݼ�-���ղ���-2y��")
				.set("����", title)
				.set("���ݼ�", dataset)
				.set("x������", xAxis)
				.set("y1������", "���")
				.set("y2������", "")
				.set("ϵ��", series)
				.doc();
	}
	
	public Document _createResOrganizationDeptsChart(Document condition,String domain) {
		this.domain=domain;
		List<String> xAxis = getXAxisOrganizationChart();
		if (xAxis.isEmpty())
			return blankChart(domain);
		String queryName = "��ѯ-���������-�������ι�������";
		String title = "�������ι�������";
		return _renderOrganizationDeptsChart(queryName, title,"organization","2019");
	}

}
