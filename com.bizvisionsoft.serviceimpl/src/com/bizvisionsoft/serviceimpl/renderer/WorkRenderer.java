package com.bizvisionsoft.serviceimpl.renderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;

public class WorkRenderer {

	public static Document renderingUnAssignmentWorkCard(Work work) {
		return new WorkRenderer(work).renderingUnAssignmentWorkCard(true, false);
	}

	public static Document renderingFinishedWorkCard(Work work) {
		return new WorkRenderer(work).setTheme(new CardTheme("deepGrey")).renderingFinishedWorkCard(true);
	}

	public static Document renderingExecutingWorkCard(Work work) {
		return new WorkRenderer(work).renderingExecutingWorkCard(true, false);
	}

	public static Document renderingPlannedWorkCard(Work work) {
		return new WorkRenderer(work).renderingPlannedWorkCard(true, false);
	}

	private Work work;

	private CardTheme theme;

	private Document doc;

	public WorkRenderer(Work work) {
		this.work = work;
		theme = new CardTheme(work);
		doc = new Document("_id", work.get_id());
	}

	public WorkRenderer setTheme(CardTheme theme) {
		this.theme = theme;
		return this;
	}

	public Document renderingFinishedWorkCard(boolean showProject) {
		int rowHeight = 172;
		StringBuffer sb = new StringBuffer();
		// 显示页签
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		sb.append(renderTitle(work.getActualFinish()));

		// 显示第一行信息
		if (showProject)
			sb.append(renderProjectLine());
		else
			Check.isAssigned(work.getStageName(),
					s -> sb.append(RenderTools.getIconTextLine(s, RenderTools.IMG_URL_TASK, theme.emphasizeText)));

		// 显示计划开始和计划完成
		String text = "计划：" + RenderTools.shortDate(work.getPlanStart()) + "~" + RenderTools.shortDate(work.getPlanFinish()) + "，完成于"
				+ RenderTools.shortDate(work.getActualFinish());
		sb.append(RenderTools.getIconTextLine(text, RenderTools.IMG_URL_CALENDAR, theme.emphasizeText));

		// 工作负责人
		sb.append(renderCharger());

		sb.append("</div>");

		return doc.append("height", rowHeight).append("html", sb.toString());
	}

	public Document renderingUnAssignmentWorkCard(boolean showProject, boolean showActionButton) {
		int rowHeight = 220;

		StringBuffer sb = new StringBuffer();
		// 显示页签
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");
		sb.append(renderTitle(work.getPlanFinish()));

		// 显示第一行信息
		if (showProject)
			sb.append(renderProjectLine());
		else
			Check.isAssigned(work.getStageName(),
					s -> sb.append(RenderTools.getIconTextLine(s, RenderTools.IMG_URL_TASK, theme.emphasizeText)));

		// 显示计划开始和计划完成
		String text = "计划：" + RenderTools.shortDate(work.getPlanStart()) + "~" + RenderTools.shortDate(work.getPlanFinish());
		sb.append(RenderTools.getIconTextLine(text, RenderTools.IMG_URL_CALENDAR, theme.emphasizeText));

		// 显示工作包和指派工作
		sb.append(renderButtons(showActionButton, "指派", "assignWork/" + work.get_id()));

		// 标签
		sb.append(renderNoticeBudgets());
		sb.append("</div>");

		return doc.append("height", rowHeight).append("html", sb.toString());
	}

	/**
	 * 渲染处理中的工作
	 * 
	 * @param c
	 * @param b
	 * 
	 * @param work
	 * @return
	 */
	public Document renderingExecutingWorkCard(boolean showProject, boolean showActionButton) {
		int rowHeight = 374;

		StringBuffer sb = new StringBuffer();
		// 显示页签
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		sb.append(renderTitle(work.getPlanFinish()));

		// 显示第一行信息
		sb.append(renderProjectLine());

		// 显示计划开始和计划完成
		String text = "计划：" + RenderTools.shortDate(work.getPlanStart()) + "~" + RenderTools.shortDate(work.getPlanFinish()) + "，开始于"
				+ RenderTools.shortDate(work.getActualStart());
		sb.append(RenderTools.getIconTextLine(text, RenderTools.IMG_URL_CALENDAR, theme.emphasizeText));

		// 工作负责人
		sb.append(renderCharger());

		// 显示两个指标
		sb.append(renderIndicators("进度", work.getWAR(), "工期", work.getDAR()));

		// 显示工作包和完成工作
		sb.append(renderButtons("完成", "finishWork/" + work.get_id()));

		// 标签
		sb.append(renderNoticeBudgets());

		sb.append("</div>");

		return doc.append("height", rowHeight).append("html", sb.toString());
	}

	/**
	 * 渲染已计划的工作
	 * 
	 * @param c
	 * @param b
	 * 
	 * @param work
	 * @return
	 */
	public Document renderingPlannedWorkCard(boolean showProject, boolean showActionButton) {
		int rowHeight = 247;

		StringBuffer sb = new StringBuffer();
		// 显示页签
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		sb.append(renderTitle(work.getPlanStart()));

		// 显示第一行信息
		sb.append(renderProjectLine());

		// 显示计划开始和计划完成
		String text = "计划：" + RenderTools.shortDate(work.getPlanStart()) + "~" + RenderTools.shortDate(work.getPlanFinish());
		sb.append(RenderTools.getIconTextLine(text, RenderTools.IMG_URL_CALENDAR, theme.emphasizeText));

		// 工作负责人
		sb.append(renderCharger());

		// 显示工作包和开始工作
		sb.append(renderButtons("开始", "startWork/" + work.get_id()));

		// 标签
		sb.append(renderNoticeBudgets());

		sb.append("</div>");

		return doc.append("height", rowHeight).append("html", sb.toString());
	}

