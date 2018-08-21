package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;
import com.mongodb.BasicDBObject;

@PersistenceCollection("accountIncome")
public class AccountIncome implements Comparable<AccountIncome> {

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
	private transient AccountIncome parent;

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@Structure("list")
	public List<AccountIncome> listSubAccountItems() {
		return ServicesLoader.get(CommonService.class).queryAccountIncome(new BasicDBObject("parent_id", _id));
	}

	@Structure("count")
	public long countSubAccountItems() {
		return ServicesLoader.get(CommonService.class).countAccoutIncome(_id);
	}

	@Behavior({ "项目科目资金计划/编辑", "项目科目实际成本/编辑" })
	private boolean behavior() {
		return countSubAccountItems() == 0;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public ObjectId get_id() {
		return _id;
	}

	public AccountIncome setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	@Override
	public int compareTo(AccountIncome o) {
		return id.compareTo(o.id);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
