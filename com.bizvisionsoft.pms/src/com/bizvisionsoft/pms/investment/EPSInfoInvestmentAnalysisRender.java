package com.bizvisionsoft.pms.investment;

import java.util.Calendar;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.grid.GridColumn;

import com.bizvisionsoft.annotations.ui.common.Init;
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
import com.bizvisionsoft.service.model.EPSInfo;

public class EPSInfoInvestmentAnalysisRender extends GridPartDefaultRender {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private String result;

	@Init
	private void init() {
		// 获取当前年、月
		Calendar currentCBSPeriod = Calendar.getInstance();
		result = "" + currentCBSPeriod.get(Calendar.YEAR);
	}

	@Override
	@GridRenderUpdateCell
	public void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell,
			@MethodParam(GridRenderUpdateCell.PARAM_COLUMN) Column column, @MethodParam(GridRenderUpdateCell.PARAM_VALUE) Object value,
			@MethodParam(GridRenderUpdateCell.PARAM_IMAGE) Object image) {
		Object element = cell.getElement();
		if ("cost".equals(column.getName())) {
			if (element instanceof EPSInfo) {
				// 获取成本管理的期间成本
				EPSInfo epsInfo = (EPSInfo) element;
				value = epsInfo.getDurationCost(result + "01", result + "12");
			}
		} else if ("profit".equals(column.getName())) {
			EPSInfo epsInfo = (EPSInfo) element;
			value = epsInfo.getDurationProfit(result + "01", result + "12");
		} else if ("roi".equals(column.getName())) {
			if (element instanceof EPSInfo) {
				// 获取成本管理的期间成本
				EPSInfo epsInfo = (EPSInfo) element;
				value = epsInfo.getROI();
			}
		} else if ("01".equals(column.getName()) || "02".equals(column.getName()) || "03".equals(column.getName())
				|| "04".equals(column.getName()) || "05".equals(column.getName()) || "06".equals(column.getName())
				|| "07".equals(column.getName()) || "08".equals(column.getName()) || "09".equals(column.getName())
				|| "10".equals(column.getName()) || "11".equals(column.getName()) || "12".equals(column.getName())) {
			if (element instanceof EPSInfo) {
				// 获取成本管理的期间成本
				EPSInfo epsInfo = (EPSInfo) element;
				value = epsInfo.getPeriodProfit(result + column.getName());
			}
		}
		super.renderCell(cell, column, value, image);
	}

	@Override
	@GridRenderColumnHeader
	public void renderColumnHeader(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		// TODO 没有修改ColumnGroupHeader的方法
		// if ("period".equals(column.getName())) {
		// column.setText(result+ "年" );
		// }
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
