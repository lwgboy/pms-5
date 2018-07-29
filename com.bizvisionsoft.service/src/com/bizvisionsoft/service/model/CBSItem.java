package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.sn.CBSItemGenerator;
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
	@Generator(name = Generator.DEFAULT_NAME, key = Generator.DEFAULT_KEY, generator = CBSItemGenerator.class, callback = Generator.NONE_CALLBACK)
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
	private boolean behaviourAdd(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId scope_id) {
		return this.scope_id.equals(scope_id);
	}

	@Behavior({ "CBS/取消" })
	private boolean behaviourUnDistribute(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId scope_id) {
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

	@Behavior("项目成本管理/编辑成本")
	private boolean behaviourEdit() {
		Calendar cal = Calendar.getInstance();
		Date date = getNextSettlementDate();
		ICBSScope scopeRootObject = getScopeRootObject();

		String status = scopeRootObject.getStatus();
		if (ProjectStatus.Processing.equals(status) || ProjectStatus.Closing.equals(status)) {
			int newYear = cal.get(Calendar.YEAR);
			int newMonth = cal.get(Calendar.MONTH);
			cal.setTime(date);
			if (cal.get(Calendar.YEAR) == newYear && cal.get(Calendar.MONTH) == newMonth) {
				return false;
			}
			return children.size() == 0 && listSubjects().size() == 0;
		}
		return false;
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

	@Structure({ "项目成本管理/list", "项目成本管理（查看）/list", "项目预算成本对比分析/list" })
	public List<Object> listSubCBSItemsAndSubjects() {
		children.forEach(c -> {
			c.parent = this;
			c.settlementDate = getNextSettlementDate();
		});
		List<Object> result = new ArrayList<Object>(children);

		if (result.size() == 0) {
			List<CBSSubjectCost> cbsSubjects = new ArrayList<CBSSubjectCost>();
			listSubjects().forEach(s -> {
				cbsSubjects.add(new CBSSubjectCost().setCBSItem(this).setAccountItem(s));

			});
			result.addAll(cbsSubjects);
		}
		return result;
	}

	@Structure({ "项目成本管理/count", "项目成本管理（查看）/count", "项目预算成本对比分析/count" })
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

	public String getId() {
		return id;
	}

	public double getBudgetSummary() {
		//////////////////////////////////////////////////////////////////////
		//// BUG: 预算成本有子项时显示错误的问题。 如果有子项，取子项合计
		//////////////////////////////////////////////////////////////////////
		double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getBudgetSummary();
			}
		} else {
			List<CBSSubject> cbsSubjects = listCBSSubjects();
			if (cbsSubjects.size() > 0) {
				for (CBSSubject cbsSubject : cbsSubjects) {
					summary += Optional.ofNullable(cbsSubject.getBudget()).orElse(0d);
				}
			} else {
				List<CBSPeriod> cbsPeriods = listCBSPeriods();
				if (cbsPeriods.size() > 0) {
					for (CBSPeriod cbsPeriod : cbsPeriods) {
						summary += Optional.ofNullable(cbsPeriod.getBudget()).orElse(0d);
					}
				}
			}
		}
		return summary;
	}

	public double getBudget(String period) {
		//////////////////////////////////////////////////////////////////////
		//// BUG: 预算成本有子项时显示错误的问题。 如果有子项，取子项合计
		//////////////////////////////////////////////////////////////////////
		double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getBudget(period);
			}
		} else {
			List<CBSSubject> cbsSubjects = listCBSSubjects();
			if (cbsSubjects.size() > 0) {
				for (CBSSubject cbsSubject : cbsSubjects) {
					if (period.equals(cbsSubject.getId())) {
						summary += Optional.ofNullable(cbsSubject.getBudget()).orElse(0d);
					}
				}
			} else {
				List<CBSPeriod> cbsPeriods = listCBSPeriods();
				if (cbsPeriods.size() > 0) {
					for (CBSPeriod cbsPeriod : cbsPeriods) {
						if (period.equals(cbsPeriod.getId())) {
							summary += Optional.ofNullable(cbsPeriod.getBudget()).orElse(0d);
						}
					}
				}
			}
		}
		return summary;
	}

	public double getBudget(String startPeriod, String endPeriod) {
		//////////////////////////////////////////////////////////////////////
		//// BUG: 预算成本有子项时显示错误的问题。 如果有子项，取子项合计
		//////////////////////////////////////////////////////////////////////
		double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getBudget(startPeriod, endPeriod);
			}
		} else {
			List<CBSSubject> cbsSubjects = listCBSSubjects();
			if (cbsSubjects.size() > 0) {
				for (CBSSubject cbsSubject : cbsSubjects) {
					if (startPeriod.compareTo(cbsSubject.getId()) <= 0
							&& endPeriod.compareTo(cbsSubject.getId()) >= 0) {
						summary += Optional.ofNullable(cbsSubject.getBudget()).orElse(0d);
					}
				}
			} else {
				List<CBSPeriod> cbsPeriods = listCBSPeriods();
				if (cbsPeriods.size() > 0) {
					for (CBSPeriod cbsPeriod : cbsPeriods) {
						if (startPeriod.compareTo(cbsPeriod.getId()) <= 0
								&& endPeriod.compareTo(cbsPeriod.getId()) >= 0) {
							summary += Optional.ofNullable(cbsPeriod.getBudget()).orElse(0d);
						}
					}
				}
			}
		}
		return summary;
	}

	public double getBudgetYearSummary(String year) {
		//////////////////////////////////////////////////////////////////////
		//// BUG: 预算成本有子项时显示错误的问题。 如果有子项，取子项合计
		//////////////////////////////////////////////////////////////////////
		double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getBudgetYearSummary(year);
			}
		} else {
			List<CBSSubject> cbsSubjects = listCBSSubjects();
			if (cbsSubjects.size() > 0) {
				for (CBSSubject cbsSubject : cbsSubjects) {
					if (cbsSubject.getId().startsWith(year)) {
						summary += Optional.ofNullable(cbsSubject.getBudget()).orElse(0d);
					}
				}
			} else {
				List<CBSPeriod> cbsPeriods = listCBSPeriods();
				if (cbsPeriods.size() > 0) {
					for (CBSPeriod cbsPeriod : cbsPeriods) {
						if (cbsPeriod.getId().startsWith(year)) {
							summary += Optional.ofNullable(cbsPeriod.getBudget()).orElse(0d);
						}
					}
				}
			}
		}
		return summary;

	}

	@Exclude
	private List<CBSSubject> cbsSubjects;

	@Exclude
	private List<CBSPeriod> cbsPeriods;

	@Exclude
	private Date settlementDate;

	public double getCostSummary() {
		//////////////////////////////////////////////////////////////////////
		//// BUG: 预算成本有子项时显示错误的问题。 如果有子项，取子项合计
		//////////////////////////////////////////////////////////////////////
		double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
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

	public List<CBSSubject> listCBSSubjects() {
		if (cbsSubjects == null) {
			cbsSubjects = ServicesLoader.get(CBSService.class).getCBSSubject(_id);
		}
		return cbsSubjects;
	}

	public List<CBSPeriod> listCBSPeriods() {
		if (cbsPeriods == null) {
			cbsPeriods = ServicesLoader.get(CBSService.class).getCBSPeriod(_id);
		}
		return cbsPeriods;
	}

	public double getCost(String period) {
		//////////////////////////////////////////////////////////////////////
		//// BUG: 预算成本有子项时显示错误的问题。 如果有子项，取子项合计
		//////////////////////////////////////////////////////////////////////
		double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getCost(period);
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

	public double getCost(String startPeriod, String endPeriod) {
		//////////////////////////////////////////////////////////////////////
		//// BUG: 预算成本有子项时显示错误的问题。 如果有子项，取子项合计
		//////////////////////////////////////////////////////////////////////
		double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getCost(startPeriod, endPeriod);
			}
		} else {
			List<CBSSubject> cbsSubjects = listCBSSubjects();
			if (cbsSubjects.size() > 0) {
				for (CBSSubject cbsSubject : cbsSubjects) {
					if (startPeriod.compareTo(cbsSubject.getId()) <= 0
							&& endPeriod.compareTo(cbsSubject.getId()) >= 0) {
						summary += Optional.ofNullable(cbsSubject.getCost()).orElse(0d);
					}
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

	public Double getCARSummary() {
		double budgetSummary = getBudgetSummary();
		if (budgetSummary != 0d) {
			return 1d * getCostSummary() / budgetSummary;
		}
		return null;
	}

	public Double getCAR(String period) {
		double budget = getBudget(period);
		if (budget != 0d) {
			return getCost(period) / budget;
		}
		return null;
	}

	public Double getCAR(String startPeriod, String endPeriod) {
		double budget = getBudget(startPeriod, endPeriod);
		if (budget != 0d) {
			return 1d * getCost(startPeriod, endPeriod) / budget;
		}
		return null;
	}

	public Double getBDRSummary() {
		double budgetSummary = getBudgetSummary();
		if (budgetSummary != 0d) {
			return 1d * (getCostSummary() - budgetSummary) / budgetSummary;
		}
		return null;
	}

	public Double getBDR(String period) {
		double budget = getBudget(period);
		if (budget != 0d) {
			return 1d * (getCost(period) - budget) / budget;
		}
		return null;
	}

	public Double getBDR(String startPeriod, String endPeriod) {
		double budget = getBudget(startPeriod, endPeriod);
		if (budget != 0d) {
			return 1d * (getCost(startPeriod, endPeriod) - budget) / budget;
		}
		return null;
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

	@Exclude
	private ICBSScope scopeRootObject;

	public ICBSScope getScopeRootObject() {
		if (scopeRootObject == null) {
			scopeRootObject = ServicesLoader.get(CBSService.class).getICBSScopeRootWork(scope_id);
			if (scopeRootObject == null) {
				scopeRootObject = ServicesLoader.get(CBSService.class).getICBSScopeRootProject(scope_id);
			}
		}
		return scopeRootObject;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	// DEMO 奥飞
	/**
	 * 支付到内部还是外部，true为内部
	 */
	private Boolean internalPayment;

	public Boolean getInternalPayment() {
		return internalPayment;
	}

	public CBSItem setInternalPayment(Boolean internalPayment) {
		this.internalPayment = internalPayment;
		return this;
	}

	private Double qty;

	public Double getQty() {
		return qty;
	}

	public CBSItem setQty(Double qty) {
		this.qty = qty;
		return this;
	}

	private Double price;

	public Double getPrice() {
		return price;
	}

	public CBSItem setPrice(Double price) {
		this.price = price;
		return this;
	}

	public Double getTotalEstimation() {
		if (countSubCBSItems() == 0) {
			return (price == null ? 0 : price) * (qty == null ? 0 : qty);
		} else {
			double summary = 0d;
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getTotalEstimation();
			}
			return summary;
		}

	}

	private CBSEstimationSetting estimationSetting;

	public CBSEstimationSetting getEstimationSetting() {
		return estimationSetting;
	}

	public CBSItem setEstimationSetting(CBSEstimationSetting estimationSetting) {
		this.estimationSetting = estimationSetting;
		return this;
	}
	/////////////////////////////////////////////////////////////////////////////////////////
}
