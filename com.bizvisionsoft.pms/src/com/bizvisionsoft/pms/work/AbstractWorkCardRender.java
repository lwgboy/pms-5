package com.bizvisionsoft.pms.work;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public abstract class AbstractWorkCardRender {

	private IBruiService brui;

	private GridTreeViewer viewer;

	private BruiAssemblyContext context;

	protected void init() {
		brui = getBruiService();
		context = getContext();
	}

	protected void uiCreated() {
		viewer = getViewer();
		viewer.getGrid().setBackground(BruiColors.getColor(BruiColor.Grey_200));
		viewer.getGrid().setData(RWT.CUSTOM_VARIANT, "board");
		viewer.getGrid().addListener(SWT.Selection, e -> {
			if (e.text != null) {
				Work work = (Work) ((GridItem) e.item).getData();
				if (e.text.startsWith("startWork/")) {
					startWork(work);
				} else if (e.text.startsWith("finishWork/")) {
					finishWork(work);
				} else {
					String idx = e.text.split("/")[1];
					if (e.text.startsWith("openWorkPackage/")) {
						openWorkPackage(work, idx);
					} else if (e.text.startsWith("assignWork/")) {
						assignWork(work);
					}
				}
			}
		});
	}

	protected GridTreeViewer getViewer() {
		return (GridTreeViewer) context.getContent("viewer");
	}

	protected abstract BruiAssemblyContext getContext();

	protected abstract IBruiService getBruiService();

	private void openWorkPackage(Work work, String idx) {
		if ("default".equals(idx)) {
			brui.openContent(brui.getAssembly("工作包计划"), new Object[] { work, null });
		} else {
			List<TrackView> wps = work.getWorkPackageSetting();
			brui.openContent(brui.getAssembly("工作包计划"), new Object[] { work, wps.get(Integer.parseInt(idx)) });
		}
	}

	private void assignWork(Work work) {
		Selector.open("指派用户选择器", context, work, l -> {
			ServicesLoader.get(WorkService.class).updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", work.get_id()))
					.set(new BasicDBObject("chargerId", ((User) l.get(0)).getUserId())).bson());

			work.setChargerId(((User) l.get(0)).getUserId());
			viewer.update(work, null);
		});
	}

	private void finishWork(Work work) {
		if (brui.confirm("完成工作", "请确认完成工作：" + work + "</span>。<br>系统将记录现在时刻为工作的实际完成时间。")) {
			List<Result> result = Services.get(WorkService.class).finishWork(brui.command(work.get_id(), new Date(), ICommand.Finish_Work));
			if (result.isEmpty()) {
				Layer.message("工作已完成");
				viewer.remove(work);
				brui.updateSidebarActionBudget("处理工作");
			}
		}
	}

	private void startWork(Work work) {
		if (brui.confirm("启动工作", "请确认启动工作" + work + "。<br>系统将记录现在时刻为工作的实际开始时间。")) {
			List<Result> result = Services.get(WorkService.class).startWork(brui.command(work.get_id(), new Date(), ICommand.Start_Work));
			if (result.isEmpty()) {
				Layer.message("工作已启动");
				Work t = Services.get(WorkService.class).getWork(work.get_id());
				viewer.update(AUtil.simpleCopy(t, work), null);
			}
		}
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

	protected void renderButtons(CardTheme theme, StringBuffer sb, Work work, String label, String href) {
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
		sb.append("<div style='margin-top:8px;padding:4px;display:flex;width:100%;justify-content:space-around;align-items:center;'>");
		btns.forEach(e -> {
			sb.append("<a class='label_card' href='" + e[0] + "' target='_rwt'>" + e[1] + "</a>");
		});
		sb.append("<a class='label_card' style='color:#" + theme.headBgColor + ";' href='" + href + "' target='_rwt'>" + label + "</a>");
		sb.append("</div>");
	}

	protected void renderIndicators(CardTheme theme, StringBuffer sb, String label1, double ind1, String label2, double ind2) {
		sb.append("<div style='padding:4px;display:flex;width:100%;justify-content:space-evenly;align-items:center;'>");

		sb.append("<div><img src='/bvs/svg?type=progress&percent=" + ind1 + "&bgColor=" + theme.contrastBgColor + "&fgColor="
				+ theme.contrastFgColor + "' width=100 height=100/>");
		sb.append("<div class='label_body1' style='text-align:center;color:#9e9e9e'>" + label1 + "</div></div>");

		sb.append("<div style='background:#d0d0d0;width:1px;height:80px'></div>");
		sb.append("<div><img src='/bvs/svg?type=progress&percent=" + ind2 + "&bgColor=" + theme.contrastBgColor + "&fgColor="
				+ theme.contrastFgColor + "' width=100 height=100/>");
		sb.append("<div class='label_body1' style='text-align:center;color:#9e9e9e'>" + label2 + "</div></div>");

		sb.append("</div>");
	}

	protected void renderTitle(CardTheme theme, StringBuffer sb, Work work) {
		sb.append("<div class='label_title brui_card_head' style='position:relative;height:64px;background:#" + theme.headBgColor
				+ ";color:#" + theme.headFgColor + ";padding:8px;word-break:break-word;white-space:pre-line;'>"
				+ "<div style='position:absolute;bottom:4px;'>" + work.getFullName() + "</div></div>");
	}

	protected void renderIconTextLine(StringBuffer sb, String text, String icon, String color) {
		sb.append("<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'><img src='" + brui.getResourceURL(icon)
				+ "' width='20' height='20'><div class='label_caption brui_text_line' style='color:#" + color
				+ ";margin-left:8px;width:100%'>" + text + "</div></div>");
	}

}
