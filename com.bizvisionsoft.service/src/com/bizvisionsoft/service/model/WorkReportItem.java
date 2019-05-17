package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkReportService;

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

	@SetValue
	private List<String> pmIds;
	
	public String domain;

	private Date planStart;

	private Date actualStart;

	private Date planFinish;

	private Date actualFinish;

	public ObjectId get_id() {
		return _id;
	}

	public Date getPlanStart() {
		return planStart;
	}

	public Date getPlanFinish() {
		return planFinish;
	}

	public Date getActualStart() {
		return actualStart;
	}

	public Date getActualFinish() {
		return actualFinish;
	}

	public String getPmRemark() {
		return pmRemark;
	}

	public void setPmRemark(String pmRemark) {
		this.pmRemark = pmRemark;
	}

	public Date getEstimatedFinish() {
		return estimatedFinish;
	}

	public void setEstimatedFinish(Date estimatedFinish) {
		this.estimatedFinish = estimatedFinish;
	}

	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}

	public String getProblems() {
		return problems;
	}

	public void setProblems(String problems) {
		this.problems = problems;
	}

	public Work getWork() {
		return work;
	}

	public ObjectId getWork_id() {
		return work_id;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public String getReportorId() {
		return reportorId;
	}

	public List<String> getPMId() {
		// 获取项目经理和本工作阶段负责人的id，查询即可
		return pmIds;
	}

	public WorkReport getWorkReport() {
		return ServicesLoader.get(WorkReportService.class).getWorkReport(report_id, domain);
	}

	public ObjectId getReport_id() {
		return report_id;
	}

	@Label
	public String toString() {
		return work.getFullName();
	}
}
