package com.bizvisionsoft.serviceimpl.renderer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.bson.Document;

import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.tools.Formatter;

public class MessageRenderer {

	public static Document render(Message msg) {
		Document doc = new Document();
		CardTheme theme = new CardTheme(CardTheme.LIGHT_GREY);
		StringBuffer sb = new StringBuffer();
		int rowHeight = 172;

		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		// Í·Ïñ
		String sender = msg.getSenderInfo();
		String senderName = sender;// .substring(0, sender.indexOf("[")).trim();
		String headPicURL = msg.getSenderHeadImageURL();
		String img;
		if (headPicURL != null) {
			img = "<img src=" + headPicURL + " style='float:left;border-radius:28px;width:48px;height:48px;'/>";
		} else {
			try {
				String alpha = Formatter.getAlphaString(senderName);
				headPicURL = "/bvs/svg?text=" + URLEncoder.encode(alpha, "utf-8") + "&color=ffffff";
				img = "<img src=" + headPicURL + " style='float:left;margin-top:4px;margin-left:4px;background-color:"
						+ ColorTheme.getHTMLDarkColor(alpha) + ";border-radius:28px;width:48px;height:48px;'/>";
			} catch (UnsupportedEncodingException e) {
				img = "";
			}
		}

		sb.append("<div class='brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor + ";padding:8px;'>"
				+ "<div>"//
				+ "<div class='label_title'>" + msg.getSubject() + "</div>"//
				+ "<div>" + senderName + "&nbsp;&nbsp;&nbsp;" + Formatter.getString(msg.getSendDate(), "yyyy-MM-dd HH:mm:ss") + "</div>"//
				+ "</div>" //
				+ img + "</div>");//

		sb.append(RenderTools.getTextMultiLine(msg.getContent(), CardTheme.TEXT_LINE[1]));

		sb.append("<div class='layui-btn layui-btn-xs layui-btn-normal' style='position:absolute;right:16px;bottom:16px;'>"
				+ "<a href='read' target='_rwt' class='layui-icon layui-icon-ok' style='color:#fff;'></a>" + "</div>");

		doc.put("height", rowHeight);
		doc.put("html", sb.toString());
		doc.put("_id", msg.get_id());
		return doc;
	}

}
