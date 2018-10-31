package com.bizvisionsoft.pms.work;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.grid.GridItem;

import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUICreated;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;

public class WorkCardRender extends AbstractWorkCardRender{

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;
	
	@Init
	protected void init() {
		super.init();
	}

	@GridRenderUICreated
	protected void uiCreated() {
		super.uiCreated();
	}

	@GridRenderUpdateCell
	protected void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell) {
		Work work = (Work) cell.getElement();
		// 执行中
		if (work.getActualStart() != null && work.getActualFinish() == null) {
			renderToFinishCard(cell, work);
		} else if (work.getActualStart() == null) {// 已计划，未开始
			renderToStartCard(cell, work);
		} else if (work.getActualFinish() != null) {// 已完成
			renderFinishedCard(cell, work);
		}
	}

	private void renderFinishedCard(ViewerCell cell, Work work) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		int rowHeight = 180;
		gridItem.setHeight(rowHeight);
		CardTheme theme = new CardTheme("deepGrey");

		StringBuffer sb = new StringBuffer();
		// 显示页签
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work);
		// 显示项目图标和名称
		renderIconTextLine(sb, "项目：" + work.getProjectName(), "img/project_c.svg", theme.lightText);

		// 显示计划开始和计划完成
		String text = "计划：" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// 工作负责人
		renderIconTextLine(sb, "负责：" + work.getChargerInfoHtml(), "img/user_c.svg", theme.emphasizeText);

		sb.append("</div>");

		cell.setText(sb.toString());

	}

	private void renderToStartCard(ViewerCell cell, Work work) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		int rowHeight = 244;
		gridItem.setHeight(rowHeight);
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// 显示页签
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work);
		// 显示项目图标和名称
		renderIconTextLine(sb, "项目：" + work.getProjectName(), "img/project_c.svg", theme.lightText);

		// 显示计划开始和计划完成
		String text = "计划：" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// 工作负责人
		renderIconTextLine(sb, "负责：" + work.getChargerInfoHtml(), "img/user_c.svg", theme.emphasizeText);

		// 显示工作包和完成工作
		renderButtons(theme, sb, work, "开始", "startWork/" + work.get_id());

		// 标签
		renderNoticeBudgets(work, sb);

		sb.append("</div>");

		cell.setText(sb.toString());

	}

	/**
	 * 绘制待处理工作卡片
	 * 
	 * @param cell
	 * @param work
	 */
	private void renderToFinishCard(ViewerCell cell, Work work) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		int rowHeight = 372;
		gridItem.setHeight(rowHeight);
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// 显示页签
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work);
		// 显示项目图标和名称
		renderIconTextLine(sb, "项目：" + work.getProjectName(), "img/project_c.svg", theme.lightText);

		// 显示计划开始和计划完成
		String text = "计划：" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish()) + "，实际开始："
				+ Formatter.getString(work.getActualStart());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// 工作负责人
		renderIconTextLine(sb, "负责：" + work.getChargerInfoHtml(), "img/user_c.svg", theme.emphasizeText);

		// 显示两个指标
		renderIndicators(theme, sb, "进度", work.getWAR(), "工期", work.getDAR());

		// 显示工作包和完成工作
		renderButtons(theme, sb, work, "完成", "finishWork/" + work.get_id());

		// 标签
		renderNoticeBudgets(work, sb);

		sb.append("</div>");

		cell.setText(sb.toString());
	}

	protected void renderNoticeBudgets(Work work, StringBuffer sb) {
		sb.append("<div style='padding:8px;display:flex;width:100%;justify-content:flex-end;align-items:center;'>");
		Check.isAssigned(work.getManageLevel(), l -> {
			if ("1".equals(l)) {
				String label = "<div class='layui-badge layui-bg-blue' style='width:36px;margin-right:4px;'>1级</div>";
				sb.append(MetaInfoWarpper.warpper(label, "这是一个1级管理级别的工作。", 3000));
			}

			if ("2".equals(l)) {
				String label = "<div class='layui-badge layui-bg-cyan' style='width:36px;margin-right:4px;'>2级</div>";
				sb.append(MetaInfoWarpper.warpper(label, "这是一个2级管理级别的工作。", 3000));
			}
		});
		// 警告
		Check.isAssigned(work.getWarningIcon(), sb::append);
	}

	@Override
	protected BruiAssemblyContext getContext() {
		return context;
	}

	@Override
	protected IBruiService getBruiService() {
		return brui;
	}

}
