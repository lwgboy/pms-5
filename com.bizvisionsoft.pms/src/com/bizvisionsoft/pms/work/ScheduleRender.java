package com.bizvisionsoft.pms.work;

import java.util.Date;
import java.util.function.BiConsumer;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;

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
import com.bizvisionsoft.bruiengine.util.CommandHandler;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.ScopeRoleParameter;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class ScheduleRender extends GridPartDefaultRender {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private GridTreeViewer viewer;

	@GridRenderUICreated
	private void uiCreated() {
		viewer = (GridTreeViewer) context.getContent("viewer");
		viewer.getGrid().addListener(SWT.Selection, e -> {
			if (e.text != null) {
				Work stage = (Work) ((GridItem) e.item).getData();
				if (!checkPermission(stage)) {
					Layer.message("您未获得操作的许可", Layer.ICON_LOCK);
					return;
				}

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

	private boolean checkPermission(Work stage) {
		String currentUserId = br.getCurrentUserId();
		if (currentUserId.equals(stage.getChargerId()))
			return true;
		return ServicesLoader.get(OBSService.class).checkScopeRole(
				new ScopeRoleParameter(currentUserId).setRoles("PM", "PPM", "WM").setScopes(stage.get_id(), stage.getProject_id()),
				br.getDomain());
	}

	private void close(Work stage) {
		CommandHandler.run(ICommand.Close_Stage, //
				"请确认阶段关闭：" + stage + "。", "阶段已关闭", "阶段关闭失败", //
				() -> Services.get(WorkService.class).closeStage(br.command(stage.get_id(), new Date(), ICommand.Close_Stage),
						br.getDomain()),
				() -> Services.get(WorkService.class)
						.closeStage(br.command(stage.get_id(), new Date(), ICommand.Close_Stage_Ignore_Warrning), br.getDomain()),
				code -> refreshGrid());
	}

	private void finish(Work stage) {
		CommandHandler.run(ICommand.Finish_Stage, //
				"请确认阶段收尾：" + stage + "。", "已开始阶段收尾", "阶段收尾失败", //
				() -> Services.get(WorkService.class).finishStage(br.command(stage.get_id(), new Date(), ICommand.Finish_Stage),
						br.getDomain()),
				() -> Services.get(WorkService.class)
						.finishStage(br.command(stage.get_id(), new Date(), ICommand.Finish_Stage_Ignore_Warrning), br.getDomain()),
				code -> refreshGrid());
	}

	private void refreshGrid() {
		GridPart content = (GridPart) context.getContent();
		content.setViewerInput();
	}

	private void start(Work stage) {
		CommandHandler.run(ICommand.Start_Stage, //
				"请确认启动阶段：" + stage + "。", "阶段启动完成", "阶段启动失败", //
				() -> Services.get(WorkService.class).startStage(br.command(stage.get_id(), new Date(), ICommand.Start_Stage),
						br.getDomain()), //
				code -> refreshGrid());
	}

	@Override
	@GridRenderUpdateCell
	public void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell,
			@MethodParam(GridRenderUpdateCell.PARAM_COLUMN) Column column,
			@MethodParam(GridRenderUpdateCell.PARAM_INPUT_ELEMENT) Object element,
			@MethodParam(GridRenderUpdateCell.PARAM_VALUE) Object value, @MethodParam(GridRenderUpdateCell.PARAM_IMAGE) Object image,
			@MethodParam(GridRenderUpdateCell.PARAM_CALLBACK) BiConsumer<String, Object> callback) {
		super.renderCell(cell, column, element, value, image, callback);
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
	public int compare(@MethodParam(GridRenderCompare.PARAM_COLUMN) Column col, @MethodParam(GridRenderCompare.PARAM_ELEMENT1) Object e1,
			@MethodParam(GridRenderCompare.PARAM_ELEMENT2) Object e2) {
		return super.compare(col, e1, e2);
	}

}
