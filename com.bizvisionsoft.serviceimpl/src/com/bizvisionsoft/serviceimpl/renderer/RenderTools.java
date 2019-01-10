package com.bizvisionsoft.serviceimpl.renderer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.ColorTheme;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;

public class RenderTools {

	public static final String STYLE_1LINE = "brui_text_line label_caption brui_line_padding";

	public static final String STYLE_NLINE = "brui_text_multiline label_caption brui_line_padding";

	public static final String STYLE_3LINE = "brui_card_text3 label_caption brui_line_padding";

	public static final String IMG_URL_CALENDAR = "rwt-resources/extres/img/calendar_c.svg";

	public static final String IMG_URL_USER = "rwt-resources/extres/img/user_c.svg";

	public static final String IMG_URL_MONEY = "rwt-resources/extres/img/money_c.svg";

	public static final String IMG_URL_PROJECT = "rwt-resources/extres/img/project_c.svg";

	public static final String IMG_URL_TASK = "rwt-resources/extres/img/task_c.svg";

	public static final String IMG_URL_TEL = "rwt-resources/extres/img/tel_c.svg";

	public static final String IMG_URL_EMAIL = "rwt-resources/extres/img/email_c.svg";

	public static String shortDate(Date date) {
		return Formatter.getString(date, "yyyy/MM/dd");
	}

