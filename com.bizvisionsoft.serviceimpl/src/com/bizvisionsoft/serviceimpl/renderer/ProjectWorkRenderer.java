package com.bizvisionsoft.serviceimpl.renderer;

import org.bson.Document;

import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;

public class ProjectWorkRenderer extends AbstractRenderer {

	public static Document renderingUnAssignmentWorkCard(Work work, String userid) {
		Document doc = new Document();
		int rowHeight = 220;
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// 显示页签
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work, work.getPlanFinish());

		// 显示第一行信息
		Check.isAssigned(work.getStageName(), s -> renderIconTextLine(sb, s, "img/task_c.svg", theme.emphasizeText));

		// 显示计划开始和计划完成
		String text = "计划：" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// 显示工作包和指派工作
		renderButtons(theme, sb, work, Check.equals(userid, work.getChargerId()), "指派", "assignWork/" + work.get_id());

		// 标签
		renderNoticeBudgets(sb, work);
		sb.append("</div>");

		doc.put("height", rowHeight);
		doc.put("html", sb.toString());
		doc.put("_id", work.get_id());
		return doc;
	}

	public static Document renderingFinishedWorkCard(Work work) {
		Document doc = new Document();
		int rowHeight = 172;
		CardTheme theme = new CardTheme("deepGrey");

		StringBuffer sb = new StringBuffer();
		// 显示页签
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work, work.getActualFinish());

		Check.isAssigned(work.getStageName(), s -> renderIconTextLine(sb, s, "img/task_c.svg", theme.emphasizeText));

		// 显示计划开始和计划完成
		String text = "计划：" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish()) + "，完成于"
				+ Formatter.getString(work.getActualFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// 工作负责人
		renderCharger(theme, sb, work);

		sb.append("</div>");
		doc.put("height", rowHeight);
		doc.put("html", sb.toString());
		doc.put("_id", work.get_id());
		return doc;
	}

	/**
	 * 渲染处理中的工作
	 * 
	 * @param work
	 * @return
	 */
	public static Document renderingExecutingWorkCard(Work work, String userid) {
		Document doc = new Document();
		int rowHeight = 374;
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// 显示页签
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work, work.getPlanFinish());

		Check.isAssigned(work.getStageName(), s -> renderIconTextLine(sb, s, "img/task_c.svg", theme.emphasizeText));

		// 显示计划开始和计划完成
		String text = "计划：" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish()) + "，开始于"
				+ Formatter.getString(work.getActualStart());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// 工作负责人
		renderCharger(theme, sb, work);

		// 显示两个指标
		renderIndicators(theme, sb, "进度", work.getWAR(), "工期", work.getDAR());

		// 显示工作包和完成工作
		renderButtons(theme, sb, work, Check.equals(userid, work.getChargerId()), "完成", "finishWork/" + work.get_id());

		// 标签
		renderNoticeBudgets(sb, work);

		sb.append("</div>");

		doc.put("height", rowHeight);
		doc.put("html", sb.toString());
		doc.put("_id", work.get_id());
		return doc;
	}

	/**
	 * 渲染已计划的工作
	 * 
	 * @param work
	 * @return
	 */
	public static Document renderingPlannedWorkCard(Work work, String userid) {
		Document doc = new Document();
		int rowHeight = 247;

		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// 显示页签
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work, work.getPlanStart());

		Check.isAssigned(work.getStageName(), s -> renderIconTextLine(sb, s, "img/task_c.svg", theme.emphasizeText));

		// 显示计划开始和计划完成
		String text = "计划：" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// 工作负责人
		renderCharger(theme, sb, work);

		// 显示工作包和开始工作
		renderButtons(theme, sb, work, Check.equals(userid, work.getChargerId()), "开始", "startWork/" + work.get_id());

		// 标签
		renderNoticeBudgets(sb, work);

		sb.append("</div>");

		doc.put("height", rowHeight);
		doc.put("html", sb.toString());
		doc.put("_id", work.get_id());
		return doc;
	}
}
