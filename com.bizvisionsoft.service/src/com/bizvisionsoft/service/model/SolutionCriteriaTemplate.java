package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("solutionCriteriaTemplate")
public class SolutionCriteriaTemplate {
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "目标和决策准则样板";

	@ReadValue
	@WriteValue
	private ObjectId _id;
	
	@ReadValue
	@WriteValue
	private String id;
	
	@ReadValue
	@WriteValue
	private boolean applicable;
	
	@ReadValue
	@WriteValue
	private List<ClassifyProblem> classifyProblems;
	
	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private String description;

	@ReadValue
	@WriteValue
	private String endResult;
	
	@ReadValue
	@WriteValue
	private List<Document> givens;
	
	@ReadValue
	@WriteValue
	private List<Document> wants;
}
