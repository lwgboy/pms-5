package com.bizvisionsoft.pms.workreport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.swt.SWT;

import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnHeader;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUICreated;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.assembly.GridPartDefaultRender;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.DateTimeInputDialog;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkReportItem;

public class ReportItemRender extends GridPartDefaultRender {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private GridTreeViewer viewer;

	@GridRenderUICreated
	private void uiCreated() {
		viewer = (GridTreeViewer) context.getContent("viewer");
		viewer.getGrid().addListener(SWT.Selection, e -> {
			if (e.text != null) {
				WorkReportItem item = (WorkReportItem) e.item.getData();
				if (e.text.startsWith("editStatement/")) {
					String initialValue = item.getStatement();
					InputDialog id = new InputDialog(brui.getCurrentShell(), "完成情况", "请输入工作完成情况", initialValue, null)
							.setTextMultiline(true);
					if (id.open() == InputDialog.OK) {
						System.out.println(id.getValue());
					}
				} else if (e.text.startsWith("editProblems/")) {
					System.out.println("editProblems");
					System.out.println(item);
				} else if (e.text.startsWith("editPMRemark/")) {
					System.out.println("editPMRemark");
					System.out.println(item);
				} else if (e.text.startsWith("editEstimatedFinish/")) {
					System.out.println("editEstimatedFinish");
					System.out.println(item);
					Date currentEstFinish = item.getEstimatedFinish();
					DateTimeInputDialog dtid = new DateTimeInputDialog(brui.getCurrentShell(), "预计完成日期",
							"请输入您估计本工作的预计完成日期", currentEstFinish, d -> {
								return d != null && d.before(new Date()) ? "请输入合法的日期" : null;
							});
					if (dtid.open() == DateTimeInputDialog.OK) {
						System.out.println(dtid.getValue());
						// 调服务保存
						// 刷新行
					}

				} else if (e.text.split("/").length > 1) {
				}
			}
		});
	}

