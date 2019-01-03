package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;
import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Formatter;

public class D4Renderer {

	public static Document renderRootCauseDesc(Document doc, String lang) {
		Document charger_meta = (Document) doc.get("charger_meta");
		Date date = doc.getDate("date");
		return renderRootCard(doc, "问题产生的根本原因", doc.getString("rootCauseDesc"), charger_meta, date);
	}

	public static Document renderEscapePoint(Document doc, String lang) {
		Document charger_meta = (Document) doc.get("charger_meta");
		Date date = doc.getDate("date");
		return renderRootCard(doc, "问题流出的逃出点", doc.getString("escapePoint"), charger_meta, date);
	}

	private static Document renderRootCard(Document doc, String title, String rootCauseDesc, Document charger_meta, Date date) {
		String dateStr = Formatter.getString(date);

		CardTheme theme = new CardTheme(CardTheme.RED);

		StringBuffer sb = new StringBuffer();

		sb.append("<div class='label_title brui_card_head' style='height:36px;background:#" + theme.headBgColor + ";color:#"
				+ theme.headFgColor + ";padding:8px;'>" + title //
				+ "</div>");//

		sb.append("<div class='brui_text_multiline' style='padding:8px 8px 0px 8px;'>" + rootCauseDesc + "</div>");

		sb.append(RenderTools.renderUserAndText(charger_meta, dateStr, theme.lightText));

		sb.insert(0, "<div class='brui_card' style='margin:" + RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderCauseConsequence(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String name = doc.getString("name");
		String desc = Optional.ofNullable(doc.getString("description")).orElse("");
		int w = doc.getInteger("weight", 1);
		String p = Formatter.getPercentageFormatString(doc.getDouble("probability"));

		sb.append("<div class='label_caption' style='display:flex;flex-direction:column;justify-content:space-around;color:white;'>");
		sb.append(
				"<div style='justify-content:center;flex-grow:1;width:44px;display:flex;flex-direction:column;align-items:center;background:#5c6bc0;margin-bottom:1px;border-radius:4px 0px 0px 0px;'>");
		sb.append("<div>" + w + "</div><div>权重</div></div>");
		sb.append(
				"<div style='justify-content:center;flex-grow:1;width:44px;display:flex;flex-direction:column;align-items:center;background:#5c6bc0;;border-radius:0px 0px 0px 4px;'>");
		sb.append("<div>" + p + "</div><div>概率</div></div>");
		sb.append("</div>");

		sb.append("<div style='width:0;flex-grow:1;padding:0px 4px;display:flex;flex-direction:column;justify-content:space-around;'>");
		sb.append("<div class='brui_text_line'>" + name + "</div><div class='brui_card_text3 label_caption' style='height:48px;'>" + desc
				+ "</div>");
		sb.append("</div>");

		rowHeight += 82;

		sb.insert(0, "<div class='brui_card_trans' style='display:flex;background:#f9f9f9;height:" + (rowHeight - 2 * RenderTools.margin)
				+ "px;margin:" + RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

}
