package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Formatter;

public class D5Renderer {

	public static Document renderSelectedPCA1(Document doc, String lang) {
		List<?> pca = (List<?>) doc.get("pca1");
		Document charger = (Document) doc.get("charger1_meta");
		Date date = doc.getDate("date1");
		return renderCard(pca, "杜绝问题产生", charger, date, lang);
	}
	
	public static Document renderSelectedPCA2(Document doc, String lang) {
		List<?> pca = (List<?>) doc.get("pca2");
		Document charger = (Document) doc.get("charger2_meta");
		Date date = doc.getDate("date2");
		return renderCard(pca, "防止问题流出", charger, date, lang);
	}

	private static Document renderCard(List<?> list, String title, Document charger, Date date, String lang) {
		String dateStr = Formatter.getString(date);

		CardTheme theme = new CardTheme(CardTheme.INDIGO);

		StringBuffer sb = new StringBuffer();
		
		sb.append("<div class='label_subhead brui_card_head' style='height:36px;background:#" + theme.headBgColor + ";color:#"
				+ theme.headFgColor + ";padding:8px;'>" + title //
				+ "</div>");//
		sb.append("<div class='layui-text'>");
		sb.append("<ul style='margin-left:12px;padding:4px 0px 4px 16px;'>");
		for (int i = 0; i < list.size(); i++) {
			Document x = (Document) list.get(i);
			sb.append("<li>" + x.getString("name") + "</li>");
		}
		sb.append("</ul>");
		sb.append("</div>");
		
		sb.append(RenderTools.renderUserAndText(charger, dateStr, theme.lightText));

		sb.insert(0, "<div class='brui_card' style='margin:" + RenderTools.margin
				+ "px;'>");
		sb.append("</div>");
		
		
		return new Document("html", sb.toString());
	}


	
}
