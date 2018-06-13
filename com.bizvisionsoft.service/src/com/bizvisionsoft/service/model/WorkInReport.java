package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("workInReport")
public class WorkInReport {
	@Persistence
	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	@Persistence
	private ObjectId work_id;

	public void setWork_id(ObjectId work_id) {
		this.work_id = work_id;
	}

	@Persistence
	private ObjectId workReport_id;

	public void setWorkReport_id(ObjectId workReport_id) {
		this.workReport_id = workReport_id;
	}

	@WriteValue
	@ReadValue
	@Persistence
	private Date estimatedFinish;

	public Date getEstimatedFinish() {
		return estimatedFinish;
	}

	@WriteValue
	@ReadValue
	@Persistence
	private String executionInfo;

	public String getExecutionInfo() {
		return executionInfo;
	}

	@WriteValue
	@ReadValue
	@Persistence
	private String question;

	public String getQuestion() {
		return question;
	}

	@WriteValue
	@ReadValue
	@Persistence
	private String verifierRemark;

	public String getVerifierRemark() {
		return verifierRemark;
	}
}
