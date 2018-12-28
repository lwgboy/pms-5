package com.bizvisionsoft.serviceimpl.renderer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.ColorTheme;
import com.bizvisionsoft.service.tools.Formatter;

public class D1CFTRenderer {

	public static Document render(Document doc, String lang) {
		CardTheme theme = new CardTheme(CardTheme.CYAN);

		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		// 头像
		String name = doc.getString("name");
		String role = getRoleName(doc.getString("role"));
		String mobile = Optional.ofNullable(doc.getString("mobile")).map(e -> "<a href='tel:" + e + "'>" + e + "</a>").orElse("");
		String position = Optional.ofNullable(doc.getString("position")).orElse("");
		String email = Optional.ofNullable(doc.getString("email")).map(e -> "<a href='mailto:" + e + "'>" + e + "</a>").orElse("");
		String dept = Optional.ofNullable(doc.getString("dept")).orElse("");
		List<?> headPics = (List<?>) doc.get("headPics");
		String headPicURL = null;
		if (Check.isAssigned(headPics)) {
			Document pic = (Document) headPics.get(0);
			headPicURL = "/bvs/fs?id=" + pic.get("_id") + "&namespace=" + pic.get("namepace") + "&name=" + pic.get("name") + "&sid=rwt";
		}
		String img;
		if (headPicURL != null) {
			img = "<img src=" + headPicURL + " style='float:left;border-radius:28px;width:48px;height:48px;'/>";
		} else {
			try {
				String alpha = Formatter.getAlphaString(name);
				headPicURL = "/bvs/svg?text=" + URLEncoder.encode(alpha, "utf-8") + "&color=ffffff";
				img = "<img src=" + headPicURL + " style='float:left;margin-top:4px;margin-left:4px;background-color:"
						+ ColorTheme.getHTMLDarkColor(alpha) + ";border-radius:28px;width:48px;height:48px;'/>";
			} catch (UnsupportedEncodingException e) {
				img = "";
			}
		}

		sb.append("<div class='brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor + ";padding:8px;'>"
				+ "<div class='label_title'><div>" + role + "</div><div class='label_subhead'>" + name + " " + dept + " " + position
				+ "</div></div>"//
				+ img //
				+ "</div>");//
		rowHeight += 64;

		sb.append(RenderTools.getIconTextLine(null, mobile, RenderTools.IMG_URL_TEL, CardTheme.TEXT_LINE));
		rowHeight += 20 + 8;
		sb.append(RenderTools.getIconTextLine(null, email, RenderTools.IMG_URL_EMAIL, CardTheme.TEXT_LINE));
		rowHeight += 20 + 8;

		sb.append("<div  style='position:absolute;right:16px;bottom:16px;'>"
				+ "<a href='delete' target='_rwt' class='layui-icon layui-icon-close'></a>" + "</div>");

		RenderTools.renderCardBoard(sb, rowHeight);

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

	private static String getRoleName(String roleId) {
		if ("0".equals(roleId)) {
			return "组长";
		} else if ("1".equals(roleId)) {
			return "设计";
		} else if ("2".equals(roleId)) {
			return "工艺";
		} else if ("3".equals(roleId)) {
			return "生产";
		} else if ("4".equals(roleId)) {
			return "质量";
		}
		return "";
	}

}
