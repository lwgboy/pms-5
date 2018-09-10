package com.bizvisionsoft.pms.cbs.assembly;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.AccountItem;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSSubject;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.serviceconsumer.Services;

public abstract class CBSSubjectGrid extends CBSGrid {


	protected CBSItem cbsItem;

	protected ICBSScope scope;

	protected List<CBSSubject> cbsSubjects;

	public void init() {
		scope = (ICBSScope) getContext().getRootInput();
		Object parentInput = getContext().getParentContext().getInput();
		if (parentInput instanceof CBSItem)
			cbsItem = (CBSItem) parentInput;

		if (cbsItem == null) {
			ObjectId cbs_id = scope.getCBS_id();
			if (cbs_id != null) {
				cbsItem = Services.get(CBSService.class).get(cbs_id);
			}
		}
		if (cbsItem != null) {
			cbsSubjects = Services.get(CBSService.class).getCBSSubject(cbsItem.get_id());
		}
		super.init();
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}

	@Override
	public void setViewerInput() {
		ArrayList<CBSItem> roots = new ArrayList<CBSItem>();
		roots.add(cbsItem);
		super.setViewerInput(roots);
	}

	@Override
	protected String getMonthlyAmountText(Object element, String period) {
		Double value = null;
		if (element instanceof CBSItem) {
			value = getMonthlyAmount((CBSItem) element, period);
		} else if (element instanceof AccountItem) {
			value = getMonthlyAmount((AccountItem) element, period);
		}
		return Optional.ofNullable(value).map(v -> Util.getGenericMoneyFormatText(v)).orElse("");
	}

	@Override
	protected String getTotalAmountText(Object element) {
		Double value = null;
		if (element instanceof CBSItem) {
			value = getTotalAmount((CBSItem) element);
		} else if (element instanceof AccountItem) {
			value = getTotalAmount((AccountItem) element);
		}
		return Optional.ofNullable(value).map(v -> Util.getGenericMoneyFormatText(v)).orElse("");
	}

	@Override
	protected String getYearlyAmountSummaryText(Object element, String year) {
		Double value = null;
		if (element instanceof CBSItem) {
			value = getYearlyAmountSummary((CBSItem) element, year);
		} else if (element instanceof AccountItem) {
			value = getYearlyAmountSummary((AccountItem) element, year);
		}
		return Optional.ofNullable(value).map(v -> Util.getGenericMoneyFormatText(v)).orElse("");
	}

	private Double getMonthlyAmount(Object item, String period) {
		if (cbsSubjects.stream().noneMatch(s -> s.getId().equals(period))) {
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
			summary = cbsSubjects.stream()
					.filter(s -> s.getSubjectNumber().equals(((AccountItem) item).getId()) && s.getId().equals(period))
					.findFirst().map(u -> getAmount(u)).orElse(0d);
		} else {
			for (Iterator<AccountItem> iterator = children.iterator(); iterator.hasNext();) {
				AccountItem child = (AccountItem) iterator.next();
				summary += getMonthlyAmount(child, period);
			}
		}
		return summary;
	}

	protected abstract Double getAmount(CBSSubject u);

	private Double getYearlyAmountSummary(Object item, String year) {
		if (cbsSubjects.stream().noneMatch(s -> s.getId().startsWith(year))) {
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

			List<Double> summarys = new ArrayList<Double>();
			cbsSubjects.stream().filter(
					s -> s.getSubjectNumber().equals(((AccountItem) item).getId()) && s.getId().startsWith(year))
					.forEach(c -> summarys.add(getAmount(c)));
			for (Double d : summarys) {
				if (d != null) {
					summary += d.doubleValue();
				}
			}
		} else {
			for (Iterator<AccountItem> iterator = children.iterator(); iterator.hasNext();) {
				AccountItem child = (AccountItem) iterator.next();
				summary += getYearlyAmountSummary(child, year);
			}
		}
		return summary;
	}

	private Double getTotalAmount(Object item) {
		List<AccountItem> children = null;
		if (item instanceof AccountItem) {
			children = ((AccountItem) item).listSubAccountItems();
		} else {
			children = ((CBSItem) item).listSubjects();
		}
		Double summary = 0d;
		if (children.isEmpty()) {
			List<Double> summarys = new ArrayList<Double>();
			cbsSubjects.stream().filter(s -> s.getSubjectNumber().equals(((AccountItem) item).getId()))
					.forEach(c -> summarys.add(getAmount(c)));
			for (Double d : summarys) {
				if (d != null) {
					summary += d.doubleValue();
				}
			}
		} else {
			for (Iterator<AccountItem> iterator = children.iterator(); iterator.hasNext();) {
				AccountItem child = (AccountItem) iterator.next();
				summary += getTotalAmount(child);
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
			return BruiColors.getColor(BruiColor.Grey_50);
		}
		return BruiColors.getColor(BruiColor.Grey_50);
	}

	public void updateCBSSubjectAmount(CBSSubject subject) {
		CBSSubject newSubject = getUpsertedCBSSubject(subject);
		cbsSubjects.remove(subject);
		cbsSubjects.add(newSubject);
		viewer.refresh();	
	}

	protected abstract CBSSubject getUpsertedCBSSubject(CBSSubject subject);

	@Override
	protected EditingSupport supportMonthlyEdit(GridViewerColumn vcol) {
		final String id = (String) vcol.getColumn().getData("name");
		return new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				try {
					double d = Util.getDoubleInput((String) value);
					CBSSubject subject = new CBSSubject().setCBSItem_id(cbsItem.get_id())
							.setSubjectNumber(((AccountItem) element).getId()).setId(id).setBudget(d);
					updateCBSSubjectAmount(subject);
				} catch (Exception e) {
					Layer.message(e.getMessage(), Layer.ICON_CANCEL);
				}
			}

			@Override
			protected Object getValue(Object element) {
				Double value = getMonthlyAmount((AccountItem) element, id);
				return value == null ? "" : value.toString();
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getGrid());
			}

			@Override
			protected boolean canEdit(Object element) {
				return element instanceof AccountItem;
			}
		};
	}

}