	@Override
	@GridRenderUpdateCell
	public void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell,
			@MethodParam(GridRenderUpdateCell.PARAM_COLUMN) Column column,
			@MethodParam(GridRenderUpdateCell.PARAM_VALUE) Object value,
			@MethodParam(GridRenderUpdateCell.PARAM_IMAGE) Object image) {
		WorkReportItem reportItem = (WorkReportItem) cell.getElement();
		String text = "";
		if ("reportWorkPlan".equals(column.getName())) {
			text = getWorkPlanText(reportItem);
		} else if ("reportWorkStatement".equals(column.getName())) {
			text = getWorkStatementText(reportItem);
		} else if ("reportProblems".equals(column.getName())) {
			text = getWorkProblemsText(reportItem);
		} else if ("reportPMRemark".equals(column.getName())) {
			text = getPMRemarkText(reportItem);
		}
		cell.setText(text);
	}

	private String getPMRemarkText(WorkReportItem ri) {
		StringBuffer sb = new StringBuffer();
		// 表格行高120，减去上下两个8px
		sb.append("<div style='height:104px;display:block;'>");

		sb.append("<div style='height:76px;" // 4行文字高度
				+ "white-space:normal; word-break:break-all;" //
				+ "text-overflow: ellipsis;"//
				+ "text-overflow:-o-ellipsis-lastline;"//
				+ "overflow: hidden;"//
				+ "display: -webkit-box;"//
				+ "-webkit-box-orient:vertical;"//
				+ "-webkit-line-clamp:4;"// 谷歌上行显示省略号
				+ "'>" + Optional.ofNullable(ri.getPmRemark()).orElse("") + "</div>");
		if (!ri.isConfirmed() && brui.getCurrentUserId().equals(ri.getPMId())) {
			sb.append(
					"<a href='editPMRemark/' target='_rwt'><button class='layui-btn layui-btn-xs layui-btn-primary' style='position:absolute;bottom:0px;right:0px;'>"
							+ "<i class='layui-icon  layui-icon-edit'></i>" + "</button></a>");
		}

		sb.append("</div>");
		return sb.toString();
	}

	private String getWorkProblemsText(WorkReportItem ri) {
		StringBuffer sb = new StringBuffer();
		// 表格行高120，减去上下两个8px
		sb.append("<div style='height:104px;display:block;'>");

		sb.append("<div style='height:76px;" // 4行文字高度
				+ "white-space:normal; word-break:break-all;" //
				+ "text-overflow: ellipsis;"//
				+ "text-overflow:-o-ellipsis-lastline;"//
				+ "overflow: hidden;"//
				+ "display: -webkit-box;"//
				+ "-webkit-box-orient:vertical;"//
				+ "-webkit-line-clamp:4;"// 谷歌上行显示省略号
				+ "'>" + Optional.ofNullable(ri.getProblems()).orElse("") + "</div>");
		if (!ri.isConfirmed() && brui.getCurrentUserId().equals(ri.getReportorId())) {
			sb.append(
					"<a href='editProblems/' target='_rwt'><button class='layui-btn layui-btn-xs layui-btn-primary' style='position:absolute;bottom:0px;right:0px;'>"
							+ "<i class='layui-icon  layui-icon-edit'></i>" + "</button></a>");
		}

		sb.append("</div>");
		return sb.toString();
	}

	private String getWorkStatementText(WorkReportItem ri) {
		StringBuffer sb = new StringBuffer();
		// 表格行高120，减去上下两个8px
		sb.append("<div style='height:104px;display:block;'>");

		sb.append("<div style='height:76px;" // 4行文字高度
				+ "white-space:normal; word-break:break-all;" //
				+ "text-overflow: ellipsis;"//
				+ "text-overflow:-o-ellipsis-lastline;"//
				+ "overflow: hidden;"//
				+ "display: -webkit-box;"//
				+ "-webkit-box-orient:vertical;"//
				+ "-webkit-line-clamp:4;"// 谷歌上行显示省略号
				+ "'>" + Optional.ofNullable(ri.getStatement()).orElse("") + "</div>");
		if (!ri.isConfirmed() && brui.getCurrentUserId().equals(ri.getReportorId())) {
			sb.append(
					"<a href='editStatement/' target='_rwt'><button class='layui-btn layui-btn-xs layui-btn-primary' style='position:absolute;bottom:0px;right:0px;'>"
							+ "<i class='layui-icon  layui-icon-edit'></i>" + "</button></a>");
		}

		sb.append("</div>");
		return sb.toString();
	}

	private String getWorkPlanText(WorkReportItem ri) {
		Work work = ri.getWork();
		StringBuffer sb = new StringBuffer();
		sb.append("<div style='height:104px;display:block;'>");

		sb.append("<div class='label_title'>" + work.getFullName() + "</div>");
		sb.append("<div>计划进度：" + formatText(work.getPlanStart()) + " ~ " + formatText(work.getPlanFinish()) + "</div>");
		sb.append("<div>实际开始：" + formatText(work.getActualStart()) + "</div>");
		sb.append("<div>预计完成：" + formatText(ri.getEstimatedFinish()));

		sb.append(
				"<a href='editEstimatedFinish/' target='_rwt'><button class='layui-btn layui-btn-xs layui-btn-primary' style='position:absolute;bottom:0px;right:0px;'>"
						+ "预计完成时间" + "</button></a>");
		if (!ri.isConfirmed() && brui.getCurrentUserId().equals(ri.getReportorId())) {
		}
		sb.append("</div>");
		return sb.toString();
	}

	@Override
	@GridRenderColumnHeader
	public void renderColumnHeader(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		super.renderColumnHeader(col, column);
	}

	private String formatText(Date date) {
		return Optional.ofNullable(date).map(d -> new SimpleDateFormat("yyyy-MM-dd").format(d)).orElse("");
	}

}
