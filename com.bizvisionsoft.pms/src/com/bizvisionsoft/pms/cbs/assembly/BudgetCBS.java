package com.bizvisionsoft.pms.cbs.assembly;

import java.util.Date;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

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
		CBSItem child = Services.get(CBSService.class).insertCBSItem(cbsItemData);
		parentCBSItem.addChild(child);
		viewer.refresh(parentCBSItem, true);
	}

	public void deleteCBSItem(CBSItem cbsItem) {
		CBSItem parentCBSItem = cbsItem.getParent();
		if (parentCBSItem == null) {
			throw new RuntimeException("不允许删除CBS根节点。");
		}
		Services.get(CBSService.class).delete(cbsItem.get_id());
		parentCBSItem.removeChild(cbsItem);
		viewer.refresh(parentCBSItem, true);
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

	@Override
	protected Color getNumberColor(Object item) {
		if (((CBSItem) item).countSubCBSItems() == 0) {
			return null;
		} else {
			return BruiColors.getColor(BruiColor.Grey_500);
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

}
