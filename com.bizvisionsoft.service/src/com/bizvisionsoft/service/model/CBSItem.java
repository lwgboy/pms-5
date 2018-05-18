package com.bizvisionsoft.service.model;

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

	@Behavior({ "CBS/�༭���趨" })
	private boolean behaviourAdd(@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId scope_id) {
		return this.scope_id.equals(scope_id);
	}

	@Behavior({ "CBS/ȡ��" })
	private boolean behaviourUnDistribute(@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId scope_id) {
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
	private void setChildren(List<ObjectId> childrenId) {
		children = ServicesLoader.get(CBSService.class)
				.createDataSet(new BasicDBObject("_id", new BasicDBObject("$in", childrenId)));
	}

	@Exclude
	private List<AccountItem> subjects;

	@Structure({ "CBSSubject/list", "CBSSubject���鿴��/list" })
	public List<AccountItem> listSubjects() {
		if (subjects == null) {
			subjects = ServicesLoader.get(CommonService.class).getAccoutItemRoot();
		}
		return subjects;
	}

	@Structure({ "CBSSubject/count", "CBSSubject���鿴��/count" })
	public long countSubjects() {
		if (subjects == null) {
			return ServicesLoader.get(CommonService.class).countAccoutItemRoot();
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

	public Double getBudgetSummary() {
		if (countSubCBSItems() == 0) {
			return Optional.ofNullable(budgetTotal).orElse(0d);
		} else {
			Double summary = 0d;
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getBudgetSummary();
			}
			return summary;
		}
	}

	public Double getBudget(String period) {
		if (countSubCBSItems() == 0) {
			return Optional.ofNullable(budget).map(b -> b.getDouble(period)).orElse(0d);
		} else {
			Double summary = 0d;
			Iterator<CBSItem> iter = children.iterator();
			while (iter.hasNext()) {
				summary += iter.next().getBudget(period);
			}
			return summary;
		}
	}

	public Double getBudgetYearSummary(String year) {
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

}
