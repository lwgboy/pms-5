package com.bizvisionsoft.serviceimpl.renderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.serviceimpl.query.JQ;

public class CatalogRenderer {

	/**
	 * ʱ�䷶Χ����ʼʱ��
	 */
	private Date start;

	/**
	 * ʱ�䷶Χ����ֹʱ��
	 */
	private Date end;

	/**
	 * ʱ������
	 */
	private String dateType;

	/**
	 * ϵ������
	 */
	private String seriesType;

	/**
	 * ��������
	 */
	private List<String> dataType;

	/**
	 * ��׼/�Ӱ�
	 */
	private String showData;

	/**
	 * �ۼ�ֵ����
	 */
	private String aggregateType;

	private List<Document> input;

	private List<String> xAxisData;

	private List<Document> series;

	private List<String> legendData;

	private List<Document> yAxis;

	private String[][] color = new String[][] { { "rgba(95,184,120,1)", "rgba(95,184,120,0.7)", "rgba(95,184,120,0)" },
			{ "rgba(0,150,136,1)", "rgba(0,150,136,0.7)", "rgba(0,150,136,0)" },
			{ "rgba(255,87,34,1)", "rgba(255,87,34,0.7)", "rgba(255,87,34,0)" },
			{ "rgba(255,184,0,1)", "rgba(255,184,0,0.7)", "rgba(255,184,0,0)" } };

	@SuppressWarnings("unchecked")
	public CatalogRenderer(Document option) {
		List<?> dateRange = (List<?>) option.get("dateRange");
		start = (Date) dateRange.get(0);
		end = (Date) dateRange.get(1);

		dateType = option.getString("dateType");
		showData = option.getString("showData");
		aggregateType = option.getString("aggregateType");

		seriesType = option.getString("seriesType");
		dataType = (List<String>) option.get("dataType");

	}

	public String getSeriesType() {
		return seriesType;
	}

	public List<String> getDataType() {
		return dataType;
	}

	public List<Document> getSeries() {
		return series;
	}

	public List<String> getLegendData() {
		return legendData;
	}

