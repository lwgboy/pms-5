package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.ColorTheme;
import com.bizvisionsoft.service.tools.Formatter;

public class D1Renderer {

	public static Document render(Document doc, String lang) {
		CardTheme theme = new CardTheme(CardTheme.INDIGO);

		StringBuffer sb = new StringBuffer();

		// 头像
		String name = doc.getString("name");
		String role = new String[] { "组长", "设计", "工艺", "生产", "质量" }[Integer.parseInt(doc.getString("role"))];
		String mobile = Optional.ofNullable(doc.getString("mobile")).map(e -> "<a href='tel:" + e + "'>" + e + "</a>").orElse("");
		String position = Optional.ofNullable(doc.getString("position")).orElse("");
		String email = Optional.ofNullable(doc.getString("email")).map(e -> "<a href='mailto:" + e + "'>" + e + "</a>").orElse("");
		String dept = Optional.ofNullable(doc.getString("dept")).orElse("");

		String img;
		String url = RenderTools.getFirstImageURL(doc, "headPics");
		if (url != null) {
			img = "<img src=" + url + " style='float:left;border-radius:28px;width:36px;height:36px;'/>";
		} else {
			String alpha = Formatter.getAlphaString(name);
			url = RenderTools.getNameImageURL(name);
			img = "<img src=" + url + " style='float:left;margin-top:4px;margin-left:4px;background-color:"
					+ ColorTheme.getHTMLDarkColor(alpha) + ";border-radius:28px;width:36px;height:36px;'/>";
		}

		sb.append("<div class='brui_card_head' style='height:48px;background:#" + theme.headBgColor + ";color:#" + theme.headFgColor
				+ ";padding:8px;'>" + "<div class='label_subhead'><div>" + role + "</div><div class='label_body1'>" + name + " " + dept
				+ " " + position + "</div></div>"//
				+ img //
				+ "</div>");//

		sb.append(RenderTools.getIconTextLine(null, mobile, RenderTools.IMG_URL_TEL, CardTheme.TEXT_LINE, 16));
		sb.append(RenderTools.getIconTextLine(null, email, RenderTools.IMG_URL_EMAIL, CardTheme.TEXT_LINE, 16));

		sb.append("<div style='position:absolute;right:12px;bottom:12px;'>"
				+ "<a href='delete' target='_rwt' class='layui-icon layui-icon-close' onmouseover='layer.tips(\"" + "删除团队成员"
				+ "\",this,{tips:1})'></a>" + "</div>");

		sb.insert(0, "<div class='brui_card' style='padding-bottom:8px;margin:8px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

}
