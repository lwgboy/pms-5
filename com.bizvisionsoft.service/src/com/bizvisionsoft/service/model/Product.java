package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("product")
public class Product {
	
	@ReadValue
	@WriteValue
	private ObjectId _id;
	
	@ReadValue
	@WriteValue
	private String id;
	
	@ReadValue
	@WriteValue
	private String name;
	
	@ReadValue
	@WriteValue
	private String series;
	
	@ReadValue
	@WriteValue
	private String sellPoint;
	
	@ReadValue
	@WriteValue
	private String position;
	
	@ReadValue
	@WriteValue
	private ObjectId project_id;
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "²úÆ·";
	
	public Product setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}
	
	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
}
