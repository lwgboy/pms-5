package com.bizvisionsoft.pms.workreport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUICreated;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkBoardInfo;
import com.bizvisionsoft.service.model.WorkInReport;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.service.model.WorkReportAssignment;
import com.mongodb.BasicDBObject;

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
				Object element = ((GridItem) e.item).getData();
				if (e.text.startsWith("edit/")) {
					edit((Work) element);
				} else if (e.text.startsWith("resource/")) {
					resource((Work) element);
				}
				viewer.refresh(element);
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

	private void resource(Work work) {
		brui.openContent(brui.getAssembly("工作报告-工作资源用量"),
				new WorkReportAssignment().setWork(work).setWorkReport((WorkReport) context.getInput()));
	}

	private void edit(Work work) {
		WorkInReport workInReport = work.getWorkInReport();
		if (workInReport == null) {
			workInReport = new WorkInReport();
			workInReport.setWork_id(work.get_id());
			workInReport.setWorkReport_id(((WorkReport) context.getInput()).get_id());
			work.setWorkInReport(workInReport);
		}
		Editor.open("工作报告-工作编辑器", context, workInReport, (r, i) -> {
			ServicesLoader.get(WorkReportService.class).updateWorkInReport(
					new FilterAndUpdate().filter(new BasicDBObject("_id", i.get_id())).set(r).bson());
			AUtil.simpleCopy(i, work.getWorkInReport());
		}).setTitle("工作执行情况");
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
		sb.append(
				"<div style='float:right;margin-right:4px;margin-top:0px;'><a class='cellbutton default' href='resource/"
						+ work.get_id()
						+ "' target='_rwt'><img src='rwt-resources/extres/img/right.svg' style='cursor:pointer;' width='16px' height='16px'></a></div>");

		sb.append("<div style='float:right;margin-right:4px;margin-top:0px;'><a class='cellbutton normal' href='edit/"
				+ work.get_id()
				+ "' target='_rwt'><img src='rwt-resources/extres/img/edit_w.svg' style='cursor:pointer;' width='16px' height='16px'></a></div>");

		// "<div style='position: absolute; overflow: hidden; -moz-user-select: none;
		// cursor: pointer; color: rgb(255, 255, 255); text-align: center;
		// font-family: 'Microsoft YaHei', Verdana, ';Lucida San', Arial, Helvetica,
		// sans-serif; font-size: 14px; font-weight: normal; font-style: normal;
		// background: rgb(0, 150, 136) none repeat scroll 0% 0%; opacity: 1; outline:
		// medium none currentcolor; z-index: 1; border-width: 1px; border-style: solid;
		// border-color: rgb(0, 150, 136); width: 100px; height: 32px; left: 108px; top:
		// 4px;' tabindex='10'>"
		// + "<div style=\"position: absolute; overflow: hidden; white-space: nowrap;
		// text-decoration: inherit; left: 12px; width: 74px; top: 6px; height:
		// 19px;\">"
		// + "<div
		// style=\"margin-left:4px;display:inline-block;\">创建干系人</div></div></div>"

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
		sb.append("<div>预计完成时间: ");
		if (workInReport != null && workInReport.getEstimatedFinish() != null)
			sb.append((actualStart == null ? "" : new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(actualStart)));
		sb.append("</div>");

		String chargerInfo = work.getChargerInfo();
		sb.append("<div>负责: ");
		if (chargerInfo != null && !"".equals(chargerInfo))
			sb.append(chargerInfo);
		sb.append("</div>");

		String assignerInfo = work.getAssignerInfo();
		sb.append("<div style='margin-right:16px;'>指派: ");
		if (assignerInfo != null && !"".equals(assignerInfo))
			sb.append(assignerInfo);
		sb.append("</div>");

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
