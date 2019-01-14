package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;
import com.bizvisionsoft.serviceimpl.query.JQ;

public class ProblemCardRenderer {

	public static final String[] priorityText = new String[] { "���", "��", "��", "��", "���" };

	public static final String[] similarDegreeText = new String[] { "��ͬ", "����", "����", "��ͬ" };

	public static final String[] cftRoleText = new String[] { "�鳤", "���", "����", "����", "����" };

	private static final CardTheme indigo = new CardTheme(CardTheme.INDIGO);

	private static final CardTheme deepGrey = new CardTheme(CardTheme.DEEP_GREY);

	private static final CardTheme red = new CardTheme(CardTheme.RED);

	public static Document renderProblem(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		// �������顿//
		appendProblemCommonInfo(doc, sb);

		// ����ͬ״̬�����ݿ顿
		if ("�����".equals(doc.get("status"))) {
			appendProblemScheduleBar(doc, sb);
		} else if ("�Ѵ���".equals(doc.get("status"))) {
		} else if ("�ѹر�".equals(doc.get("status"))) {
			appendProblemCostInfo(doc, sb);
		} else if ("��ȡ��".equals(doc.get("status"))) {
		}

		// ��ָ���
		Document chart = createProblemInstuctors(doc);
		if (chart != null)
			sb.append("<div name='" + doc.get("_id") + "' style='width:100%;height:120px;'></div>");
		else
			sb.append("<div style='width:100%;height:24px;'></div>");// ռλ

		// ����ť�顿
		appendProblemButtons(doc, sb);

		// ����Ƭ������
		RenderTools.appendCardBg(sb);

		Document result = new Document("_id", doc.get("_id")).append("html", sb.toString());
		if (chart != null)
			result.append("charts", Arrays.asList(chart));
		return result;
	}

	private static void appendProblemCostInfo(Document doc, StringBuffer sb) {
		Optional.ofNullable((Document) doc.get("cost")).map(c -> c.getDouble("summary")).map(c -> Formatter.getString(c, "��#,###.00"))
				.ifPresent(s -> RenderTools.appendLabelAndTextLine(sb, "��ʧ��", s));
	}

	private static void appendProblemCommonInfo(Document doc, StringBuffer sb) {
		RenderTools.appendHeader(sb, indigo, doc.getString("name"), 36);

		// ������Ƭ
		// Document photoDoc = Optional.ofNullable(doc.get("d2ProblemPhoto"))
		// .map(d -> ((List<?>) d).isEmpty() ? null : (Document) ((List<?>)
		// d).get(((List<?>) d).size() - 1)).orElse(null);
		// if (photoDoc != null) {
		// sb.append("<img src='" + RenderTools.getFirstFileURL(photoDoc, "problemImg")
		// + "'
		// style='cursor:pointer;width:100%;height:auto;'onclick='$.getJSON(\"bvs/imgf?c=d2ProblemPhoto&i="
		// + photoDoc.get("_id") + "&f=problemImg\",function(json){layer.photos({photos:
		// json});});'" + "/>");
		// }

		RenderTools.appendLabelAndTextLine(sb, "���",
				Optional.ofNullable((Document) doc.get("classifyProblem")).map(d -> d.getString("path")).orElse(""));

		Check.isAssigned(doc.getString("custInfo"), s -> RenderTools.appendLabelAndTextLine(sb, "�ͻ���", s));
		Check.isAssigned(doc.getString("partName"), s -> RenderTools.appendLabelAndTextLine(sb, "�����", s));

		String pnum = Check.isAssignedThen(doc.getString("partNum"), s -> "S/N:" + s).orElse("");
		String prev = Check.isAssignedThen(doc.getString("partVer"), s -> " Rev:" + s).orElse("");
		String lotNum = Check.isAssignedThen(doc.getString("lotNum"), s -> s).orElse("");
		if (Check.isAssigned(pnum, prev, lotNum))
			RenderTools.appendLabelAndTextLine(sb, "���Σ�", pnum + prev + " Lot:" + lotNum);

		String by = Optional.ofNullable(doc.getString("issueBy")).orElse("");
		String on = Optional.ofNullable(doc.getDate("issueDate")).map(Formatter::getString).orElse("");
		if (Check.isAssigned(by, on))
			RenderTools.appendLabelAndTextLine(sb, "����", on + " " + by);

		// RenderTools.appendLabelAndTextLine(sb, "��Դ��",
		// doc.getString("initiatedFrom"));
	}

