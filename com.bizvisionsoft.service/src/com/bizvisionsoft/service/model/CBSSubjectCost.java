package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.ServicesLoader;

public class CBSSubjectCost implements Comparable<CBSSubjectCost>, ICBSAmount {

	private CBSItem cbsItem;

	@ReadValue
	private String id;

	@ReadValue
	private String name;

	@ReadValue("scopeId")
	public String getId() {
		return id;
	}

	@ReadValue("scopename")
	public String getName() {
		return name;
	}

	private CBSSubjectCost parent;

	private List<CBSSubjectCost> children = new ArrayList<CBSSubjectCost>();

	public CBSSubjectCost setCBSItem(CBSItem cbsItem) {
		this.cbsItem = cbsItem;
		return this;
	}

	public CBSSubjectCost setAccountItem(AccountItem accountItem) {
		this.id = accountItem.getId();
		this.name = accountItem.getName();

		for (AccountItem a : accountItem.listSubAccountItems()) {
			children.add(new CBSSubjectCost().setDomain(domain).setCBSItem(cbsItem).setParent(this).setAccountItem(a));
		}

		return this;
	}

	public CBSSubjectCost setParent(CBSSubjectCost parent) {
		this.parent = parent;
		return this;
	}

	@Structure("list")
	public List<CBSSubjectCost> listSubAccountItems() {
		Collections.sort(children);
		return children;
	}

	@Structure("count")
	public long countSubAccountItems() {
		return children.size();
	}

	@Override
	public int compareTo(CBSSubjectCost o) {
		return id.compareTo(o.id);
	}

	@Exclude
	private List<CBSSubject> cbsSubjects;

	public String domain;

	public List<CBSSubject> listCBSSubjects() {
		if (cbsSubjects == null) {
			cbsSubjects = ServicesLoader.get(CBSService.class).getAllSubCBSSubjectByNumber(cbsItem.get_id(), id, domain);
		}
		return cbsSubjects;
	}

