package com.bizvisionsoft.pms.work;

import java.util.Date;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;

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
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ICommand;
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
				Work stage = ((Work) ((GridItem) e.item).getData());
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
		CommandHandler.run(ICommand.Close_Stage, //
				"ÇëÈ·ÈÏ½×¶Î¹Ø±Õ£º" + stage + "¡£", "½×¶ÎÒÑ¹Ø±Õ", "½×¶Î¹Ø±ÕÊ§°Ü", //
				() -> Services.get(WorkService.class)
						.closeStage(brui.command(stage.get_id(), new Date(), ICommand.Close_Stage)),
				() -> Services.get(WorkService.class)
						.closeStage(brui.command(stage.get_id(), new Date(), ICommand.Close_Stage_Ignore_Warrning)),
				code -> refreshGrid());
	}

	private void finish(Work stage) {
		CommandHandler.run(ICommand.Finish_Stage, //
				"ÇëÈ·ÈÏ½×¶ÎÊÕÎ²£º" + stage + "¡£", "ÒÑ¿ªÊ¼½×¶ÎÊÕÎ²", "½×¶ÎÊÕÎ²Ê§°Ü", //
				() -> Services.get(WorkService.class)
						.finishStage(brui.command(stage.get_id(), new Date(), ICommand.Finish_Stage)),
				() -> Services.get(WorkService.class)
						.finishStage(brui.command(stage.get_id(), new Date(), ICommand.Finish_Stage_Ignore_Warrning)),
				code -> refreshGrid());
	}

	private void refreshGrid() {
		GridPart content = (GridPart) context.getContent();
		content.setViewerInput();
	}

	private void start(Work stage) {
		CommandHandler.run(ICommand.Start_Stage, //
				"ÇëÈ·ÈÏÆô¶¯½×¶Î£º" + stage + "¡£", "½×¶ÎÆô¶¯Íê³É", "½×¶ÎÆô¶¯Ê§°Ü", //
				() -> Services.get(WorkService.class)
						.startStage(brui.command(stage.get_id(), new Date(), ICommand.Start_Stage)), //
				code -> refreshGrid());
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
