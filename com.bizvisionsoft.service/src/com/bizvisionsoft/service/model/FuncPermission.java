package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.Map;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadOptions;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;

@PersistenceCollection("funcPermission")
public class FuncPermission {

	@ReadValue
	@WriteValue
	private ObjectId _id;
	
	@ReadValue
	@WriteValue
	private String role;

	@ReadValue
	@WriteValue
	private String id;

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private String type;

	@ReadValue
	@WriteValue
	private Date from;

	@ReadValue
	@WriteValue
	private Date to;

	@ReadValue
	@WriteValue
	private CreationInfo creationInfo;

	@WriteValue("organization")
	private void setOrganization(Organization org) {
		if (org != null) {
			type = "组织";
			id = org.getId();
			name = org.getFullName();
		} else {
			type = null;
			id = null;
			name = null;
		}
	}

	@WriteValue("user")
	private void setUser(User user) {
		if (user != null) {
			type = "用户";
			id = user.getUserId();
			name = user.getName();
		} else {
			type = null;
			id = null;
			name = null;
		}
	}

	@ReadValue("authBy")
	private String getAuthBy() {
		return creationInfo.getCreatorInfo();
	}

	@ReadValue("authOn")
	private Date getAuthOn() {
		return creationInfo.date;
	}
	
	@ReadOptions("role")
	private Map<String,String> getRoleOption(){
		return ServicesLoader.get(CommonService.class).getDictionaryIdNamePair("功能角色");
	}

}
