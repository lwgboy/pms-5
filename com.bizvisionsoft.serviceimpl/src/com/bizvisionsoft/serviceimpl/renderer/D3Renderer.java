package com.bizvisionsoft.serviceimpl.renderer;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.ColorTheme;
import com.bizvisionsoft.service.tools.Formatter;

public class D3Renderer {

	public static Document renderICA(Document doc, String lang) {
		CardTheme theme = new CardTheme(CardTheme.INDIGO);

		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String action = doc.getString("action");
		String objective = doc.getString("objective");
		String priority = new String[] { "最高", "高", "中", "低", "最低" }[Integer.parseInt(doc.getString("priority"))];
		String planStart = Formatter.getString(doc.get("planStart"), "yyyy/MM/dd HH:mm:ss");
		String planFinish = Formatter.getString(doc.get("planFinish"), "yyyy/MM/dd HH:mm:ss");
		String budget = doc.getString("budget");
		Document chargerData = (Document) doc.get("charger_meta");

		Document verification = (Document) doc.get("verification");

		String status = doc.getBoolean("finish", false) ? "已完成" : ((verification != null) ? verification.getString("title") : "已创建");

		ColorTheme.BruiColor bg;
		if (verification != null) {
			((Document) verification).getString("title");
			if ("未通过验证".equals(status)) {
				bg = ColorTheme.BruiColor.Red_400;
			} else if ("已验证".equals(status)) {
				bg = ColorTheme.BruiColor.Teal;
			} else {
				bg = ColorTheme.BruiColor.Indigo;
			}
		} else {
			bg = ColorTheme.BruiColor.Indigo;
		}

		String[] color = new String[] { "000000", "757575" };

		sb.append("<div class='brui_card_head' style='background:" + bg + ";color:#" + theme.headFgColor + ";padding:8px;'>"
				+ "<div class='brui_card_text'>" + action + "</div>"//
				+ "<div style='text-align:center;margin-left:8px'><div class='label_headline'>" + priority + "</div>"
				+ "<div class='label_caption'>优先级</div></div>"//
				+ "</div>");//

		rowHeight += 64;

		sb.append(RenderTools.getTextLineNoBlank("预期结果", objective, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.getTextLineNoBlank("计划开始", planStart, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.getTextLineNoBlank("计划完成", planFinish, CardTheme.TEXT_LINE));
		rowHeight += 24;

		sb.append(RenderTools.getTextLineNoBlank("费用预算", budget, CardTheme.TEXT_LINE));
		rowHeight += 24;

		String url = RenderTools.getFirstImageURL(chargerData, "headPics");
		String name = chargerData.getString("name");
		if (url != null) {
			sb.append("<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'>"//
					+ "<img src='" + url + "' style='border-radius:17px;' width='28' height='28'/>"//
					+ "<span class='label_caption' style='margin-left:4px;color:#" + color[1] + "'>" + name + "</span>" //
					+ "&nbsp;&nbsp;"//
					+ "<span class='layui-badge layui-bg-blue layui-btn-fluid' style='width: 80px;'>" + status + "</span>"//
					+ "</div>"//
					+ "</div>");
			rowHeight += 36;
		} else {
			sb.append("<div style='padding:8px 8px 0px 0px;display:flex;align-items:center;'>"//
					+ "<div class='label_caption brui_text_line' style='margin-left:8px;width:100%;display:inline-flex;'>" //
					+ "<span style='color:#" + color[0] + "'>" + "行动负责" + "</span>" //
					+ "&nbsp;:&nbsp;"//
					+ "<span style='color:#" + color[1] + "'>" + name + "</span>" //
					+ "&nbsp;&nbsp;"//
					+ "<span class='layui-badge layui-bg-blue layui-btn-fluid' style='width: 80px;'>" + status + "</span>"//
					+ "</div>"//
					+ "</div>");
			rowHeight += 24;
		}
		// sb.append("<div style='position:absolute;right:16px;top:80px;'>"
		// + "</div>");

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

		sb.insert(0, "<div class='brui_card_trans' style='background:#f9f9f9;height:" + (rowHeight - 2 * RenderTools.margin) + "px;margin:"
				+ RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

	public static Document renderICAVerified(Document doc, Object d3ica_id, String lang) {
		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String[] color = new String[] { "fff", "fff" };

		Document user_meta = (Document) doc.get("user_meta");
		String userName = user_meta.getString("name");

		String title = doc.getString("title");
		String comment = doc.getString("comment");
		String _date = Formatter.getString(doc.getDate("date"), "yyyy/MM/dd HH:mm:ss");

		sb.append("<div class='brui_card_text' style='color:#fff;display:flex;align-items:center;height:36px;padding:8px 0px 0px 8px;'>" //
				+ title + "</div>");//
		rowHeight += 36;

		sb.append("<div style='height:56px'>" + RenderTools.getTextMultiLineNoBlank3("", comment, color) + "</div>");
		rowHeight += 56;

		String url = RenderTools.getFirstImageURL(user_meta, "headPics");
		if (url != null) {
			sb.append("<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'>"//
					+ "<img src='" + url + "' style='border-radius:17px;' width='28' height='28'/>"//
					+ "<span class='label_caption' style='margin-left:4px;color:#" + color[1] + "'>" + userName + "</span>" //
					+ "<span class='label_caption' style='margin-left:4px;color:#" + color[0] + "'>" + _date + "</span>"//
					+ "</div>"//
					+ "</div>");
			rowHeight += 36;
		} else {
			sb.append("<div style='padding:8px 8px 0px 0px;display:flex;align-items:center;'>"//
					+ "<div class='label_caption brui_text_line' style='margin-left:8px;width:100%;display:inline-flex;'>" //
					+ "<span style='color:#" + color[0] + "'>验证人</span>" //
					+ "&nbsp;:&nbsp;"//
					+ "<span style='color:#" + color[1] + "'>" + userName + "</span>" //
					+ "<span class='label_caption' style='margin-left:4px;color:#" + color[1] + "'>" + _date + "</span>"//
					+ "</div>"//
					+ "</div>");
			rowHeight += 24;
		}

		// 删除按钮
		sb.append("<div style='position:absolute;right:16px;bottom:16px;'>"// 8+16+16
				+ "<a href='deleteVerified' target='_rwt' class='layui-icon layui-icon-close' style='color:white;' onmouseover='layer.tips(\"" + "删除ICA验证记录"
				+ "\",this,{tips:1})'></a>" //
				+ "</div>");
		ColorTheme.BruiColor bg = title.equals("未通过验证") ? ColorTheme.BruiColor.Red_400 : ColorTheme.BruiColor.Teal;
		sb.insert(0, "<div class='brui_card_trans' style='background:" + bg + ";height:" + (rowHeight - 2 * RenderTools.margin)
				+ "px;margin:" + RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", d3ica_id).append("html", sb.toString()).append("height", rowHeight);
	}

}
