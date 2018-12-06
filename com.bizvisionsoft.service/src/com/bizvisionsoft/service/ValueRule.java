package com.bizvisionsoft.service;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("valueRule")
public class ValueRule {

	@ReadValue
	@WriteValue
	public ObjectId _id;

	@ReadValue
	@WriteValue
	public Boolean enable;

	@ReadValue
	@WriteValue
	public String className;

	@ReadValue
	@WriteValue
	public String fieldName;

	@Label
	public String label() {
		return className + "." + fieldName;
	}

	@Exclude
	@ReadValue(ReadValue.TYPE)
	public static final String type = "取值规则";

}
