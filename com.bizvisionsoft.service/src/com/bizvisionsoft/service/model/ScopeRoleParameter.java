package com.bizvisionsoft.service.model;

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;

public class ScopeRoleParameter {
	
	public List<ObjectId> scopes;
	
	public List<String> roles;
	
	public String userId;
	
	public ScopeRoleParameter( String userId) {
		this.userId = userId;
	}

	public ScopeRoleParameter setScopes(ObjectId...ids) {
		scopes = Arrays.asList(ids);
		return this;
	}
	
	public ScopeRoleParameter setRoles(String...roleIds) {
		roles = Arrays.asList(roleIds);
		return this;
	}
	
}
