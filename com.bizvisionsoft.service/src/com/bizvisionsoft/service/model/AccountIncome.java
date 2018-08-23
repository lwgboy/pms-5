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

/**
 * TODO id, parentId,subIds ����index, ����idΪΨһ����
 * @author hua
 *
 */
@PersistenceCollection("accountIncome")
public class AccountIncome implements Comparable<AccountIncome> {

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
	private String typeName = "�����Ŀ";

	/**
	 * �ͻ��˶���
	 */
	@Exclude
	private transient AccountIncome parent;

	@Exclude
	private List<AccountIncome> children;
	
	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@Structure("list")
	public List<AccountIncome> listSubAccountItems() {
		return ServicesLoader.get(CommonService.class).queryAccountIncome(new BasicDBObject("parentId", id));
	}

	@Structure("count")
	public long countSubAccountItems() {
		return ServicesLoader.get(CommonService.class).countAccoutIncome(id);
	}

	@Behavior({ "��Ŀ����Ԥ��/�༭����Ԥ��","��Ŀ����ʵ��/�༭����ʵ��" })
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
		if(children==null) {
			children = listSubAccountItems();
		}
		return children;
	}
	
	public String getParentId() {
		return parentId;
	}

}