	public double getBudgetSummary() {
		Double summary = 0d;
		if (children.size() > 0) {
			Iterator<CBSSubjectCost> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getBudgetSummary();
			}
		} else {
			List<CBSSubject> cbsSubjects = listCBSSubjects();
			if (cbsSubjects.size() > 0) {
				for (CBSSubject cbsSubject : cbsSubjects) {
					summary += Optional.ofNullable(cbsSubject.getBudget()).orElse(0d);
				}
			}
		}
		return summary;
	}

	public double getPeriodBudget(String period) {
		Double summary = 0d;
		if (children.size() > 0) {
			Iterator<CBSSubjectCost> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getPeriodBudget(period);
			}
		} else {
			List<CBSSubject> cbsSubjects = listCBSSubjects();
			if (cbsSubjects.size() > 0) {
				for (CBSSubject cbsSubject : cbsSubjects) {
					if (period.equals(cbsSubject.getId())) {
						summary += Optional.ofNullable(cbsSubject.getBudget()).orElse(0d);
					}
				}
			}
		}
		return summary;
	}

	public double getDurationBudget(String startPeriod, String endPeriod) {
		Double summary = 0d;
		if (children.size() > 0) {
			Iterator<CBSSubjectCost> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getDurationBudget(startPeriod, endPeriod);
			}
		} else {
			List<CBSSubject> cbsSubjects = listCBSSubjects();
			if (cbsSubjects.size() > 0) {
				for (CBSSubject cbsSubject : cbsSubjects) {
					if (startPeriod.compareTo(cbsSubject.getId()) <= 0 && endPeriod.compareTo(cbsSubject.getId()) >= 0) {
						summary += Optional.ofNullable(cbsSubject.getBudget()).orElse(0d);
					}
				}
			}
		}
		return summary;
	}

	public double getCostSummary() {
		Double summary = 0d;
		if (children.size() > 0) {
			Iterator<CBSSubjectCost> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getCostSummary();
			}
		} else {
			List<CBSSubject> cbsSubjects = listCBSSubjects();
			if (cbsSubjects.size() > 0) {
				for (CBSSubject cbsSubject : cbsSubjects) {
					summary += Optional.ofNullable(cbsSubject.getCost()).orElse(0d);
				}
			}
		}
		return summary;
	}

	public double getPeriodCost(String period) {
		Double summary = 0d;
		if (children.size() > 0) {
			Iterator<CBSSubjectCost> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getPeriodCost(period);
			}
		} else {
			List<CBSSubject> cbsSubjects = listCBSSubjects();
			if (cbsSubjects.size() > 0) {
				for (CBSSubject cbsSubject : cbsSubjects) {
					if (period.equals(cbsSubject.getId())) {
						summary += Optional.ofNullable(cbsSubject.getCost()).orElse(0d);
					}
				}
			}
		}
		return summary;
	}

	public double getDurationCost(String startPeriod, String endPeriod) {
		Double summary = 0d;
		if (children.size() > 0) {
			Iterator<CBSSubjectCost> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getDurationCost(startPeriod, endPeriod);
			}
		} else {
			List<CBSSubject> cbsSubjects = listCBSSubjects();
			if (cbsSubjects.size() > 0) {
				for (CBSSubject cbsSubject : cbsSubjects) {
					if (startPeriod.compareTo(cbsSubject.getId()) <= 0 && endPeriod.compareTo(cbsSubject.getId()) >= 0) {
						summary += Optional.ofNullable(cbsSubject.getCost()).orElse(0d);
					}
				}
			}
		}
		return summary;
	}

	public Double getCARSummary() {
		Double budgetSummary = getBudgetSummary();
		if (budgetSummary != 0d) {
			return 1d * getCostSummary() / budgetSummary;
		}
		return null;
	}

	public Double getPeriodCAR(String period) {
		Double budget = getPeriodBudget(period);
		if (budget != 0d) {
			return 1d * getPeriodCost(period) / budget;
		}
		return null;
	}

	public Double getDurationCAR(String startPeriod, String endPeriod) {
		Double budget = getDurationBudget(startPeriod, endPeriod);
		if (budget != 0d) {
			return 1d * getDurationCost(startPeriod, endPeriod) / budget;
		}
		return null;
	}

	public Double getBDRSummary() {
		Double budgetSummary = getBudgetSummary();
		if (budgetSummary != 0d) {
			return 1d * (getCostSummary() - budgetSummary) / budgetSummary;
		}
		return null;
	}

	public Double getPeriodBDR(String period) {
		Double budget = getPeriodBudget(period);
		if (budget != 0d) {
			return 1d * (getPeriodCost(period) - budget) / budget;
		}
		return null;
	}

	public Double getDurationBDR(String startPeriod, String endPeriod) {
		Double budget = getDurationBudget(startPeriod, endPeriod);
		if (budget != 0d) {
			return 1d * (getDurationCost(startPeriod, endPeriod) - budget) / budget;
		}
		return null;
	}

	@Behavior("项目成本管理/编辑成本")
	private boolean behaviourEdit(@MethodParam(MethodParam.DOMAIN) String domain) {
		Calendar cal = Calendar.getInstance();
		Date date = cbsItem.getNextSettlementDate();
		ICBSScope scopeRootObject = cbsItem.getScopeRootObject();

		String status = scopeRootObject.getStatus();
		if (ProjectStatus.Processing.equals(status) || ProjectStatus.Closing.equals(status)) {
			int newYear = cal.get(Calendar.YEAR);
			int newMonth = cal.get(Calendar.MONTH);
			cal.setTime(date);
			if (cal.get(Calendar.YEAR) == newYear && cal.get(Calendar.MONTH) == newMonth) {
				return false;
			}
			return children.size() == 0;
		}
		return false;
	}

	public CBSSubject getCBSSubjects(String id) {
		for (CBSSubject s : listCBSSubjects()) {
			if (id.equals(s.getId())) {
				return s;
			}
		}
		return null;
	}

	public CBSItem getCbsItem() {
		return cbsItem;
	}

	public void updateCBSSubjects(CBSSubject cbsSubject) {
		if (parent != null) {
			parent.updateCBSSubjects(cbsSubject);
		}
		cbsItem.updateCBSSubjects(cbsSubject);
		boolean update = false;
		for (CBSSubject cbss : listCBSSubjects()) {
			if (cbsSubject.getId().equals(cbss.getId()) && cbsSubject.getCbsItem_id().equals(cbss.getCbsItem_id())
					&& cbsSubject.getSubjectNumber().equals(cbss.getSubjectNumber())) {
				update = true;
				cbss.setCost(cbss.getCost());
			}
		}
		if (!update) {
			cbsSubjects.add(cbsSubject);
		}
	}

	public double getPeriodOverspend(String period) {
		return getPeriodCost(period) - getPeriodBudget(period);
	}

	public double getDurationOverspend(String startPeriod, String endPeriod) {
		return getDurationCost(startPeriod, endPeriod) - getDurationBudget(startPeriod, endPeriod);
	}

	public double getOverspendSummary() {
		return getCostSummary() - getBudgetSummary();
	}

	public CBSSubjectCost setDomain(String domain) {
		this.domain = domain;
		return this;
	}

}
