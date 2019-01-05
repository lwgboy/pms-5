package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;

public class ProblemCardRenderer {

	private static final String[] priorityText = new String[] { "���", "��", "��", "��", "���" };

	private static final String[] cftRoleText = new String[] { "�鳤", "���", "����", "����", "����" };

	private static final CardTheme indigo = new CardTheme(CardTheme.INDIGO);

	private static final CardTheme deepGrey = new CardTheme(CardTheme.DEEP_GREY);

	private static final CardTheme red = new CardTheme(CardTheme.RED);

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

		RenderTools.appendCardBg(sb, "white");

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

		RenderTools.appendCardBg(sb, "white");

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD2PhotoCard(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		sb.append("<img src='" + RenderTools.getFirstFileURL(doc, "problemImg")
				+ "' style='cursor:pointer;width:100%;height:auto;border-radius:4px 4px 0px 0px;' onclick='$.getJSON(\"bvs/imgf?c=d2ProblemPhoto&i="
				+ doc.get("_id") + "&f=problemImg\", function(json){layer.photos({photos: json});});'" + "/>");

		RenderTools.appendLine(sb, doc.getString("problemImgDesc"), RenderTools.STYLE_3LINE);

		RenderTools.appendLine(sb,
				Formatter.getString(doc.getDate("receiveDate")) + "/" + doc.getString("receiver") + " " + doc.getString("location"),
				RenderTools.STYLE_1LINE);

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "ɾ��ͼƬ����", "deletephoto");

		RenderTools.appendCardBg(sb, "white");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", 240);
	}

	public static Document renderD3ICA(Document doc, String lang) {

		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String action = doc.getString("action");
		String objective = doc.getString("objective");
		String priority = priorityText[Integer.parseInt(doc.getString("priority"))];
		String planStart = Formatter.getString(doc.get("planStart"));
		String planFinish = Formatter.getString(doc.get("planFinish"));
		String budget = doc.getString("budget");
		Document chargerData = (Document) doc.get("charger_meta");

		Document verification = (Document) doc.get("verification");

		boolean finished = doc.getBoolean("finish", false);
		String status;
		CardTheme theme = indigo;
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

		sb.append("<div class='brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor + ";padding:8px;'>"
				+ "<div class='label_subhead brui_card_text'>" + action + "</div>"//
				+ "<div style='text-align:center;margin-left:8px'><div class='label_headline'>" + priority + "</div>"
				+ "<div class='label_caption'>���ȼ�</div></div>"//
				+ "</div>");//

		rowHeight += 64;

		RenderTools.appendLine(sb, "Ԥ�ڽ����", CardTheme.TEXT_LINE[0], objective, CardTheme.TEXT_LINE[1]);
		rowHeight += 24;

		RenderTools.appendLine(sb, "ִ�мƻ���", CardTheme.TEXT_LINE[0], planStart + " ~ " + planFinish, CardTheme.TEXT_LINE[1]);
		rowHeight += 24;

		RenderTools.appendLine(sb, "����Ԥ�㣺", CardTheme.TEXT_LINE[0], budget, CardTheme.TEXT_LINE[1]);
		rowHeight += 24;

		RenderTools.appendUserAndText(sb, chargerData, status);
		rowHeight += 36;

		if (!finished) {
			// ɾ����ť
			RenderTools.appendButton(sb, "layui-icon-ok", 12 + 16 + 8 + 16 + 8 + 16 + 8, 12, "���ICA�����", "finishICA");

			// �༭��ť
			RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8 + 16 + 8, 12, "�༭ICA", "editICA");

			// ��֤��ť
			RenderTools.appendButton(sb, "layui-icon-survey", 12 + 16 + 8, 12, "��֤ICA", "verificationICA");

			// ��ɰ�ť
			RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "ɾ��ICA", "deleteICA");
		}

		RenderTools.appendCardBg(sb, "white");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

	public static Document renderD4(Document doc, String title, String rootCauseDesc, Document charger_meta, Date date, String lang) {

		StringBuffer sb = new StringBuffer();

		RenderTools.appendHeader(sb, red, title, 36);

		RenderTools.appendLine(sb, rootCauseDesc, RenderTools.STLYE_NLINE);

		RenderTools.appendUserAndText(sb, charger_meta, Formatter.getString(date));

		RenderTools.appendCardBg(sb, "white");

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD4CauseConsequence(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String name = doc.getString("name");
		String desc = Optional.ofNullable(doc.getString("description")).orElse("");
		int w = doc.getInteger("weight", 1);
		String p = Formatter.getPercentageFormatString(doc.getDouble("probability"));

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
				+ "</div><div class='brui_card_text3 label_caption' style='height:48px;'>" + desc + "</div>");
		sb.append("</div>");

		rowHeight += 82;

		sb.insert(0, "<div class='brui_card_trans' style='display:flex;background:#f9f9f9;height:" + (rowHeight - 2 * RenderTools.margin)
				+ "px;margin:" + RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

	public static Document renderD5PCA(List<?> list, String title, Document charger, Date date, String lang) {
		StringBuffer sb = new StringBuffer();
		RenderTools.appendHeader(sb, indigo, title, 36);

		sb.append("<div class='layui-text'>");
		sb.append("<ul style='margin-left:12px;padding:8px 8px 0px 16px;'>");
		for (int i = 0; i < list.size(); i++) {
			sb.append("<li class='label_caption' style='margin-top:0px;'>" + ((Document) list.get(i)).getString("name") + "</li>");
		}
		sb.append("</ul>");
		sb.append("</div>");

		RenderTools.appendUserAndText(sb, charger, Formatter.getString(date));

		RenderTools.appendCardBg(sb, "white");
		return new Document("html", sb.toString());
	}

	public static Document renderD6IVPCA(Document t, String lang) {

		StringBuffer sb = new StringBuffer();

		String title;
		if ("make".equals(t.getString("actionType"))) {
			title = "ʵʩ/ȷ����������ľ�����ʩ";
		} else {
			title = "ʵʩ/ȷ�����������ľ�����ʩ";
		}
		RenderTools.appendHeader(sb, indigo, title, 36);

		RenderTools.appendLine(sb, t.getString("iv"), RenderTools.STLYE_NLINE);

		List<?> list = (List<?>) t.get("attachments");
		if (Check.isAssigned(list)) {
			RenderTools.appendLine(sb, "����", RenderTools.STYLE_1LINE);
			RenderTools.appendMultiFiles(sb, list);
		}

		Date date = t.getDate("date");
		boolean closed = t.getBoolean("closed", false);
		String status = closed ? ("<span class='layui-badge  layui-bg-blue'>" + "�ѹر�" + "</span>")
				: ("<span class='layui-badge'>" + "δ�ر�" + "</span>");
		RenderTools.appendUserAndText(sb, (Document) t.get("charger_meta"), Formatter.getString(date) + " " + status);

		if (!closed) {
			RenderTools.appendButton(sb, "layui-icon-ok", 12 + 16 + 8 + 16 + 8, 12, "�ر�PCA", "closePCA");

			RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8, 12, "�༭PCAʵʩ����֤��¼", "editPCA");

			RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "ɾ��PCAʵʩ��ȷ�ϼ�¼", "deletePCA");
		}

		RenderTools.appendCardBg(sb, "white");
		return new Document("html", sb.toString());
	}

	public static Document renderD7Similar(Document t, String lang) {
		// TODO Auto-generated method stub
		return t;
	}

	public static Document renderD7SPA(Document t, String lang) {
		// TODO Auto-generated method stub
		return t;
	}

}
