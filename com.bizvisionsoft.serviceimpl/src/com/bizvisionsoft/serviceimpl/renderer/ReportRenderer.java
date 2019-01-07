package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;

import org.bson.Document;

import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;

public class ReportRenderer {

	public static Document render(WorkReport report) {
		CardTheme theme = new CardTheme(CardTheme.TEAL);
		StringBuffer sb = new StringBuffer();

		String type = report.getType();
		Date period = report.getPeriod();
		String number = "";
		if (WorkReport.TYPE_DAILY.equals(type)) {
			number = Formatter.getString(period, "M/d�ձ�");
		} else if (WorkReport.TYPE_WEEKLY.equals(type)) {
			number = Formatter.getString(period, "M-W�ܱ�");
		} else if (WorkReport.TYPE_MONTHLY.equals(type)) {
			number = Formatter.getString(period, "M�±�");
		}

		sb.append("<div class='brui_card_head' style='height:48px;background:#" + theme.headBgColor + ";color:#" + theme.headFgColor
				+ ";padding:8px'><div><a class='label_subhead' href='openItem/' target='_rwt' style='color:#" + theme.headFgColor + "';>"
				+ report.getProjectName() + "</a><div class='label_caption'>"
				+ Check.isAssignedThen(report.getProjectNumber(), n -> "S/N: " + n).orElse("S/N: ����") + "</div>"//
				+ "</div>"
				+ "<div class='label_title'>" + number + "</div>"//
				+ "</div>");
		
		String remark = report.getWorkRemark();
		if (Check.isAssigned(remark)) {
			RenderTools.appendText(sb, "��Ҫ���", RenderTools.STYLE_1LINE);
			RenderTools.appendText(sb, remark, RenderTools.STYLE_3LINE);
		}

		remark = report.getOtherRemark();
		if (Check.isAssigned(remark)) {
			RenderTools.appendText(sb, "�������⣺", RenderTools.STYLE_1LINE);
			RenderTools.appendText(sb, remark, RenderTools.STYLE_3LINE);
		}

		RenderTools.appendIconLabelAndTextLine(sb, RenderTools.IMG_URL_USER, "�����ˣ�", report.warpperReporterInfo());

		
		RenderTools.appendButton(sb, "layui-icon-ok", 12, 12, "ȷ�ϱ���", "confirm");
		
		RenderTools.appendCardBg(sb);
		
		return new Document("_id", report.get_id()).append("html", sb.toString());
	}



}
