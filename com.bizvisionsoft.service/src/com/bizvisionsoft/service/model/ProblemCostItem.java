package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("problemCostItem")
public class ProblemCostItem {

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "问题成本项";

	@ReadValue
	@WriteValue
	private ObjectId _id;
	
	@ReadValue
	@WriteValue
	private ObjectId problem_id;

	@ReadValue
	@WriteValue
	private Date date;

	@ReadValue
	@WriteValue
	private ClassifyProblemLost classifyProblemLost;

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private double drAmount;

	@ReadValue
	@WriteValue
	private double crAmount;

	@ReadValue
	@SetValue
	private double summary;

	@ReadValue
	@WriteValue
	private String operator;

	@ReadValue
	@WriteValue
	private String sn;

	@ReadValue
	@WriteValue
	private String remark;

	@Override
	@Label
	public String toString() {
		return name + (sn == null ? "" : (" [" + sn + "]"));
	}

	@ReadValue("accountId")
	public String getAccountId() {
		return classifyProblemLost.id;
	}
	
	public void setProblem_id(ObjectId problem_id) {
		this.problem_id = problem_id;
	}

	public ObjectId get_id() {
		return _id;
	}
}
