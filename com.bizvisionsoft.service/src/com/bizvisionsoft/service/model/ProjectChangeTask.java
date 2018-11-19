package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;

public class ProjectChangeTask {

	@Persistence
	public String user;

	@ReadValue("userInfo")
	public String getUser() {
		return Optional.ofNullable(user).map(id -> ServicesLoader.get(UserService.class).get(id).toString()).orElse("");
	}

	@ReadValue
	@Persistence
	public Date date;

	@ReadValue
	@Persistence
	public String choice;

	@ReadValue
	@WriteValue
	@Persistence
	public String comment;

	@ReadValue
	@Persistence
	public String name;

	@Exclude
	public ObjectId projectChange_id;

	public static ProjectChangeTask getCancelInstance(String userid, ObjectId projectChange_id, String name, String comment) {
		ProjectChangeTask task = new ProjectChangeTask();
		task.user = userid;
		task.projectChange_id = projectChange_id;
		task.date = new Date();
		task.choice = ProjectChange.CHOICE_CANCEL;
		task.name = name;
		task.comment = comment;
		return task;
	}

	public static ProjectChangeTask getPassInstance(String userid, ObjectId projectChange_id, String name, String comment) {
		ProjectChangeTask task = new ProjectChangeTask();
		task.user = userid;
		task.projectChange_id = projectChange_id;
		task.date = new Date();
		task.choice = ProjectChange.CHOICE_CONFIRM;
		task.name = name;
		task.comment = comment;
		return task;
	}

}
