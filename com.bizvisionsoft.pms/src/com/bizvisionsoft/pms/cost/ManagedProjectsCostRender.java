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
		// 获取当前年、月
		Calendar currentCBSPeriod = Calendar.getInstance();
		int newYear = currentCBSPeriod.get(Calendar.YEAR);
		int newMonth = currentCBSPeriod.get(Calendar.MONTH);

		Date date = null;
		// 获取传入的CBSItem 从成本管理打开项目成本管理时，从contextInput获取
		Object input = context.getInput();
		if (input == null) {
			// 获取传入的CBSItem 从项目、阶段打开项目成本管理时，从contextRootInput获取
			Object rootInput = context.getRootInput();
			if (rootInput instanceof ICBSScope) {
				ICBSScope icbsScope = (ICBSScope) rootInput;
				input = Services.get(CBSService.class).get(icbsScope.getCBS_id());
			}
		}
		// input不为空时，为打开项目成本管理，这时当前期间从项目中获取，并为项目下一结算月份
		if (input != null) {
			if (input instanceof CBSItem) {
				CBSItem cbsItem = (CBSItem) input;
				scope_id = cbsItem.getScope_id();
				date = cbsItem.getNextSettlementDate();
				// 如果项目下一结算月份等于当前月份，则日期为当前结算月份
				currentCBSPeriod.setTime(date);
				if (currentCBSPeriod.get(Calendar.YEAR) == newYear
						&& currentCBSPeriod.get(Calendar.MONTH) == newMonth) {
					currentCBSPeriod.add(Calendar.MONTH, -1);
				}
			}
		}
		// 从首页打开成本管理，结算月份为当前系统整体结算期间
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
				// 获取成本管理的当前期间成本
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCost(result);
			} else if (element instanceof CBSSubjectCost) {
				// 获取项目成本管理的当前期间成本
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCost(result);
			}
		} else if ("totalCost".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// 获取成本管理的总成本
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCostSummary();
			} else if (element instanceof CBSSubjectCost) {
				// 获取项目成本管理的总成本
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCostSummary();
			}
		} else if ("totalBudget".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// 获取成本管理的总预算
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getBudgetSummary();
			} else if (element instanceof CBSSubjectCost) {
				// 获取项目成本管理的总预算
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getBudgetSummary();
			}
		} else if ("totalOverspend".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// 获取成本管理的总预算
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getOverspendSummary();
			} else if (element instanceof CBSSubjectCost) {
				// 获取项目成本管理的总预算
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getOverspendSummary();
			}
		} else if ("totalCAR".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// 获取成本管理的总CAR
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCARSummary();
			} else if (element instanceof CBSSubjectCost) {
				// 获取项目成本管理的总CAR
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCARSummary();
			}
		} else if ("totalBDR".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// 获取成本管理的总预算偏差
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getBDRSummary();
			} else if (element instanceof CBSSubjectCost) {
				// 获取项目成本管理的总预算偏差
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getBDRSummary();
			}
		} else if ("cost".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// 获取成本管理的期间成本
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCost(result);
			} else if (element instanceof CBSSubjectCost) {
				// 获取项目成本管理的期间成本
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCost(result);
			}
		} else if ("budget".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// 获取成本管理的期间预算
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getBudget(result);
			} else if (element instanceof CBSSubjectCost) {
				// 获取项目成本管理的期间预算
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getBudget(result);
			}
		} else if ("overspend".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// 获取成本管理的总预算
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getOverspend(result);
			} else if (element instanceof CBSSubjectCost) {
				// 获取项目成本管理的总预算
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getOverspend(result);
			}
		} else if ("car".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// 获取成本管理的期间CAR
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCAR(result);
			} else if (element instanceof CBSSubjectCost) {
				// 获取项目成本管理的期间CAR
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCAR(result);
			}
		} else if ("bdr".equals(column.getName())) {
			if (element instanceof CBSItem) {
				// 获取成本管理的期间预算偏差
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getBDR(result);
			} else if (element instanceof CBSSubjectCost) {
				// 获取项目成本管理的期间预算偏差
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
		// 修改当期成本显示列名
		if ("periodCost".equals(column.getName())) {
			column.setText(
					"期间：" + result.substring(0, 4) + "/" + Integer.parseInt(result.substring(4, 6)) + " （单位：万元）");
		}
		// TODO 没有修改ColumnGroupHeader的方法
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
