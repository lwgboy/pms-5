package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("dictionary")
public class Dictionary {
	////////////////////////////////////////////////////////////////////////////////////////////////////
	@Persistence
	@ReadValue
	@WriteValue
	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	@Persistence
	@ReadValue
	@WriteValue
	private String id;

	public String getId() {
		return id;
	}

	@ReadValue
	@WriteValue
	private String name;

	public String getName() {
		return name;
	}

	@ReadValue
	@WriteValue
	private String description;

	@ReadValue
	@WriteValue
	private String type;

	@Override
	@Label
	public String toString() {
		return name + "[" + id + "]";
	}
}
