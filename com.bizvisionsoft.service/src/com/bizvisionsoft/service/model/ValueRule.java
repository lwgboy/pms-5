package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.tools.Check;

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
	public String colName;

	@ReadValue
	@WriteValue
	public String fieldName;

	public String domain;

	@Label
	public String label() {
		if (Check.isAssigned(className)) {
			return className + "." + fieldName;
		} else {
			return colName + "." + fieldName;
		}
	}

	@Exclude
	@ReadValue(ReadValue.TYPE)
	public static final String type = "取值规则";

}
