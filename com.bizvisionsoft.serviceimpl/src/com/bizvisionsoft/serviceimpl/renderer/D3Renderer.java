package com.bizvisionsoft.serviceimpl.renderer;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Formatter;

public class D3Renderer {

	public static Document renderICA(Document doc, String lang) {

		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String action = doc.getString("action");
		String objective = doc.getString("objective");
		String priority = new String[] { "���", "��", "��", "��", "���" }[Integer.parseInt(doc.getString("priority"))];
		String planStart = Formatter.getString(doc.get("planStart"));
		String planFinish = Formatter.getString(doc.get("planFinish"));
		String budget = doc.getString("budget");
		Document chargerData = (Document) doc.get("charger_meta");

		Document verification = (Document) doc.get("verification");

		CardTheme theme = new CardTheme(CardTheme.INDIGO);

		boolean finished = doc.getBoolean("finish", false);
		String status;
		if (finished) {
			status = "<span class='layui-badge  layui-bg-green'>" + "�����" + "</span>";
			theme = new CardTheme(CardTheme.DEEP_GREY);
		} else if (verification != null) {
			String style;
			status = verification.getString("title");
			if ("����֤".equals(status)) {
				style = "layui-badge  layui-bg-green";
//				theme = new CardTheme(CardTheme.TEAL);
			} else {
				style = "layui-badge";
//				theme = new CardTheme(CardTheme.RED);
			}
			String title = verification.getString("title");
			String comment = verification.getString("comment");
			String _date = Formatter.getString(verification.getDate("date"), "yyyy-MM-dd HH:mm:ss");
			Document user_meta = (Document) verification.get("user_meta");
			String name = user_meta.getString("name");
			String verifyInfo = comment + "<br>" + name + " " + _date;
			
			status = "<span class='" + style + "' style='cursor:pointer;' onclick='layer.alert(\"" + verifyInfo + "\", {\"skin\": \"layui-layer-lan\",title:\"" + title
					+ "\"})'>" + status + "</span>";
		} else {
			status = "<span class='layui-badge  layui-bg-blue'>" + "�Ѵ���" + "</span>";
			theme = new CardTheme(CardTheme.INDIGO);
		}

		// String status = doc.getBoolean("finish", false) ? "�����" : ((verification !=
		// null) ? verification.getString("title") : "�Ѵ���");

		sb.append("<div class='brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor + ";padding:8px;'>"
				+ "<div class='label_subhead brui_card_text'>" + action + "</div>"//
				+ "<div style='text-align:center;margin-left:8px'><div class='label_headline'>" + priority + "</div>"
				+ "<div class='label_caption'>���ȼ�</div></div>"//
				+ "</div>");//

		rowHeight += 64;

		sb.append(RenderTools.getTextLineNoBlank("Ԥ�ڽ��", objective, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.getTextLineNoBlank("ִ�мƻ�", planStart + " ~ " + planFinish, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.getTextLineNoBlank("����Ԥ��", budget, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.renderUserAndText(chargerData, status, theme.lightText));
		rowHeight += 36;

		if (!finished) {
			// ɾ����ť
			sb.append("<div style='position:absolute;right:88px;bottom:16px;'>"// 8+16+16
					+ "<a href='deleteICA' target='_rwt' class='layui-icon layui-icon-close' onmouseover='layer.tips(\"" + "ɾ��ICA"
					+ "\",this,{tips:1})'></a>" //
					+ "</div>");
			// �༭��ť
			sb.append("<div style='position:absolute;right:64px;bottom:16px;'>"
					+ "<a href='editICA' target='_rwt' class='layui-icon layui-icon-edit' onmouseover='layer.tips(\"" + "�༭ICA"
					+ "\",this,{tips:1})'></a>" //
					+ "</div>");
			// ��֤��ť
			sb.append("<div style='position:absolute;right:40px;bottom:16px;'>"// 8+16+16
					+ "<a href='verificationICA' target='_rwt' class='layui-icon layui-icon-survey' onmouseover='layer.tips(\"" + "��֤ICA"
					+ "\",this,{tips:1})'></a>" //
					+ "</div>");
			// ��ɰ�ť
			sb.append("<div style='position:absolute;right:16px;bottom:16px;'>"
					+ "<a href='finishICA' target='_rwt' class='layui-icon layui-icon-ok' onmouseover='layer.tips(\"" + "���ICA�����"
					+ "\", this, {tips: 1})'></a>" //
					+ "</div>");
		}

		sb.insert(0, "<div class='brui_card_trans' style='background:#f9f9f9;height:" + (rowHeight - 2 * RenderTools.margin) + "px;margin:"
				+ RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

	public static String getICAVerifyInfo(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String[] color = new String[] { "fff", "fff" };

		Document user_meta = (Document) doc.get("user_meta");

		String title = doc.getString("title");
		String comment = doc.getString("comment");
		String _date = Formatter.getString(doc.getDate("date"), "yyyy-MM-dd HH:mm:ss");

		sb.append("<div class='brui_card_text' style='color:#fff;display:flex;align-items:center;height:36px;padding:8px 0px 0px 8px;'>" //
				+ title + "</div>");//
		rowHeight += 36;

		sb.append("<div style='height:56px'>" + RenderTools.getTextMultiLineNoBlank3("", comment, color) + "</div>");
		rowHeight += 56;

		sb.append(RenderTools.renderUserAndText(user_meta, _date, CardTheme.TEXT_LINE[0]));
		rowHeight += 36;

		String bg = title.equals("δͨ����֤") ? "e84e40" : "5c6bc0";
		sb.insert(0, "<div class='brui_card_trans' style='background:#" + bg + ";height:" + (rowHeight - 2 * RenderTools.margin)
				+ "px;margin:" + RenderTools.margin + "px;'>");
		sb.append("</div>");
		return sb.toString();
	}

}
