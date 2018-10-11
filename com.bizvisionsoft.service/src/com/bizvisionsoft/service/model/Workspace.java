package com.bizvisionsoft.service.model;

import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;

public class Workspace {

	private ObjectId work_id;

	public ObjectId getWork_id() {
		return work_id;
	}

	public Workspace setWork_id(ObjectId work_id) {
		this.work_id = work_id;
		return this;
	}

	private String checkoutBy;

	public String getCheckoutBy() {
		return checkoutBy;
	}

	public Workspace setCheckoutBy(String checkoutBy) {
		this.checkoutBy = checkoutBy;
		return this;
	}

	private ObjectId space_id;

	public ObjectId getSpace_id() {
		return space_id;
	}

	public Workspace setSpace_id(ObjectId space_id) {
		this.space_id = space_id;
		return this;
	}

	private ObjectId project_id;

	public ObjectId getProject_id() {
		return project_id;
	}

	public Workspace setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}

	public static Workspace newInstance(ObjectId project_id, ObjectId work_id, ObjectId space_id, String checkoutBy) {
		return new Workspace().setProject_id(project_id).setWork_id(work_id).setSpace_id(space_id)
				.setCheckoutBy(checkoutBy);

	}

	public static Workspace newInstance(ObjectId project_id, ObjectId space_id, String checkoutBy) {
		return new Workspace().setProject_id(project_id).setSpace_id(space_id).setCheckoutBy(checkoutBy);
	}

	public User getCheckoutUser() {
		return Optional.ofNullable(this.checkoutBy).map(_id -> ServicesLoader.get(UserService.class).get(checkoutBy))
				.orElse(null);
	}
}
