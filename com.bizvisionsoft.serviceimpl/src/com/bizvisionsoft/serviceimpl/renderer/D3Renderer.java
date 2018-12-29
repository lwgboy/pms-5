package com.bizvisionsoft.serviceimpl.renderer;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.ColorTheme;
import com.bizvisionsoft.service.tools.Formatter;

public class D3Renderer {

	public static Document renderICA(Document doc, String lang) {
		CardTheme theme = new CardTheme(CardTheme.INDIGO);

		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String action = doc.getString("action");
		String objective = doc.getString("objective");
		String priority = new String[] { "���", "��", "��", "��", "���" }[Integer.parseInt(doc.getString("priority"))];
		String planStart = Formatter.getString(doc.get("planStart"), "yyyy/MM/dd HH:mm:ss");
		String planFinish = Formatter.getString(doc.get("planFinish"), "yyyy/MM/dd HH:mm:ss");
		String budget = doc.getString("budget");
		Document chargerData = (Document) doc.get("charger_meta");

		String[] color = new String[] { "000000", "757575" };

		sb.append("<div class='brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor + ";padding:8px;'>"
				+ "<div class='brui_card_text'>" + action + "</div>"//
				+ "<div style='text-align:center;'><div class='label_headline'>" + priority + "</div>"
				+ "<div class='label_caption'>���ȼ�</div></div>"//
				+ "</div>");//

		rowHeight += 64;

		sb.append(RenderTools.getTextLineNoBlank("Ԥ�ڽ��", objective, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.getTextLineNoBlank("�ƻ���ʼ", planStart, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.getTextLineNoBlank("�ƻ����", planFinish, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.getTextLineNoBlank("����Ԥ��", budget, CardTheme.TEXT_LINE));
		rowHeight += 24;

		String url = RenderTools.getFirstImageURL(chargerData, "headPics");
		String name = chargerData.getString("name");
		if (url != null) {
			sb.append("<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'>"//
					+ "<img src='" + url + "' style='border-radius:17px;' width='28' height='28'/>"//
					+ "<span class='label_caption' style='margin-left:4px;color:#" + color[1] + "'>" + name + "</span>" //
					+ "</div>");
			rowHeight += 36;
		} else {
			sb.append(RenderTools.getTextLineNoBlank("�ж�����", name, CardTheme.TEXT_LINE));
			rowHeight += 24;
		}

		// ɾ����ť
		sb.append("<div style='position:absolute;right:88px;bottom:16px;'>"// 8+16+16
				+ "<a href='deleteICA' target='_rwt' class='layui-icon layui-icon-close'></a>" //
				+ "</div>");
		// �༭��ť
		sb.append("<div style='position:absolute;right:64px;bottom:16px;'>"
				+ "<a href='editICA' target='_rwt' class='layui-icon layui-icon-edit'></a>" //
				+ "</div>");
		// ��֤��ť
		sb.append("<div style='position:absolute;right:40px;bottom:16px;'>"// 8+16+16
				+ "<a href='verificationICA' target='_rwt' class='layui-icon layui-icon-survey'></a>" //
				+ "</div>");
		// ��ɰ�ť
		sb.append("<div style='position:absolute;right:16px;bottom:16px;'>"
				+ "<a href='finishICA' target='_rwt' class='layui-icon layui-icon-ok'></a>" //
				+ "</div>");

		sb.insert(0, "<div class='brui_card_trans' style='background:#f9f9f9;height:" + (rowHeight - 2 * RenderTools.margin) + "px;margin:"
				+ RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

	public static Document renderICAVerified(Document doc, Object d3ica_id, String lang) {
		CardTheme theme = new CardTheme(CardTheme.INDIGO);

		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String img;
		Document user_meta = (Document) doc.get("user_meta");
		String userName = user_meta.getString("name");
		String url = RenderTools.getFirstImageURL(user_meta, "headPics");
		if (url != null) {
			img = "<img src=" + url + " style='float:left;border-radius:28px;width:48px;height:48px;'/>";
		} else {
			String alpha = Formatter.getAlphaString(userName);
			url = RenderTools.getNameImageURL(userName);
			img = "<img src=" + url + " style='float:left;margin-top:4px;margin-left:4px;background-color:"
					+ ColorTheme.getHTMLDarkColor(alpha) + ";border-radius:28px;width:48px;height:48px;'/>";
		}

		String title = doc.getString("title");
		String comment = doc.getString("comment");

		sb.append("<div class='brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor + ";padding:8px;'>"
				+ "<div class='brui_card_text' style='display:flex;align-items:center;'>" + img//
				+ "<span class='label_title' style='margin-left:4px'>" + title + "</span>" //
				+ "</div>"//
				+ "</div>");//
		rowHeight += 64;

		sb.append("<div style='height:72px'>"
				+ RenderTools.getTextMultiLineNoBlank3("", comment,
						CardTheme.TEXT_LINE)
				+ "</div>");
		rowHeight += 72;

		sb.insert(0, "<div class='brui_card_trans' style='background:#f9f9f9;height:" + (rowHeight - 2 * RenderTools.margin) + "px;margin:"
				+ RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", d3ica_id).append("html", sb.toString()).append("height", rowHeight);
	}

}
