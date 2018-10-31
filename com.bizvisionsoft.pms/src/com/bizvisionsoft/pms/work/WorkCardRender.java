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
import com.bizvisionsoft.service.tools.Formatter;

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
		renderProjectLine(theme,sb, work);

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
		int rowHeight = 232;
		gridItem.setHeight(rowHeight);
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// 显示页签
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work);
		// 标签
//		renderNoticeBudgets(work, sb);

		// 显示项目图标和名称
		renderProjectLine(theme,sb, work);

		// 显示计划开始和计划完成
		String text = "计划：" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// 工作负责人
		renderIconTextLine(sb, "负责：" + work.getChargerInfoHtml(), "img/user_c.svg", theme.emphasizeText);

		// 显示工作包和完成工作
		renderButtons(theme, sb, work, "开始", "startWork/" + work.get_id());


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
		int rowHeight = 360;
		gridItem.setHeight(rowHeight);
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// 显示页签
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work);
		
		// 标签
//		renderNoticeBudgets(work, sb);
		
		// 显示项目图标和名称
		renderProjectLine(theme,sb, work);

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


		sb.append("</div>");

		cell.setText(sb.toString());
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
