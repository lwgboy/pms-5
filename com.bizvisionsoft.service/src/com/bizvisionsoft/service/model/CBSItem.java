package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;
import com.mongodb.BasicDBObject;

@PersistenceCollection("cbs")
public class CBSItem {

	////////////////////////////////////////////////////////////////////////////////////////////////
	// 基本的一些字段

	/** 标识 Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private ObjectId parent_id;

	/** 编号 Y **/
	@ReadValue
	@WriteValue
	private String id;

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private String description;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "CBS";

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@ReadValue
	@WriteValue
	private ObjectId scope_id;

	@Persistence
	@ReadValue
	private String scopename;

	@ReadValue
	@SetValue
	public String scopeId;

	@ReadValue
	@SetValue
	public String scopeCharger;

	@ReadValue
	@SetValue
	public Date scopePlanStart;

	@ReadValue
	@SetValue
	public Date scopePlanFinish;

	@ReadValue
	@SetValue
	public String scopeStatus;

	@Behavior({ "CBS/编辑和设定" })
	private boolean behaviourAdd(@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId scope_id) {
		return this.scope_id.equals(scope_id);
	}

	@Behavior({ "CBS/取消" })
	private boolean behaviourUnDistribute(@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId scope_id) {
		return !this.scope_id.equals(scope_id) && scopeRoot;
	}

	@Behavior({ "CBS/编辑", "CBS/分配" })
	private boolean behaviourEditName() {
		return !scopeRoot;
	}

	@Behavior({ "CBS/科目", "CBS/预算" })
	private boolean behaviourEditAmount() {
		return countSubCBSItems() == 0;
	}

	@Behavior({ "CBS/删除" })
	private boolean behaviourDelete() {
		return !scopeRoot && countSubCBSItems() == 0;
	}

	@Behavior({ "预算成本对比分析/打开项目预算成本对比分析" })
	private boolean behaviourOpen() {
		return scopeRoot;
	}

	@SetValue
	@ReadValue
	private Double budgetTotal;

	@SetValue
	private Document budget;

	private List<CBSItem> children;

	/**
	 * 客户端对象
	 */
	@Exclude
	private transient CBSItem parent;

	@SetValue("children")
	private void setChildren(List<ObjectId> childrenId) {
		children = ServicesLoader.get(CBSService.class)
				.createDataSet(new BasicDBObject("_id", new BasicDBObject("$in", childrenId)));
	}

	@Exclude
	private List<AccountItem> subjects;

	@Structure({ "CBSSubject/list", "CBSSubject（查看）/list" })
	public List<AccountItem> listSubjects() {
		if (subjects == null) {
			subjects = ServicesLoader.get(CommonService.class).getAccoutItemRoot();
		}
		return subjects;
	}

	@Structure({ "CBSSubject/count", "CBSSubject（查看）/count" })
	public long countSubjects() {
		if (subjects == null) {
			return ServicesLoader.get(CommonService.class).countAccoutItemRoot();
		} else {
			return subjects.size();
		}
	}

	@Structure({ "CBS/list", "CBS（查看）/list" })
	public List<CBSItem> listSubCBSItems() {
		children.forEach(c -> c.parent = this);
		return children;
	}

	@Structure({ "CBS/count", "CBS（查看）/count" })
	public long countSubCBSItems() {
		return children.size();
	}

	@Structure({ "项目成本管理/list", "项目预算成本对比分析/list" })
	public List<Object> listSubCBSItemsAndSubjects() {
		children.forEach(c -> {
			c.parent = this;
			c.settlementDate = getNextSettlementDate();
		});
		List<Object> result = new ArrayList<Object>(children);

		if (result.size() == 0) {
			if (subjects == null) {
				subjects = ServicesLoader.get(CommonService.class).getAccoutItemRoot();
			}
			List<CBSSubjectCost> cbsSubjects = new ArrayList<CBSSubjectCost>();
			subjects.forEach(s -> {
				cbsSubjects.add(new CBSSubjectCost().setCBSItem(this).setAccountItem(s));

			});
			result.addAll(cbsSubjects);
		}
		return result;
	}

	@Structure({ "项目成本管理/count", "项目预算成本对比分析/count" })
	public long countSubCBSItemsAndSubjects() {
		long result = children.size();
		if (result == 0) {
			if (subjects == null) {
				result += ServicesLoader.get(CommonService.class).countAccoutItemRoot();
			} else {
				result += subjects.size();
			}
		}
		return result;
	}

