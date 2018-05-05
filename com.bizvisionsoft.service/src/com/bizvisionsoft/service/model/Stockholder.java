package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("stockholder")
public class Stockholder {

	@ReadValue
	@WriteValue
	@Label
	private String name;

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String type;

	@ReadValue
	@WriteValue
	private String description;

	@ReadValue
	@WriteValue
	private String contact;

	@ReadValue
	@WriteValue
	private String tel;

	@ReadValue
	@WriteValue
	private String fax;

	@ReadValue
	@WriteValue
	private String email;

	@ReadValue
	@WriteValue
	private ObjectId project_id;

	public Stockholder setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}
}


