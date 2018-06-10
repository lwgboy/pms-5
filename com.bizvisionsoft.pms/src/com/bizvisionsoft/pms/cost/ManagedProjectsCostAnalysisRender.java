package com.bizvisionsoft.pms.cost;

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
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSSubjectCost;

public class ManagedProjectsCostAnalysisRender extends GridPartDefaultRender {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService bruiService;

	private String result;

	@Init
	private void init() {
		// ��ȡ��ǰ�ꡢ��
		Calendar currentCBSPeriod = Calendar.getInstance();
		result = "" + currentCBSPeriod.get(Calendar.YEAR);
	}

	@Override
	@GridRenderUpdateCell
	public void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell,
			@MethodParam(GridRenderUpdateCell.PARAM_COLUMN) Column column,
			@MethodParam(GridRenderUpdateCell.PARAM_VALUE) Object value,
			@MethodParam(GridRenderUpdateCell.PARAM_IMAGE) Object image) {
		Object element = cell.getElement();
		if ("totalCost".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// ��ȡ�ɱ�������ܳɱ�
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCostSummary();
			} else if (element instanceof CBSSubjectCost) {
				// ��ȡ��Ŀ�ɱ�������ܳɱ�
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCostSummary();
			}
		} else if ("totalBudget".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// ��ȡ�ɱ��������Ԥ��
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getBudgetSummary();
			} else if (element instanceof CBSSubjectCost) {
				// ��ȡ��Ŀ�ɱ��������Ԥ��
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getBudgetSummary();
			}
		} else if ("totalOverspend".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// ��ȡ�ɱ��������Ԥ��
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getOverspendSummary();
			} else if (element instanceof CBSSubjectCost) {
				// ��ȡ��Ŀ�ɱ��������Ԥ��
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getOverspendSummary();
			}
		} else if ("totalCAR".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// ��ȡ�ɱ��������CAR
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCARSummary();
			} else if (element instanceof CBSSubjectCost) {
				// ��ȡ��Ŀ�ɱ��������CAR
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCARSummary();
			}
		} else if ("totalBDR".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// ��ȡ�ɱ��������Ԥ��ƫ��
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getBDRSummary();
			} else if (element instanceof CBSSubjectCost) {
				// ��ȡ��Ŀ�ɱ��������Ԥ��ƫ��
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getBDRSummary();
			}
		} else if ("cost".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// ��ȡ�ɱ�������ڼ�ɱ�
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCost(result + "01", result + "12");
			} else if (element instanceof CBSSubjectCost) {
				// ��ȡ��Ŀ�ɱ�������ڼ�ɱ�
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCost(result + "01", result + "12");
			}
		} else if ("budget".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// ��ȡ�ɱ�������ڼ�Ԥ��
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getBudget(result + "01", result + "12");
			} else if (element instanceof CBSSubjectCost) {
				// ��ȡ��Ŀ�ɱ�������ڼ�Ԥ��
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getBudget(result + "01", result + "12");
			}
		} else if ("01".equals(column.getName()) || "02".equals(column.getName()) || "03".equals(column.getName())
				|| "04".equals(column.getName()) || "05".equals(column.getName()) || "06".equals(column.getName())
				|| "07".equals(column.getName()) || "08".equals(column.getName()) || "09".equals(column.getName())
				|| "10".equals(column.getName()) || "11".equals(column.getName()) || "12".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// ��ȡ�ɱ�������ڼ�CAR
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCost(result + column.getName());
			} else if (element instanceof CBSSubjectCost) {
				// ��ȡ��Ŀ�ɱ�������ڼ�CAR
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCost(result + column.getName());
			}
		}
		super.renderCell(cell, column, value, image);
	}

	@Override
	@GridRenderColumnHeader
	public void renderColumnHeader(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		// TODO û���޸�ColumnGroupHeader�ķ���
		// if ("period".equals(column.getName())) {
		// column.setText(result+ "��" );
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
	public int compare(@MethodParam(GridRenderCompare.PARAM_COLUMN) Column col,
			@MethodParam(GridRenderCompare.PARAM_ELEMENT1) Object e1,
			@MethodParam(GridRenderCompare.PARAM_ELEMENT2) Object e2) {
		return super.compare(col, e1, e2);
	}

}
