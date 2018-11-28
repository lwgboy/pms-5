package com.bizvisionsoft.serviceimpl.renderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.bizvisionsoft.serviceimpl.renderer.ColorTheme.BruiColor;

public class BudgetNCostChartRenderer extends BasicServiceImpl {

	private static final String Chart = "ͼ��-Ԥ��ɱ�";

	private static final String AmountChart = "ͼ��-Ԥ��ɱ�-���";

	private static final String PieLeftChart = "ͼ��-Ԥ��ɱ�-LeftPie";

	private static final String PieRightChart = "ͼ��-Ԥ��ɱ�-RightPie";

	private static final String SummaryChart = "ͼ��-Ԥ��ɱ�-�ۼƽ��";

	private static final String StackCost = "�ɱ�";

	private static final String StackBudget = "Ԥ��";

	private class BudgetNCostData {
		private double[] budgets;

		private double[] costs;

		private double totalBudget;

		private double totalCost;

		public BudgetNCostData(int xAxisCount) {
			budgets = new double[xAxisCount];
			costs = new double[xAxisCount];
		}

		public void build(Document doc) {
			double budget = Optional.ofNullable(doc.getDouble("budget")).orElse(0d);
			double cost = Optional.ofNullable(doc.getDouble("cost")).orElse(0d);

			int idx = xAxisData.indexOf(((Document) doc.get("_id")).getString("id"));
			if (idx != -1) {
				// �����ܶ�
				totalBudget += budget;
				totalCost += cost;

				budgets[idx] = budget;
				costs[idx] = cost;

				// �������ֵ
				summary(idx, budget, cost);
			}
		}

	}

	private List<Document> input;
	private Date start;
	private Date end;
	private String dateType;
	private List<String> dataType;
	private String seriesType;
	private boolean aggregate;
	private boolean showPercentage;

	private double[] totalAggBudget;
	private double[] totalAggCost;

	private Map<String, BudgetNCostData> pieData;

	private ArrayList<Document> series;
	private ArrayList<String> legendData;
	private Document group;
	private Document match;
	private ArrayList<String> xAxisData;
	private String rightTitle;
	private String leftTitle;
	private String gridTop;
	private List<Document> yAxis;

	@SuppressWarnings("unchecked")
	public BudgetNCostChartRenderer(Document condition) {
		checkResChartOption(condition);
		Document option = (Document) condition.get("option");
		List<?> dateRange = (List<?>) option.get("dateRange");
		start = (Date) dateRange.get(0);
		end = (Date) dateRange.get(1);
		dateType = option.getString("dateType");
		dataType = (List<String>) option.get("dataType");
		seriesType = option.getString("seriesType");
		aggregate = option.getBoolean("aggregate", false);
		showPercentage = option.getBoolean("showPercentage", false);
		input = ((List<Document>) condition.get("input"));
	}

	public Document render() {
		series = new ArrayList<Document>();
		legendData = new ArrayList<String>();

		// ����ʱ�����ͣ�����group
		initializeGroup();

		// ����ʱ�����ͺ���ֹʱ�䣬����xAxis��match
		initializeXAxisAndMatch();

		// �����ۼ����ͣ�����Y��
		initializeYAxis();

		// ����ϵ�����͹���series
		appendSeries();

		JQ jq = new JQ(Chart).set("xAxisData", xAxisData).set("rightTitle", rightTitle).set("leftTitle", leftTitle)
				.set("legendData", legendData).set("series", series).set("yAxis", yAxis).set("gridTop", gridTop);
		return jq.doc();
	}

	/**
	 * ����Series
	 */
	private void appendSeries() {
		gridTop = "60";
		Map<String, BudgetNCostData> budgetNCostData = query();
		for (String key : budgetNCostData.keySet()) {
			// �����������ͼ���bar
			BudgetNCostData b = budgetNCostData.get(key);
			if (dataType.contains(StackBudget)) {
				appendCatalog(AmountChart, key, StackBudget, b.budgets);
			}
			if (dataType.contains(StackCost)) {
				appendCatalog(AmountChart, key, StackCost, b.costs);
			}
		}

		// ����pie��data
		List<Document> pie1 = new ArrayList<Document>();
		List<Document> pie2 = new ArrayList<Document>();
		for (String key : pieData.keySet()) {
			BudgetNCostData b = pieData.get(key);
			// ����Ԥ��pie
			if (dataType.contains(StackBudget)) {
				appendPie(pie1, key, b.totalBudget);
			}
			// ���ɳɱ�pie
			if (dataType.contains(StackCost)) {
				appendPie(pie2, key, b.totalCost);
			}
		}
		// �����ʾռ�Ȳ��Ҵ��ڶ��ϵ��ʱ
		if (showPercentage && input.size() > 1) {
			gridTop = "45%";
			if (pie1.size() > 0) {
				appendPie(PieLeftChart, "Ԥ��", pie1);
				leftTitle = "Ԥ��";
			}

			// �����Ҫ���سɱ���������Ƿ����Ԥ�������سɱ�pie
			if (pie2.size() > 0) {
				appendPie(PieRightChart, "�ɱ�", pie2);
				rightTitle = "�ɱ�";
			}

		}
		// ���ɻ���ֵ
		if (aggregate) {
			if (dataType.contains(StackBudget)) {
				appendSummary(SummaryChart, "Ԥ��", totalAggBudget, BruiColor.Light_Blue);
			}
			if (dataType.contains(StackCost)) {
				appendSummary(SummaryChart, "�ɱ�", totalAggCost, BruiColor.Teal);
			}
		}
	}

