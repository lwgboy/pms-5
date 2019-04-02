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

@PersistenceCollection("accountItem")
public class AccountItem implements Comparable<AccountItem> {

	////////////////////////////////////////////////////////////////////////////////////////////////
	// ������һЩ�ֶ�

	/** ��ʶ Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String parentId;

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
	public static final String typeName = "�����Ŀ";

	/**
	 * �ͻ��˶���
	 */
	@Exclude
	private transient AccountItem parent;

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@Structure("list")
	public List<AccountItem> listSubAccountItems() {
		return ServicesLoader.get(CommonService.class).queryAccountItem(new BasicDBObject("parentId", id));
	}

	@Structure("count")
	public long countSubAccountItems() {
		return ServicesLoader.get(CommonService.class).countAccoutItem(id);
	}

	@Behavior({ "��Ŀ��Ŀ�ʽ�ƻ�/�༭", "��Ŀ��Ŀʵ�ʳɱ�/�༭" })
	public boolean behavior() {
		return countSubAccountItems() == 0;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public ObjectId get_id() {
		return _id;
	}

	public AccountItem setParentId(String parent_id) {
		this.parentId = parent_id;
		return this;
	}

	public String getParentId() {
		return parentId;
	}

	@Override
	public int compareTo(AccountItem o) {
		return id.compareTo(o.id);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
