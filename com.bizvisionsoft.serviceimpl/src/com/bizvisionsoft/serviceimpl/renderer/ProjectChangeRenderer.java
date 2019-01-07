package com.bizvisionsoft.serviceimpl.renderer;

import org.bson.Document;

import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;

public class ProjectChangeRenderer {

	public static Document render(ProjectChange pc) {
		StringBuffer sb = new StringBuffer();

		CardTheme theme = new CardTheme(CardTheme.CYAN);
		sb.append("<div class='brui_card_head' style='height:48px;background:#" + theme.headBgColor + ";color:#" + theme.headFgColor
				+ ";padding:8px'><div><a class='label_subhead' href='openItem/' target='_rwt' style='color:#" + theme.headFgColor + "';>"
				+ pc.getProjectName() + "</a><div class='label_caption'>"
				+ Check.isAssignedThen(pc.getProjectNumber(), n -> "S/N: " + n).orElse("S/N: 待定") + "</div>"//
				+ "</div></div>");

		RenderTools.appendText(sb, "申请原因：", RenderTools.STYLE_1LINE);
		RenderTools.appendText(sb, pc.getReason(), RenderTools.STYLE_3LINE);

		RenderTools.appendLabelAndTextLine(sb, "申请时间：", Formatter.getString(pc.getApplicantDate()));
		RenderTools.appendIconLabelAndTextLine(sb, RenderTools.IMG_URL_USER, "申请人：", pc.warpperApplicantInfo());

		RenderTools.appendCardBg(sb);

		return new Document("_id", pc.get_id()).append("html", sb.toString());
	}

}
