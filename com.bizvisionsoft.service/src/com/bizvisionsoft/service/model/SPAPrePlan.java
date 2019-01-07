package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("spaPrePlan")
public class SPAPrePlan {

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "系统性预防措施预案";

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private String description;

	@ReadValue
	@WriteValue
	private String action;

	@ReadValue
	@WriteValue
	private String detail;

	public String getAction() {
		return action;
	}

	public String getDetail() {
		return detail;
	}


}
