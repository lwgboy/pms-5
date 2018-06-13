package com.bizvisionsoft.service.model;

import com.bizvisionsoft.annotations.md.service.Label;

public class WorkReportAssignment {

	private Work work;

	private WorkReport workReport;

	public Work getWork() {
		return work;
	}

	public WorkReportAssignment setWork(Work work) {
		this.work = work;
		return this;
	}

	public WorkReport getWorkReport() {
		return workReport;
	}

	public WorkReportAssignment setWorkReport(WorkReport workReport) {
		this.workReport = workReport;
		return this;
	}
	
	@Label
	public String toString() {
		return work.toString();
	}

}
