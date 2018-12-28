package com.bizvisionsoft.serviceimpl.renderer;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Formatter;

public class D3Renderer {

	public static Document renderICA(Document doc, String lang) {
		CardTheme theme = new CardTheme(CardTheme.CYAN);
		
		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;
		
		String action = doc.getString("action");
		String objective = doc.getString("objective");
		String priority = new String[] {"最高","高","中","低","最低"}[Integer.parseInt(doc.getString("priority"))];
		String planStart =Formatter.getString(doc.get("planStart"),"yyyy/MM/dd HH:mm:ss");
		String planFinish =Formatter.getString(doc.get("planFinish"),"yyyy/MM/dd HH:mm:ss");
		String budget =doc.getString("budget");
		String chargerId =doc.getString("charger");
		Document chargerData = (Document) doc.get("charger_meta");

		rowHeight += 64;
		sb.append("<div class='label_title brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor
				+ ";padding:8px'>" + "<div class='brui_card_text'>" + action + "</div></div>");
		
		rowHeight += 20 + 8;
		RenderTools.getIconTextLine("计划", planStart + " ~ " + planFinish, RenderTools.IMG_URL_CALENDAR, CardTheme.TEXT_LINE);
		
		rowHeight += 20 + 8;
		RenderTools.getTextLine("预算", budget,  CardTheme.TEXT_LINE);
		
		
		
		sb.append("<div style='position:absolute;right:16px;bottom:16px;'>"
				+ "<a href='editICA' target='_rwt' class='layui-icon layui-icon-edit'></a>" + "</div>");

		sb.insert(0, "<div class='brui_card_trans' style='background:#f8f8f8;height:" + (rowHeight - 2 * RenderTools.margin) + "px;margin:"
				+ RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}


}
