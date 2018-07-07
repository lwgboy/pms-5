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

	public String getUserId() {
		return userId;
	}
	
	@Override
	@Label
	public String toString() {
		return userName;
	}

}
