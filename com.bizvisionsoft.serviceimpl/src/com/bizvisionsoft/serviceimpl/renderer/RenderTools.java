package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;

import com.bizvisionsoft.service.tools.Formatter;

public class RenderTools {

	public static final String IMG_URL_CALENDAR = "rwt-resources/extres/img/calendar_c.svg";

	public static final String IMG_URL_USER = "rwt-resources/extres/img/user_c.svg";
	
	public static final String IMG_URL_PROJECT = "rwt-resources/extres/img/project_c.svg";

	public static String getIconTextLine(String text, String iconURL, String color) {
		return "<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'><img src='" + iconURL
				+ "' width='20' height='20'><div class='label_caption brui_text_line' style='color:#" + color
				+ ";margin-left:8px;width:100%'>" + text + "</div></div>";
	}

	public static String getTextLine(String text, String color) {
		return "<div class='label_caption brui_text_line' style='padding:8px 8px 0px 36px;height:28px;color:#" + color + "'>" + text
				+ "</div>";
	}

	public static String shortDate(Date date) {
		return Formatter.getString(date, "yyyy/M/d");
	}

}
