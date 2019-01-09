package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("problemReactionPrePlan")
public class ProblemReactionPrePlan {

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "�ж�Ԥ��";

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

	@ReadValue
	@WriteValue
	private String objective;

	public String getAction() {
		return action;
	}

	public String getDetail() {
		return detail;
	}

	public String getObjective() {
		return objective;
	}

}
