package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;

public class ProblemCardRenderer {

	private static final String[] priorityText = new String[] { "最高", "高", "中", "低", "最低" };

	private static final String[] similarDegreeText = new String[] { "相同", "近似", "类似", "不同" };

	public static final String[] cftRoleText = new String[] { "组长", "设计", "工艺", "生产", "质量" };

	private static final CardTheme indigo = new CardTheme(CardTheme.INDIGO);

	private static final CardTheme deepGrey = new CardTheme(CardTheme.DEEP_GREY);

	private static final CardTheme red = new CardTheme(CardTheme.RED);

	public static Document renderD0ERA(Document doc, String lang) {
		Document charger = (Document) doc.get("charger_meta");
		Date planStart = doc.getDate("planStart");
		Date planFinish = doc.getDate("planFinish");
		Date actualStart = doc.getDate("actualStart");
		Date actualFinish = doc.getDate("actualFinish");
		
		String title = doc.getString("name");
		List<?> list = (List<?>) doc.get("era");
		StringBuffer sb = new StringBuffer();
		
		renderListItemsCard(sb, list, title, charger, planStart,planFinish,actualStart,actualFinish, red);

		RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8, 12, "编辑紧急应变措施", "editERA");

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "删除紧急应变措施", "deleteERA");

		return new Document("html", sb.toString()).append("_id", doc.get("_id"));
	}

	public static Document renderD1CFTMember(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		// 头像
		String name = doc.getString("name");
		String role = cftRoleText[Integer.parseInt(doc.getString("role"))];
		String mobile = Optional.ofNullable(doc.getString("mobile")).map(e -> "<a href='tel:" + e + "'>" + e + "</a>").orElse("");
		String position = Optional.ofNullable(doc.getString("position")).orElse("");
		String email = Optional.ofNullable(doc.getString("email")).map(e -> "<a href='mailto:" + e + "'>" + e + "</a>").orElse("");
		String dept = Optional.ofNullable(doc.getString("dept")).orElse("");
		String img = RenderTools.getUserHeadPicURL(doc, name);

		RenderTools.appendHeader(sb, indigo, "<div class='label_subhead'><div>" + role + "</div><div class='label_body1'>" + name + " "
				+ dept + " " + position + "</div></div>" + img, 48);

		RenderTools.appendIconTextLine(sb, RenderTools.IMG_URL_TEL, 16, mobile);

		RenderTools.appendIconTextLine(sb, RenderTools.IMG_URL_EMAIL, 16, email);

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "删除团队成员", "delete");

		RenderTools.appendCardBg(sb);

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD25W2H(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		RenderTools.appendHeader(sb, red, "5W2H", 36);

		RenderTools.appendLabelAndMultiLine(sb, "What / 当前状况", "deep_orange", Optional.ofNullable(doc.getString("what")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "When / 发现时间", "deep_orange", Optional.ofNullable(doc.getString("when")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "Where / 地点和位置", "deep_orange", Optional.ofNullable(doc.getString("where")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "Who / 有关人员", "deep_orange", Optional.ofNullable(doc.getString("who")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "Why / 原因推测", "deep_orange", Optional.ofNullable(doc.getString("why")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "How / 怎样发现的问题", "deep_orange", Optional.ofNullable(doc.getString("how")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendLabelAndMultiLine(sb, "How many / 频度，数量", "deep_orange", Optional.ofNullable(doc.getString("howmany")).orElse(""),
				CardTheme.TEXT_LINE);

		RenderTools.appendButton(sb, "layui-icon-edit", 12, 12, "编辑问题描述", "editpd");

		RenderTools.appendCardBg(sb);

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD2PhotoCard(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();

		sb.append("<img src='" + RenderTools.getFirstFileURL(doc, "problemImg")
				+ "' style='cursor:pointer;width:100%;height:auto;border-radius:4px 4px 0px 0px;' onclick='$.getJSON(\"bvs/imgf?c=d2ProblemPhoto&i="
				+ doc.get("_id") + "&f=problemImg\", function(json){layer.photos({photos: json});});'" + "/>");

		RenderTools.appendText(sb, doc.getString("problemImgDesc"), RenderTools.STYLE_3LINE);

		RenderTools.appendText(sb,
				Formatter.getString(doc.getDate("receiveDate")) + "/" + doc.getString("receiver") + " " + doc.getString("location"),
				RenderTools.STYLE_1LINE);

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "删除图片资料", "deletephoto");

		RenderTools.appendCardBg(sb);

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

		sb.append("<div class='brui_card_head' style='height:48px;background:#" + theme.headBgColor + ";color:#" + theme.headFgColor
				+ ";padding:8px;'>" + "<div class='label_subhead brui_card_text'>" + action + "</div>"//
				+ "<div style='text-align:center;margin-left:8px'><div class='label_title'>" + priority + "</div>"
				+ "<div class='label_caption'>优先级</div></div>"//
				+ "</div>");//

		rowHeight += 64;

		RenderTools.appendLabelAndTextLine(sb, "预期结果：", objective);
		rowHeight += 24;

		RenderTools.appendLabelAndTextLine(sb, "执行计划：", planStart + " ~ " + planFinish);
		rowHeight += 24;

		RenderTools.appendLabelAndTextLine(sb, "费用预算：", budget);
		rowHeight += 24;

		RenderTools.appendUserAndText(sb, chargerData, status);
		rowHeight += 36;

		if (!finished) {
			// 删除按钮
			RenderTools.appendButton(sb, "layui-icon-ok", 12 + 16 + 8 + 16 + 8 + 16 + 8, 12, "标记ICA已完成", "finishICA");

			// 编辑按钮
			RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8 + 16 + 8, 12, "编辑ICA", "editICA");

			// 验证按钮
			RenderTools.appendButton(sb, "layui-icon-survey", 12 + 16 + 8, 12, "验证ICA", "verificationICA");

			// 完成按钮
			RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "删除ICA", "deleteICA");
		}

		RenderTools.appendCardBg(sb);

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

	public static Document renderD4(Document doc, String title, String rootCauseDesc, Document charger_meta, Date date, String lang) {

		StringBuffer sb = new StringBuffer();

		RenderTools.appendHeader(sb, red, title, 36);

		RenderTools.appendText(sb, rootCauseDesc, RenderTools.STYLE_NLINE);

		RenderTools.appendUserAndText(sb, charger_meta, Formatter.getString(date));

		RenderTools.appendCardBg(sb);

		return new Document("_id", doc.get("_id")).append("html", sb.toString());
	}

	public static Document renderD4CauseConsequence(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String name = doc.getString("name");
		String desc = Optional.ofNullable(doc.getString("description")).orElse("");
		int w = doc.getInteger("weight", 1);
		String p = Formatter.getPercentageFormatString(doc.getDouble("probability") / 100);

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

	public static Document renderD5PCA(List<?> list, String title, Document charger, Date date, String lang) {
		StringBuffer sb = new StringBuffer();
		renderListItemsCard2(sb, list, title, charger, date, indigo);
		return new Document("html", sb.toString());
	}

	private static void renderListItemsCard2(StringBuffer sb, List<?> list, String title, Document charger, Date date, CardTheme theme) {
		RenderTools.appendHeader(sb, theme, title, 36);
		sb.append("<div class='layui-text'>");
		sb.append("<ul style='margin-left:12px;padding:8px 8px 0px 16px;'>");
		for (int i = 0; i < list.size(); i++) {
			sb.append("<li class='label_caption' style='margin-top:0px;'>" + ((Document) list.get(i)).getString("name") + "</li>");
		}
		sb.append("</ul>");
		sb.append("</div>");

		RenderTools.appendUserAndText(sb, charger, Formatter.getString(date));

		RenderTools.appendCardBg(sb);
	}

	private static void renderListItemsCard(StringBuffer sb, List<?> list, String title, Document charger, Date planStart, Date planFinish,
			Date actualStart, Date actualFinish, CardTheme theme) {
		RenderTools.appendHeader(sb, theme, title, 36);

		RenderTools.appendSchedule(sb, planStart, planFinish, actualStart, actualFinish);
		
		sb.append("<div class='layui-text'>");
		sb.append("<ul style='margin-left:12px;padding:8px 8px 0px 16px;'>");
		for (int i = 0; i < list.size(); i++) {
			sb.append("<li class='label_caption' style='margin-top:0px;'>" + ((Document) list.get(i)).getString("name") + "</li>");
		}
		sb.append("</ul>");
		sb.append("</div>");

		
		RenderTools.appendUserAndText(sb, charger, null);

		RenderTools.appendCardBg(sb);
	}

	public static Document renderD6IVPCA(Document t, String lang) {

		StringBuffer sb = new StringBuffer();

		String title;
		if ("make".equals(t.getString("actionType"))) {
			title = "实施/确认问题产生的纠正措施";
		} else {
			title = "实施/确认问题流出的纠正措施";
		}
		RenderTools.appendHeader(sb, indigo, title, 36);

		RenderTools.appendText(sb, t.getString("iv"), RenderTools.STYLE_NLINE);

		List<?> list = (List<?>) t.get("attachments");
		if (Check.isAssigned(list)) {
			RenderTools.appendText(sb, "附件", RenderTools.STYLE_1LINE);
			RenderTools.appendMultiFiles(sb, list);
		}

		Date date = t.getDate("date");
		boolean closed = t.getBoolean("closed", false);
		String status = closed ? ("<span class='layui-badge  layui-bg-blue'>" + "已确认" + "</span>")
				: ("<span class='layui-badge'>" + "未确认" + "</span>");
		RenderTools.appendUserAndText(sb, (Document) t.get("charger_meta"), Formatter.getString(date) + " " + status);

		if (!closed) {
			RenderTools.appendButton(sb, "layui-icon-ok", 12 + 16 + 8 + 16 + 8, 12, "确认PCA", "closePCA");

			RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8, 12, "编辑PCA实施和验证记录", "editPCA");

			RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "删除PCA实施和确认记录", "deletePCA");
		}

		RenderTools.appendCardBg(sb);
		return new Document("html", sb.toString()).append("_id", t.get("_id"));
	}

	public static Document renderD7Similar(Document t, String lang) {
		StringBuffer sb = new StringBuffer();

		String label = similarDegreeText[Integer.parseInt(t.getString("degree"))];

		String type = t.getString("similar");

		int prob = t.getDouble("prob").intValue();

		sb.append("<div class='brui_card_head' style='height:48px;background:#" + indigo.headBgColor + ";color:#" + indigo.headFgColor
				+ ";padding:8px;'>" + "<div class='label_subhead brui_card_text' style='flex-grow:1;'>相似情形：" + type + "</div>"//
				+ "<div style='text-align:center;margin-left:8px'><div class='label_title'>" + label + "</div>"
				+ "<div class='label_caption'>相似度</div></div>"//
				+ "<div style='text-align:center;margin-left:8px'><div class='label_title'>" + prob
				+ "<span style='font-size:9px;'>%</span></div>" + "<div class='label_caption'>可能性</div></div>"//
				+ "</div>");//

		RenderTools.appendText(sb, t.getString("desc"), RenderTools.STYLE_NLINE);

		List<?> ids = (List<?>) t.get("id");
		if (Check.isAssigned(ids)) {
			RenderTools.appendText(sb, "识别相似情形：", RenderTools.STYLE_1LINE);
			sb.append("<div class='layui-text'>");
			sb.append("<ul style='margin-left:12px;padding:8px 8px 0px 16px;'>");
			for (int i = 0; i < ids.size(); i++) {
				Document d = (Document) ids.get(i);
				String text = Optional.ofNullable(d.getString("id")).orElse("") + " "
						+ Optional.ofNullable(d.getString("keyword")).orElse("");
				sb.append("<li class='label_caption grey' style='margin-top:0px;'>" + text + "</li>");
			}
			sb.append("</ul>");
			sb.append("</div>");
		}

		RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8, 12, "编辑相似物", "editSimilar");

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "删除相似物", "deleteSimilar");

		RenderTools.appendCardBg(sb);

		return new Document("html", sb.toString()).append("_id", t.get("_id")).append("type", "similar");
	}

	public static Document renderD7SPA(Document t, String lang) {
		StringBuffer sb = new StringBuffer();
		RenderTools.appendHeader(sb, indigo, "系统性预防措施", 36);

		RenderTools.appendText(sb, t.getString("action"), RenderTools.STYLE_NLINE);

		Document charger = (Document) t.get("charger_meta");

		Date date = t.getDate("date");

		boolean finished = t.getBoolean("finish", false);
		String status = finished ? ("<span class='layui-badge  layui-bg-blue'>" + "已完成" + "</span>")
				: ("<span class='layui-badge'>" + "未完成" + "</span>");

		RenderTools.appendUserAndText(sb, charger, Formatter.getString(date) + " " + status);

		if (!finished) {
			RenderTools.appendButton(sb, "layui-icon-ok", 12 + 16 + 8 + 16 + 8, 12, "关闭PCA", "finishSPA");

			RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8, 12, "编辑系统性预防措施", "editSPA");

			RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "删除系统性预防措施", "deleteSPA");
		} else {
			RenderTools.appendButton(sb, "layui-icon-right", 12, 12, "查看系统性预防措施", "readSPA");
		}

		RenderTools.appendCardBg(sb);

		return new Document("html", sb.toString()).append("_id", t.get("_id"));
	}

	public static Document renderD8Exp(Document t, String lang) {
		StringBuffer sb = new StringBuffer();

		String firstFileURL = RenderTools.getFirstFileURL(t, "vedio");
		if (firstFileURL != null) {
			sb.append("<video style='border-radius:4px 4px 0px 0px;' width='100%' height='auto' controls>");
			sb.append("<source src='" + firstFileURL + "' type='video/mp4'>");
			sb.append("</video>");
		} else {
			RenderTools.appendHeader(sb, indigo, "经验教训总结", 36);
		}

		RenderTools.appendText(sb, t.getString("name"), RenderTools.STYLE_1LINE);

		RenderTools.appendText(sb, t.getString("abstract"), RenderTools.STYLE_3LINE);

		Document charger = (Document) t.get("charger_meta");
		Date date = t.getDate("date");
		RenderTools.appendUserAndText(sb, charger, Formatter.getString(date));

		RenderTools.appendButton(sb, "layui-icon-edit", 12 + 16 + 8, 12, "编辑经验总结", "editExp");

		RenderTools.appendButton(sb, "layui-icon-close", 12, 12, "删除经验总结", "deleteExp");

		RenderTools.appendCardBg(sb);
		return new Document("html", sb.toString()).append("_id", t.get("_id"));
	}

}
