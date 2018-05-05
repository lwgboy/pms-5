package com.bizvisionsoft.service.model;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
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

	@Behavior({ "CBS/编辑" })
	private boolean behaviourEditName() {
		return !scopeRoot;
		// TODO 传参数问题
	}

	@Behavior({ "CBS/删除", "CBS/科目", "CBS/预算" })
	private boolean behaviourEditAmount() {
		return !scopeRoot && countSubCBSItems() == 0;
		// TODO 传参数问题
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

	@Structure("CBSSubject/list")
	public List<AccountItem> listSubjects() {
		if (subjects == null) {
			subjects = ServicesLoader.get(CommonService.class).getAccoutItemRoot();
		}
		return subjects;
	}

	@Structure("CBSSubject/count")
	public long countSubjects() {
		if (subjects == null) {
			return ServicesLoader.get(CommonService.class).countAccoutItemRoot();
		} else {
			return subjects.size();
		}
	}

	@Structure("CBS/list")
	public List<CBSItem> listSubCBSItems() {
		children.forEach(c -> c.parent = this);
		return children;
	}

	@Structure("CBS/count")
	public long countSubCBSItems() {
		return children.size();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	private boolean scopeRoot;

	public ObjectId get_id() {
		return _id;
	}

	public CBSItem setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public CBSItem set_id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public CBSItem setScope_id(ObjectId scope_id) {
		this.scope_id = scope_id;
		return this;
	}

	public CBSItem setScopeRoot(boolean scopeRoot) {
		this.scopeRoot = scopeRoot;
		return this;
	}

	public boolean isScopeRoot() {
		return scopeRoot;
	}

	public CBSItem setName(String name) {
		this.name = name;
		return this;
	}

	public ObjectId getScope_id() {
		return scope_id;
	}

	public CBSItem setId(String id) {
		this.id = id;
		return this;
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

	public static CBSItem newSubItem(CBSItem parent) {
		return new CBSItem().setParent_id(parent.get_id()).setScope_id(parent.getScope_id()).setScopeRoot(false);
	}

	public CBSItem getParent() {
		return parent;
	}

	public void addChild(CBSItem child) {
		child.parent = this;
		children.add(child);
	}

	public void removeChild(CBSItem child) {
		children.remove(child);
	}

	public void setParent(CBSItem parent) {
		this.parent = parent;
	}

}
