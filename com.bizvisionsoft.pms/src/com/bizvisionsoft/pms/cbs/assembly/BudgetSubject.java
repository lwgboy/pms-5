package com.bizvisionsoft.pms.cbs.assembly;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.eclipse.nebula.widgets.grid.Grid;
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
import com.bizvisionsoft.service.model.AccountItem;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSSubject;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.serviceconsumer.Services;

public class BudgetSubject extends BudgetGrid {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService bruiService;

	private CBSItem cbsItem;

	private ICBSScope scope;

	private List<CBSSubject> accountBudget;

	@Init
	public void init() {
		setContext(context);
		setConfig(context.getAssembly());
		setBruiService(bruiService);
		scope = (ICBSScope) context.getRootInput();
		cbsItem = (CBSItem) context.getParentContext().getInput();
		if (cbsItem == null) {
			ObjectId cbs_id = scope.getCBS_id();
			if (cbs_id != null) {
				cbsItem = Services.get(CBSService.class).get(cbs_id);
			}
		}
		if (cbsItem != null) {
			accountBudget = Services.get(CBSService.class).getCBSSubject(cbsItem.get_id());
		}
		super.init();
	}

	@CreateUI
	public void createUI(Composite parent) {
		if (cbsItem != null) {
			super.createUI(parent);
		} else {
			Layer.message("无法编制科目预算。", Layer.ICON_CANCEL);
		}
	}

	@Override
	protected Grid createGridControl(Composite parent) {
		Grid grid = super.createGridControl(parent);
		grid.setFooterVisible(true);
		return grid;
	}

	@Override
	public void setViewerInput() {
		ArrayList<CBSItem> roots = new ArrayList<CBSItem>();
		roots.add(cbsItem);
		super.setViewerInput(roots);
	}

	@Override
	protected String getBudgetText(Object element, String period) {
		Double value = null;
		if (element instanceof CBSItem) {
			value = getBudget((CBSItem) element, period);
		} else if (element instanceof AccountItem) {
			value = getBudget((AccountItem) element, period);
		}
		return Optional.ofNullable(value).map(v -> Util.getGenericMoneyFormatText(v)).orElse("");
	}

	@Override
	protected String getBudgetTotalText(Object element) {
		Double value = null;
		if (element instanceof CBSItem) {
			value = getBudgetTotal((CBSItem) element);
		} else if (element instanceof AccountItem) {
			value = getBudgetTotal((AccountItem) element);
		}
		return Optional.ofNullable(value).map(v -> Util.getGenericMoneyFormatText(v)).orElse("");
	}

	@Override
	protected String getBudgetYearSummaryText(Object element, String year) {
		Double value = null;
		if (element instanceof CBSItem) {
			value = getBudgetYearSummary((CBSItem) element, year);
		} else if (element instanceof AccountItem) {
			value = getBudgetYearSummary((AccountItem) element, year);
		}
		return Optional.ofNullable(value).map(v -> Util.getGenericMoneyFormatText(v)).orElse("");
	}

	private Double getBudget(Object item, String period) {
		if (accountBudget.stream().noneMatch(s -> s.getId().equals(period))) {
			return 0d;
		}

		List<AccountItem> children = null;
		if (item instanceof AccountItem) {
			children = ((AccountItem) item).listSubAccountItems();
		} else {
			children = ((CBSItem) item).listSubjects();
		}
		Double summary = 0d;
		if (children.isEmpty()) {
			summary = accountBudget.stream()
					.filter(s -> s.getSubjectNumber().equals(((AccountItem) item).getId()) && s.getId().equals(period))
					.findFirst().map(u -> u.getBudget()).orElse(0d);
		} else {
			for (Iterator<AccountItem> iterator = children.iterator(); iterator.hasNext();) {
				AccountItem child = (AccountItem) iterator.next();
				summary += getBudget(child, period);
			}
		}
		return summary;
	}

	private Double getBudgetYearSummary(Object item, String year) {
		if (accountBudget.stream().noneMatch(s -> s.getId().startsWith(year))) {
			return 0d;
		}

		List<AccountItem> children = null;
		if (item instanceof AccountItem) {
			children = ((AccountItem) item).listSubAccountItems();
		} else {
			children = ((CBSItem) item).listSubjects();
		}
		Double summary = 0d;
		if (children.isEmpty()) {
			// summary = accountBudget.stream().filter(
			// s -> s.getSubjectNumber().equals(((AccountItem) item).getId()) &&
			// s.getId().startsWith(year))
			// .forEach(u -> u.getBudget()).orElse(0d);

			List<Double> summarys = new ArrayList<Double>();
			accountBudget.stream().filter(
					s -> s.getSubjectNumber().equals(((AccountItem) item).getId()) && s.getId().startsWith(year))
					.forEach(c -> {
						summarys.add(c.getBudget());
					});
			for (Double d : summarys) {
				if (d != null) {
					summary += d.doubleValue();
				}
			}
		} else {
			for (Iterator<AccountItem> iterator = children.iterator(); iterator.hasNext();) {
				AccountItem child = (AccountItem) iterator.next();
				summary += getBudgetYearSummary(child, year);
			}
		}
		return summary;
	}

	private Double getBudgetTotal(Object item) {
		List<AccountItem> children = null;
		if (item instanceof AccountItem) {
			children = ((AccountItem) item).listSubAccountItems();
		} else {
			children = ((CBSItem) item).listSubjects();
		}
		Double summary = 0d;
		if (children.isEmpty()) {
			List<Double> summarys = new ArrayList<Double>();
			accountBudget.stream().filter(s -> s.getSubjectNumber().equals(((AccountItem) item).getId())).forEach(c -> {
				summarys.add(c.getBudget());
			});
			for (Double d : summarys) {
				if (d != null) {
					summary += d.doubleValue();
				}
			}
		} else {
			for (Iterator<AccountItem> iterator = children.iterator(); iterator.hasNext();) {
				AccountItem child = (AccountItem) iterator.next();
				summary += getBudgetTotal(child);
			}
		}
		return summary;
	}

	@Override
	protected Date[] getRange() {
		return scope.getCBSRange();
	}

	@Override
	protected Color getNumberColor(Object item) {
		if (item instanceof AccountItem && ((AccountItem) item).countSubAccountItems() == 0) {
			return null;
		} else if (item instanceof CBSItem) {
			return BruiColors.getColor(BruiColor.Grey_900);
		}
		return BruiColors.getColor(BruiColor.Grey_500);
	}

	@Override
	protected String getBudgetFootText(String name) {
		Double value = cbsItem.getBudget(name);
		return Optional.ofNullable(value).map(v -> Util.getGenericMoneyFormatText(v)).orElse("");
	}

	@Override
	protected String getBudgetTotalFootText() {
		Double value = cbsItem.getBudgetSummary();
		return Optional.ofNullable(value).map(v -> Util.getGenericMoneyFormatText(v)).orElse("");
	}

	@Override
	protected String getBudgetYearSummaryFootText(String name) {
		Double value = cbsItem.getBudgetYearSummary(name);
		return Optional.ofNullable(value).map(v -> Util.getGenericMoneyFormatText(v)).orElse("");
	}

	public void updateCBSSubjectBudget(AccountItem account, CBSSubject subject) {
		CBSSubject newSubject = Services.get(CBSService.class).upsertCBSSubjectBudget(subject);
		accountBudget.remove(subject);
		accountBudget.add(newSubject);
		viewer.refresh();
	}

}
