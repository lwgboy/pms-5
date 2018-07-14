package com.bizvisionsoft.service.model;

import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;

public class OBSItemWarpper {

	@ReadValue
	@SetValue
	private String userId;

	@ReadValue
	@SetValue
	private String userName;

	@ReadValue
	@SetValue
	private String roleId;

	@ReadValue
	@SetValue
	private String role;

	@ReadValue
	@SetValue
	private String name;

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	@Override
	@Label
	public String toString() {
		return userName;
	}

	public OBSItemWarpper setUser(User user) {
		if (user != null) {
			this.userId = user.getUserId();
			this.userName = user.getName();
		}
		return this;
	}

}
