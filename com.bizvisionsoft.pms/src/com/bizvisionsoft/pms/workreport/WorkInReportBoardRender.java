package com.bizvisionsoft.pms.workreport;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkBoardInfo;
import com.bizvisionsoft.service.model.WorkInReport;
import com.bizvisionsoft.serviceconsumer.Services;

public class WorkInReportBoardRender {

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
				// TODO 操作
				if (e.text.startsWith("startWork/")) {
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

	private void finishWork(Work work) {
		if (brui.confirm("完成工作", "请确认完成工作<span style='color:red;'>" + work + "</span>。\n系统将记录现在时刻为工作的实际完成时间。")) {
			List<Result> result = Services.get(WorkService.class).finishWork(brui.command(work.get_id(), new Date()));
			if (result.isEmpty()) {
				Layer.message("工作已完成。");
				viewer.remove(work);
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
		Date actualStart = work.getActualStart();
		Date actualFinish = work.getActualFinish();
		// if (actualStart == null) {
		// sb.append("<div style='float:right;margin-right:16px;margin-top:0px;'><a
		// href='editor/" + work.get_id()
		// + "' target='_rwt'><img class='layui-btn layui-btn-sm' style='padding:6px
		// 10px;' src='rwt-resources/extres/img/start_w.svg'/></a></div>");
		// } else if (actualFinish == null) {
		// sb.append("<div style='float:right;margin-right:16px;margin-top:0px;'><a
		// href='finishWork/" + work.get_id()
		// + "' target='_rwt'><img class='layui-btn layui-btn-normal layui-btn-sm'
		// style='padding:6px 10px;'
		// src='rwt-resources/extres/img/finish_w.svg'/></a></div>");
		// }

		sb.append("<div style='font-size: 22px;'>" + work.getFullName() + "</div>");
		sb.append("<div style='width:100%;margin-top:2px;display:inline-flex;justify-content:space-between;'>");

		sb.append("<div>计划: " + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(work.getPlanStart()) + " ~ "
				+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(work.getPlanFinish()));
		sb.append("</div>");

		sb.append("<div>实际: "
				+ (actualStart == null ? "" : new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(actualStart)) + " ~ "
				+ (actualFinish == null ? "" : new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(actualFinish)));
		sb.append("</div>");

		WorkInReport workInReport = work.getWorkInReport();
		if (workInReport != null && workInReport.getEstimatedFinish() != null) {
			sb.append("<div>预计完成时间: "
					+ (actualStart == null ? "" : new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(actualStart)) );
			sb.append("</div>");
		}

		String chargerInfo = work.getChargerInfo();
		if (chargerInfo != null && !"".equals(chargerInfo))
			sb.append("<div>负责: " + chargerInfo + "</div>");

		String assignerInfo = work.getAssignerInfo();
		if (assignerInfo != null && !"".equals(assignerInfo))
			sb.append("<div style='margin-right:16px;'>指派: " + assignerInfo + "</div>");

		sb.append("</div>");
		cell.setText(sb.toString());
	}

	private void renderContent(ViewerCell cell, Work work) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		gridItem.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		gridItem.setHeight(132);
		WorkInReport workInReport = work.getWorkInReport();

		StringBuffer sb = new StringBuffer();
		sb.append(
				"<div style='display:inline-flex;padding-right:3px;margin-top:3px;width:33%;'><div style='justify-content:space-between;width:100%;'>");
		// ind = 0.2365555d;
		sb.append("<div style='width:112px;'>完成情况：</div>");
		sb.append("<div style='margin-left:16px;flex:auto;white-space:pre-wrap;'>");
		if (workInReport != null && workInReport.getExecutionInfo() != null)
			sb.append(workInReport.getExecutionInfo());
		sb.append("</div></div></div>");

		sb.append(
				"<div style='display:inline-flex;padding-right:3px;margin-top:3px;width:33%;'><div style='justify-content:space-between;width:100%;'>");
		// ind = 0.2365555d;
		sb.append("<div style='width:112px;'>存在的问题：</div>");
		sb.append("<div style='margin-left:16px;flex:auto;white-space:pre-wrap;'>");
		if (workInReport != null && workInReport.getQuestion() != null)
			sb.append(workInReport.getQuestion());
		sb.append("</div></div></div>");

		sb.append(
				"<div style='display:inline-flex;padding-right:3px;margin-top:3px;width:33%;'><div style='justify-content:space-between;width:100%;'>");
		// ind = 0.2365555d;
		sb.append("<div style='width:112px;'>项目经理批注：</div>");
		sb.append("<div style='margin-left:16px;flex:auto;white-space:pre-wrap;'>");
		if (workInReport != null && workInReport.getVerifierRemark() != null)
			sb.append(workInReport.getVerifierRemark());
		sb.append("</div></div></div>");

		cell.setText(sb.toString());
	}

}
