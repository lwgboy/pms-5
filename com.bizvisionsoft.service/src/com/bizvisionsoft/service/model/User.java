package com.bizvisionsoft.service.model;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;

@PersistenceCollection(value = "user")
public class User {

	@Persistence
	private ObjectId _id;

	@Persistence
	@ReadValue({ "userId", "组织角色/id", "资源类型/id" })
	@WriteValue({ "userId", "组织角色/id" })
	private String userId;

	@ImageURL({ "userId", "组织角色/id", "资源类型/id" })
	private String logo = "/img/user_c.svg";

	@ReadValue
	@WriteValue
	@Persistence
	private String password;

	@WriteValue("password2")
	private void setPassword2(String password) {
		if (this.password != null && !this.password.isEmpty() && !password.equals(this.password))
			throw new RuntimeException("两次输入的密码不一致。");
	}

	@Persistence
	@ReadValue
	@WriteValue
	private String name;

	@Persistence
	@ReadValue
	@WriteValue
	private String email;

	@Persistence
	@ReadValue
	@WriteValue
	private String tel;

	@Persistence
	@ReadValue
	@WriteValue
	private String mobile;

	@Persistence
	@ReadValue
	@WriteValue
	private String weixin;

	@Persistence
	@ReadValue
	@WriteValue
	private boolean activated;

	@Persistence
	@ReadValue
	@WriteValue
	private List<RemoteFile> headPics;

	@Persistence("org_id")
	private ObjectId organizationId;

	@SetValue
	@ReadValue
	private String orgFullName;

	@Persistence
	@ReadValue
	@WriteValue
	private List<String> certificates;

	@Persistence
	@ReadValue
	@WriteValue
	private ObjectId resourceType_id;

	public String getUserId() {
		return userId;
	}

	public String getHeadpicURL() {
		if (headPics != null && headPics.size() > 0)
			return headPics.get(0).getURL(ServicesLoader.url);
		return null;
	}

	@WriteValue("organization ")
	public void setOrganization(Organization org) {
		this.organizationId = Optional.ofNullable(org).map(o -> o.get_id()).orElse(null);
		this.orgFullName = Optional.ofNullable(org).map(o -> o.getFullName()).orElse(null);
	}

	@ReadValue("organization ")
	public Organization getOrganization() {
		return Optional.ofNullable(organizationId).map(_id -> ServicesLoader.get(OrganizationService.class).get(_id))
				.orElse(null);
	}

	@WriteValue("resourceType ")
	public void setResourceType(ResourceType rt) {
		this.resourceType_id = Optional.ofNullable(rt).map(o -> o.get_id()).orElse(null);
	}

	@ReadValue("resourceType ")
	public ResourceType getResourceType() {
		return Optional.ofNullable(resourceType_id)
				.map(_id -> ServicesLoader.get(CommonService.class).getResourceType(_id)).orElse(null);
	}

	@Override
	@Label
	public String toString() {
		return name + " [" + userId + "]";
	}

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "用户";

	public boolean isActivated() {
		return activated;
	}

	public String getName() {
		return name;
	}
	
	public List<RemoteFile> getHeadPics() {
		return headPics;
	}

}