	private static Document createProblemInstuctors(Document doc) {
		Object _id = doc.get("_id");
		/////////////////////////////////////////////////////////////////
		// ָ��
		// 1. ����PCI(����ؼ�ָ��)
		int severityInd = Optional.ofNullable((Document) doc.get("severityInd")).map(d -> d.getInteger("index")).orElse(0);
		// int detectionInd = Optional.ofNullable((Document)
		// doc.get("detectionInd")).map(d -> d.getInteger("index")).orElse(0);
		// int freqInd = Optional.ofNullable((Document) doc.get("freqInd")).map(d ->
		// d.getInteger("index")).orElse(0);
		// 2. incidenceIndӰ��̶�
		int incidenceInd = Optional.ofNullable((Document) doc.get("incidenceInd")).map(d -> d.getInteger("index")).orElse(0);
		// 3. lostInd��ʧ�̶�
		int lostInd = Optional.ofNullable((Document) doc.get("lostInd")).map(d -> d.getInteger("index")).orElse(0);
		// 4. urgencyInd�����̶�
		// int urgencyInd = Optional.ofNullable((Document) doc.get("urgencyInd")).map(d
		// -> d.getInteger("index")).orElse(0);
		if (severityInd + incidenceInd + lostInd > 0) {
			Document chart = new Document();
			chart.append("renderTo", "" + _id);
			chart.append("option", new JQ("ͼ��-ͨ��-С���������Ǳ�").set("name1", "���ض�").set("value1", severityInd).set("name2", "Ӱ��̶�")
					.set("value2", incidenceInd).set("name3", "��ʧ�̶�").set("value3", lostInd).doc());
			return chart;
		}
		return null;
	}

