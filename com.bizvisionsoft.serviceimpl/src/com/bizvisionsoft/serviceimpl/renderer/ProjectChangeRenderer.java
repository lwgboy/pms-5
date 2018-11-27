package com.bizvisionsoft.serviceimpl.renderer;

import org.bson.Document;

import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;

public class ProjectChangeRenderer {

	public static Document render(ProjectChange pc) {
		return new ProjectChangeRenderer(pc).render();
	}

	private ProjectChange pc;

	private CardTheme theme;

	private int rowHeight;

	public ProjectChangeRenderer(ProjectChange pc) {
		this.pc = pc;
		theme = new CardTheme(CardTheme.CYAN);
		rowHeight = 3 * RenderTools.margin;
	}

	private Document render() {
		StringBuffer sb = new StringBuffer();

		sb.append(renderTitle());
		// 申请人
		sb.append(renderApplicant());

		sb.append(renderRightButton());

		// 申请原因
		sb.append(renderReason());

		RenderTools.renderCardBoard(sb, rowHeight);

		return new Document("_id", pc.get_id()).append("html", sb.toString()).append("height", rowHeight);
	}

	private String renderRightButton() {
		rowHeight += 40;
		return "<div class='layui-btn layui-btn-xs layui-btn-normal' style='position:absolute;right:58px;bottom:16px;'>"//
				// 通过
				+ "<a href='pass' target='_rwt' class='layui-icon layui-icon-ok' style='color:#fff;'></a>" //
				+ "</div>"//
				+ "<div class='layui-btn layui-btn-xs layui-btn-danger' style='position:absolute;right:16px;bottom:16px;'>"//
				// TODO 缺少拒绝图标
				+ "<a href='cancel' target='_rwt' class='layui-icon layui-icon-close' style='color:#fff;'></a>" //
				+ "</div>";
	}

	private String renderReason() {
		rowHeight += 72;
		return RenderTools.getTextMultiLine("申请原因", pc.getReason(), CardTheme.TEXT_LINE);
	}

	private String renderApplicant() {
		rowHeight += 20 + 8;
		return RenderTools.getIconTextLine("申请人", pc.warpperApplicantInfo(), RenderTools.IMG_URL_USER, CardTheme.TEXT_LINE);
	}

	private String renderTitle() {
		rowHeight += 64;
		return "<div class='brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor + ";padding:8px'>" //
				+ "<div>"//
				+ "<div class='label_title'>" + pc.getProjectName() + "</div>"//
				+ "<div>" + Check.isAssignedThen(pc.getProjectNumber(), n -> "S/N: " + n).orElse("S/N: 待定") + "</div>"//
				+ "</div>" //
				+ "<div class='label_title' style='text-align:right;'>申请时间<br/>" + Formatter.getString(pc.getApplicantDate(), "M/d")
				+ "</div>"//
				+ "</div>";
	}
}
