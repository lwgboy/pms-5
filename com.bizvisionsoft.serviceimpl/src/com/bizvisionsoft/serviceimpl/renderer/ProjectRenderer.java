package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.News;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;

public class ProjectRenderer {

	public static Document render(Project pj) {
		return new ProjectRenderer(pj).render();
	}

	private CardTheme theme;

	private Project pj;

	private int rowHeight;

	public ProjectRenderer(Project pj) {
		this.pj = pj;
		theme = new CardTheme(pj);
		rowHeight = 3 * RenderTools.margin;
	}

	private Document render() {
		StringBuffer sb = new StringBuffer();
		if (ProjectStatus.Created.equals(pj.getStatus())) {
			renderCreateProject(sb);
		} else if (ProjectStatus.Processing.equals(pj.getStatus())) {
			renderProcessingProject(sb);
		} else if (ProjectStatus.Closing.equals(pj.getStatus())) {
			renderClosingProject(sb);
		} else {
			renderClosedProject(sb);
		}
		RenderTools.renderCardBoard(sb, rowHeight);
		
		return new Document("_id", pj.get_id()).append("html", sb.toString()).append("height", rowHeight);
	}

	private void renderClosedProject(StringBuffer sb) {
		sb.append(renderTitle());

		sb.append(renderPM());

		sb.append(renderOrg());

		sb.append(renderPlanSchedule());
		
		sb.append(renderActualSchedule());

		if (pj.isStageEnable()) {
			sb.append(renderProjectStage());
		}

	}

	private void renderClosingProject(StringBuffer sb) {
		sb.append(renderTitle());

		sb.append(renderPM());

		sb.append(renderOrg());

		sb.append(renderPlanSchedule());
		
		if(pj.getActualStart()!=null) {
			sb.append(renderActualSchedule());
		}

		if (pj.isStageEnable()) {
			sb.append(renderProjectStage());
		}

		sb.append(renderTimeline());

		sb.append(renderIndicators());

		sb.append(renderWarningNotice());
	}

	private void renderProcessingProject(StringBuffer sb) {
		sb.append(renderTitle());

		sb.append(renderPM());

		sb.append(renderOrg());

		sb.append(renderPlanSchedule());

		if(pj.getActualStart()!=null) {
			sb.append(renderActualSchedule());
		}
		
		if (pj.isStageEnable()) {
			sb.append(renderProjectStage());
		}

		sb.append(renderTimeline());

		sb.append(renderIndicators());

		sb.append(renderWarningNotice());

	}

	private void renderCreateProject(StringBuffer sb) {
		sb.append(renderTitle());

		sb.append(renderPM());

		sb.append(renderOrg());

		sb.append(renderPlanSchedule());

	}

	private String renderPlanSchedule() {
		rowHeight += 20 + 8;
		String text = RenderTools.shortDate(pj.getPlanStart()) + " ~ " + RenderTools.shortDate(pj.getPlanFinish()) + " <span style='color:#"
				+ CardTheme.TEXT_LINE[0] + "'>工期：</span>" + pj.getPlanDuration() + "天";
		return RenderTools.getIconTextLine("计划进度", text, RenderTools.IMG_URL_CALENDAR, CardTheme.TEXT_LINE);
	}

	private String renderActualSchedule() {
		rowHeight += 20 + 8;
		Date actualStart = pj.getActualStart();
		Date actualFinish = pj.getActualFinish();
		String text = RenderTools.shortDate(actualStart) + " ~ " + RenderTools.shortDate(actualFinish) + " <span style='color:#"
				+ CardTheme.TEXT_LINE[0] + "'>工期：</span>" + pj.getActualDuration() + "天";
		return RenderTools.getTextLine("实际进度", text, CardTheme.TEXT_LINE);
	}

	
	private String renderOrg() {
		rowHeight += 20 + 8;
		return RenderTools.getTextLine("承担单位", pj.getImpUnitOrgFullName(), CardTheme.TEXT_LINE);
	}

	private String renderPM() {
		rowHeight += 20 + 8;
		return RenderTools.getIconTextLine("项目经理", pj.warpperPMInfo(), RenderTools.IMG_URL_USER, CardTheme.TEXT_LINE);
	}

	private String renderTitle() {
		rowHeight += 64;
		return "<div class='brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor + ";padding:8px'>" //
				+ "<div>"//
				+ "<div class='label_title'>" + pj.getName() + "</div>"//
				+ "<div>" + Check.isAssignedThen(pj.getProjectNumber(), n -> "S/N: " + n).orElse("S/N: 待定") + "</div>"//
				+ "</div>" //
				+ "<div class='label_title'>" + pj.getStatus() + "</div>"//
				+ "</div>";

	}

