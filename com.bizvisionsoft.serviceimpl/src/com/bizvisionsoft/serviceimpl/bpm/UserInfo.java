package com.bizvisionsoft.serviceimpl.bpm;

import java.util.ArrayList;
import java.util.Iterator;

import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;

public class UserInfo implements org.kie.internal.task.api.UserInfo {

	@Override
	public String getDisplayName(OrganizationalEntity entity) {
		return "DEMO:"+entity.getId();
	}

	@Override
	public Iterator<OrganizationalEntity> getMembersForGroup(Group group) {
		return new ArrayList<OrganizationalEntity>().iterator();
	}

	@Override
	public boolean hasEmail(Group group) {
		return false;
	}

	@Override
	public String getEmailForEntity(OrganizationalEntity entity) {
		return "DEMOGROUPEmail@yaozheng.com.cn";
	}

	@Override
	public String getLanguageForEntity(OrganizationalEntity entity) {
		return "zh";
	}

	@Override
	public String getEntityForEmail(String email) {
		return "DEMOUSEREmail@yaozheng.com.cn";
	}

	

}
