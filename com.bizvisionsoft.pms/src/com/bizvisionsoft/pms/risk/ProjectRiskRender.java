package com.bizvisionsoft.pms.risk;

import java.util.function.BiConsumer;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;

import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnHeader;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.assembly.GridPartDefaultRender;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.model.RBSItem;
import com.bizvisionsoft.service.model.RiskEffect;
import com.bizvisionsoft.service.model.RiskResponse;
import com.bizvisionsoft.service.model.RiskResponseType;

public class ProjectRiskRender extends GridPartDefaultRender {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	@Override
	@GridRenderUpdateCell
	public void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell,
			@MethodParam(GridRenderUpdateCell.PARAM_COLUMN) Column column,
			@MethodParam(GridRenderUpdateCell.PARAM_INPUT_ELEMENT) Object element,
			@MethodParam(GridRenderUpdateCell.PARAM_VALUE) Object value,
			@MethodParam(GridRenderUpdateCell.PARAM_IMAGE) Object image,
			@MethodParam(GridRenderUpdateCell.PARAM_CALLBACK) BiConsumer<String, Object> callback) {
		super.renderCell(cell, column, element, value, image, callback);
		if (cell == null)
			return;
		if (cell.getElement() instanceof RBSItem) {
			cell.setBackground(BruiColors.getColor(BruiColor.Grey_50));// 设置背景色
		} else if (cell.getElement() instanceof RiskEffect) {// RBSItem
			GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
			// gridItem.setColumnSpan(1, 1);// 演示如何合并单元格
			gridItem.setColumnSpan(6, 7);
		} else if (cell.getElement() instanceof RiskResponseType) {
			cell.setBackground(BruiColors.getColor(BruiColor.Grey_100));// 设置背景色
			GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
			gridItem.setColumnSpan(0, 1);
			gridItem.setColumnSpan(4, 2);
			gridItem.setColumnSpan(7, 6);
		} else if (cell.getElement() instanceof RiskResponse) {
			GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
			gridItem.setColumnSpan(0, 1);
			gridItem.setColumnSpan(4, 2);
			gridItem.setColumnSpan(7, 6);
		}
	}

	@Override
	@GridRenderColumnHeader
	public void renderColumnHeader(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		super.renderColumnHeader(col, column);
	}

}
