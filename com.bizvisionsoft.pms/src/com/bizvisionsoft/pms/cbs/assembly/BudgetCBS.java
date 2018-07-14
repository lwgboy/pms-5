package com.bizvisionsoft.pms.cbs.assembly;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSPeriod;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.serviceconsumer.Services;

public class BudgetCBS extends BudgetGrid {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService bruiService;

	private ICBSScope scope;

	@Init
	public void init() {
		setContext(context);
		setConfig(context.getAssembly());
		setBruiService(bruiService);
		scope = (ICBSScope) context.getRootInput();
		super.init();
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}

	public void addCBSItem(CBSItem parentCBSItem, CBSItem cbsItemData) {
		try {
			CBSItem child = Services.get(CBSService.class).insertCBSItem(cbsItemData);
			parentCBSItem.addChild(child);
			viewer.refresh(parentCBSItem, true);
		} catch (Exception e) {
			String message = e.getMessage();
			if (message.indexOf("index") >= 0) {
				Layer.message("请勿在同一范围内重复添加相同编号的成本项。", Layer.ICON_CANCEL);
			}
		}
	}

	public void deleteCBSItem(CBSItem cbsItem) {
		CBSItem parentCBSItem = cbsItem.getParent();
		if (parentCBSItem == null) {
			throw new RuntimeException("不允许删除CBS根节点。");
		}
		Services.get(CBSService.class).delete(cbsItem.get_id());
		parentCBSItem.removeChild(cbsItem);
		viewer.refresh();
	}

	public void updateCBSPeriodBudget(CBSItem cbsItem, CBSPeriod periodData) {
		CBSItem parentCBSItem = cbsItem.getParent();
		if (parentCBSItem == null) {
			throw new RuntimeException("不允许更改CBS根节点预算。");
		}
		Services.get(CBSService.class).updateCBSPeriodBudget(periodData);
		CBSItem newCbsItem = Services.get(CBSService.class).get(((CBSItem) cbsItem).get_id());
		newCbsItem.setParent(cbsItem.getParent());
		replaceItem(cbsItem, newCbsItem);
		// viewer.update(cbsItem, null);
		viewer.refresh();
	}

	@Override
	protected Date[] getRange() {
		return scope.getCBSRange();
	}

	public ICBSScope getScope() {
		return scope;
	}

	@Override
	protected Color getNumberColor(Object item) {
		if (((CBSItem) item).countSubCBSItems() == 0) {
			return null;
		} else {
			return BruiColors.getColor(BruiColor.Grey_50);
		}
	}

	@Override
	protected String getBudgetTotalText(Object element) {
		return Util.getGenericMoneyFormatText(((CBSItem) element).getBudgetSummary());
	}

	@Override
	protected String getBudgetYearSummaryText(Object element, String year) {
		return Util.getGenericMoneyFormatText(((CBSItem) element).getBudgetYearSummary(year));
	}

	@Override
	protected String getBudgetText(Object element, String name) {
		return Util.getGenericMoneyFormatText(((CBSItem) element).getBudget(name));
	}

	/**
	 * 编辑各月预算 TODO : 考虑权限
	 */
	@Override
	protected EditingSupport supportMonthlyEdit(GridViewerColumn vcol) {
		final String name = (String) vcol.getColumn().getData("name");
		return new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				try {
					saveCBSItemPeriodBudgetInput((CBSItem) element, name, value);
				} catch (Exception e) {
					Layer.message(e.getMessage(), Layer.ICON_CANCEL);
				}
			}

			@Override
			protected Object getValue(Object element) {
				return Optional.ofNullable(((CBSItem) element).getBudget(name)).map(v -> {
					if (v == 0)
						return "";
					return "" + v;
				}).orElse("");
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getGrid());
			}

			@Override
			protected boolean canEdit(Object element) {
				return ((CBSItem) element).countSubCBSItems() == 0;
			}
		};
	}

	protected void saveCBSItemPeriodBudgetInput(CBSItem item, String name, Object input) throws Exception {
		double inputAmount;
		try {
			if ("".equals(input)) {
				inputAmount = 0;
			} else {
				inputAmount = Double.parseDouble(input.toString());
			}
		} catch (Exception e) {
			throw new Exception("请输入数字");
		}

		///////////////////////////////////////
		// 避免在没有修改的时候调用服务端程序
		double oldAmount = item.getBudget(name);
		if (inputAmount == oldAmount) {
			return;
		}
		///////////////////////////////////////

		CBSPeriod period = new CBSPeriod()//
				.setCBSItem_id(((CBSItem) item).get_id());
		Util.ifInstanceThen(context.getRootInput(), ICBSScope.class, r -> period.setRange(r.getCBSRange()));
		period.setBudget(inputAmount);
		period.setId(name);
		Date periodDate = new SimpleDateFormat("yyyyMM").parse(period.getId());
		period.checkRange(periodDate);
		updateCBSPeriodBudget(((CBSItem) item), period);
	}

}