	private static void appendProblemButtons(Document doc, StringBuffer sb) {
		// ��ӡ���ť��
		if ("�����".equals(doc.get("status"))) {
			RenderTools.appendButton(sb, "layui-icon-right", 12, 12, "������T.O.P.S.��ҳ", "open8D");
		} else if ("�Ѵ���".equals(doc.get("status"))) {
			RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8, 12, "�༭����ָ��", "editProblem");
			RenderTools.appendButton(sb, "layui-icon-right", 12, 12, "��������������", "kickoff");
		} else if ("�ѹر�".equals(doc.get("status"))) {
			RenderTools.appendButton(sb, "layui-icon-right", 12, 12, "������T.O.P.S.��ҳ", "open8D");
		} else if ("��ȡ��".equals(doc.get("status"))) {
			// TODO �鿴���ⶨ�壿
		}
	}

	private static void appendProblemScheduleBar(Document doc, StringBuffer sb) {
		/////////////////////////////////////////////////////////////////
		// ������
		// ��״̬�ֶΡ�icaConfirmed, pcaApproved,pcaValidated,pcaConfirmed
		String[] msgs = new String[] { "��ȷ����ʱ���ƴ�ʩ��Ч��", //
				"����׼���þ�����ʩ�ķ�����ʼִ�С�", //
				"ͨ�����ڼ�����þ�����ʩ�ܹ�������Ч��", //
				"ͨ��ʵʩ����֤�����þ�����ʩ�ܹ�������⣬�ﵽԤ��Ŀ�ꡣ" //
		};
		String[] titles = new String[] { "ICAȷ��", "PCA��׼", "PCA��֤", "PCAȷ��" };
		String[] fields = new String[] { "icaConfirmed", "pcaApproved", "pcaValidated", "pcaConfirmed" };

		String label = "";
		int max = 0;
		for (int i = 0; i < fields.length; i++) {
			if ((Document) doc.get(fields[i]) != null) {
				max = Math.max(max, i);
			}
			label += MetaInfoWarpper.warpper(titles[i], msgs[i]);
		}

		sb.append("<div class='layui-progress layui-progress' style='margin:8px 8px 0px 8px;'>");
		sb.append("<div class='layui-progress-bar' style='width:" + 100 * (max + 1) / (1 + fields.length) + "%'></div>");
		sb.append("</div>");
		sb.append("<div class='label_caption brui_ly_hline brui_line_padding' style='color:#" + indigo.lightText + ";padding-top:0px;'>");
		sb.append(label);
		sb.append("</div>");

		// ������Ӧ�ԡ�eraStarted,eraStopped
		Document eraStarted = (Document) doc.get("eraStarted");
		Document eraStopped = (Document) doc.get("eraStopped");
		if (eraStarted != null) {// ����Ӧ��������
			String text, msg;
			if (eraStopped != null) {
				msg = eraStopped.getString("userName") + Formatter.getString(eraStopped.getDate("date")) + "<br>����ֹ����Ӧ�Դ�ʩ";
				text = "<span class='layui-badge'>" + "ERA ����ֹ" + "</span>";
			} else {
				msg = eraStarted.getString("userName") + Formatter.getString(eraStarted.getDate("date")) + "<br>����������Ӧ�Դ�ʩ";
				text = "<span class='layui-badge layui-bg-blue'>" + "ERA ������" + "</span>";
			}
			text = MetaInfoWarpper.warpper(text, msg);
			RenderTools.appendText(sb, text, RenderTools.STYLE_1LINE);
		}
	}

	public static Document renderD0ERA(Document doc, String lang) {
		StringBuffer sb = renderAction(doc, red, null);
		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD1CFTMember(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		// ͷ��
		String name = doc.getString("name");
		String role = cftRoleText[Integer.parseInt(doc.getString("role"))];
		String mobile = Optional.ofNullable(doc.getString("mobile")).map(e -> "<a href='tel:" + e + "'>" + e + "</a>").orElse("");
		String position = Optional.ofNullable(doc.getString("position")).orElse("");
		String email = Optional.ofNullable(doc.getString("email")).map(e -> "<a href='mailto:" + e + "'>" + e + "</a>").orElse("");
		String dept = Optional.ofNullable(doc.getString("dept")).orElse("");
		String img = RenderTools.getUserHeadPicURL(doc, name);

		RenderTools.appendHeader(sb, indigo, "<div class='label_subhead'><div>" + role + "</div><div class='label_body1'>" + name + " "
				+ dept + " " + position + "</div></div>" + img, 48);

		RenderTools.appendIconTextLine(sb, RenderTools.IMG_URL_TEL, 16, mobile);

		RenderTools.appendIconTextLine(sb, RenderTools.IMG_URL_EMAIL, 16, email);

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "ɾ���Ŷӳ�Ա", "delete");

		RenderTools.appendCardBg(sb);

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD25W2H(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		RenderTools.appendHeader(sb, red, "5W2H", 36);

		RenderTools.appendLabelAndMultiLine(sb, "What / ��ǰ״��", "deep_orange", Optional.ofNullable(doc.getString("what")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "When / ����ʱ��", "deep_orange", Optional.ofNullable(doc.getString("when")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "Where / �ص��λ��", "deep_orange", Optional.ofNullable(doc.getString("where")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "Who / �й���Ա", "deep_orange", Optional.ofNullable(doc.getString("who")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "Why / ԭ���Ʋ�", "deep_orange", Optional.ofNullable(doc.getString("why")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "How / �������ֵ�����", "deep_orange", Optional.ofNullable(doc.getString("how")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "How many / Ƶ�ȣ�����", "deep_orange", Optional.ofNullable(doc.getString("howmany")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendButton(sb, "layui-icon-edit", 12, 12, "�༭��������", "editpd");

		RenderTools.appendCardBg(sb);

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD2PhotoCard(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		sb.append("<img src='" + RenderTools.getFirstFileURL(doc, "problemImg")
				+ "' style='cursor:pointer;width:100%;height:auto;border-radius:4px 4px 0px 0px;' onclick='$.getJSON(\"bvs/imgf?c=d2ProblemPhoto&i="
				+ doc.get("_id") + "&f=problemImg\", function(json){layer.photos({photos: json});});'" + "/>");

		RenderTools.appendText(sb, doc.getString("problemImgDesc"), RenderTools.STYLE_3LINE);

		RenderTools.appendText(sb,
				Formatter.getString(doc.getDate("receiveDate")) + "/" + doc.getString("receiver") + " " + doc.getString("location"),
				RenderTools.STYLE_1LINE);

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "ɾ��ͼƬ����", "deletephoto");

		RenderTools.appendCardBg(sb);

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", 240);
	}

	public static Document renderD3ICA(Document doc, String lang) {
		StringBuffer sb = renderAction(doc, indigo, null);
		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	private static StringBuffer renderAction(Document doc, CardTheme theme, String head) {
		boolean finished = doc.getBoolean("finish", false);
		String action = doc.getString("action");
		String objective = doc.getString("objective");
		String priority = priorityText[Integer.parseInt(doc.getString("priority"))];
		Date planStart = doc.getDate("planStart");
		Date planFinish = doc.getDate("planFinish");
		Date actualStart = doc.getDate("actualStart");
		Date actualFinish = doc.getDate("actualFinish");
		Document chargerData = (Document) doc.get("charger_meta");
		Document verification = (Document) doc.get("verification");

		String status;
		if (finished) {
			status = "<span class='layui-badge  layui-bg-green'>" + "�����" + "</span>";
			theme = deepGrey;
		} else if (verification != null) {
			String style;
			status = verification.getString("title");
			if ("����֤".equals(status)) {
				style = "layui-badge  layui-bg-green";
				// theme = new CardTheme(CardTheme.TEAL);
			} else {
				style = "layui-badge";
				// theme = new CardTheme(CardTheme.RED);
			}
			String title = verification.getString("title");
			String comment = verification.getString("comment");
			String _date = Formatter.getString(verification.getDate("date"));
			Document user_meta = (Document) verification.get("user_meta");
			String name = user_meta.getString("name");
			String verifyInfo = comment + "<br>" + name + " " + _date;

			status = "<span class='" + style + "' style='cursor:pointer;' onclick='layer.alert(\"" + verifyInfo
					+ "\", {\"skin\": \"layui-layer-lan\",title:\"" + title + "\"})'>" + status + "</span>";
		} else {
			status = "<span class='layui-badge  layui-bg-blue'>" + "�Ѵ���" + "</span>";
		}

		StringBuffer sb = new StringBuffer();
		if (head == null) {
			sb.append("<div class='brui_card_head' style='height:48px;background:#" + theme.headBgColor + ";color:#" + theme.headFgColor
					+ ";padding:8px;'>" + "<div class='label_subhead brui_card_text'>" + action + "</div>"//
					+ "<div style='text-align:center;margin-left:8px'><div class='label_title'>" + priority + "</div>"
					+ "<div class='label_caption'>���ȼ�</div></div>"//
					+ "</div>");//
		} else {
			sb.append("<div class='brui_card_head' style='height:48px;background:#" + theme.headBgColor + ";color:#" + theme.headFgColor
					+ ";padding:8px;'>" + "<div class='label_subhead brui_card_text'>" + head + "</div>"//
					+ "<div style='text-align:center;margin-left:8px'><div class='label_title'>" + priority + "</div>"
					+ "<div class='label_caption'>���ȼ�</div></div>"//
					+ "</div>");//
			RenderTools.appendText(sb, action, RenderTools.STYLE_NLINE);
		}

		RenderTools.appendLabelAndTextLine(sb, "Ԥ�ڽ����", objective, 0);

		RenderTools.appendSchedule(sb, planStart, planFinish, actualStart, actualFinish);

		RenderTools.appendUserAndText(sb, chargerData, status);

		if (!doc.getBoolean("finish", false)) {
			RenderTools.appendButton(sb, "layui-icon-ok", 12 + 16 + 8 + 16 + 8 + 16 + 8, 12, "���", "finish");

			RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8 + 16 + 8, 12, "�༭", "edit");

			RenderTools.appendButton(sb, "layui-icon-survey", 12 + 16 + 8, 12, "��֤", "verify");

			RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "ɾ��", "delete");
		} else {
			RenderTools.appendButton(sb, "layui-icon-more", 12, 12, "��ϸ", "read");
		}

		RenderTools.appendCardBg(sb);
		return sb;
	}

	public static Document renderD4(Document doc, String title, String rootCauseDesc, Document charger_meta, Date date, String lang) {

		StringBuffer sb = new StringBuffer();

		RenderTools.appendHeader(sb, red, title, 36);

		RenderTools.appendText(sb, rootCauseDesc, RenderTools.STYLE_NLINE);

		RenderTools.appendUserAndText(sb, charger_meta, Formatter.getString(date));

		RenderTools.appendCardBg(sb);

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD4CauseConsequence(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();
		int rowHeight = 8 * 3;

		String name = doc.getString("name");
		String desc = Optional.ofNullable(doc.getString("description")).orElse("");
		int w = doc.getInteger("weight", 1);
		String p = Formatter.getPercentageFormatString(doc.getDouble("probability") / 100);

		sb.append("<div class='label_caption' style='display:flex;flex-direction:column;justify-content:space-around;color:white;'>");
		sb.append(
				"<div style='justify-content:center;flex-grow:1;width:44px;display:flex;flex-direction:column;align-items:center;background:#5c6bc0;margin-bottom:1px;border-radius:4px 0px 0px 0px;'>");
		sb.append("<div>" + w + "</div><div>Ȩ��</div></div>");
		sb.append(
				"<div style='justify-content:center;flex-grow:1;width:44px;display:flex;flex-direction:column;align-items:center;background:#5c6bc0;;border-radius:0px 0px 0px 4px;'>");
		sb.append("<div>" + p + "</div><div>����</div></div>");
		sb.append("</div>");

		sb.append("<div style='width:0;flex-grow:1;padding:0px 4px;display:flex;flex-direction:column;justify-content:space-around;'>");
		sb.append("<div class='brui_text_line label_caption'>" + name
				+ "</div><div class='brui_card_text label_caption' style='height:32px;'>" + desc + "</div>");

		Optional.ofNullable((Document) doc.get("classifyCause")).map(c -> c.getString("path")).ifPresent(path -> {
			sb.append("<div class='brui_text_line label_caption'>");
			Arrays.asList(path.split("/"))
					.forEach(em -> sb.append("<span class='layui-bg-green layui-badge' style='margin-right:4px;'>" + em + "</span>"));
			sb.append("</div>");
		});
		sb.append("</div>");

		rowHeight += 82;

		sb.insert(0,
				"<div class='brui_card_trans' style='display:flex;background:#f9f9f9;height:" + (rowHeight - 2 * 8) + "px;margin:8px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

	public static Document renderD5PCA(List<?> list, String title, Document charger, Date planStart, Date planFinish, String lang) {
		StringBuffer sb = new StringBuffer();
		renderListItemsCard(sb, list, title, charger, planStart, planFinish, null, null, indigo);
		return new Document("html", sb.toString());
	}

	private static void renderListItemsCard(StringBuffer sb, List<?> list, String title, Document charger, Date planStart, Date planFinish,
			Date actualStart, Date actualFinish, CardTheme theme) {
		RenderTools.appendHeader(sb, theme, title, 36);

		RenderTools.appendSchedule(sb, planStart, planFinish, actualStart, actualFinish);

		RenderTools.appendList(sb, list, indigo.lightText, o -> ((Document) o).getString("name"));

		RenderTools.appendUserAndText(sb, charger, null);

		RenderTools.appendCardBg(sb);
	}

	public static Document renderD6IVPCA(Document doc, String lang) {

		String head;
		if ("make".equals(doc.getString("actionType"))) {
			head = "ʵʩ/ȷ����������ľ�����ʩ";
		} else {
			head = "ʵʩ/ȷ�����������ľ�����ʩ";
		}
		StringBuffer sb = renderAction(doc, indigo, head);
		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD7Similar(Document t, String lang) {
		StringBuffer sb = new StringBuffer();

		String label = similarDegreeText[Integer.parseInt(t.getString("degree"))];

		String type = t.getString("similar");

		int prob = t.getDouble("prob").intValue();

		sb.append("<div class='brui_card_head' style='height:48px;background:#" + indigo.headBgColor + ";color:#" + indigo.headFgColor
				+ ";padding:8px;'>" + "<div class='label_subhead brui_card_text' style='flex-grow:1;'>�������Σ�" + type + "</div>"//
				+ "<div style='text-align:center;margin-left:8px'><div class='label_title'>" + label + "</div>"
				+ "<div class='label_caption'>���ƶ�</div></div>"//
				+ "<div style='text-align:center;margin-left:8px'><div class='label_title'>" + prob
				+ "<span style='font-size:9px;'>%</span></div>" + "<div class='label_caption'>������</div></div>"//
				+ "</div>");//

		RenderTools.appendText(sb, t.getString("desc"), RenderTools.STYLE_NLINE);

		List<?> ids = (List<?>) t.get("id");
		if (Check.isAssigned(ids)) {
			RenderTools.appendText(sb, "ʶ���������Σ�", RenderTools.STYLE_1LINE);

			RenderTools.appendList(sb, ids, indigo.lightText, o -> {
				Document d = (Document) o;
				return Optional.ofNullable(d.getString("id")).orElse("") + " " + Optional.ofNullable(d.getString("keyword")).orElse("");
			});

		}

		RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8, 12, "�༭������", "editSimilar");

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "ɾ��������", "deleteSimilar");

		RenderTools.appendCardBg(sb);

		return new Document("html", sb.toString()).append("_id", t.get("_id")).append("type", "similar");
	}

	public static Document renderD7SPA(Document t, String lang) {
		StringBuffer sb = new StringBuffer();
		RenderTools.appendHeader(sb, indigo, "ϵͳ��Ԥ����ʩ", 36);

		RenderTools.appendText(sb, t.getString("action"), RenderTools.STYLE_NLINE);

		Document charger = (Document) t.get("charger_meta");

		Date date = t.getDate("date");

		boolean finished = t.getBoolean("finish", false);
		String status = finished ? ("<span class='layui-badge  layui-bg-blue'>" + "�����" + "</span>")
				: ("<span class='layui-badge'>" + "δ���" + "</span>");

		RenderTools.appendUserAndText(sb, charger, Formatter.getString(date) + " " + status);

		if (!finished) {
			RenderTools.appendButton(sb, "layui-icon-ok", 12 + 16 + 8 + 16 + 8, 12, "�ر�PCA", "finishSPA");

			RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8, 12, "�༭ϵͳ��Ԥ����ʩ", "editSPA");

			RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "ɾ��ϵͳ��Ԥ����ʩ", "deleteSPA");
		} else {
			RenderTools.appendButton(sb, "layui-icon-right", 12, 12, "�鿴ϵͳ��Ԥ����ʩ", "readSPA");
		}

		RenderTools.appendCardBg(sb);

		return new Document("html", sb.toString()).append("_id", t.get("_id"));
	}

	public static Document renderD8Exp(Document t, String lang) {
		StringBuffer sb = new StringBuffer();

		String firstFileURL = RenderTools.getFirstFileURL(t, "video");
		if (firstFileURL != null) {
			sb.append("<video style='border-radius:4px 4px 0px 0px;' width='100%' height='auto' controls preload='auto'>");
			sb.append("<source src='" + firstFileURL + "' type='video/mp4'>");
			sb.append("</video>");
		} else {
			RenderTools.appendHeader(sb, indigo, "�����ѵ�ܽ�", 36);
		}

		RenderTools.appendText(sb, t.getString("name"), RenderTools.STYLE_1LINE);

		RenderTools.appendText(sb, t.getString("abstract"), RenderTools.STYLE_3LINE);

		Document charger = (Document) t.get("charger_meta");
		Date date = t.getDate("date");
		RenderTools.appendUserAndText(sb, charger, Formatter.getString(date));

		RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8, 12, "�༭�����ܽ�", "editExp");

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "ɾ�������ܽ�", "deleteExp");

		RenderTools.appendCardBg(sb);
		return new Document("html", sb.toString()).append("_id", t.get("_id"));
	}

}
