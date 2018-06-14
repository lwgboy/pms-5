package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("workReportItem")
public class WorkReportItem {

	@WriteValue
	@ReadValue
	private ObjectId _id;

	@WriteValue
	@ReadValue
	private ObjectId work_id;

	@WriteValue
	@ReadValue
	private ObjectId report_id;

	@SetValue
	private Work work;

	@WriteValue
	@ReadValue
	private Date estimatedFinish;

	@WriteValue
	@ReadValue
	private String statement;

	@WriteValue
	@ReadValue
	private String problems;

	@WriteValue
	@ReadValue
	private String pmRemark;
	
	@ReadValue
	@WriteValue
	private boolean confirmed;
	
	@ReadValue
	@WriteValue
	private String reportorId;

	public String getPmRemark() {
		return pmRemark;
	}
	
	public Date getEstimatedFinish() {
		return estimatedFinish;
	}
	
	public String getStatement() {
		return statement;
	}
	
	public String getProblems() {
		return problems;
	}

	public Work getWork() {
		return work;
	}
	
	public boolean isConfirmed() {
		return confirmed;
	}
	
	public String getReportorId() {
		return reportorId;
	}

	public String getPMId() {
		//获取项目经理和本工作阶段负责人的id，查询即可
		return "yj";
	}

	@Label
	public String toString() {
		return work.getFullName();
	}
}
