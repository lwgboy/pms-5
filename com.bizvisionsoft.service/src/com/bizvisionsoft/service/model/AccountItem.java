package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;
import com.mongodb.BasicDBObject;

@PersistenceCollection("accountItem")
public class AccountItem implements Comparable<AccountItem> {

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
	private String typeName = "财务科目";

	/**
	 * 客户端对象
	 */
	@Exclude
	private transient AccountItem parent;

	private List<AccountItem> children = new ArrayList<AccountItem>();

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@SetValue("children")
	private void setChildren(List<ObjectId> childrenId) {
		children = ServicesLoader.get(CommonService.class)
				.queryAccountItem(new BasicDBObject("_id", new BasicDBObject("$in", childrenId)));
	}

	@Structure("list")
	public List<AccountItem> listSubAccountItems() {
		children.forEach(c -> c.parent = this);
		Collections.sort(children);
		return children;
	}

	@Structure("count")
	public long countSubAccountItems() {
		return children.size();
	}

	@Behavior({ "CBSSubject/编辑" })
	private boolean behavior() {
		return countSubAccountItems() == 0;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public ObjectId get_id() {
		return _id;
	}

	public AccountItem setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public void addChild(AccountItem item) {
		item.parent = this;
		children.add(item);
		Collections.sort(children);
	}

	@Override
	public int compareTo(AccountItem o) {
		return id.compareTo(o.id);
	}
	
	public String getId() {
		return id;
	}

}