	private String renderProjectStage() {
		rowHeight += 38;

		StringBuffer text = new StringBuffer();
		text.append("<div class='layui-btn-group' "
				+ "style='padding:8px 8px 0px 8px;width: 100%;display:inline-flex;justify-content:space-between;'>");
		ServicesLoader.get(ProjectService.class).listStage(pj.get_id()).forEach(work -> {
			String style;
			if (work.getStartOn() != null && work.getFinishOn() != null) {
				style = "layui-btn layui-btn-sm";
			} else if (work.getStartOn() != null && work.getFinishOn() == null) {
				style = "layui-btn layui-btn-normal layui-btn-sm";
			} else {
				style = "layui-btn layui-btn-primary layui-btn-sm";
			}
			text.append("<div class='" + style + "' style='width: 100%;'>" + work.getText() + "</div>");
		});
		text.append("</div>");
		return text.toString();
	}

	private String renderTimeline() {
		StringBuffer sb = new StringBuffer();
		List<News> news = ServicesLoader.get(ProjectService.class).getRecentNews(pj.get_id(), 5);
		if (news.size() > 0) {
			rowHeight += 8;
			sb.append("<div style='padding:8px 16px 0px 16px;'>");
			for (int i = 0; i < news.size(); i++) {
				sb.append("<div class='label_caption' style='height:20px;display:flex;color:#" + theme.lightText
						+ ";white-space:nowrap;overflow:hidden;text-overflow:ellipsis;width:100%;'>"
						+ "<i class='layui-icon layui-icon-circle' style='margin-right:4px;'></i>" + news.get(i).getSummary() + "</div>");

				rowHeight += 20;
			}
			sb.append("</div>");
		}
		return sb.toString();
	}

	private String renderWarningNotice() {
		Date _actual = pj.getActualFinish();
		Date _plan = pj.getPlanFinish();
		if (_actual == null) {
			_actual = new Date();
		}
		String text = null;
		String msg = null;
		if (_actual.after(_plan)) {
			text = "<span class='layui-badge'>超期</span>";
			msg = "当前时间已经超过项目计划完成时间";
		} else {
			Integer idx = pj.getOverdueIndex();
			if (idx != null) {
				switch (idx) {
				case 0:
					text = "<span class='layui-badge'>&#8544;级预警</span>";
					msg = "根据进度估算，按当前进展速度，项目将要超期，应考虑关键路径的工作进行赶工。";
					break;
				case 1:
					text = "<span class='layui-badge layui-bg-orange'>&#8545;级预警</span>";
					msg = "根据进度估算，项目的1级管理节点将超期。";
					break;
				case 2:
					text = "<span class='layui-badge layui-bg-orange'>&#8546;级预警</span>";
					msg = "根据进度估算，项目的2级管理节点将超期。";
					break;
				}
			}
		}

		if (text != null) {
			String html = MetaInfoWarpper.warpper(text, msg);
			rowHeight += 35;
			return "<div style='margin-top:8px;padding:8px;display:flex;width:100%;justify-content:flex-end;align-items:center;'>" + html
					+ "</div>";

		}
		return "";
	}

	private String renderIndicators() {
		StringBuffer sb = new StringBuffer();

		String label;
		Double ind = pj.getWAR();
		if (ind != null) {
			label = "项目所有工作累计的实际工期与计划工期的比值，反映项目工作量的完成情况。";
			appendIndicator(sb, ind, "工作量完成率", label, CardTheme.CONTRAST_TEAL);
		}

		ind = pj.getDAR();
		if (ind != null) {
			label = "项目实际工期与计划工期的比值，反映项目截至目前耗时状况。";
			appendIndicator(sb, ind, "工期完成率", label, CardTheme.CONTRAST_CYAN);
		}

		ind = pj.getCAR();
		if (ind != null) {
			label = "项目累计的实际成本与计划成本（预算）的比值，反映项目截至目前消耗的资金状况。";
			appendIndicator(sb, ind, "预算使用率", label, CardTheme.CONTRAST_INDIGO);
		}

		ind = pj.getDurationProbability();
		if (ind != null) {
			label = "根据风险模拟后，计算按计划完成项目的概率。";
			appendIndicator(sb, ind, "按期完工概率", label,
					ind > 0.75 ? CardTheme.CONTRAST_TEAL : (ind > 0.5 ? CardTheme.CONTRAST_BLUE : CardTheme.CONTRAST_ORANGE));
		}

		return sb.toString();
	}

	private void appendIndicator(StringBuffer sb, Double ind, String label, String text, String[] indColor) {
		rowHeight += 80;

		sb.append("<div style='padding:8px 16px 0px 16px;display:flex;width:100%;align-items:center;'>");
		sb.append("<img style='flex-shrink:0;' src='/bvs/svg?type=progress&percent=" + ind + "&bgColor=" + indColor[0] + "&fgColor="
				+ indColor[1] + "' width=72px height=72px/>");
		sb.append("<div class='brui_card_text3 label_caption' style='margin-left:8px;padding-left:8px;border-left:solid 2px #"
				+ theme.lightText + ";'>" + //
				"<div style='color:#" + CardTheme.TEXT_LINE[0] + "'>" + label + "</div>" + "<div style='color:#" + CardTheme.TEXT_LINE[1]
				+ "'>" + text + "</div>"//
				+ "</div>");//
		sb.append("</div>");
	}

}
