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
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.sn.CBSItemGenerator;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;
import com.mongodb.BasicDBObject;

@PersistenceCollection("cbs")
public class CBSItem implements ICBSAmount {

	////////////////////////////////////////////////////////////////////////////////////////////////
	// ������һЩ�ֶ�

	/** ��ʶ Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private ObjectId parent_id;

	/** ��� Y **/
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
	public static final String typeName = "CBS";

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

	@SetValue
	@ReadValue
	private String scopeChargerInfo;

	@SetValue
	private UserMeta scopeChargerInfo_meta;

	@ReadValue("scopeChargerInfoHtml")
	public String getScopeChargerInfoHtml() {
		if (scopeChargerInfo == null) {
			return "";
		}
		return "<div class='brui_ly_hline'>" + warpperScopeChargerInfo() + "</div>";
	}

	public String warpperScopeChargerInfo() {
		return MetaInfoWarpper.userInfo(scopeChargerInfo_meta, scopeChargerInfo);
	}

	@ReadValue
	@SetValue
	public Date scopePlanStart;

	@ReadValue
	@SetValue
	public Date scopePlanFinish;

	@ReadValue
	@SetValue
	public String scopeStatus;
	
	@Exclude
	public String domain;

	@Behavior({ "CBS/�༭���趨" })
	private boolean behaviourAdd(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId scope_id) {
		return this.scope_id.equals(scope_id);
	}

	@Behavior({ "CBS/ȡ��" })
	private boolean behaviourUnDistribute(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId scope_id) {
		return !this.scope_id.equals(scope_id) && scopeRoot;
	}

	@Behavior({ "CBS/�༭", "CBS/����" })
	private boolean behaviourEditName() {
		return !scopeRoot;
	}

	@Behavior({ "CBS/��Ŀ", "CBS/Ԥ��" })
	private boolean behaviourEditAmount() {
		return countSubCBSItems() == 0;
	}

	@Behavior({ "CBS/ɾ��" })
	private boolean behaviourDelete() {
		return !scopeRoot && countSubCBSItems() == 0;
	}

	@Behavior({ "Ԥ��ɱ��Աȷ���/����ĿԤ��ɱ��Աȷ���" })
	private boolean behaviourOpen() {
		return scopeRoot;
	}

	@Behavior("��Ŀ�ɱ�����/�༭�ɱ�")
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
	 * �ͻ��˶���
	 */
	@Exclude
	private transient CBSItem parent;

	@SetValue("children")
	private void setChildren(List<ObjectId> childrenId ) {
		children = ServicesLoader.get(CBSService.class)
				.createDataSet(new Query().filter(new BasicDBObject("_id", new BasicDBObject("$in", childrenId))).bson(), domain);
	}

	@Exclude
	private List<AccountItem> subjects;

	@Structure({ "��Ŀ��Ŀ�ʽ�ƻ�/list", "��Ŀ��Ŀ�ʽ�ƻ����鿴��/list", "��Ŀ��Ŀʵ�ʳɱ�/list", "��Ŀ��Ŀʵ�ʳɱ����鿴��/list" })
	public List<AccountItem> listSubjects() {
		if (subjects == null) {
			subjects = ServicesLoader.get(CommonService.class).getAccoutItemRoot(domain);
		}
		return subjects;
	}

	@Structure({ "��Ŀ��Ŀ�ʽ�ƻ�/count", "��Ŀ��Ŀ�ʽ�ƻ����鿴��/count", "��Ŀ��Ŀʵ�ʳɱ�/count", "��Ŀ��Ŀʵ�ʳɱ����鿴��/count" })
	public long countSubjects() {
		if (subjects == null) {
			return ServicesLoader.get(CommonService.class).countAccoutItemRoot(domain);
		} else {
			return subjects.size();
		}
	}

	@Structure({ "CBS/list", "CBS���鿴��/list" })
	public List<CBSItem> listSubCBSItems() {
		children.forEach(c -> c.parent = this);
		return children;
	}

	@Structure({ "CBS/count", "CBS���鿴��/count" })
	public long countSubCBSItems() {
		return children.size();
	}

	@Structure({ "��Ŀ�ɱ�����/list", "��Ŀ�ɱ������鿴��/list", "��ĿԤ��ɱ��Աȷ���/list" })
	public List<Object> listSubCBSItemsAndSubjects() {
		List<Object> cbsSubjects = new ArrayList<Object>();
		listSubjects().forEach(s -> {
			cbsSubjects.add(new CBSSubjectCost().setDomain(domain).setCBSItem(this).setAccountItem(s));
		});
		return cbsSubjects;
	}

	@Structure({ "��Ŀ�ɱ�����/count", "��Ŀ�ɱ������鿴��/count", "��ĿԤ��ɱ��Աȷ���/count" })
	public long countSubCBSItemsAndSubjects() {
		return ServicesLoader.get(CommonService.class).countAccoutItemRoot(domain);
	}

	@Structure({ "Ԥ��ɱ��Աȷ���/list" })
	public List<Object> listSubCBSSubjects() {
		List<Object> result = new ArrayList<Object>();

		if (subjects == null) {
			subjects = ServicesLoader.get(CommonService.class).getAccoutItemRoot(domain);
		}
		List<CBSSubjectCost> cbsSubjects = new ArrayList<CBSSubjectCost>();
		subjects.forEach(s -> {
			cbsSubjects.add(new CBSSubjectCost().setDomain(domain).setCBSItem(this).setAccountItem(s));

		});
		result.addAll(cbsSubjects);
		return result;
	}