	/**
	 * ����Y��
	 */
	private void initializeYAxis() {
		yAxis = new ArrayList<>();

		// ���ؽ��Y��
		yAxis.add(new Document("type", "value").append("name", "����Ԫ��").append("axisLabel", new Document("formatter", "{value}")));

		if (aggregate) {
			// �����ۼƽ��Y��
			yAxis.add(new Document("type", "value").append("name", "�ۼƽ�Ԫ��").append("axisLabel", new Document("formatter", "{value}")));
		}
	}

	private void appendSummary(String seriesJQ, String postfix, double[] ds, BruiColor color) {
		List<Double> data = Formatter.toList(ds);
		Document doc = new JQ(seriesJQ).set("name", "�ۼ�" + postfix).set("color1", ColorTheme.getHtmlColor(color.getRgba(0xff)))
				.set("color2", ColorTheme.getHtmlColor(color.getRgba(0x66))).set("color3", ColorTheme.getHtmlColor(color.getRgba(0)))
				.set("stack", "�ۼ�").set("data", data).doc();
		series.add(doc);
		legendData.add("�ۼ�" + postfix);
	}

	private void appendPie(String chartString, String key, List<Document> ds) {
		series.add(new JQ(chartString).set("name", key).set("data", ds).doc());
	}

	private void appendPie(List<Document> pie, String key, double value) {
		pie.add(new Document("name", key).append("value", value));
		legendData.add(key);
	}

	private void appendCatalog(String chartString, String key, String stack, double[] ds) {
		List<Double> data = Formatter.toList(ds);
		series.add(new JQ(chartString).set("name", key + stack).set("stack", key + stack).set("data", data).doc());
		legendData.add(key + stack);
	}

	/**
	 * ��ѯ��ȡ����
	 * 
	 * @return
	 */
	private Map<String, BudgetNCostData> query() {
		Map<String, BudgetNCostData> resDataMap = new HashMap<String, BudgetNCostData>();
		pieData = new HashMap<String, BudgetNCostData>();
		List<Object> list = new ArrayList<Object>();
		input.forEach(d -> {
			// ��������ʱ�Ĳ�ѯ���
			list.add(d.get("match"));

			// ����pie������
			Document inputMatch = new Document(match);
			inputMatch.putAll((Document) d.get("match"));
			BudgetNCostData data = query(inputMatch);
			String label = d.getString("label");
			pieData.put(label, data);

			// ���ѡ���ϵ������Ϊ����ʱ����������
			if ("����".equals(seriesType))
				resDataMap.put(label, data);
		});
		// ���ѡ���ϵ�����Ͳ�Ϊ����ʱ����������
		if (!"����".equals(seriesType)) {
			match.append("$or", list);
			resDataMap.put("", query(match));
		}
		return resDataMap;
	}

	private BudgetNCostData query(Document inputMatch) {
		BudgetNCostData budgetNCostData = new BudgetNCostData(xAxisData.size());
		List<Bson> pipeline = new JQ("��ͼ-Ԥ��ͳɱ�").array();
		pipeline.add(new Document("$match", inputMatch));
		pipeline.add(new Document("$group", group));
		c("accountItem").aggregate(pipeline).forEach((Document d) -> {
			budgetNCostData.build(d);
		});
		return budgetNCostData;
	}

	/**
	 * ����x��ͻ�����ѯ����
	 */
	private void initializeXAxisAndMatch() {
		match = new Document();
		xAxisData = new ArrayList<String>();
		List<Double> agg = new ArrayList<Double>();
		// ���ڲ�ѯ��Χ
		List<String> ml = new ArrayList<String>();

		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		SimpleDateFormat sdf;
		if ("��".equals(dateType)) {
			sdf = new SimpleDateFormat("yyyyMM");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				ml.add(sdf.format(cal.getTime()));
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

			sdf = new SimpleDateFormat("yyyyMM");
			cal.setTime(start);
			while (!cal.getTime().after(end)) {
				ml.add(sdf.format(cal.getTime()));
				cal.add(Calendar.MONTH, 1);
			}
		}
		match.append("id", new Document("$in", ml));

		totalAggBudget = Formatter.toArray(agg);
		totalAggCost = Formatter.toArray(agg);
	}

	/**
	 * ��������
	 */
	private void initializeGroup() {
		Document groupId = new Document();
		if ("��".equals(dateType)) {
			groupId.append("id", new Document("$substr", Arrays.asList("$id", 0, 6)));
		} else {
			groupId.append("id", new Document("$substr", Arrays.asList("$id", 0, 4)));
		}

		group = new Document("_id", groupId).append("budget", new Document("$sum", "$budget")).append("cost",
				new Document("$sum", "$cost"));
	}

	/**
	 * ���ͼ���������׳�����
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
						throw new ServiceException("���ڷ�Χ���ݲ��Ϸ�");
					}
				} else {
					throw new ServiceException("���ڷ�Χ���ݲ��Ϸ�");
				}
			} else {
				throw new ServiceException("���ڷ�Χ���ʹ���");
			}
		} else {
			throw new ServiceException("ѡ�����ʹ���");
		}
	}

	/**
	 * �������ֵ
	 * 
	 * @param idx
	 *            ��ǰֵ�ڻ��������е�index
	 * @param budget
	 *            ��Ҫ��ӵ�����ֵ�е�Ԥ��
	 * @param cost
	 *            ��Ҫ��ӵ�����ֵ�еĳɱ�
	 */
	private void summary(int idx, double budget, double cost) {
		for (int i = idx; i < xAxisData.size(); i++) {
			totalAggBudget[i] += budget;
			totalAggCost[i] += cost;
		}
	}

}
