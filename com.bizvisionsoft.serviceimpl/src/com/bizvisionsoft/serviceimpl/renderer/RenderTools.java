package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;

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
		if(label!=null) label +="&nbsp;:&nbsp;"; else label="";
		return "<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'>"//
				+ "<img src='" + iconURL + "' width='20' height='20'>"//
				+ "<div class='label_caption brui_text_line' style='margin-left:8px;width:100%;display:inline-flex;'>" //
				+ "<span style='color:#" + color[0] + "'>" + label + "</span>" //
				+ "<span style='color:#" + color[1] + "'>" + text + "</span>" //
				+ "</div>"//
				+ "</div>";
	}

	public static String getTextLine(String label, String text, String[] color) {
		if(label!=null) label +="&nbsp;:&nbsp;"; else label="";
		return "<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'>"//
				+ "<div style='width:20px;height:20px'></div>"//
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

	public static String getTextMultiLine(String text, String color) {
		return "<div class='brui_card_text3 label_caption' style='padding:8px 8px 0px 8px;'>" + //
				"<div style='color:#" + color + "'>" + text + "</div>"//
				+ "</div>";//
	}

}
