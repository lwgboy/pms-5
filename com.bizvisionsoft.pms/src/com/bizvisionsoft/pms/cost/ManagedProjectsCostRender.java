package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;
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
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSSubjectCost;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceconsumer.Services;

public class ManagedProjectsCostRender extends GridPartDefaultRender {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService bruiService;

	private String result;

	private ObjectId scope_id;

	private Document doc;

	@Init
	private void init() {
		// ��ȡ��ǰ�ꡢ��
		Calendar currentCBSPeriod = Calendar.getInstance();
		int newYear = currentCBSPeriod.get(Calendar.YEAR);
		int newMonth = currentCBSPeriod.get(Calendar.MONTH);

		Date date = null;
		// ��ȡ�����CBSItem �ӳɱ��������Ŀ�ɱ�����ʱ����contextInput��ȡ
		Object input = context.getInput();
		if (input == null) {
			// ��ȡ�����CBSItem ����Ŀ���׶δ���Ŀ�ɱ�����ʱ����contextRootInput��ȡ
			Object rootInput = context.getRootInput();
			if (rootInput instanceof ICBSScope) {
				ICBSScope icbsScope = (ICBSScope) rootInput;
				input = Services.get(CBSService.class).get(icbsScope.getCBS_id());
			}
		}
		// input��Ϊ��ʱ��Ϊ����Ŀ�ɱ�������ʱ��ǰ�ڼ����Ŀ�л�ȡ����Ϊ��Ŀ��һ�����·�
		if (input != null) {
			if (input instanceof CBSItem) {
				CBSItem cbsItem = (CBSItem) input;
				scope_id = cbsItem.getScope_id();
				date = cbsItem.getNextSettlementDate();
				// �����Ŀ��һ�����·ݵ��ڵ�ǰ�·ݣ�������Ϊ��ǰ�����·�
				currentCBSPeriod.setTime(date);
				if (currentCBSPeriod.get(Calendar.YEAR) == newYear
						&& currentCBSPeriod.get(Calendar.MONTH) == newMonth) {
					currentCBSPeriod.add(Calendar.MONTH, -1);
				}
			}
		}
		// ����ҳ�򿪳ɱ����������·�Ϊ��ǰϵͳ��������ڼ�
		if (date == null) {
			date = Services.get(CommonService.class).getCurrentCBSPeriod();
			currentCBSPeriod.setTime(date);
		}

		result = "" + currentCBSPeriod.get(Calendar.YEAR);
		result += String.format("%02d", currentCBSPeriod.get(java.util.Calendar.MONTH) + 1);

		String userId = bruiService.getCurrentUserId();
		if (scope_id != null)
			doc = Services.get(CBSService.class).getCBSSummary(scope_id, result, result,userId);
		else
			doc = Services.get(CBSService.class).getCBSSummary(result, result,userId);
	}

	@Override
	@GridRenderUpdateCell
	public void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell,
			@MethodParam(GridRenderUpdateCell.PARAM_COLUMN) Column column,
			@MethodParam(GridRenderUpdateCell.PARAM_VALUE) Object value,
			@MethodParam(GridRenderUpdateCell.PARAM_IMAGE) Object image) {
		Object element = cell.getElement();
		if ("periodCost".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// ��ȡ�ɱ�����ĵ�ǰ�ڼ�ɱ�
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCost(result);
			} else if (element instanceof CBSSubjectCost) {
				// ��ȡ��Ŀ�ɱ�����ĵ�ǰ�ڼ�ɱ�
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCost(result);
			}
		} else if ("totalCost".equals(column.getName())) {
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
				value = cbsItem.getCost(result);
			} else if (element instanceof CBSSubjectCost) {
				// ��ȡ��Ŀ�ɱ�������ڼ�ɱ�
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCost(result);
			}
		} else if ("budget".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// ��ȡ�ɱ�������ڼ�Ԥ��
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getBudget(result);
			} else if (element instanceof CBSSubjectCost) {
				// ��ȡ��Ŀ�ɱ�������ڼ�Ԥ��
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getBudget(result);
			}
		} else if ("overspend".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// ��ȡ�ɱ��������Ԥ��
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getOverspend(result);
			} else if (element instanceof CBSSubjectCost) {
				// ��ȡ��Ŀ�ɱ��������Ԥ��
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getOverspend(result);
			}
		} else if ("car".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// ��ȡ�ɱ�������ڼ�CAR
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCAR(result);
			} else if (element instanceof CBSSubjectCost) {
				// ��ȡ��Ŀ�ɱ�������ڼ�CAR
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCAR(result);
			}
		} else if ("bdr".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// ��ȡ�ɱ�������ڼ�Ԥ��ƫ��
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getBDR(result);
			} else if (element instanceof CBSSubjectCost) {
				// ��ȡ��Ŀ�ɱ�������ڼ�Ԥ��ƫ��
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getBDR(result);
			}
		}

		super.renderCell(cell, column, value, image);
	}

	@Override
	@GridRenderColumnHeader
	public void renderColumnHeader(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		// �޸ĵ��ڳɱ���ʾ����
		if ("periodCost".equals(column.getName())) {
			column.setText(
					"�ڼ䣺" + result.substring(0, 4) + "/" + Integer.parseInt(result.substring(4, 6)) + " ����λ����Ԫ��");
		}
		// TODO û���޸�ColumnGroupHeader�ķ���
		// if ("period".equals(column.getName())) {
		// column.setText(result.substring(0, 4) + "/" + result.substring(4, 6));
		// }
		super.renderColumnHeader(col, column);
	}

	@Override
	@GridRenderColumnFooter
	public void renderColumnFooter(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		Object name = col.getData("name");
		Object value = null;
		if ("totalCost".equals(name)) {
			value = doc.get("totalCost");
		} else if ("totalBudget".equals(name)) {
			value = doc.get("totalBudget");
		} else if ("totalOverspend".equals(name)) {
			double totalCost = doc.get("totalCost") != null ? ((Number) doc.get("totalCost")).doubleValue() : 0d;
			double totalBudget = doc.get("totalBudget") != null ? ((Number) doc.get("totalBudget")).doubleValue() : 0d;
			value = totalCost - totalBudget;
		} else if ("cost".equals(name)) {
			value = doc.get("cost");
		} else if ("budget".equals(name)) {
			value = doc.get("budget");
		} else if ("overspend".equals(name)) {
			double cost = doc.get("cost") != null ? ((Number) doc.get("cost")).doubleValue() : 0d;
			double budget = doc.get("budget") != null ? ((Number) doc.get("budget")).doubleValue() : 0d;
			value = cost - budget;
		}

		if (value != null && value instanceof Number && ((Number) value).doubleValue() != 0)
			col.setFooterText(Formatter.getString(value));

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
