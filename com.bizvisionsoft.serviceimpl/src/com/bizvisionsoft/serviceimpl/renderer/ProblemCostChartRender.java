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

import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
import com.bizvisionsoft.serviceimpl.query.JQ;

public class ProblemCostChartRender extends BasicServiceImpl {

	private List<Document> input;

	private Date from;

	private Date to;

	private String xAxis;// %Y#%Y-%m#%Y-%m-%d#catalog

	private List<String> x0AxisData;

	private String dateFormat;

	private String title;

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

	private boolean isSelectRoot() {
		return input.size() == 1 && input.get(0).get("_id") == null;
	}

	public static Document renderClassifyCostChart(Document condition) {
		return new ProblemCostChartRender(condition).render();
	}

	private Document render() {
		if (!isSelectRoot()) {
			// ����ɸѡ����
			return renderClassifyPeriodChart();
		} else {
			return renderRootPeriodChart();
		}
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

		List<Document> series = Arrays.asList(new Document("type", "bar")//
				.append("name", "��ʧ���").append("label", new Document("normal", new Document("show", true).append("position", "top")))//
		);

		JQ chart = new JQ("ͼ��-ͨ��-�����ݼ�")//
				.set("����", title)//
				.set("���ݼ�", new Document("source", source))//
				.set("x������", "�ڼ�")//
				.set("x������", x0AxisData)//
				.set("y������", "��ʧ�������Ԫ��").set("ϵ��", series)//
				.set("���ݷ���", x0AxisData.size() > 10 ? new Document() : false)//
				.set("ͼ��", false);
		return chart.doc();
	}

	private Document renderClassifyPeriodChart() {
		List<Bson> pipe = new ArrayList<>();
		List<ObjectId> ids = input.stream().map(doc -> doc.getObjectId("_id")).collect(Collectors.toList());
		pipe.add(new Document("$match", new Document("_id", new Document("$in", ids))));
		new JQ("��ѯ-����ɱ�-��ʱ��ͳɱ�����").set("��ʼʱ��", from).set("��ֹʱ��", to).set("���ڷ����ʽ", dateFormat).appendTo(pipe);

		ArrayList<Document> dataset = c("classifyProblemLost").aggregate(pipe).into(new ArrayList<>());
		// ����ϵ�����ݼ�
		List<Document> summary = new ArrayList<>();

		ArrayList<Document> series = new ArrayList<Document>();
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
					.append("encode", new Document("x","_id").append("y", "amount"))
					.append("label", new Document("normal", new Document("show", true).append("position", "top")));
			series.add(amount);
		}

		// ׷���ܼ�ϵ��
		dataset.add(new Document("source", summary));// �ܼ�
		series.add(new Document("datasetIndex", dataset.size() - 1)//
				.append("type", "bar")//
				.append("barWidth", "20%")//
				.append("xAxisIndex", 1).append("yAxisIndex", 1)//
				.append("encode", new Document("x","_id").append("y", "amount"))
				.append("label", new Document("normal", new Document("show", true).append("position", "top"))));

		//�����ܼ�ϵ�е�x��
		
		
		// ׷�ӱ�ͼ
		series.add(new Document("datasetIndex", dataset.size() - 1)//
				.append("name", "����ռ��")//
				.append("type", "pie")//
				.append("center", Arrays.asList("80%","25%")).append("radius", Arrays.asList("20%","30%"))
				.append("label", new Document("normal",new Document("formatter","{b}\n{d}%")))
				);

		JQ jq = new JQ("ͼ��-ͨ��-��������Ŀ")//
				.set("����", title)//
				.set("���ݼ�", dataset)//
				.set("x������", "�ڼ�")//
				.set("x������", x0AxisData)//
				.set("y������", "��ʧ�������Ԫ��")//
				.set("ϵ��", series)//
				.set("���ݷ���", x0AxisData.size() > 10 ? new Document("type","slider").append("bottom", 40) : false)//
				.set("ͼ��", new Document("bottom",10).append("type", "scroll"));
		Document doc = jq.doc();
		return doc;
	}

	private void initializeXAxis() {
		x0AxisData = new ArrayList<String>();
		// markLineData = new Document();
		Calendar cal = Calendar.getInstance();
		cal.setTime(from);
		List<Double> agg = new ArrayList<Double>();
		SimpleDateFormat sdf;
		if ("date".equals(xAxis)) {
			dateFormat = "%Y-%m-%d";
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			while (!cal.getTime().after(to)) {
				x0AxisData.add(sdf.format(cal.getTime()));
				agg.add(0d);
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
			title  = Formatter.getString(from, "yyyy��MM��dd��") +"��" +Formatter.getString(to, "yyyy��MM��dd��") +"�ڼ� ������ʧ�ɱ�";
		} else if ("month".equals(xAxis)) {
			dateFormat = "%Y-%m";
			sdf = new SimpleDateFormat("yyyy-MM");
			while (!cal.getTime().after(to)) {
				x0AxisData.add(sdf.format(cal.getTime()));
				agg.add(0d);
				cal.add(Calendar.MONTH, 1);
			}
			title  = Formatter.getString(from, "yyyy��MM��") +"��" +Formatter.getString(to, "yyyy��MM��") +"�ڼ� ������ʧ�ɱ�";
		} else if ("year".equals(xAxis)) {
			dateFormat = "%Y";
			sdf = new SimpleDateFormat("yyyy");
			while (!cal.getTime().after(to)) {
				x0AxisData.add(sdf.format(cal.getTime()));
				agg.add(0d);
				cal.add(Calendar.YEAR, 1);
			}
			title  = Formatter.getString(from, "yyyy��") +"��" +Formatter.getString(to, "yyyy��") +"�ڼ� ������ʧ�ɱ�";
		}

	}

}
