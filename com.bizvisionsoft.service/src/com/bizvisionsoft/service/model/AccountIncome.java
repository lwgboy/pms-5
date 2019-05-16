package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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

/**
 * TODO id, parentId,subIds 增加index, 其中id为唯一索引
 * 
 * @author hua
 *
 */
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
	private String parentId;

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
	public static final String typeName = "财务科目";

	@ReadValue
	@WriteValue
	private String type;

	@ReadValue
	@WriteValue
	private String formula;

	/**
	 * 客户端对象
	 */
	@Exclude
	private transient AccountIncome parent;

	@Exclude
	public String domain;

	@Exclude
	private List<AccountIncome> children;

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@Structure("list")
	public List<AccountIncome> listSubAccountItems() {
		return ServicesLoader.get(CommonService.class).queryAccountIncome(new BasicDBObject("parentId", id), domain);
	}

	@Structure("count")
	public long countSubAccountItems() {
		return ServicesLoader.get(CommonService.class).countAccoutIncome(id, domain);
	}

	@Behavior({ "项目收益预测/编辑收益预测", "项目收益实现/编辑收益实现" })
	private boolean behavior() {
		return countSubAccountItems() == 0;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public ObjectId get_id() {
		return _id;
	}

	public AccountIncome setParentId(String parentId) {
		this.parentId = parentId;
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

	public List<AccountIncome> getSubAccountItems() {
		if (children == null) {
			children = listSubAccountItems();
		}
		return children;
	}

	public boolean hasChildren() {
		if (children == null) {
			children = listSubAccountItems();
		}
		return children.size() > 0;
	}

	public String getParentId() {
		return parentId;
	}

	public String getFormula() {
		return formula;
	}

	public static AccountIncome search(List<AccountIncome> list, Function<AccountIncome, Boolean> condition) {
		for (int i = 0; i < list.size(); i++) {
			AccountIncome item = list.get(i);
			if (Boolean.TRUE.equals(condition.apply(item))) {
				return item;
			}
			List<AccountIncome> items = item.getSubAccountItems();
			if (items != null) {
				item = search(items, condition);
				if (item != null) {
					return item;
				}
			}
		}
		return null;
	}

	public static List<AccountIncome> collect(List<AccountIncome> list, Function<AccountIncome, Boolean> condition) {
		ArrayList<AccountIncome> result = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			AccountIncome item = list.get(i);
			if (Boolean.TRUE.equals(condition.apply(item))) {
				result.add(item);
			}
			List<AccountIncome> items = item.getSubAccountItems();
			if (items != null) {
				result.addAll(collect(items, condition));
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccountIncome other = (AccountIncome) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
