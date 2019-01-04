package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.ColorTheme;
import com.bizvisionsoft.service.tools.Formatter;

public class ProblemCardRenderer {

	private static final String[] priorityText = new String[] { "最高", "高", "中", "低", "最低" };

	private static final String[] cftRoleText = new String[] { "组长", "设计", "工艺", "生产", "质量" };

	private static final CardTheme indigo = new CardTheme(CardTheme.INDIGO);

	private static final CardTheme deepGrey = new CardTheme(CardTheme.DEEP_GREY);

	private static final CardTheme red = new CardTheme(CardTheme.RED);

	public static Document renderD1CFTMember(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		// 头像
		String name = doc.getString("name");
		String role = cftRoleText[Integer.parseInt(doc.getString("role"))];
		String mobile = Optional.ofNullable(doc.getString("mobile")).map(e -> "<a href='tel:" + e + "'>" + e + "</a>").orElse("");
		String position = Optional.ofNullable(doc.getString("position")).orElse("");
		String email = Optional.ofNullable(doc.getString("email")).map(e -> "<a href='mailto:" + e + "'>" + e + "</a>").orElse("");
		String dept = Optional.ofNullable(doc.getString("dept")).orElse("");
		String img = getUserHeadPicURL(doc, name);

		appendHeader(sb, indigo, "<div class='label_subhead'><div>" + role + "</div><div class='label_body1'>" + name + " " + dept + " "
				+ position + "</div></div>" + img, 48);

		appendIconTextLine(sb, RenderTools.IMG_URL_TEL, 16, mobile);

		appendIconTextLine(sb, RenderTools.IMG_URL_EMAIL, 16, email);

		appendButton(sb, "layui-icon-close", 12, 12, "删除团队成员", "delete");

		appendCardBg(sb, "white");

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD25W2H(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		appendHeader(sb, red, "5W2H", 36);

		appendLabelAndMultiLine(sb, "What / 当前状况", "deep_orange", Optional.ofNullable(doc.getString("what")).orElse(""),
				CardTheme.TEXT_LINE);

		appendLabelAndMultiLine(sb, "When / 发现时间", "deep_orange", Optional.ofNullable(doc.getString("when")).orElse(""),
				CardTheme.TEXT_LINE);

		appendLabelAndMultiLine(sb, "Where / 地点和位置", "deep_orange", Optional.ofNullable(doc.getString("where")).orElse(""),
				CardTheme.TEXT_LINE);

		appendLabelAndMultiLine(sb, "Who / 有关人员", "deep_orange", Optional.ofNullable(doc.getString("who")).orElse(""), CardTheme.TEXT_LINE);

		appendLabelAndMultiLine(sb, "Why / 原因推测", "deep_orange", Optional.ofNullable(doc.getString("why")).orElse(""), CardTheme.TEXT_LINE);

		appendLabelAndMultiLine(sb, "How / 怎样发现的问题", "deep_orange", Optional.ofNullable(doc.getString("how")).orElse(""),
				CardTheme.TEXT_LINE);

		appendLabelAndMultiLine(sb, "How many / 频度，数量", "deep_orange", Optional.ofNullable(doc.getString("howmany")).orElse(""),
				CardTheme.TEXT_LINE);

		appendButton(sb, "layui-icon-edit", 12, 12, "编辑问题描述", "editpd");

		appendCardBg(sb, "white");

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD2PhotoCard(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		sb.append("<img src='" + RenderTools.getFirstImageURL(doc, "problemImg")
				+ "' style='cursor:pointer;width:100%;height:auto;border-radius:4px 4px 0px 0px;' onclick='$.getJSON(\"bvs/imgf?c=d2ProblemPhoto&i="
				+ doc.get("_id") + "&f=problemImg\", function(json){layer.photos({photos: json});});'" + "/>");

		appendFix3Line(sb, doc.getString("problemImgDesc"));

		appendFix1Line(sb,
				Formatter.getString(doc.getDate("receiveDate")) + "/" + doc.getString("receiver") + " " + doc.getString("location"));

		appendButton(sb, "layui-icon-close", 12, 12, "删除图片资料", "deletephoto");

		appendCardBg(sb, "white");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", 240);
	}

	public static Document renderD3ICA(Document doc, String lang) {

		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String action = doc.getString("action");
		String objective = doc.getString("objective");
		String priority = priorityText[Integer.parseInt(doc.getString("priority"))];
		String planStart = Formatter.getString(doc.get("planStart"));
		String planFinish = Formatter.getString(doc.get("planFinish"));
		String budget = doc.getString("budget");
		Document chargerData = (Document) doc.get("charger_meta");

		Document verification = (Document) doc.get("verification");

		boolean finished = doc.getBoolean("finish", false);
		String status;
		CardTheme theme = indigo;
		if (finished) {
			status = "<span class='layui-badge  layui-bg-green'>" + "已完成" + "</span>";
			theme = deepGrey;
		} else if (verification != null) {
			String style;
			status = verification.getString("title");
			if ("已验证".equals(status)) {
				style = "layui-badge  layui-bg-green";
				// theme = new CardTheme(CardTheme.TEAL);
			} else {
				style = "layui-badge";
				// theme = new CardTheme(CardTheme.RED);
			}
			String title = verification.getString("title");
			String comment = verification.getString("comment");
			String _date = Formatter.getString(verification.getDate("date"));
			Document user_meta = (Document) verification.get("user_meta");
			String name = user_meta.getString("name");
			String verifyInfo = comment + "<br>" + name + " " + _date;

			status = "<span class='" + style + "' style='cursor:pointer;' onclick='layer.alert(\"" + verifyInfo
					+ "\", {\"skin\": \"layui-layer-lan\",title:\"" + title + "\"})'>" + status + "</span>";
		} else {
			status = "<span class='layui-badge  layui-bg-blue'>" + "已创建" + "</span>";
		}

		sb.append("<div class='brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor + ";padding:8px;'>"
				+ "<div class='label_subhead brui_card_text'>" + action + "</div>"//
				+ "<div style='text-align:center;margin-left:8px'><div class='label_headline'>" + priority + "</div>"
				+ "<div class='label_caption'>优先级</div></div>"//
				+ "</div>");//

		rowHeight += 64;

		appendFix1Line(sb, "预期结果：", CardTheme.TEXT_LINE[0], objective, CardTheme.TEXT_LINE[1]);
		rowHeight += 24;

		appendFix1Line(sb, "执行计划：", CardTheme.TEXT_LINE[0], planStart + " ~ " + planFinish, CardTheme.TEXT_LINE[1]);
		rowHeight += 24;

		appendFix1Line(sb, "费用预算：", CardTheme.TEXT_LINE[0], budget, CardTheme.TEXT_LINE[1]);
		rowHeight += 24;

		appendUserAndText(sb, chargerData, status);
		rowHeight += 36;

		if (!finished) {
			// 删除按钮
			appendButton(sb, "layui-icon-close", 12 + 16 + 8 + 16 + 8 + 16 + 8, 12, "删除ICA", "deleteICA");

			// 编辑按钮
			appendButton(sb, "layui-icon-edit", 12 + 16 + 8 + 16 + 8, 12, "编辑ICA", "editICA");

			// 验证按钮
			appendButton(sb, "layui-icon-survey", 12 + 16 + 8, 12, "验证ICA", "verificationICA");

			// 完成按钮
			appendButton(sb, "layui-icon-ok", 12, 12, "标记ICA已完成", "finishICA");

		}

		appendCardBg(sb, "white");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

	public static Document renderD4RootCauseDesc(Document doc, String lang) {
		return renderD4Card(doc, "问题产生的根本原因", doc.getString("rootCauseDesc"), (Document) doc.get("charger_meta"), doc.getDate("date"));
	}

	public static Document renderD4EscapePoint(Document doc, String lang) {
		return renderD4Card(doc, "问题流出的逃出点", doc.getString("escapePoint"), (Document) doc.get("charger_meta"), doc.getDate("date"));
	}

	private static Document renderD4Card(Document doc, String title, String rootCauseDesc, Document charger_meta, Date date) {

		StringBuffer sb = new StringBuffer();

		appendHeader(sb, red, title, 36);

		appendMultiLine(sb, rootCauseDesc);

		appendUserAndText(sb, charger_meta, Formatter.getString(date));

		appendCardBg(sb, "white");

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
		sb.append("<div class='brui_text_line label_caption'>" + name
				+ "</div><div class='brui_card_text3 label_caption' style='height:48px;'>" + desc + "</div>");
		sb.append("</div>");

		rowHeight += 82;

		sb.insert(0, "<div class='brui_card_trans' style='display:flex;background:#f9f9f9;height:" + (rowHeight - 2 * RenderTools.margin)
				+ "px;margin:" + RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

	public static Document renderD5PCA1(Document doc, String lang) {
		return renderD5Card((List<?>) doc.get("pca1"), "杜绝问题产生", (Document) doc.get("charger1_meta"), doc.getDate("date1"), lang);
	}

	public static Document renderD5PCA2(Document doc, String lang) {
		return renderD5Card((List<?>) doc.get("pca2"), "防止问题流出", (Document) doc.get("charger2_meta"), doc.getDate("date2"), lang);
	}

	private static Document renderD5Card(List<?> list, String title, Document charger, Date date, String lang) {
		String dateStr = Formatter.getString(date);

		StringBuffer sb = new StringBuffer();

		appendHeader(sb, indigo, title, 36);

		sb.append("<div class='layui-text'>");
		sb.append("<ul style='margin-left:12px;padding:8px 8px 0px 16px;'>");
		for (int i = 0; i < list.size(); i++) {
			sb.append("<li class='label_caption' style='margin-top:0px;'>" + ((Document) list.get(i)).getString("name") + "</li>");
		}
		sb.append("</ul>");
		sb.append("</div>");

		appendUserAndText(sb, charger, dateStr);

		appendCardBg(sb, "white");

		return new Document("html", sb.toString());
	}

	public static Document renderD6IVPCA(Document t) {
		// TODO Auto-generated method stub
		return null;
	}

	private static void appendButton(StringBuffer sb, String icon, int right, int bottom, String tips, String target) {
		sb.append("<div style='position:absolute;right:" + right + "px;bottom:" + bottom + "px;'>" + "<a href='" + target
				+ "' target='_rwt' class='layui-icon " + icon + "' onmouseover='layer.tips(\"" + tips + "\",this,{tips:1})'></a></div>");
	}

	public static void appendLabelAndMultiLine(StringBuffer sb, String label, String labelStyle, String text, String[] color) {
		sb.append("<div class='label_caption' style='padding:8px 8px 0px 8px;'>" + //
				"<div class='" + labelStyle + "' style='color:#" + color[0] + "'>" + label + "</div>"
				+ "<div class='brui_text_multiline' style='color:#" + color[1] + "'>" + text + "</div>"//
				+ "</div>");//
	}

	private static void appendHeader(StringBuffer sb, CardTheme theme, String text, int height) {
		sb.append("<div class='label_subhead brui_card_head' style='height:" + height + "px;background:#" + theme.headBgColor + ";color:#"
				+ theme.headFgColor + ";padding:8px;'>" + text//
				+ "</div>");//
	}

	private static void appendIconTextLine(StringBuffer sb, String iconURL, int size, String text) {
		sb.append("<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'>"//
				+ "<img src='" + iconURL + "' width='" + size + "px' height='" + size + "px'>"//
				+ "<div class='label_caption brui_text_line' style='margin-left:8px;'>" //
				+ text //
				+ "</div>"//
				+ "</div>");
	}

	private static String getUserHeadPicURL(Document doc, String name) {
		String img;
		String url = RenderTools.getFirstImageURL(doc, "headPics");
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

	public static void appendUserAndText(StringBuffer sb, Document user, String dateStr) {
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
		sb.append("<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'>" + img
				+ "<span class='label_caption' style='margin-left:4px;'>" + name + "&nbsp;&nbsp;" + dateStr + "</span>" //
				+ "</div>");
	}

	private static void appendCardBg(StringBuffer sb, String bg) {
		sb.insert(0, "<div class='brui_card2' style='background:" + bg + ";'>");
		sb.append("</div>");
	}

	private static void appendFix3Line(StringBuffer sb, String text) {
		if (text != null) {
			sb.append("<div class='brui_card_text3 label_caption' style='padding:8px 8px 0px 8px;'>" + text + "</div>");//
		}
	}

	private static void appendMultiLine(StringBuffer sb, String text) {
		if (text != null) {
			sb.append("<div class='brui_text_multiline label_caption' style='padding:8px 8px 0px 8px;'>" + text + "</div>");//
		}
	}

	private static void appendFix1Line(StringBuffer sb, String text) {
		if (text != null) {
			sb.append("<div class='brui_text_line label_caption' style='padding:8px 8px 0px 8px;'>" + text + "</div>");//
		}
	}

	private static void appendFix1Line(StringBuffer sb, String label, String color1, String text, String color2) {
		sb.append("<div style='padding:8px 8px 0px 0px;display:flex;align-items:center;'>"//
				+ "<div class='label_caption brui_text_line' style='margin-left:8px;width:100%;display:inline-flex;'>" //
				+ "<span style='color:#" + color1 + "'>" + label + "</span>" //
				+ "<span style='color:#" + color2 + "'>" + text + "</span>" //
				+ "</div>"//
				+ "</div>");
	}
}
