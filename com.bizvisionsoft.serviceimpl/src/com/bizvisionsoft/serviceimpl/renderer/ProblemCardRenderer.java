package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.ColorTheme;
import com.bizvisionsoft.service.tools.Formatter;

public class ProblemCardRenderer {

	public static Document renderD1CFTMember(Document doc, String lang) {
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
	
	public static Document renderD25W2H(Document doc, String lang) {
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

	public static Document renderD3PhotoCard(Document doc, String lang) {
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
	
	public static Document renderD3ICA(Document doc, String lang) {

		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String action = doc.getString("action");
		String objective = doc.getString("objective");
		String priority = new String[] { "最高", "高", "中", "低", "最低" }[Integer.parseInt(doc.getString("priority"))];
		String planStart = Formatter.getString(doc.get("planStart"));
		String planFinish = Formatter.getString(doc.get("planFinish"));
		String budget = doc.getString("budget");
		Document chargerData = (Document) doc.get("charger_meta");

		Document verification = (Document) doc.get("verification");

		CardTheme theme = new CardTheme(CardTheme.INDIGO);

		boolean finished = doc.getBoolean("finish", false);
		String status;
		if (finished) {
			status = "<span class='layui-badge  layui-bg-green'>" + "已完成" + "</span>";
			theme = new CardTheme(CardTheme.DEEP_GREY);
		} else if (verification != null) {
			String style;
			status = verification.getString("title");
			if ("已验证".equals(status)) {
				style = "layui-badge  layui-bg-green";
//				theme = new CardTheme(CardTheme.TEAL);
			} else {
				style = "layui-badge";
//				theme = new CardTheme(CardTheme.RED);
			}
			String title = verification.getString("title");
			String comment = verification.getString("comment");
			String _date = Formatter.getString(verification.getDate("date"), "yyyy-MM-dd HH:mm:ss");
			Document user_meta = (Document) verification.get("user_meta");
			String name = user_meta.getString("name");
			String verifyInfo = comment + "<br>" + name + " " + _date;
			
			status = "<span class='" + style + "' style='cursor:pointer;' onclick='layer.alert(\"" + verifyInfo + "\", {\"skin\": \"layui-layer-lan\",title:\"" + title
					+ "\"})'>" + status + "</span>";
		} else {
			status = "<span class='layui-badge  layui-bg-blue'>" + "已创建" + "</span>";
			theme = new CardTheme(CardTheme.INDIGO);
		}

		// String status = doc.getBoolean("finish", false) ? "已完成" : ((verification !=
		// null) ? verification.getString("title") : "已创建");

		sb.append("<div class='brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor + ";padding:8px;'>"
				+ "<div class='label_subhead brui_card_text'>" + action + "</div>"//
				+ "<div style='text-align:center;margin-left:8px'><div class='label_headline'>" + priority + "</div>"
				+ "<div class='label_caption'>优先级</div></div>"//
				+ "</div>");//

		rowHeight += 64;

		sb.append(RenderTools.getTextLineNoBlank("预期结果", objective, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.getTextLineNoBlank("执行计划", planStart + " ~ " + planFinish, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.getTextLineNoBlank("费用预算", budget, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.renderUserAndText(chargerData, status, theme.lightText));
		rowHeight += 36;

		if (!finished) {
			// 删除按钮
			sb.append("<div style='position:absolute;right:88px;bottom:16px;'>"// 8+16+16
					+ "<a href='deleteICA' target='_rwt' class='layui-icon layui-icon-close' onmouseover='layer.tips(\"" + "删除ICA"
					+ "\",this,{tips:1})'></a>" //
					+ "</div>");
			// 编辑按钮
			sb.append("<div style='position:absolute;right:64px;bottom:16px;'>"
					+ "<a href='editICA' target='_rwt' class='layui-icon layui-icon-edit' onmouseover='layer.tips(\"" + "编辑ICA"
					+ "\",this,{tips:1})'></a>" //
					+ "</div>");
			// 验证按钮
			sb.append("<div style='position:absolute;right:40px;bottom:16px;'>"// 8+16+16
					+ "<a href='verificationICA' target='_rwt' class='layui-icon layui-icon-survey' onmouseover='layer.tips(\"" + "验证ICA"
					+ "\",this,{tips:1})'></a>" //
					+ "</div>");
			// 完成按钮
			sb.append("<div style='position:absolute;right:16px;bottom:16px;'>"
					+ "<a href='finishICA' target='_rwt' class='layui-icon layui-icon-ok' onmouseover='layer.tips(\"" + "标记ICA已完成"
					+ "\", this, {tips: 1})'></a>" //
					+ "</div>");
		}

		sb.insert(0, "<div class='brui_card_trans' style='background:#f9f9f9;height:" + (rowHeight - 2 * RenderTools.margin) + "px;margin:"
				+ RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

	public static String getICAVerifyInfo(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String[] color = new String[] { "fff", "fff" };

		Document user_meta = (Document) doc.get("user_meta");

		String title = doc.getString("title");
		String comment = doc.getString("comment");
		String _date = Formatter.getString(doc.getDate("date"), "yyyy-MM-dd HH:mm:ss");

		sb.append("<div class='brui_card_text' style='color:#fff;display:flex;align-items:center;height:36px;padding:8px 0px 0px 8px;'>" //
				+ title + "</div>");//
		rowHeight += 36;

		sb.append("<div style='height:56px'>" + RenderTools.getTextMultiLineNoBlank3("", comment, color) + "</div>");
		rowHeight += 56;

		sb.append(RenderTools.renderUserAndText(user_meta, _date, CardTheme.TEXT_LINE[0]));
		rowHeight += 36;

		String bg = title.equals("未通过验证") ? "e84e40" : "5c6bc0";
		sb.insert(0, "<div class='brui_card_trans' style='background:#" + bg + ";height:" + (rowHeight - 2 * RenderTools.margin)
				+ "px;margin:" + RenderTools.margin + "px;'>");
		sb.append("</div>");
		return sb.toString();
	}
	
	public static Document renderD4RootCauseDesc(Document doc, String lang) {
		Document charger_meta = (Document) doc.get("charger_meta");
		Date date = doc.getDate("date");
		return renderD4Card(doc, "问题产生的根本原因", doc.getString("rootCauseDesc"), charger_meta, date);
	}

	public static Document renderD4EscapePoint(Document doc, String lang) {
		Document charger_meta = (Document) doc.get("charger_meta");
		Date date = doc.getDate("date");
		return renderD4Card(doc, "问题流出的逃出点", doc.getString("escapePoint"), charger_meta, date);
	}

	private static Document renderD4Card(Document doc, String title, String rootCauseDesc, Document charger_meta, Date date) {
		String dateStr = Formatter.getString(date);

		CardTheme theme = new CardTheme(CardTheme.RED);

		StringBuffer sb = new StringBuffer();

		sb.append("<div class='label_subhead brui_card_head' style='height:36px;background:#" + theme.headBgColor + ";color:#"
				+ theme.headFgColor + ";padding:8px;'>" + title //
				+ "</div>");//

		sb.append("<div class='brui_text_multiline' style='padding:8px 8px 0px 8px;'>" + rootCauseDesc + "</div>");

		sb.append(RenderTools.renderUserAndText(charger_meta, dateStr, theme.lightText));

		sb.insert(0, "<div class='brui_card' style='margin:" + RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD4CauseConsequence(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String name = doc.getString("name");
		String desc = Optional.ofNullable(doc.getString("description")).orElse("");
		int w = doc.getInteger("weight", 1);
		String p = Formatter.getPercentageFormatString(doc.getDouble("probability"));

		sb.append("<div class='label_caption' style='display:flex;flex-direction:column;justify-content:space-around;color:white;'>");
		sb.append(
				"<div style='justify-content:center;flex-grow:1;width:44px;display:flex;flex-direction:column;align-items:center;background:#5c6bc0;margin-bottom:1px;border-radius:4px 0px 0px 0px;'>");
		sb.append("<div>" + w + "</div><div>权重</div></div>");
		sb.append(
				"<div style='justify-content:center;flex-grow:1;width:44px;display:flex;flex-direction:column;align-items:center;background:#5c6bc0;;border-radius:0px 0px 0px 4px;'>");
		sb.append("<div>" + p + "</div><div>概率</div></div>");
		sb.append("</div>");

		sb.append("<div style='width:0;flex-grow:1;padding:0px 4px;display:flex;flex-direction:column;justify-content:space-around;'>");
		sb.append("<div class='brui_text_line'>" + name + "</div><div class='brui_card_text3 label_caption' style='height:48px;'>" + desc
				+ "</div>");
		sb.append("</div>");

		rowHeight += 82;

		sb.insert(0, "<div class='brui_card_trans' style='display:flex;background:#f9f9f9;height:" + (rowHeight - 2 * RenderTools.margin)
				+ "px;margin:" + RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}
	
	public static Document renderD5PCA1(Document doc, String lang) {
		List<?> pca = (List<?>) doc.get("pca1");
		Document charger = (Document) doc.get("charger1_meta");
		Date date = doc.getDate("date1");
		return renderCard(pca, "杜绝问题产生", charger, date, lang);
	}
	
	public static Document renderD5PCA2(Document doc, String lang) {
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
			sb.append("<li style='margin-top:0px;'>" + x.getString("name") + "</li>");
		}
		sb.append("</ul>");
		sb.append("</div>");
		
		sb.append(RenderTools.renderUserAndText(charger, dateStr, theme.lightText));

		sb.insert(0, "<div class='brui_card' style='margin:" + RenderTools.margin
				+ "px;'>");
		sb.append("</div>");
		
		
		return new Document("html", sb.toString());
	}

	public static Document renderD6IVPCA(Document t) {
		// TODO Auto-generated method stub
		return null;
	}

}
