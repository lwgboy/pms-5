package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.SelectionValidation;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.annotations.ui.common.MethodParam;

@PersistenceCollection("causeRelation")
public class CauseConsequence {

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
	private ObjectId parent_id;

	@ReadValue
	@WriteValue
	private int weight;

	@ReadValue
	@WriteValue
	private double probability;

	@ReadValue
	@WriteValue
	private ObjectId problem_id;

	@ReadValue
	@WriteValue
	private String subject;

	@ReadValue
	@WriteValue
	private String type;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "“ÚÀÿ";
	
	@ReadValue
	@WriteValue
	private ClassifyCause classifyCause;

	@Override
	@Label
	public String toString() {
		return name;
	}
	
	@SelectionValidation("classifyCause")
	private boolean selectable(@MethodParam(MethodParam.OBJECT) ClassifyCause elem) {
		return elem.isLeaf;
	}

	public CauseConsequence setProblem_id(ObjectId problem_id) {
		this.problem_id = problem_id;
		return this;
	}

	public CauseConsequence setSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public CauseConsequence setType(String type) {
		this.type = type;
		return this;
	}

	public String getSubject() {
		return subject;
	}

	public ObjectId get_id() {
		return _id;
	}

	public CauseConsequence setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public String getDescription() {
		return description ;
	}

	public String getName() {
		return name;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public double getProbability() {
		return probability;
	}
	
	public ClassifyCause getClassifyCause() {
		return classifyCause;
	}

}