	private String renderTitle(Date date) {
		String name = work.getFullName();
		String _date = Formatter.getString(date, "M/d");
		return "<div class='label_title brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor
				+ ";padding:8px'>" + "<div class='brui_card_text'>" + name + "</div><div class='label_display1'>" + _date + "</div></div>";
	}

	private String renderProjectLine() {
		return "<div style='padding-left:8px;padding-top:8px;display:flex;align-items:center;'><img src='" + RenderTools.IMG_URL_PROJECT
				+ "' width='20' height='20'><a href='openProject/' target='_rwt' class='label_caption brui_text_line' style='color:#"
				+ theme.lightText + ";margin-left:8px;width:100%'>项目：" + work.getProjectName() + "</a></div>";
	}

	private String renderCharger() {
		return renderUser("负责", work.warpperChargerInfo(), theme.emphasizeText);
	}

	private String renderUser(String title, String text, String color) {
		return "<div style='padding-left:8px;padding-top:8px;display:flex;align-items:center;'><img src='" + RenderTools.IMG_URL_USER
				+ "' width='20' height='20'><div class='label_caption brui_text_line' style='color:#" + color
				+ ";margin-left:8px;width:100%;display:flex;'>" + title + "：<span style='cursor:pointer;'>" + text + "</span></div></div>";
	}

	private String renderNoticeBudgets() {
		StringBuffer sb = new StringBuffer();
		sb.append("<div style='margin-top:8px;padding:4px;display:flex;width:100%;justify-content:flex-end;align-items:center;'>");
		Double value = work.getTF();
		if (value != null) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;'>TF " + (int) Math.ceil(value) + "</div>";
			sb.append(MetaInfoWarpper.warpper(label,
					"总时差（TF）：<br>在不影响总工期的前提下，本工作可以利用的机动时间，即工作的最迟开始时间与最早开始时间之差。利用这段时间延长工作的持续时间或推迟其开工时间，不会影响计划的总工期。", 3000));
		}

		value = work.getFF();
		if (value != null) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;'>FF " + (int) Math.ceil(value) + "</div>";
			sb.append(
					MetaInfoWarpper.warpper(label, "自由时差（FF）：<br>在不影响其紧后工作最早开始时间的条件下，本工作可以利用的机动时间。即该工作的所有紧后工作的最早开始时间，减去该工作的最早结束时间。", 3000));
		}

		value = work.getTF();
		if (value != null && value.doubleValue() == 0) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;'>CP</div>";
			sb.append(MetaInfoWarpper.warpper(label, "本工作处于项目关键路径", 3000));
		}

		String manageLevel = work.getManageLevel();
		if ("1".equals(manageLevel)) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;'>&#8544;</div>";
			sb.append(MetaInfoWarpper.warpper(label, "这是一个1级管理级别的工作。", 3000));
		}

		if ("2".equals(manageLevel)) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;'>&#8545;</div>";
			sb.append(MetaInfoWarpper.warpper(label, "这是一个2级管理级别的工作。", 3000));
		}
		// 警告
		Check.isAssigned(work.getWarningIcon(), sb::append);

		sb.append("</div>");
		return sb.toString();
	}

	private String renderButtons(String label, String href) {
		return renderButtons(true, label, href);
	}

	private String renderButtons(boolean showActionButton, String label, String href) {
		List<TrackView> wps = work.getWorkPackageSetting();
		List<String[]> btns = new ArrayList<>();
		if (Check.isNotAssigned(wps)) {
			btns.add(new String[] { "openWorkPackage/default", "工作包" });
		} else if (wps.size() == 1) {
			btns.add(new String[] { "openWorkPackage/0", wps.get(0).getName() });
		} else {
			for (int i = 0; i < wps.size(); i++) {
				btns.add(new String[] { "openWorkPackage/" + i, wps.get(i).getName() });
			}
		}

		StringBuffer sb = new StringBuffer();
		sb.append("<div style='margin-top:16px;padding:4px;display:flex;width:100%;justify-content:space-around;align-items:center;'>");
		btns.forEach(e -> {
			sb.append("<a class='label_card' href='" + e[0] + "' target='_rwt'>" + e[1] + "</a>");
		});
		if (showActionButton)
			sb.append(
					"<a class='label_card' style='color:#" + theme.headBgColor + ";' href='" + href + "' target='_rwt'>" + label + "</a>");
		sb.append("</div>");
		return sb.toString();
	}

	private String renderIndicators(String label1, double ind1, String label2, double ind2) {
		StringBuffer sb = new StringBuffer();
		sb.append("<div style='padding:4px;display:flex;width:100%;justify-content:space-evenly;align-items:center;'>");

		sb.append("<div><img src='/bvs/svg?type=progress&percent=" + ind1 + "&bgColor=" + theme.contrastBgColor + "&fgColor="
				+ theme.contrastFgColor + "' width=100 height=100/>");
		sb.append("<div class='label_body1' style='text-align:center;color:#9e9e9e'>" + label1 + "</div></div>");

		sb.append("<div style='background:#d0d0d0;width:1px;height:80px'></div>");
		sb.append("<div><img src='/bvs/svg?type=progress&percent=" + ind2 + "&bgColor=" + theme.contrastBgColor + "&fgColor="
				+ theme.contrastFgColor + "' width=100 height=100/>");
		sb.append("<div class='label_body1' style='text-align:center;color:#9e9e9e'>" + label2 + "</div></div>");

		sb.append("</div>");
		return sb.toString();
	}

}
