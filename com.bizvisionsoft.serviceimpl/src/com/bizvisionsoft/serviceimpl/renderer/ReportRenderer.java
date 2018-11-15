package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;

import org.bson.Document;

import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;

public class ReportRenderer {

	private WorkReport report;
	private int rowHeight;
	private CardTheme theme;

	public static Document render(WorkReport report) {
		return new ReportRenderer(report).render();
	}

	public ReportRenderer(WorkReport report) {
		this.report = report;
		rowHeight = RenderTools.margin * 3;
		theme = new CardTheme(CardTheme.CYAN);
	}

	private Document render() {
		StringBuffer sb = new StringBuffer();

		sb.append(renderTitle());
		// 报告人
		sb.append(renderReporter());

		String remark = report.getWorkRemark();
		if (Check.isAssigned(remark)) {
			sb.append(renderMultiline("重要活动",remark));
		}

		remark = report.getOtherRemark();
		if (Check.isAssigned(remark)) {
			sb.append(renderMultiline("其他问题",remark));
		}
		
		sb.append(renderRightButton());

		RenderTools.renderCardBoard(sb, rowHeight);
		return new Document("_id", report.get_id()).append("html", sb.toString()).append("height", rowHeight);
	}

	private Object renderRightButton() {
		return "<div class='layui-btn layui-btn-xs layui-btn-normal' style='position:absolute;right:16px;bottom:16px;'>"
				+ "<a href='confirm' target='_rwt' class='layui-icon layui-icon-ok' style='color:#fff;'></a>"
				+ "</div>";
	}

	private String renderMultiline(String label,String text) {
		rowHeight += 72;
		return
				"<div class='brui_card_text3 label_caption' style='padding:8px 8px 0px 36px;'>" + //
				"<div style='color:#" + CardTheme.TEXT_LINE[0] + "'>" + label + "</div>" + "<div style='color:#" + CardTheme.TEXT_LINE[1]
				+ "'>" + text + "</div>"//
				+ "</div>";//
	}

	private String renderReporter() {
		rowHeight += 20 + 8;
		return RenderTools.getIconTextLine("报告人:", report.warpperReporterInfo(), RenderTools.IMG_URL_USER, CardTheme.TEXT_LINE);
	}

	private String renderTitle() {
		String type = report.getType();
		Date period = report.getPeriod();
		String number = "";
		if (WorkReport.TYPE_DAILY.equals(type)) {
			number = "日报<br>" + Formatter.getString(period, "M/d");
		} else if (WorkReport.TYPE_WEEKLY.equals(type)) {
			number = "周报<br>" + Formatter.getString(period, "M-W");
		} else if (WorkReport.TYPE_MONTHLY.equals(type)) {
			number = "月报<br>" + Formatter.getString(period, "M月");
		}

		rowHeight += 64;
		return "<div class='brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor + ";padding:8px'>" //
				+ "<div>"//
				+ "<div class='label_title'>" + report.getProjectName() + "</div>"//
				+ "<div>" + Check.isAssignedThen(report.getProjectNumber(), n -> "S/N: " + n).orElse("S/N: 待定") + "</div>"//
				+ "</div>" //
				+ "<div class='label_title' style='text-align:right;'>" + number + "</div>"//
				+ "</div>";

	}

}