	@Structure({ "预算成本对比分析/list" })
	public List<Object> listSubCBSSubjects() {
		List<Object> result = new ArrayList<Object>();

		if (subjects == null) {
			subjects = ServicesLoader.get(CommonService.class).getAccoutItemRoot();
		}
		List<CBSSubjectCost> cbsSubjects = new ArrayList<CBSSubjectCost>();
		subjects.forEach(s -> {
			cbsSubjects.add(new CBSSubjectCost().setCBSItem(this).setAccountItem(s));

		});
		result.addAll(cbsSubjects);
		return result;
	}

	@Structure({ "预算成本对比分析/count" })
	public long countSubCBSSubjects() {
		long result = 0;
		if (subjects == null) {
			result += ServicesLoader.get(CommonService.class).countAccoutItemRoot();
		} else {
			result += subjects.size();
		}
		return result;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	private boolean scopeRoot;

	public ObjectId get_id() {
		return _id;
	}

	public void setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public void setScope_id(ObjectId scope_id) {
		this.scope_id = scope_id;
	}

	public void setScopeRoot(boolean scopeRoot) {
		this.scopeRoot = scopeRoot;
	}

	public boolean isScopeRoot() {
		return scopeRoot;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ObjectId getScope_id() {
		return scope_id;
	}

	public String getScopeName() {
		return scopename;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getBudgetSummary() {
		Double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getBudgetSummary();
			}
		}
		List<CBSSubject> cbsSubjects = listCBSSubjects();
		if (cbsSubjects.size() > 0) {
			for (CBSSubject cbsSubject : cbsSubjects) {
				summary += Optional.ofNullable(cbsSubject.getBudget()).orElse(0d);
			}
		}
		return summary;
	}

	public double getBudget(String period) {
		Double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getBudget(period);
			}
		}
		List<CBSSubject> cbsSubjects = listCBSSubjects();
		if (cbsSubjects.size() > 0) {
			for (CBSSubject cbsSubject : cbsSubjects) {
				if (period.equals(cbsSubject.getId())) {
					summary += Optional.ofNullable(cbsSubject.getBudget()).orElse(0d);
				}
			}
		}
		return summary;
	}

	public double getBudget(String startPeriod, String endPeriod) {
		double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getBudget(startPeriod, endPeriod);
			}
		}
		List<CBSSubject> cbsSubjects = listCBSSubjects();
		if (cbsSubjects.size() > 0) {
			for (CBSSubject cbsSubject : cbsSubjects) {
				if (startPeriod.compareTo(cbsSubject.getId()) <= 0 && endPeriod.compareTo(cbsSubject.getId()) >= 0) {
					summary += Optional.ofNullable(cbsSubject.getBudget()).orElse(0d);
				}
			}
		}
		return summary;
	}

	public double getBudgetYearSummary(String year) {
		Double summary = 0d;
		if (countSubCBSItems() == 0) {
			if (budget == null || budget.isEmpty()) {
				return 0d;
			}
			Iterator<String> iter = budget.keySet().iterator();
			while (iter.hasNext()) {
				String k = iter.next();
				if (k.startsWith(year)) {
					Object v = budget.get(k);
					if (v instanceof Number) {
						summary += ((Number) v).doubleValue();
					}
				}
			}
		} else {
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getBudgetYearSummary(year);
			}
		}
		return summary;

	}

	@Exclude
	private List<CBSSubject> cbsSubjects;

	@Exclude
	private Date settlementDate;

	public double getCostSummary() {
		Double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getCostSummary();
			}
		}
		List<CBSSubject> cbsSubjects = listCBSSubjects();
		if (cbsSubjects.size() > 0) {
			for (CBSSubject cbsSubject : cbsSubjects) {
				summary += Optional.ofNullable(cbsSubject.getCost()).orElse(0d);
			}
		}
		return summary;
	}

	public List<CBSSubject> listCBSSubjects() {
		if (cbsSubjects == null) {
			cbsSubjects = ServicesLoader.get(CBSService.class).getCBSSubject(_id);
		}
		return cbsSubjects;
	}

	public double getCost(String period) {
		Double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getCost(period);
			}
		}
		List<CBSSubject> cbsSubjects = listCBSSubjects();
		if (cbsSubjects.size() > 0) {
			for (CBSSubject cbsSubject : cbsSubjects) {
				if (period.equals(cbsSubject.getId())) {
					summary += Optional.ofNullable(cbsSubject.getCost()).orElse(0d);
				}
			}
		}
		return summary;
	}

	public double getCost(String startPeriod, String endPeriod) {
		double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getCost(startPeriod, endPeriod);
			}
		}
		List<CBSSubject> cbsSubjects = listCBSSubjects();
		if (cbsSubjects.size() > 0) {
			for (CBSSubject cbsSubject : cbsSubjects) {
				if (startPeriod.compareTo(cbsSubject.getId()) <= 0 && endPeriod.compareTo(cbsSubject.getId()) >= 0) {
					summary += Optional.ofNullable(cbsSubject.getCost()).orElse(0d);
				}
			}
		}
		return summary;
	}

	public static CBSItem getInstance(CBSItem parent) {
		CBSItem cbsItem = getInstance();

		cbsItem.setParent_id(parent.get_id());
		cbsItem.setScope_id(parent.getScope_id());
		cbsItem.setScopeRoot(false);
		cbsItem.setScopeName(parent.getScopeName());

		return cbsItem;
	}

	public static CBSItem getInstance(ICBSScope cbsScope, boolean scopeRoot) {
		CBSItem cbsItem = getInstance();
		cbsItem.setScope_id(cbsScope.getScope_id());
		cbsItem.setScopeName(cbsScope.getScopeName());
		cbsItem.setScopeRoot(scopeRoot);

		return cbsItem;
	}

	public static CBSItem getInstance() {
		CBSItem cbsItem = new CBSItem();
		cbsItem.set_id(new ObjectId());
		return cbsItem;
	}

	private void setScopeName(String scopeName) {
		this.scopename = scopeName;
	}

	public CBSItem getParent() {
		return parent;
	}

	public ObjectId getParent_id() {
		return parent_id;
	}

	public void addChild(CBSItem child) {
		child.parent = this;
		children.add(child);
	}

	public void addChild(List<CBSItem> childs) {
		childs.forEach(child -> {
			child.parent = this;
		});
		children.addAll(childs);
	}

	public void removeChild(CBSItem child) {
		children.remove(child);
	}

	public void setParent(CBSItem parent) {
		this.parent = parent;
	}

	public Date getNextSettlementDate() {
		if (settlementDate == null) {
			settlementDate = ServicesLoader.get(CBSService.class).getNextSettlementDate(scope_id);
		}
		return settlementDate;
	}

	public void updateCBSSubjects(CBSSubject cbsSubject) {
		if (parent != null) {
			parent.updateCBSSubjects(cbsSubject);
		}
		boolean update = false;
		for (CBSSubject cbss : listCBSSubjects()) {
			if (cbsSubject.getId().equals(cbss.getId()) && cbsSubject.getCbsItem_id().equals(cbss.getCbsItem_id())
					&& cbsSubject.getSubjectNumber().equals(cbss.getSubjectNumber())) {
				update = true;
				cbss.setCost(cbsSubject.getCost());
			}
		}
		if (!update && cbsSubjects.size() > 0) {
			cbsSubjects.add(cbsSubject);
		}
	}

	public Object getCARSummary() {
		Double budgetSummary = getBudgetSummary();
		if (budgetSummary != 0d) {
			return 1d * getCostSummary() / budgetSummary;
		}
		return "--";
	}

	public Object getCAR(String period) {
		Double budget = getBudget(period);
		if (budget != 0d) {
			return 1d * getCost(period) / budget;
		}
		return "--";
	}

	public Object getCAR(String startPeriod, String endPeriod) {
		Double budget = getBudget(startPeriod, endPeriod);
		if (budget != 0d) {
			return 1d * getCost(startPeriod, endPeriod) / budget;
		}
		return "--";
	}

	public Object getBDRSummary() {
		Double budgetSummary = getBudgetSummary();
		if (budgetSummary != 0d) {
			return 1d * (getCostSummary() - budgetSummary) / budgetSummary;
		}
		return "--";
	}

	public Object getBDR(String period) {
		Double budget = getBudget(period);
		if (budget != 0d) {
			return 1d * (getCost(period) - budget) / budget;
		}
		return "--";
	}

	public Object getBDR(String startPeriod, String endPeriod) {
		Double budget = getBudget(startPeriod, endPeriod);
		if (budget != 0d) {
			return 1d * (getCost(startPeriod, endPeriod) - budget) / budget;
		}
		return "--";
	}

	@SetValue
	public Double cbsSubjectBudget;

	@SetValue
	public Double cbsSubjectCost;

	public double getOverspend(String period) {
		return getCost(period) - getBudget(period);
	}

	public double getOverspend(String startPeriod, String endPeriod) {
		return getCost(startPeriod, endPeriod) - getBudget(startPeriod, endPeriod);
	}

	public double getOverspendSummary() {
		return getCostSummary() - getBudgetSummary();
	}
}