	public static String getFirstFileURL(Document doc, String imgField) {
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

	public static void appendButton(StringBuffer sb, String icon, int right, int bottom, String tips, String target) {
		sb.append("<div style='position:absolute;right:" + right + "px;bottom:" + bottom + "px;'>" + "<a href='" + target
				+ "' target='_rwt' class='layui-icon " + icon + "' onmouseover='layer.tips(\"" + tips + "\",this,{tips:1})'></a></div>");
	}

	public static void appendLabelAndMultiLine(StringBuffer sb, String label, String labelStyle, String text, String[] color) {
		sb.append("<div class='label_caption brui_line_padding'>" + //
				"<div class='" + labelStyle + "' style='color:#" + color[0] + "'>" + label + "</div>"
				+ "<div class='brui_text_multiline' style='color:#" + color[1] + "'>" + text + "</div>"//
				+ "</div>");//
	}

	public static void appendHeader(StringBuffer sb, CardTheme theme, String text, int height) {
		sb.append("<div class='label_subhead brui_card_head' style='height:" + height + "px;background:#" + theme.headBgColor + ";color:#"
				+ theme.headFgColor + ";padding:8px;'>" + text//
				+ "</div>");//
	}

	public static String getUserHeadPicURL(Document doc, String name) {
		String img;
		String url = RenderTools.getFirstFileURL(doc, "headPics");
		if (url != null) {
			img = "<img src=" + url + " style='border-radius:28px;width:36px;height:36px;'/>";
		} else {
			String alpha = Formatter.getAlphaString(name);
			url = RenderTools.getNameImageURL(name);
			img = "<img src=" + url + " style='margin-top:4px;margin-left:4px;background-color:" + ColorTheme.getHTMLDarkColor(alpha)
					+ ";border-radius:28px;width:36px;height:36px;'/>";
		}
		return img;
	}

	public static void appendUserAndText(StringBuffer sb, Document user, String text) {
		String name = user.getString("name");
		String url = RenderTools.getFirstFileURL(user, "headPics");
		appendUserAndText(sb, url, name, text);
	}

	public static void appendUserAndText(StringBuffer sb, String url, String name, String text) {
		String img;
		if (url != null) {
			img = "<img src=" + url + " style='border-radius:17px;width:28px;height:28px;'/>";
		} else {
			String alpha = Formatter.getAlphaString(name);
			url = RenderTools.getNameImageURL(name);
			img = "<img src=" + url + " style='margin-top:4px;margin-left:4px;background-color:" + ColorTheme.getHTMLDarkColor(alpha)
					+ ";border-radius:17px;width:28px;height:28px;'/>";
		}
		text = text == null ? "" : ("&nbsp;&nbsp;" + text);
		sb.append("<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'>" + img
				+ "<span class='label_caption' style='margin-left:4px;'>" + name + text + "</span>" //
				+ "</div>");
	}

	public static void appendCardBg(StringBuffer sb, String bg) {
		sb.insert(0, "<div class='brui_card2' style='background:" + bg + ";'>");
		sb.append("</div>");
	}

	public static void appendCardBg(StringBuffer sb) {
		appendCardBg(sb, "white");
	}

	public static void appendText(StringBuffer sb, String text, String style) {
		if (text != null) {
			sb.append("<div class='" + style + "'>" + text + "</div>");//
		}
	}

	public static void appendLabelAndTextLine(StringBuffer sb, String label, String color1, String text, String color2, int marginLeft) {
		sb.append("<div class='brui_line_padding label_caption' style='align-items:center;width:100%;display:flex;'>" //
				+ "<div style='margin-left:" + marginLeft + "px;color:#" + color1 + "'>" + (label == null ? "" : label) + "</div>" //
				+ "<div class='brui_text_line' style='color:#" + color2 + "'>" + (text == null ? "" : text) + "</div>" //
				+ "</div>");
	}

	public static void appendLabelAndTextLine(StringBuffer sb, String label, String text) {
		appendLabelAndTextLine(sb, label, CardTheme.TEXT_LINE[0], text, CardTheme.TEXT_LINE[1], 0);
	}

	public static void appendLabelAndTextLine(StringBuffer sb, String label, String text, int maringLeft) {
		appendLabelAndTextLine(sb, label, CardTheme.TEXT_LINE[0], text, CardTheme.TEXT_LINE[1], maringLeft);
	}

	public static void appendIconLabelAndTextLine(StringBuffer sb, String iconUrl, int size, String label, String color1, String text,
			String color2) {
		sb.append("<div class='brui_line_padding' style='display:flex;align-items:center;'>"//
				+ "<img src='" + iconUrl + "' width='" + size + "' height='" + size + "'>"//
				+ "<div class='label_caption' style='margin-left:8px;width:100%;display:inline-flex;'>" //
				+ "<div style='color:#" + color1 + "'>" + (label == null ? "" : label) + "</div>" //
				+ "<div class='brui_text_line' style='color:#" + color2 + "'>" + (text == null ? "" : text) + "</div>" //
				+ "</div>"//
				+ "</div>");
	}

	public static void appendIconLabelAndTextLine(StringBuffer sb, String iconUrl, String label, String text) {
		appendIconLabelAndTextLine(sb, iconUrl, 16, label, CardTheme.TEXT_LINE[0], text, CardTheme.TEXT_LINE[1]);
	}

	public static void appendIconTextLine(StringBuffer sb, String iconURL, int size, String text) {
		sb.append("<div class='brui_line_padding' style='display:flex;align-items:center;'>"//
				+ "<img src='" + iconURL + "' width='" + size + "px' height='" + size + "px'>"//
				+ "<div class='label_caption brui_text_line' style='margin-left:8px;'>" //
				+ (text == null ? "" : text) //
				+ "</div>"//
				+ "</div>");
	}

	public static void appendMultiFiles(StringBuffer sb, List<?> list) {
		for (int i = 0; i < list.size(); i++) {
			Document fileData = (Document) list.get(i);
			String name = fileData.getString("name");
			String url = "/bvs/fs?id=" + fileData.get("_id") + "&namespace=" + fileData.get("namepace") + "&name=" + name + "&sid=rwt";
			sb.append("<a href='" + url + "' class='brui_line_padding brui_text_line label_caption grey'>" + name + "</a>");//
		}
	}

	public static void appendSchedule(StringBuffer sb, Date planStart, Date planFinish, Date actualStart, Date actualFinish) {
		String text = shortDate(planStart) + "~" + shortDate(planFinish);
		appendIconLabelAndTextLine(sb, IMG_URL_CALENDAR, "计划：", text);

		text = null;
		if (actualStart != null)
			text = shortDate(actualStart);
		if (actualFinish != null)
			text += "~" + shortDate(actualFinish);

		if (text != null)
			appendLabelAndTextLine(sb, "实际：", text, 24);
	}

	public static <T> void appendList(StringBuffer sb, List<T> list, String color, Function<T, String> func) {
		for (int i = 0; i < list.size(); i++) {
			T item = list.get(i);
			sb.append("<div class='label_caption' style='padding:8px 16px 0px 12px;display:flex;color:#" + color
					+ ";White-space:nowrap;overflow:hidden;text-overflow:ellipsis;width:100%;align-items:baseline;'>"
					+ "<i class='layui-icon layui-icon-circle' style='font-size:7px;margin-right:8px;'></i>"
					+ (func == null ? item : func.apply(item)) + "</div>");

		}
	}
	
	public static void appendIndicator(StringBuffer sb, Double ind, String label, String text, String[] indColor) {
		sb.append("<div><div class='label_caption' style='text-align:center;color:#9e9e9e'>" + MetaInfoWarpper.warpper(label, text)
				+ "</div><img src='/bvs/svg?type=progress&percent=" + ind + "&bgColor=" + indColor[0] + "&fgColor=" + indColor[1]
				+ "' width=72 height=72/></div>");
	}

}
