package com.bizvisionsoft.pms.work;

import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnFooter;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnHeader;
import com.bizvisionsoft.annotations.ui.grid.GridRenderCompare;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUICreated;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.assembly.GridPartDefaultRender;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class ScheduleRender extends GridPartDefaultRender {

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
				Work stage = Services.get(WorkService.class)
						.getOpenStage(((Work) ((GridItem) e.item).getData()).get_id(), brui.getCurrentUserId());
				Project project = stage.getProject();
				Shell shell = brui.getCurrentShell();
				if (e.text.startsWith("start/")) {
					if (project != null && ProjectStatus.Processing.equals(project.getStatus())) {

						boolean ok = MessageDialog.openConfirm(shell, "启动阶段",
								"请确认启动阶段" + stage + "。\n系统将记录现在时刻为阶段的启动时间，并向本阶段项目组成员发出启动通知。");
						if (!ok) {
							return;
						}
						List<Result> result = Services.get(WorkService.class).startStage(brui.command(stage.get_id()));
						boolean b = true;
						String message = "";
						if (!result.isEmpty()) {
							for (Result r : result)
								if (Result.TYPE_ERROR == r.type) {
									Layer.message(r.message, Layer.ICON_CANCEL);
									b = false;
								} else {
									message += r.message + "<br>";
								}
						}

						if (b) {
							message = "阶段已启动。<br>" + message;
							Layer.message(message);
							GridPart content = (GridPart) context.getContent();
							content.setViewerInput();
						}
					} else {
						Layer.message("阶段所在项目未启动，无法启动阶段。", Layer.ICON_CANCEL);
					}
				} else if (e.text.startsWith("closing/")) {
					if (project != null && ProjectStatus.Processing.equals(project.getStatus())) {

						boolean ok = MessageDialog.openConfirm(shell, "完工阶段",
								"请确认完工阶段" + stage + "。\n系统将记录现在时刻为阶段的完工时间，并向本阶段项目组成员发出完工通知。");
						if (!ok) {
							return;
						}
						List<Result> result = Services.get(WorkService.class)
								.finishStage(brui.command(stage.get_id(), new Date()));
						boolean b = true;
						String message = "";
						if (!result.isEmpty()) {
							for (Result r : result)
								if (Result.TYPE_ERROR == r.type) {
									Layer.message(r.message, Layer.ICON_CANCEL);
									b = false;
								} else {
									message += r.message + "<br>";
								}
						}

						if (b) {
							message = "阶段已完工。<br>" + message;
							Layer.message(message);
							GridPart content = (GridPart) context.getContent();
							content.setViewerInput();
						}
						// TODO 显示多条错误信息的通用方法
					} else {
						Layer.message("阶段所在项目未启动，无法完工阶段。", Layer.ICON_CANCEL);
					}
				} else if (e.text.startsWith("closed/")) {
					boolean ok = MessageDialog.openConfirm(shell, "关闭阶段",
							"请确认关闭阶段" + stage + "。\n系统将记录现在时刻为阶段的关闭时间，并向本阶段项目组成员发出关闭通知。");
					if (!ok) {
						return;
					}
					List<Result> result = Services.get(WorkService.class)
							.closeStage(brui.command(stage.get_id(), new Date()));
					if (result.isEmpty()) {
						Layer.message("阶段已关闭。");
						GridPart content = (GridPart) context.getContent();
						content.setViewerInput();
					}
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
		super.renderCell(cell, column, value, image);
	}

	@Override
	@GridRenderColumnHeader
	public void renderColumnHeader(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		super.renderColumnHeader(col, column);
	}

	@Override
	@GridRenderColumnFooter
	public void renderColumnFooter(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		super.renderColumnFooter(col, column);
	}

	@Override
	@GridRenderCompare
	public int compare(@MethodParam(GridRenderCompare.PARAM_COLUMN) Column col,
			@MethodParam(GridRenderCompare.PARAM_ELEMENT1) Object e1,
			@MethodParam(GridRenderCompare.PARAM_ELEMENT2) Object e2) {
		return super.compare(col, e1, e2);
	}

}
