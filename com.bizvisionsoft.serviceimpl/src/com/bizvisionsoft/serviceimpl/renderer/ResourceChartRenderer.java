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

		// ����ʱ�����ͺ���ֹʱ�䣬����group��_id��xAxis����
		initializeXAxis();

		initializeData();

		// ����ϵ�����͹���series
		if ("����".equals(seriesType)) {
			input.forEach(d -> {
				appendSeries(d.getString("label"));
			});
		} else {
			appendSeries("");

		}
		// �����ۼ�ֵ���͹���series
		if ("�ܼ�".equals(aggregateType)) {
			if (dataType.contains("�ƻ�")) {
				appendSummary("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼƼƻ���ʱ", "�ۼƼƻ���ʱ", totalPlanAggTime, BruiColor.Light_Blue);
				appendSummary("ͼ��-��Դͼ��-�ۼƽ��", "�ۼƼƻ����", "�ۼƼƻ����", totalPlanAggAmount, BruiColor.Light_Blue);
			}
			if (dataType.contains("ʵ��")) {
				appendSummary("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼ�ʵ�ʹ�ʱ", "�ۼ�ʵ�ʹ�ʱ", totalActualAggTime, BruiColor.Teal);
				appendSummary("ͼ��-��Դͼ��-�ۼƽ��", "�ۼ�ʵ�ʽ��", "�ۼ�ʵ�ʽ��", totalActualAggAmount, BruiColor.Teal);
			}
		} else if ("��׼�Ӱ�".equals(aggregateType)) {
			if (dataType.contains("�ƻ�")) {
				appendSummary("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼƼƻ���׼��ʱ", "�ۼƼƻ���ʱ", basicPlanAggTime, BruiColor.Light_Blue);
				appendSummary("ͼ��-��Դͼ��-�ۼƽ��", "�ۼƼƻ���׼���", "�ۼƼƻ����", basicPlanAggAmount, BruiColor.Light_Blue);

				appendSummary("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼƼƻ��Ӱ๤ʱ", "�ۼƼƻ���ʱ", extraPlanAggTime, BruiColor.Orange);
				appendSummary("ͼ��-��Դͼ��-�ۼƽ��", "�ۼƼƻ��Ӱ���", "�ۼƼƻ����", extraPlanAggAmount, BruiColor.Orange);
			}
			if (dataType.contains("ʵ��")) {
				appendSummary("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼ�ʵ�ʱ�׼��ʱ", "�ۼ�ʵ�ʹ�ʱ", basicActualAggTime, BruiColor.Teal);
				appendSummary("ͼ��-��Դͼ��-�ۼƽ��", "�ۼ�ʵ�ʱ�׼���", "�ۼ�ʵ�ʽ��", basicActualAggAmount, BruiColor.Teal);

				appendSummary("ͼ��-��Դͼ��-�ۼƹ�ʱ", "�ۼ�ʵ�ʼӰ๤ʱ", "�ۼ�ʵ�ʹ�ʱ", extraActualAggTime, BruiColor.Red);
				appendSummary("ͼ��-��Դͼ��-�ۼƽ��", "�ۼ�ʵ�ʼӰ���", "�ۼ�ʵ�ʽ��", extraActualAggAmount, BruiColor.Red);
			}
		}

		// ������Դͼ��
		JQ jq = new JQ("ͼ��-��Դͼ��").set("title", "").set("legendData", legendData).set("xAxisData", xAxisData).set("series", series);
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
		if ("��".equals(dateType)) {
			group_id.append("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$id"));
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
			// TODO ��Ҫ������ѡ��Դ����Դ������������
			// markLineData.append("data", "");
		} else if ("��".equals(dateType)) {
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
	 * ����ۼ�ϵ��
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
	 * ��ӽڵ�ϵ��
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
		String timeChart = "ͼ��-��Դͼ��-��ʱ";
		String amountChart = "ͼ��-��Դͼ��-���";
		if (dataType.contains("�ƻ�")) {
			// ��ȡ�ƻ���Դ����
			ResourceData r = query("resourcePlan", "$planBasicQty", "$planOverTimeQty");
			if ("��׼".equals(showData)) {
				appendCatalog(timeChart, label + "�ƻ���׼��ʱ", label + "�ƻ���ʱ", r.basicTime());
				appendCatalog(amountChart, label + "�ƻ���׼���", label + "�ƻ����", r.basicAmounts());

				// �����ۼ�ֵ
				summary(totalPlanAggTime, totalPlanAggAmount, basicPlanAggTime, basicPlanAggAmount, r.basicTime(), r.basicAmounts());
			} else if ("�Ӱ�".equals(showData)) {
				appendCatalog(timeChart, label + "�ƻ��Ӱ๤ʱ", label + "�ƻ���ʱ", r.extraTime());
				appendCatalog(amountChart, label + "�ƻ��Ӱ���", label + "�ƻ����", r.extraAmounts());

				// �����ۼ�ֵ
				summary(totalPlanAggTime, totalPlanAggAmount, extraPlanAggTime, extraPlanAggAmount, r.extraTime(), r.extraAmounts());
			} else if ("����".equals(showData)) {
				appendCatalog(timeChart, label + "�ƻ���ʱ", label + "�ƻ���ʱ", r.list(r.totalTime));
				appendCatalog(amountChart, label + "�ƻ����", label + "�ƻ����", r.list(r.totalAmounts));

				// �����ۼ�ֵ
				summary(totalPlanAggTime, totalPlanAggAmount, basicPlanAggTime, basicPlanAggAmount, r.basicTime(), r.basicAmounts());

				summary(totalPlanAggTime, totalPlanAggAmount, extraPlanAggTime, extraPlanAggAmount, r.extraTime(), r.extraAmounts());
			} else {
				appendCatalog(timeChart, label + "�ƻ���׼��ʱ", label + "�ƻ���ʱ", r.basicTime());
				appendCatalog(amountChart, label + "�ƻ���׼���", label + "�ƻ����", r.basicAmounts());
				appendCatalog(timeChart, label + "�ƻ��Ӱ๤ʱ", label + "�ƻ���ʱ", r.extraTime());
				appendCatalog(amountChart, label + "�ƻ��Ӱ���", label + "�ƻ����", r.extraAmounts());

				// �����ۼ�ֵ
				summary(totalPlanAggTime, totalPlanAggAmount, basicPlanAggTime, basicPlanAggAmount, r.basicTime(), r.basicAmounts());
				summary(totalPlanAggTime, totalPlanAggAmount, extraPlanAggTime, extraPlanAggAmount, r.extraTime(), r.extraAmounts());
			}
		}
		if (dataType.contains("ʵ��")) {
			// ��ȡʵ����Դ����
			ResourceData r = query("resourceActual", "$actualBasicQty", "$actualOverTimeQty");
			if ("��׼".equals(showData)) {
				appendCatalog(timeChart, label + "ʵ�ʱ�׼��ʱ", label + "ʵ�ʹ�ʱ", r.basicTime());
				appendCatalog(amountChart, label + "ʵ�ʱ�׼���", label + "ʵ�ʽ��", r.basicAmounts());

				// �����ۼ�ֵ
				summary(totalActualAggTime, totalActualAggAmount, basicActualAggTime, basicActualAggAmount, r.basicTime(),
						r.basicAmounts());
			} else if ("�Ӱ�".equals(showData)) {
				// if (markLineData.get("data") != null && r.basicWorks != null)
				// markLineData.put("data", Arrays.asList(new Document("yAxis",
				// r.basicWorks).append("name", catalogLabel + "�����")));

				appendCatalog(timeChart, label + "ʵ�ʼӰ๤ʱ", label + "ʵ�ʹ�ʱ", r.extraTime());
				appendCatalog(amountChart, label + "ʵ�ʼӰ���", label + "ʵ�ʽ��", r.extraAmounts());

				// �����ۼ�ֵ
				summary(totalActualAggTime, totalActualAggAmount, extraActualAggTime, extraActualAggAmount, r.extraTime(),
						r.extraAmounts());
			} else if ("����".equals(showData)) {
				appendCatalog(timeChart, label + "ʵ�ʹ�ʱ", label + "ʵ�ʹ�ʱ", r.totalTime());
				appendCatalog(amountChart, label + "ʵ�ʽ��", label + "ʵ�ʽ��", r.totalAmounts());

				// �����ۼ�ֵ
				summary(totalActualAggTime, totalActualAggAmount, basicActualAggTime, basicActualAggAmount, r.basicTime(),
						r.basicAmounts());

				summary(totalActualAggTime, totalActualAggAmount, extraActualAggTime, extraActualAggAmount, r.extraTime(),
						r.extraAmounts());
			} else {
				// if (markLineData.get("data") != null && r.basicWorks != null)
				// markLineData.put("data", Arrays.asList(new Document("yAxis",
				// r.basicWorks).append("name", catalogLabel + "�����")));

				appendCatalog(timeChart, label + "ʵ�ʱ�׼��ʱ", label + "ʵ�ʹ�ʱ", r.basicTime());
				appendCatalog(amountChart, label + "ʵ�ʱ�׼���", label + "ʵ�ʽ��", r.basicAmounts());
				appendCatalog(timeChart, label + "ʵ�ʼӰ๤ʱ", label + "ʵ�ʹ�ʱ", r.extraTime());
				appendCatalog(amountChart, label + "ʵ�ʼӰ���", label + "ʵ�ʽ��", r.extraAmounts());

				// �����ۼ�ֵ
				summary(totalActualAggTime, totalActualAggAmount, basicActualAggTime, basicActualAggAmount, r.basicTime(),
						r.basicAmounts());
				summary(totalActualAggTime, totalActualAggAmount, extraActualAggTime, extraActualAggAmount, r.extraTime(),
						r.extraAmounts());
			}

		}

	}

	/**
	 * �����ۼ�ֵ���ͣ������ۼ�ֵ
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
		if ("�ܼ�".equals(aggregateType)) {
			summary(totalAggWorkTimeData, totalAggAmountData, resourceWorkTimeData, resourceAmountData);
		} else if ("��׼�Ӱ�".equals(aggregateType)) {
			summary(aggWorkTimeData, aggAmountData, resourceWorkTimeData, resourceAmountData);
		}
	}

	/**
	 * ���㹤ʱ�ͽ����ۼ�ֵ
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
		// ��ѯ��ȡ����
		buildMatchCondition();
		c(col).aggregate(new JQ("��ѯ-��Դͼ��").set("$basicQty", basicQtyName).set("$overTimeQty", overTimeQtyName).set("match", match)
				.set("group_id", group_id).array()).forEach((Document d) -> resData.build(d));
		return resData;
	}

	private void buildMatchCondition() {
		// ������ʱ�乹����ѯ���
		match = new Document("id", new Document("$gte", start).append("$lte", end));
		List<ObjectId> resourceIds = new ArrayList<>();
		List<ObjectId> workIds = new ArrayList<>();

		input.forEach(doc -> {
			String type = doc.getString("type");
			if (ResourceType.class.getName().equals(type)) {
				Document meta = ((Document) doc.get("meta"));
				Object resTypeId = doc.get("_id");
				Object _id = meta.get("org_id");
				if (_id != null) {// ѡȡ����֯�¼�������
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
				pipe.addAll(new JQ("��ѯ-ͨ��-�¼�����ȡ��-������").set("from", "organization").set("startWith", "$_id").set("connectFromField", "_id")
						.set("connectToField", "parent_id").array());
				pipe.addAll(new JQ("׷��-��֯����Դ").array());
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
		if (_id != null) {// ѡȡ������Ŀ�¼�������
			pipe.add(Aggregates.match(new Document("project_id", _id)));
			pipe.addAll(new JQ("��ѯ-����-��Դ").array());
		} else {
			_id = meta.get("stage_id");
			if (_id != null) {// ѡȡ���ǽ׶��¼�������
				pipe.add(Aggregates.match(new Document("_id", _id)));
				pipe.addAll(new JQ("��ѯ-ͨ��-�¼�����ȡ��-������").set("from", "work").set("startWith", "$_id").set("connectFromField", "_id")
						.set("connectToField", "parent_id").array());
				pipe.addAll(new JQ("��ѯ-����-��Դ").array());
			}
		}
		return pipe;
	}

}
