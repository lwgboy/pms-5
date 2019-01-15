package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("problemActionPlan")
public class ProblemActionPlan {

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "ÐÐ¶¯Ô¤°¸";

	@ReadValue
	@WriteValue
	private ObjectId _id;
	
	@ReadValue
	@WriteValue
	private String id;
	
	@ReadValue
	@WriteValue
	private List<String> stage;
	
	@ReadValue
	@WriteValue
	private List<ClassifyProblem> classifyProblems;
	
	@ReadValue
	@WriteValue
	private String usage;
	
	@ReadValue
	@WriteValue
	private boolean applicable;

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
