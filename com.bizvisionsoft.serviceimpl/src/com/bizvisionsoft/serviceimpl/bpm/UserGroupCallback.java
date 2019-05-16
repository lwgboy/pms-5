package com.bizvisionsoft.serviceimpl.bpm;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.service.common.Service;

public class UserGroupCallback implements org.kie.api.task.UserGroupCallback {

	@Override
	public boolean existsUser(String userId) {
		return Service.database.getCollection("user").countDocuments(new Document("userId", userId)) == 1;
	}

	@Override
	public boolean existsGroup(String groupId) {
		return true;
	}

	@Override
	public List<String> getGroupsForUser(String userId) {
		return new ArrayList<String>();
	}

}
