package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;

@PersistenceCollection("workInReport")
public class WorkInReport {
	@Persistence
	private ObjectId _id;

	@Persistence
	private ObjectId work_id;

	@Persistence
	private ObjectId workReport_id;

	@Persistence
	private Date estimatedFinish;

	public Date getEstimatedFinish() {
		return estimatedFinish;
	}

	@Persistence
	private String executionInfo;

	public String getExecutionInfo() {
		return executionInfo;
	}

	@Persistence
	private String question;

	public String getQuestion() {
		return question;
	}

	@Persistence
	private String verifierRemark;

	public String getVerifierRemark() {
		return verifierRemark;
	}
}
