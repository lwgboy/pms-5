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
import com.bizvisionsoft.service.model.ICommand;
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
				if (e.text.startsWith("start/")) {
					start(stage);
				} else if (e.text.startsWith("finish/")) {
					finish(stage);
				} else if (e.text.startsWith("close/")) {
					close(stage);
				}
			}
		});
	}

	private void close(Work stage) {
		Project project = stage.getProject();
		if (project != null && ProjectStatus.Processing.equals(project.getStatus())) {
			String title = "阶段关闭";
			String sucessText = "阶段已关闭";
			if (!MessageDialog.openConfirm(brui.getCurrentShell(), title, "请确认关闭阶段：" + stage + "。")) {
				return;
			}
			List<Result> result = Services.get(WorkService.class)
					.closeStage(brui.command(stage.get_id(), new Date(), ICommand.Close_Stage));
			if (checkResult(result, title, sucessText)) {
				GridPart content = (GridPart) context.getContent();
				content.setViewerInput();
			}
		} else {
			Layer.message("项目进行中才能执行阶段关闭", Layer.ICON_CANCEL);
		}
	}

	private void finish(Work stage) {
		Project project = stage.getProject();
		if (project != null && ProjectStatus.Processing.equals(project.getStatus())) {
			String title = "阶段收尾";
			String sucessText = "阶段已完工";
			if (!MessageDialog.openConfirm(brui.getCurrentShell(), title, "请确认收尾阶段：" + stage + "。")) {
				return;
			}
			List<Result> result = Services.get(WorkService.class)
					.finishStage(brui.command(stage.get_id(), new Date(), ICommand.Finish_Stage));
			if (checkResult(result, title, sucessText)) {
				GridPart content = (GridPart) context.getContent();
				content.setViewerInput();
			}
		} else {
			Layer.message("项目进行中才能执行阶段收尾", Layer.ICON_CANCEL);
		}
	}

	private void start(Work stage) {
		Project project = stage.getProject();
		if (project != null && ProjectStatus.Processing.equals(project.getStatus())) {
			String title = "阶段启动";
			String sucessText = "阶段已启动";
			if (!MessageDialog.openConfirm(brui.getCurrentShell(), title, "请确认启动阶段：" + stage + "。")) {
				return;
			}
			List<Result> result = Services.get(WorkService.class)
					.startStage(brui.command(stage.get_id(), new Date(), ICommand.Start_Stage));
			if (checkResult(result, title, sucessText)) {
				GridPart content = (GridPart) context.getContent();
				content.setViewerInput();
			}

		} else {
			Layer.message("项目进行中才能执行阶段启动", Layer.ICON_CANCEL);
		}
	}

	private boolean checkResult(List<Result> result, String title, String sucessText) {
		boolean hasError = false;
		boolean hasWarning = false;
		Shell shell = brui.getCurrentShell();

		String message = "";
		if (!result.isEmpty()) {
			for (Result r : result)
				if (Result.TYPE_ERROR == r.type) {
					hasError = true;
					message += "错误：" + r.message + "<br>";
				} else if (Result.TYPE_WARNING == r.type) {
					hasError = true;
					message += "警告：" + r.message + "<br>";
				} else {
					message += "信息：" + r.message + "<br>";
				}
		}

		if (message.isEmpty()) {
			Layer.message(sucessText);
		} else {
			if (hasError) {
				MessageDialog.openError(shell, title, message);
				return false;
			} else if (hasWarning) {
				MessageDialog.openWarning(shell, title, sucessText + "，请注意以下提示信息：<br>" + message);
			} else {
				MessageDialog.openInformation(shell, title, sucessText + "，请注意以下提示信息：<br>" + message);
			}
		}
		return true;
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