	public String getDBDateFormat() {
		if ("��".equals(dateType)) {
			return "%Y-%m-%d";
		} else if ("��".equals(dateType)) {
			return "%Y-%m";
		} else {
			return "%Y";
		}
	}

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}

	/**
	 * ��ȡx������
	 * 
	 * @return
	 */
	public List<String> getxAxisData() {
		if (xAxisData == null) {
			xAxisData = new ArrayList<String>();
			SimpleDateFormat sdf;
			Calendar cal = Calendar.getInstance();
			cal.setTime(start);
			// ����ʱ�����ͺ���ֹʱ�乹��X������
			if ("��".equals(dateType)) {
				sdf = new SimpleDateFormat("yyyy-MM-dd");
				while (!cal.getTime().after(end)) {
					xAxisData.add(sdf.format(cal.getTime()));
					cal.add(Calendar.DAY_OF_MONTH, 1);
				}
			} else if ("��".equals(dateType)) {
				sdf = new SimpleDateFormat("yyyy-MM");
				while (!cal.getTime().after(end)) {
					xAxisData.add(sdf.format(cal.getTime()));
					cal.add(Calendar.MONTH, 1);
				}
			} else {
				sdf = new SimpleDateFormat("yyyy");
				while (!cal.getTime().after(end)) {
					xAxisData.add(sdf.format(cal.getTime()));
					cal.add(Calendar.YEAR, 1);
				}
			}
		}
		return xAxisData;
	}

	public void setInput(List<Document> input) {
		this.input = input;
	}

	/**
	 * ����Series
	 */
	@SuppressWarnings("unchecked")
	public void loadSeries() {
		series = new ArrayList<Document>();
		legendData = new ArrayList<String>();
		yAxis = new ArrayList<Document>();

		List<Double> totalPlanAggWorkTimeData = new ArrayList<Double>();
		List<Double> totalPlanAggAmountData = new ArrayList<Double>();
		List<Double> basicPlanAggWorkTimeData = new ArrayList<Double>();
		List<Double> basicPlanAggAmountData = new ArrayList<Double>();
		List<Double> overTimePlanAggWorkTimeData = new ArrayList<Double>();
		List<Double> overTimePlanAggAmountData = new ArrayList<Double>();

		List<Double> totalActualAggWorkTimeData = new ArrayList<Double>();
		List<Double> totalActualAggAmountData = new ArrayList<Double>();
		List<Double> basicActualAggWorkTimeData = new ArrayList<Double>();
		List<Double> basicActualAggAmountData = new ArrayList<Double>();
		List<Double> overTimeActualAggWorkTimeData = new ArrayList<Double>();
		List<Double> overTimeActualAggAmountData = new ArrayList<Double>();

		// ���ع�ʱY��
		yAxis.add(new Document("type", "value").append("name", "��ʱ��Сʱ��").append("axisLabel", new Document("formatter", "{value}")));
		// ���ؽ��Y��
		yAxis.add(new Document("type", "value").append("name", "��Ԫ��").append("axisLabel", new Document("formatter", "{value}"))
				.append("gridIndex", 1));
		// ��������ѭ������Series
		input.forEach((Document doc) -> {
			// ��ȡ��ʾ����
			String label = doc.getString("label");
			if (dataType.contains("�ƻ�")) {
				// ��������Ϊ�ƻ�ʱ����ȡdoc�д洢�ļƻ�ֵ
				Object plan = doc.get("plan");
				if (plan instanceof Document) {
					// �ֱ���ؼƻ���ʱ�ͼƻ���Series
					appendWorkTimeSeries(label + "�ƻ�", (Document) plan, new Document());
					appendAmountSeries(label + "�ƻ�", (Document) plan);
					// ���ݻ������ͣ�����ƻ�����ֵ
					if ("�ܼ�".equals(aggregateType)) {
						calculationAggregateData(totalPlanAggWorkTimeData, totalPlanAggAmountData,
								(List<Double>) ((Document) plan).get("totalQty"), (List<Double>) ((Document) plan).get("totalAmount"));
					} else if ("��׼�Ӱ�".equals(aggregateType)) {
						calculationAggregateData(basicPlanAggWorkTimeData, basicPlanAggAmountData,
								(List<Double>) ((Document) plan).get("basicQty"), (List<Double>) ((Document) plan).get("basicAmount"));
						calculationAggregateData(overTimePlanAggWorkTimeData, overTimePlanAggAmountData,
								(List<Double>) ((Document) plan).get("overTimeQty"),
								(List<Double>) ((Document) plan).get("overTimeAmount"));
					}
				}
			}
			if (dataType.contains("ʵ��")) {
				// ��������Ϊʵ��ʱ����ȡdoc�д洢��ʵ��ֵ�ͱ�׼��ʱ
				Object actual = doc.get("actual");
				Object works = doc.get("works");
				if (actual instanceof Document) {
					Document markLineData = new Document();
					// ʱ������Ϊ�գ����ұ�׼��ʱ��Ϊ��ʱ��������ʱ����ߡ�
					if ("��".equals(dateType) && works != null) {
						markLineData.append("data", Arrays.asList(new Document("yAxis", works).append("name", label + "�����")));
					}

					// �ֱ����ʵ�ʹ�ʱ��ʵ�ʽ�Series
					appendWorkTimeSeries(label + "ʵ��", (Document) actual, markLineData);
					appendAmountSeries(label + "ʵ��", (Document) actual);

					// ���ݻ������ͣ�����ʵ�ʻ���ֵ
					if ("�ܼ�".equals(aggregateType)) {
						calculationAggregateData(totalActualAggWorkTimeData, totalActualAggAmountData,
								(List<Double>) ((Document) actual).get("totalQty"), (List<Double>) ((Document) actual).get("totalAmount"));
					} else if ("��׼�Ӱ�".equals(aggregateType)) {
						calculationAggregateData(basicActualAggWorkTimeData, basicActualAggAmountData,
								(List<Double>) ((Document) actual).get("basicQty"), (List<Double>) ((Document) actual).get("basicAmount"));
						calculationAggregateData(overTimeActualAggWorkTimeData, overTimeActualAggAmountData,
								(List<Double>) ((Document) actual).get("overTimeQty"),
								(List<Double>) ((Document) actual).get("overTimeAmount"));
					}
				}
			}
		});

		// �����ۼ��ھ������ۼ�ֵ
		if ("�ܼ�".equals(aggregateType)) {
			// �����ۼƹ�ʱY��
			yAxis.add(new Document("type", "value").append("name", "�ۼƹ�ʱ��Сʱ��").append("axisLabel", new Document("formatter", "{value}")));
			// �����ۼƽ��Y��
			yAxis.add(new Document("type", "value").append("name", "�ۼƽ�Ԫ��").append("axisLabel", new Document("formatter", "{value}"))
					.append("gridIndex", 1));
			// �ܼ�ʱ���ֱ�����������ͼ��ؼƻ����ۼ�ֵ��ʵ�����ۼ�ֵ
			if (dataType.contains("�ƻ�")) {
				appendAggregateWorkTimeSeries("�ƻ�", "�ƻ�", totalPlanAggWorkTimeData, color[0]);
				appendAggregateAmountSeries("�ƻ�", "�ƻ�", totalPlanAggAmountData, color[0]);
			}
			if (dataType.contains("ʵ��")) {
				appendAggregateWorkTimeSeries("ʵ��", "ʵ��", totalActualAggWorkTimeData, color[1]);
				appendAggregateAmountSeries("ʵ��", "ʵ��", totalActualAggAmountData, color[1]);
			}
		} else if ("��׼�Ӱ�".equals(aggregateType)) {
			// �����ۼƹ�ʱY��
			yAxis.add(new Document("type", "value").append("name", "�ۼƹ�ʱ��Сʱ��").append("axisLabel", new Document("formatter", "{value}")));
			// �����ۼƽ��Y��
			yAxis.add(new Document("type", "value").append("name", "�ۼƽ�Ԫ��").append("axisLabel", new Document("formatter", "{value}"))
					.append("gridIndex", 1));

			// ��׼�Ӱ�ʱ���ֱ�����������ͼ��ؼƻ���׼�ۼ�ֵ���ƻ��Ӱ��ۼ�ֵ��ʵ�ʱ�׼�ۼ�ֵ��ʵ�ʼӰ��ۼ�ֵ
			if (dataType.contains("�ƻ�")) {
				appendAggregateWorkTimeSeries("�ƻ���׼", "�ƻ�", basicPlanAggWorkTimeData, color[0]);
				appendAggregateWorkTimeSeries("�ƻ��Ӱ�", "�ƻ�", overTimePlanAggWorkTimeData, color[2]);
				appendAggregateAmountSeries("�ƻ���׼", "�ƻ�", basicPlanAggAmountData, color[0]);
				appendAggregateAmountSeries("�ƻ��Ӱ�", "�ƻ�", overTimePlanAggAmountData, color[2]);
			}
			if (dataType.contains("ʵ��")) {
				appendAggregateWorkTimeSeries("ʵ�ʱ�׼", "ʵ��", basicActualAggWorkTimeData, color[1]);
				appendAggregateWorkTimeSeries("ʵ�ʼӰ�", "ʵ��", overTimeActualAggWorkTimeData, color[3]);
				appendAggregateAmountSeries("ʵ�ʱ�׼", "ʵ��", basicActualAggAmountData, color[1]);
				appendAggregateAmountSeries("ʵ�ʼӰ�", "ʵ��", overTimeActualAggAmountData, color[3]);
			}
		}
	}

	/**
	 * �����ۼƹ�ʱ
	 * 
	 * @param name
	 *            ����
	 * @param stack
	 *            ����
	 * @param data
	 * @param color
	 */
	private void appendAggregateWorkTimeSeries(String name, String stack, List<Double> data, String[] color) {
		series.add(new JQ("ͼ��-��Դͼ��-�ۼƹ�ʱ").set("name", "�ۼ�" + name + "��ʱ").set("color1", color[0]).set("color2", color[1])
				.set("color3", color[2]).set("stack", "�ۼ�" + stack + "��ʱ").set("data", data).doc());
		legendData.add("�ۼ�" + name + "��ʱ");
	}

	/**
	 * �����ۼƽ��
	 * 
	 * @param name
	 *            ����
	 * @param stack
	 *            ����
	 * @param data
	 * @param color
	 */
	private void appendAggregateAmountSeries(String name, String stack, List<Double> data, String[] color) {
		series.add(new JQ("ͼ��-��Դͼ��-�ۼƽ��").set("name", "�ۼ�" + name + "���").set("color1", color[0]).set("color2", color[1])
				.set("color3", color[2]).set("stack", "�ۼ�" + stack + "���").set("data", data).doc());
		legendData.add("�ۼ�" + name + "���");
	}

	/**
	 * ���㹤ʱ�ͽ����ۼ�ֵ
	 * 
	 * @param aggWorkTimeDatas
	 * @param aggAmountDatas
	 * @param resourceWorkTimeData
	 * @param resourceAmountData
	 */
	private void calculationAggregateData(List<Double> aggWorkTimeDatas, List<Double> aggAmountDatas, List<Double> resourceWorkTimeData,
			List<Double> resourceAmountData) {
		Double wd = null;
		Double ad = null;
		if (aggWorkTimeDatas.size() > 0) {
			for (int i = 0; i < aggWorkTimeDatas.size(); i++) {
				Double d = resourceWorkTimeData.get(i);
				if (d != null && wd != null)
					wd += d;
				else if (d != null && wd == null)
					wd = d;

				Double aggWorkTimeData = aggWorkTimeDatas.get(i);
				if (aggWorkTimeData != null && wd != null)
					aggWorkTimeData += wd;
				else if (aggWorkTimeData == null && wd != null)
					aggWorkTimeData = wd;

				aggWorkTimeDatas.set(i, aggWorkTimeData);

				d = resourceAmountData.get(i);
				if (d != null && ad != null)
					ad += d;
				else if (d != null && ad == null)
					ad = d;

				Double aggAmountData = aggAmountDatas.get(i);
				if (aggAmountData != null && ad != null)
					aggAmountData += ad;
				else if (aggAmountData == null && ad != null)
					aggAmountData = ad;

				aggAmountDatas.set(i, aggAmountData);
			}
		} else {
			for (Double d : resourceWorkTimeData) {
				if (d != null && wd != null)
					wd += d;
				else if (d != null && wd == null)
					wd = d;
				aggWorkTimeDatas.add(wd);
			}
			for (Double d : resourceAmountData) {
				if (d != null && ad != null)
					ad += d;
				else if (d != null && ad == null)
					ad = d;
				aggAmountDatas.add(ad);
			}
		}
	}

	/**
	 * ���ݱ�׼/�Ӱ๹����ʱseries��legendData
	 * 
	 * @param label
	 *            ϵ������
	 * @param doc
	 * @param markLineData
	 *            �����
	 */
	private void appendWorkTimeSeries(String label, Document doc, Document markLineData) {
		if ("��׼".equals(showData)) {
			// ��׼ʱ�����ر�׼��ʱ��series�У�����ӱ����
			series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "��׼" + "��ʱ").set("stack", label + "��ʱ").set("data", doc.get("basicQty"))
					.set("markLineData", new Document()).doc());
			legendData.add(label + "��׼" + "��ʱ");
		} else if ("�Ӱ�".equals(showData)) {
			// �Ӱ�ʱ�����ؼӰ๤ʱ��series�У�����ӱ����
			series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "�Ӱ�" + "��ʱ").set("stack", label + "��ʱ").set("data", doc.get("overTimeQty"))
					.set("markLineData", markLineData).doc());
			legendData.add(label + "�Ӱ�" + "��ʱ");
		} else if ("����".equals(showData)) {
			// ����ʱ�������ܹ�ʱ��series�У�����ӱ����
			series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "" + "��ʱ").set("stack", label + "��ʱ").set("data", doc.get("totalQty"))
					.set("markLineData", new Document()).doc());
			legendData.add(label + "" + "��ʱ");
		} else {
			// �ֱ���ر�׼�ͼӰ๤ʱ��series�У���Ϊ�Ӱ๤ʱ��ӱ����
			series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "��׼" + "��ʱ").set("stack", label + "��ʱ").set("data", doc.get("basicQty"))
					.set("markLineData", new Document()).doc());
			legendData.add(label + "��׼" + "��ʱ");

			series.add(new JQ("ͼ��-��Դͼ��-��ʱ").set("name", label + "�Ӱ�" + "��ʱ").set("stack", label + "��ʱ").set("data", doc.get("overTimeQty"))
					.set("markLineData", markLineData).doc());
			legendData.add(label + "�Ӱ�" + "��ʱ");
		}
	}

	/**
	 * ���ݱ�׼/�Ӱ๹�����series��legendData
	 * 
	 * @param label
	 *            ϵ������
	 * @param doc
	 */
	private void appendAmountSeries(String label, Document doc) {
		if ("��׼".equals(showData)) {
			// ��׼ʱ�����ر�׼��series��
			series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "��׼" + "���").set("stack", label + "���").set("data", doc.get("basicAmount"))
					.doc());
			legendData.add(label + "��׼" + "���");
		} else if ("�Ӱ�".equals(showData)) {
			// �Ӱ�ʱ�����ؼӰ��series��
			series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "�Ӱ�" + "���").set("stack", label + "���")
					.set("data", doc.get("overTimeAmount")).doc());
			legendData.add(label + "�Ӱ�" + "���");
		} else if ("����".equals(showData)) {
			// ����ʱ�������ܽ�series��
			series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "" + "���").set("stack", label + "���").set("data", doc.get("totalAmount"))
					.doc());
			legendData.add(label + "" + "���");
		} else {
			// �ֱ���ر�׼�ͼӰ��series��
			series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "��׼" + "���").set("stack", label + "���").set("data", doc.get("basicAmount"))
					.doc());
			legendData.add(label + "��׼" + "���");

			series.add(new JQ("ͼ��-��Դͼ��-���").set("name", label + "�Ӱ�" + "���").set("stack", label + "���")
					.set("data", doc.get("overTimeAmount")).doc());
			legendData.add(label + "�Ӱ�" + "���");
		}
	}

	/**
	 * �����Ⱦ���
	 * 
	 * @param input
	 * @return
	 */
	public Document render(List<Document> input) {
		this.input = input;
		loadSeries();
		JQ jq = new JQ("ͼ��-��Դͼ��").set("title", "").set("legendData", getLegendData()).set("xAxisData", getxAxisData()).set("yAxis", yAxis)
				.set("series", getSeries());
		return jq.doc();
	}

}
