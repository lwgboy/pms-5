package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("certificate")
public class Certificate {

	public ObjectId _id;

	@ReadValue
	@WriteValue
	@Label
	public String name;//TODO Î¨Ò»

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "×Ê¸ñ";

	
	@ReadValue
	@WriteValue
	private String description;

	@ReadValue
	@WriteValue
	private String scope;
	
	@ReadValue
	@WriteValue
	private String type;

}
