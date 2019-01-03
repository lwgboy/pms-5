package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;
import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.tools.Formatter;

public class D2Renderer {

	public static Document renderPDCard(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();
		String what = Optional.ofNullable(doc.getString("what")).orElse("");
		String when = Optional.ofNullable(doc.getString("when")).orElse("");
		String where = Optional.ofNullable(doc.getString("where")).orElse("");
		String who = Optional.ofNullable(doc.getString("who")).orElse("");
		String why = Optional.ofNullable(doc.getString("why")).orElse("");
		String how = Optional.ofNullable(doc.getString("how")).orElse("");
		String howmany = Optional.ofNullable(doc.getString("howmany")).orElse("");

		String[] color = new String[] { "000000", "757575" };
		sb.append(
				RenderTools.getTextMultiLineNoBlank("<span class='deep_orange' style='font-weight:700;'>What / 当前状况</span>", what, color));
		sb.append(
				RenderTools.getTextMultiLineNoBlank("<span class='deep_orange' style='font-weight:700;'>When / 发现时间</span>", when, color));
		sb.append(RenderTools.getTextMultiLineNoBlank("<span class='deep_orange' style='font-weight:700;'>Where / 地点和位置</span>", where,
				color));
		sb.append(RenderTools.getTextMultiLineNoBlank("<span class='deep_orange' style='font-weight:700;'>Who / 有关人员</span>", who, color));
		sb.append(RenderTools.getTextMultiLineNoBlank("<span class='deep_orange' style='font-weight:700;'>Why / 原因推测</span>", why, color));
		sb.append(
				RenderTools.getTextMultiLineNoBlank("<span class='deep_orange' style='font-weight:700;'>How / 怎样发现的问题</span>", how, color));

		sb.append(RenderTools.getTextMultiLineNoBlank("<span class='deep_orange' style='font-weight:700;'>How many / 频度，数量</span>", howmany,
				color));

		sb.append("<div style='position:absolute;right:12px;bottom:12px;'>"
				+ "<a href='editpd' target='_rwt' class='layui-icon layui-icon-edit' onmouseover='layer.tips(\"" + "编辑问题描述"
				+ "\",this,{tips:1})'></a></div>");

		sb.insert(0, "<div class='brui_card_trans' style='background:#f8f8f8;margin:8px;padding-top:8px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderPhotoCard(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();
		String imgUrl = RenderTools.getFirstImageURL(doc, "problemImg");

		sb.append("<img src='" + imgUrl + "' style='width:100%;height:auto;border-radius:4px 4px 0px 0px;'"
				+ "onclick='$.getJSON(\"bvs/imgf?c=d2ProblemPhoto&i=" + doc.get("_id")
				+ "&f=problemImg\", function(json){layer.photos({photos: json});});'" + "/>");

		String[] color = new String[] { "000000", "757575" };
		String text = doc.getString("problemImgDesc");
		if (text != null) {
			sb.append("<div class='brui_card_text3 label_caption' style='padding:8px 8px 0px 8px;'>" //
					+ "<div style='color:#" + color[1] + "'>" + text + "</div>"//
					+ "</div>");//
		}

		Date date = doc.getDate("receiveDate");
		String receiver = doc.getString("receiver");
		String location = doc.getString("location");
		sb.append(RenderTools.getTextLineNoBlank(null, Formatter.getString(date) + "/" + receiver + " " + location, color));

		sb.append("<div style='position:absolute;right:12px;bottom:12px;'>"
				+ "<a href='deletephoto' target='_rwt' class='layui-icon layui-icon-close' onmouseover='layer.tips(\"" + "删除图片资料"
				+ "\",this,{tips:1})'></a>" + "</div>");

		sb.insert(0, "<div class='brui_card' style='padding-bottom:8px;margin:" + RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

}
