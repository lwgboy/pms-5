package com.bizvisionsoft.serviceimpl.renderer;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Formatter;

public class D3Renderer {

	public static Document renderICA(Document doc, String lang) {
		CardTheme theme = new CardTheme(CardTheme.INDIGO);

		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String action = doc.getString("action");
		String objective = doc.getString("objective");
		String priority = new String[] { "最高", "高", "中", "低", "最低" }[Integer.parseInt(doc.getString("priority"))];
		String planStart = Formatter.getString(doc.get("planStart"), "yyyy/MM/dd HH:mm:ss");
		String planFinish = Formatter.getString(doc.get("planFinish"), "yyyy/MM/dd HH:mm:ss");
		String budget = doc.getString("budget");
		Document chargerData = (Document) doc.get("charger_meta");

		String[] color = new String[] { "000000", "757575" };

		sb.append("<div class='brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor + ";padding:8px;'>"
				+ "<div class='brui_card_text'>" + action + "</div>"//
				+ "<div style='text-align:center;'><div class='label_headline'>" + priority + "</div>"
				+ "<div class='label_caption'>优先级</div></div>"//
				+ "</div>");//

		rowHeight += 64;

		sb.append(RenderTools.getTextLineNoBlank("预期结果", objective, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.getTextLineNoBlank("计划开始", planStart, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.getTextLineNoBlank("计划完成", planFinish, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.getTextLineNoBlank("费用预算", budget, CardTheme.TEXT_LINE));
		rowHeight += 24;
		
		String url = RenderTools.getFirstImageURL(chargerData, "headPics");
		String name = chargerData.getString("name");
		if (url != null) {
			sb.append("<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'>"//
			+ "<img src='" + url + "' style='border-radius:17px;' width='28' height='28'/>"//
			+ "<span class='label_caption' style='margin-left:4px;color:#" + color[1] + "'>" + name + "</span>" //
			+ "</div>");
			rowHeight += 36;
		}else {
			sb.append(RenderTools.getTextLineNoBlank("行动负责", name, CardTheme.TEXT_LINE));
			rowHeight += 24;
		}
		
		sb.append("<div style='position:absolute;right:16px;bottom:16px;'>"
				+ "<a href='editICA' target='_rwt' class='layui-icon layui-icon-edit'></a>" + "</div>");

		sb.insert(0, "<div class='brui_card_trans' style='background:#f9f9f9;height:" + (rowHeight - 2 * RenderTools.margin) + "px;margin:"
				+ RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

}