	@Structure({ "Ԥ��ɱ��Աȷ���/count" })
	public long countSubCBSSubjects() {
		long result = 0;
		if (subjects == null) {
			result += ServicesLoader.get(CommonService.class).countAccoutItemRoot(domain);
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
		//// BUG: Ԥ��ɱ�������ʱ��ʾ��������⡣ ��������ȡ����ϼ�
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

	public double getPeriodBudget(String period) {
		//////////////////////////////////////////////////////////////////////
		//// BUG: Ԥ��ɱ�������ʱ��ʾ��������⡣ ��������ȡ����ϼ�
		//////////////////////////////////////////////////////////////////////
		double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
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

	public double getDurationBudget(String startPeriod, String endPeriod) {
		//////////////////////////////////////////////////////////////////////
		//// BUG: Ԥ��ɱ�������ʱ��ʾ��������⡣ ��������ȡ����ϼ�
		//////////////////////////////////////////////////////////////////////
		double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
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
			} else {
				List<CBSPeriod> cbsPeriods = listCBSPeriods();
				if (cbsPeriods.size() > 0) {
					for (CBSPeriod cbsPeriod : cbsPeriods) {
						if (startPeriod.compareTo(cbsPeriod.getId()) <= 0 && endPeriod.compareTo(cbsPeriod.getId()) >= 0) {
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
		//// BUG: Ԥ��ɱ�������ʱ��ʾ��������⡣ ��������ȡ����ϼ�
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
		//// BUG: Ԥ��ɱ�������ʱ��ʾ��������⡣ ��������ȡ����ϼ�
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
			cbsSubjects = ServicesLoader.get(CBSService.class).getCBSSubject(_id, domain);
		}
		return cbsSubjects;
	}

	public List<CBSPeriod> listCBSPeriods() {
		if (cbsPeriods == null) {
			cbsPeriods = ServicesLoader.get(CBSService.class).getCBSPeriod(_id, domain);
		}
		return cbsPeriods;
	}

	public double getPeriodCost(String period) {
		double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
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
		double summary = 0d;
		if (countSubCBSItems() > 0) {
			Iterator<CBSItem> iter = children.iterator();
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

	public static CBSItem getInstance(CBSItem parent,String domain) {
		CBSItem cbsItem = getInstance(domain);

		cbsItem.setParent_id(parent.get_id());
		cbsItem.setScope_id(parent.getScope_id());
		cbsItem.setScopeRoot(false);
		cbsItem.setScopeName(parent.getScopeName());

		return cbsItem;
	}

	public static CBSItem getInstance(ICBSScope cbsScope, boolean scopeRoot,String domain) {
		CBSItem cbsItem = getInstance(domain);
		cbsItem.setScope_id(cbsScope.getScope_id());
		cbsItem.setScopeName(cbsScope.getScopeName());
		cbsItem.setScopeRoot(scopeRoot);

		return cbsItem;
	}

	public static CBSItem getInstance(String domain) {
		CBSItem cbsItem = new CBSItem();
		cbsItem.set_id(new ObjectId());
		cbsItem.domain = domain;
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
			settlementDate = ServicesLoader.get(CBSService.class).getNextSettlementDate(scope_id, domain);
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

	public Double getPeriodCAR(String period) {
		double budget = getPeriodBudget(period);
		if (budget != 0d) {
			return getPeriodCost(period) / budget;
		}
		return null;
	}

	public Double getDurationCAR(String startPeriod, String endPeriod) {
		double budget = getDurationBudget(startPeriod, endPeriod);
		if (budget != 0d) {
			return 1d * getDurationCost(startPeriod, endPeriod) / budget;
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

	public Double getPeriodBDR(String period) {
		double budget = getPeriodBudget(period);
		if (budget != 0d) {
			return 1d * (getPeriodCost(period) - budget) / budget;
		}
		return null;
	}

	public Double getDurationBDR(String startPeriod, String endPeriod) {
		double budget = getDurationBudget(startPeriod, endPeriod);
		if (budget != 0d) {
			return 1d * (getDurationCost(startPeriod, endPeriod) - budget) / budget;
		}
		return null;
	}

	@SetValue
	public Double cbsSubjectBudget;

	@SetValue
	public Double cbsSubjectCost;

	public double getPeriodOverspend(String period) {
		return getPeriodCost(period) - getPeriodBudget(period);
	}

	public double getDurationOverspend(String startPeriod, String endPeriod) {
		return getDurationCost(startPeriod, endPeriod) - getDurationBudget(startPeriod, endPeriod);
	}

	public double getOverspendSummary() {
		return getCostSummary() - getBudgetSummary();
	}

	@Exclude
	private ICBSScope scopeRootObject;

	public ICBSScope getScopeRootObject() {
		if (scopeRootObject == null) {
			scopeRootObject = ServicesLoader.get(CBSService.class).getICBSScopeRootWork(scope_id, domain);
			if (scopeRootObject == null) {
				scopeRootObject = ServicesLoader.get(CBSService.class).getICBSScopeRootProject(scope_id, domain);
			}
		}
		return scopeRootObject;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	// DEMO �·�
	/**
	 * ֧�����ڲ������ⲿ��trueΪ�ڲ�
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
