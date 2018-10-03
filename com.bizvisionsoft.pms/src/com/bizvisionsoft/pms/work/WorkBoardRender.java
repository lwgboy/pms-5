package com.bizvisionsoft.pms.work;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUICreated;
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
import com.bizvisionsoft.service.model.WorkBoardInfo;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class WorkBoardRender {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private GridTreeViewer viewer;

	@GridRenderUICreated
	private void uiCreated() {
		viewer = (GridTreeViewer) context.getContent("viewer");
		viewer.getGrid().setBackground(BruiColors.getColor(BruiColor.Grey_50));
		viewer.getGrid().addListener(SWT.Selection, e -> {
			if (e.text != null) {
				if (e.text.startsWith("startWork/")) {
					startWork((Work) ((GridItem) e.item).getData());
				} else if (e.text.startsWith("finishWork/")) {
					finishWork((Work) ((GridItem) e.item).getData());
				} else {
					String idx = e.text.split("/")[1];
					if (e.text.startsWith("openWorkPackage/")) {
						openWorkPackage((WorkBoardInfo) ((GridItem) e.item).getData(), idx);
					} else if (e.text.startsWith("assignWork/")) {
						assignWork((WorkBoardInfo) ((GridItem) e.item).getData());
					}
				}
			} else {
				Object element = ((GridItem) e.item).getData();
				if (element instanceof Work) {
					viewer.setExpandedElements(new Object[] { element });
				}
			}
		});
		if (((List<?>) viewer.getInput()).size() > 0) {
			viewer.setExpandedElements(new Object[] { ((List<?>) viewer.getInput()).get(0) });
		}
	}

	private void openWorkPackage(WorkBoardInfo workInfo, String idx) {
		Work work = workInfo.getWork();
		if ("default".equals(idx)) {
			brui.openContent(brui.getAssembly("工作包计划"), new Object[] { work, null });
		} else {
			List<TrackView> wps = work.getWorkPackageSetting();
			brui.openContent(brui.getAssembly("工作包计划"), new Object[] { work, wps.get(Integer.parseInt(idx)) });
		}
	}

	private void assignWork(WorkBoardInfo workInfo) {
		Work work = workInfo.getWork();
		Selector.open("指派用户选择器", context, work, l -> {
			ServicesLoader.get(WorkService.class)
					.updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", work.get_id()))
							.set(new BasicDBObject("chargerId", ((User) l.get(0)).getUserId())).bson());

			work.setChargerId(((User) l.get(0)).getUserId());
			viewer.update(work, null);
		});
	}

	private void finishWork(Work work) {
		if (brui.confirm("完成工作", "请确认完成工作：" + work + "</span>。<br>系统将记录现在时刻为工作的实际完成时间。")) {
			List<Result> result = Services.get(WorkService.class)
					.finishWork(brui.command(work.get_id(), new Date(), ICommand.Finish_Work));
			if (result.isEmpty()) {
				Layer.message("工作已完成");
				viewer.remove(work);
				brui.updateSidebarActionBudget("处理工作");
			}
		}
	}

	private void startWork(Work work) {
		if (brui.confirm("启动工作", "请确认启动工作" + work + "。<br>系统将记录现在时刻为工作的实际开始时间。")) {
			List<Result> result = Services.get(WorkService.class)
					.startWork(brui.command(work.get_id(), new Date(), ICommand.Start_Work));
			if (result.isEmpty()) {
				Layer.message("工作已启动");
				Work t = Services.get(WorkService.class).getWork(work.get_id());
				viewer.update(AUtil.simpleCopy(t, work), null);
			}
		}
	}

	@GridRenderUpdateCell
	private void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell) {
		Object element = cell.getElement();
		if (element instanceof Work) {
			renderTitle(cell, (Work) element);
		} else if (element instanceof WorkBoardInfo) {
			renderContent(cell, ((WorkBoardInfo) element).getWork());
		}
	}

	private void renderTitle(ViewerCell cell, Work work) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		gridItem.setHeight(84);
		gridItem.setBackground(BruiColors.getColor(BruiColor.Grey_50));
		StringBuffer sb = new StringBuffer();
		// 开始和完成按钮
		if (work.getActualStart() == null) {
			sb.append("<div style='float:right;margin-right:16px;margin-top:0px;'><a href='startWork/" + work.get_id()
					+ "' target='_rwt'><img class='layui-btn layui-btn-sm' style='padding:6px 10px;' src='rwt-resources/extres/img/start_w.svg'/></a></div>");
		} else if (work.getActualFinish() == null) {
			sb.append("<div style='float:right;margin-right:16px;margin-top:0px;'><a href='finishWork/" + work.get_id()
					+ "' target='_rwt'><img class='layui-btn layui-btn-normal layui-btn-sm' style='padding:6px 10px;' src='rwt-resources/extres/img/finish_w.svg'/></a></div>");
		}

		sb.append(
				"<div style='margin-right: 64px;overflow: hidden;word-break: break-word;white-space: nowrap;text-overflow: ellipsis;'>"
						+ work.getProjectName() + "</div>");
		sb.append("<div class='label_title' style='margin-right: 64px;overflow: hidden;word-break: break-word;white-space: nowrap;text-overflow: ellipsis;'>" + work.getFullName() + "</div>");
		sb.append(
				"<div style='width:100%;margin-top:2px;display:inline-flex;justify-content:space-between;'><div style='display:inline-flex;'>计划: "
						+ Formatter.getString(work.getPlanStart()) + " ~ "
						+Formatter.getString(work.getPlanFinish()) );

		String warningIcon = work.getWarningIcon();
		// 根据warningIcon是否为null，显示其进度状态
		if (warningIcon != null) {
			sb.append(warningIcon);
		}

		sb.append("</div>");
		String chargerInfo = work.getChargerInfoHtml();
		sb.append("<div style='margin-right:16px;'>负责: "
				+ (chargerInfo == null ? "<a class='layui-btn layui-btn-xs layui-btn-radius layui-btn-warm'>需指派</a>"
						: chargerInfo)
				+ "</div></div>");
		cell.setText(sb.toString());
	}

	private void renderContent(ViewerCell cell, Work work) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		gridItem.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		gridItem.setHeight(112);

		String perc;
		Double ind;

		StringBuffer sb = new StringBuffer();
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 显示进度指标信息
		sb.append(
				"<div style='padding-right:32px;margin-top:8px;width:100%;'><div style='display:inline-flex;justify-content:space-between;width:100%;'>");
		ind = work.getWAR();
		// ind = 0.2365555d;
		sb.append("<div style='width:112px;'>工作量：</div>");
		sb.append("<div class='layui-progress layui-progress-big' style='margin-left:16px;flex:auto;'>");
		if (ind != null) {
			NumberFormat df = DecimalFormat.getInstance();
			df.setMaximumFractionDigits(1);
			perc = df.format(100 * ind.doubleValue()) + "%";
			sb.append("<div class='layui-progress-bar' style='width:" + perc + ";'><span class='layui-progress-text'>"
					+ perc + "</span></div>");
		}
		sb.append("</div></div></div>");

		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 显示工期指标信息
		ind = work.getDAR();
		// ind = 0.9365555d;
		sb.append(
				"<div style='padding-right:32px;margin-top:8px;width:100%;'><div style='display:inline-flex;justify-content:space-between;width:100%;'>");
		sb.append("<div style='width:112px;'>工期(天)：" + "</div>");
		sb.append("<div class='layui-progress layui-progress-big' style='margin-left:16px;flex:auto;'>");
		if (ind != null) {
			NumberFormat df = DecimalFormat.getInstance();
			df.setMaximumFractionDigits(1);
			perc = df.format(100 * ind.doubleValue()) + "%";
			sb.append("<div class='layui-progress-bar' style='width:" + perc + ";'><span class='layui-progress-text'>"
					+ perc + "</span></div>");
		}
		sb.append("</div></div></div>");

		sb.append("<div style='display:inline-flex;width:100%;justify-content:flex-end;padding-right:24px'>");
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 工作包按钮
		List<TrackView> wps = work.getWorkPackageSetting();
		if (Check.isNotAssigned(wps)) {
			sb.append(
					"<a class='layui-btn layui-btn-sm layui-btn-primary' style='float:right;margin-top:8px;margin-right:4px;' href='"
							+ "openWorkPackage/default" + "' target='_rwt'>" + "工作包" + "</a>");
		} else if (wps.size() == 1) {
			sb.append(
					"<a class='layui-btn layui-btn-sm layui-btn-primary' style='float:right;margin-top:8px;margin-right:4px;' href='"
							+ "openWorkPackage/0" + "' target='_rwt'>" + wps.get(0).getName() + "</a>");

		} else {
			for (int i = 0; i < wps.size(); i++) {
				sb.append(
						"<a class='layui-btn layui-btn-sm layui-btn-primary' style='float:right;margin-top:8px;margin-right:4px;' href='"
								+ "openWorkPackage/" + i + "' target='_rwt'>" + wps.get(i).getName() + "</a>");
			}
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 指派按钮
		if (brui.getCurrentUserId().equals(work.getAssignerId())) {
			sb.append(
					"<a class='layui-btn layui-btn-sm layui-btn-normal' style='float:right; width:60px;margin-top:8px;margin-right:4px;' href='assignWork/"
							+ work.get_id() + "' target='_rwt'>指派</a>");
		}
		sb.append("</div>");

		cell.setText(sb.toString());
	}

}
