package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("baseline")
public class Baseline {

	@Persistence
	private ObjectId _id;

	@Persistence
	private ObjectId project_id;

	@WriteValue("creationInfo")
	@ReadValue
	private CreationInfo creationInfo;

	@ReadValue
	@WriteValue
	@Persistence
	private String description;

	@ReadValue
	@WriteValue
	@Persistence
	private String name;

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public Baseline setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}

	public ObjectId getProject_id() {
		return project_id;
	}

	public CreationInfo getCreationInfo() {
		return creationInfo;
	}

	@ReadValue("createDate")
	public Date getCreateDate() {
		return creationInfo.date;
	}

	@ReadValue("creater")
	public String getCreater() {
		return creationInfo.userName + "[" + creationInfo.userId + "]";
	}

	public String getDescription() {
		return description;
	}
	
	public String getName() {
		return name;
	}
	
	public Baseline setName(String name) {
		this.name = name;
		return this;
	}
}
