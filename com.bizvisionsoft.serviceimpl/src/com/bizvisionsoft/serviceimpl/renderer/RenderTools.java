package com.bizvisionsoft.serviceimpl.renderer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.ColorTheme;
import com.bizvisionsoft.service.tools.Formatter;

public class RenderTools {

	public static final int margin = 8;

	public static final String IMG_URL_CALENDAR = "rwt-resources/extres/img/calendar_c.svg";

	public static final String IMG_URL_USER = "rwt-resources/extres/img/user_c.svg";

	public static final String IMG_URL_PROJECT = "rwt-resources/extres/img/project_c.svg";

	public static final String IMG_URL_TASK = "rwt-resources/extres/img/task_c.svg";

	public static final String IMG_URL_TEL = "rwt-resources/extres/img/tel_c.svg";

	public static final String IMG_URL_EMAIL = "rwt-resources/extres/img/email_c.svg";

	public static String getIconTextLine(String label, String text, String iconURL, String[] color) {
		return getIconTextLine(label, text, iconURL, color, 20);
	}

	public static String getIconTextLine(String label, String text, String iconURL, String[] color, int size) {
		if (label != null)
			label += "&nbsp;:&nbsp;";
		else
			label = "";
		return "<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'>"//
				+ "<img src='" + iconURL + "' width='" + size + "' height='" + size + "'>"//
				+ "<div class='label_caption brui_text_line' style='margin-left:8px;width:100%;display:inline-flex;'>" //
				+ "<span style='color:#" + color[0] + "'>" + label + "</span>" //
				+ "<span style='color:#" + color[1] + "'>" + text + "</span>" //
				+ "</div>"//
				+ "</div>";
	}

	public static String getTextLine(String label, String text, String[] color) {
		if (label != null)
			label += "&nbsp;:&nbsp;";
		else
			label = "";
		return "<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'>"//
				+ "<div style='width:20px;height:20px'></div>"//
				+ "<div class='label_caption brui_text_line' style='margin-left:8px;width:100%;display:inline-flex;'>" //
				+ "<span style='color:#" + color[0] + "'>" + label + "</span>" //
				+ "<span style='color:#" + color[1] + "'>" + text + "</span>" //
				+ "</div>"//
				+ "</div>";
	}

	public static String getTextLineNoBlank(String label, String text, String[] color) {
		if (label != null)
			label += "&nbsp;:&nbsp;";
		else
			label = "";
		return "<div style='padding:8px 8px 0px 0px;display:flex;align-items:center;'>"//
				+ "<div class='label_caption brui_text_line' style='margin-left:8px;width:100%;display:inline-flex;'>" //
				+ "<span style='color:#" + color[0] + "'>" + label + "</span>" //
				+ "<span style='color:#" + color[1] + "'>" + text + "</span>" //
				+ "</div>"//
				+ "</div>";
	}

	public static String shortDate(Date date) {
		return Formatter.getString(date, "yyyy/MM/dd");
	}

	public static void renderCardBoard(StringBuffer sb, int rowHeight) {
		sb.insert(0, "<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");
		sb.append("</div>");
	}

	public static String getTextMultiLine(String label, String text, String[] color) {
		return "<div class='brui_card_text3 label_caption' style='padding:8px 8px 0px 36px;'>" + //
				"<div style='color:#" + color[0] + "'>" + label + "</div>" + "<div style='color:#" + color[1] + "'>" + text + "</div>"//
				+ "</div>";//
	}

	public static String getTextMultiLineNoBlank3(String label, String text, String[] color) {
		return "<div class='brui_card_text3 label_caption' style='padding:8px 8px 0px 8px;'>" + //
				"<div style='color:#" + color[0] + "'>" + label + "</div>" + "<div style='color:#" + color[1] + "'>" + text + "</div>"//
				+ "</div>";//
	}

	public static String getTextMultiLineNoBlank(String label, String text, String[] color) {
		return "<div class='label_caption' style='padding:0px 8px 8px 8px;'>" + //
				"<div style='color:#" + color[0] + "'>" + label + "</div>" + "<div class='brui_text_multiline' style='color:#" + color[1]
				+ "'>" + text + "</div>"//
				+ "</div>";//
	}

	public static String getTextMultiLineNoBlank2(String label, String text, String[] color) {
		return "<div class='brui_card_text2 label_caption' style='padding:8px 8px 0px 8px;'>" + //
				"<div style='color:#" + color[0] + "'>" + label + "</div>" + "<div style='color:#" + color[1] + "'>" + text + "</div>"//
				+ "</div>";//
	}

	public static String getTextMultiLine(String text, String color) {
		return "<div class='brui_card_text3 label_caption' style='padding:8px 8px 0px 8px;'>" + //
				"<div style='color:#" + color + "'>" + text + "</div>"//
				+ "</div>";//
	}

	public static String getFirstImageURL(Document doc, String imgField) {
		List<?> headPics = (List<?>) doc.get(imgField);
		if (Check.isAssigned(headPics)) {
			Document pic = (Document) headPics.get(0);
			return "/bvs/fs?id=" + pic.get("_id") + "&namespace=" + pic.get("namepace") + "&name=" + pic.get("name") + "&sid=rwt";
		}
		return null;
	}

	public static String getNameImageURL(String name) {
		try {
			return "/bvs/svg?text=" + URLEncoder.encode(name, "utf-8") + "&color=ffffff";
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	public static String tooltips(String message, String text) {
		return "<div style='cursor:pointer;' " + "onmouseover='layer.tips(\"" + message + "\", this, {tips: 1})'>" + text + "</div>";
	}

	public static String renderUserAndText(Document user, String dateStr, String color) {
		String img;
		String name = user.getString("name");
		String url = RenderTools.getFirstImageURL(user, "headPics");
		if (url != null) {
			img = "<img src=" + url + " style='border-radius:17px;width:28px;height:28px;'/>";
		} else {
			String alpha = Formatter.getAlphaString(name);
			url = RenderTools.getNameImageURL(name);
			img = "<img src=" + url + " style='margin-top:4px;margin-left:4px;background-color:" + ColorTheme.getHTMLDarkColor(alpha)
					+ ";border-radius:17px;width:28px;height:28px;'/>";
		}
		return "<div style='padding:8px 8px 8px 8px;display:flex;align-items:center;'>" + img
				+ "<span class='label_caption' style='margin-left:4px;color:#" + color + "'>" + name + "&nbsp;&nbsp;" + dateStr + "</span>" //
				+ "</div>";
	}
}
