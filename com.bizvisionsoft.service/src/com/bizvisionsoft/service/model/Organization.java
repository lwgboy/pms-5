package com.bizvisionsoft.service.model;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.tools.Checker;

@PersistenceCollection(value = "organization")
public class Organization {

	@Persistence
	private ObjectId _id;

	/**
	 * 组织编号
	 */
	@Persistence
	@ReadValue
	@WriteValue
	private String id;

	/**
	 * 上级组织
	 */
	@Persistence
	private ObjectId parent_id;

	@ReadValue("parent")
	public Organization getParent() {
		return Optional.ofNullable(this.parent_id).map(_id -> ServicesLoader.get(OrganizationService.class).get(_id))
				.orElse(null);
	}
	
	/**
	 * 所属项目
	 */
	@Persistence
	private ObjectId project_id;

	/**
	 * 组织名称
	 */
	@Persistence
	@ReadValue
	@WriteValue
	private String name;

	/**
	 * 全称
	 */
	@Persistence
	@ReadValue
	@WriteValue
	@Label
	private String fullName;

	/**
	 * 组织类型
	 */
	@Persistence
	@ReadValue
	@WriteValue
	private String type;

	@Persistence
	private String managerId;

	@ReadValue("managerInfo")
	@WriteValue("managerInfo")
	@Exclude
	private String managerInfo;

	@WriteValue("manager")
	private void setManager(User manager) {
		if (manager == null) {
			managerId = null;
			managerInfo = "";
		} else {
			managerId = manager.getUserId();
			managerInfo = manager.toString();
		}
	}

	@ReadValue("manager")
	private User getManager() {
		return Optional.ofNullable(managerId).map(id -> {
			try {
				return ServicesLoader.get(UserService.class).get(id);
			} catch (Exception e) {
				return null;
			}
		}).orElse(null);
	}

	@Structure("组织管理/list")
	public List<Organization> getSub() {
		Checker.checkNull(_id);
		return ServicesLoader.get(OrganizationService.class).getSub(_id);
	}

	@Structure("组织管理/count")
	public long countSub() {
		Checker.checkNull(_id);
		return ServicesLoader.get(OrganizationService.class).countSub(_id);
	}

	@Label
	public String toString() {
		return fullName == null ? "" : fullName;
	}
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "组织";

	public ObjectId getParentId() {
		return parent_id;
	}

	public Organization setParentId(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public String getName() {
		return name;
	}

	public Organization setName(String name) {
		this.name = name;
		return this;
	}

	public String getFullName() {
		return fullName;
	}

	public Organization setFullName(String fullName) {
		this.fullName = fullName;
		return this;
	}

	public String getType() {
		return type;
	}

	public Organization setType(String type) {
		this.type = type;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
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
		Organization other = (Organization) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}

}
