package com.bizvisionsoft.pms.cost;

import java.util.Calendar;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.grid.GridColumn;

import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnFooter;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnHeader;
import com.bizvisionsoft.annotations.ui.grid.GridRenderCompare;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.assembly.GridPartDefaultRender;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSSubjectCost;
import com.bizvisionsoft.serviceconsumer.Services;

public class ManagedProjectsCostRender extends GridPartDefaultRender {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService bruiService;

	@Override
	@GridRenderUpdateCell
	public void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell,
			@MethodParam(GridRenderUpdateCell.PARAM_COLUMN) Column column,
			@MethodParam(GridRenderUpdateCell.PARAM_VALUE) Object value,
			@MethodParam(GridRenderUpdateCell.PARAM_IMAGE) Object image) {
		Object element = cell.getElement();
		if ("periodCost".equals(column.getName())) {
			if (element instanceof CBSItem) {
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCost(column.getText());
			} else if (element instanceof CBSSubjectCost) {
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCost(column.getText());
			}
		} else if ("totalCost".equals(column.getName())) {
			if (element instanceof CBSItem) {
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCostSummary();
			} else if (element instanceof CBSSubjectCost) {
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCostSummary();
			}
		}
		super.renderCell(cell, column, value, image);
	}

	@Override
	@GridRenderColumnHeader
	public void renderColumnHeader(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		if ("periodCost".equals(column.getName())) {
			Calendar currentCBSPeriod = Services.get(CommonService.class).getCurrentCBSPeriod();
			String result = "" + currentCBSPeriod.get(Calendar.YEAR);
			result += String.format("%02d", currentCBSPeriod.get(java.util.Calendar.MONTH) + 1);
			column.setText(result);
		}
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
