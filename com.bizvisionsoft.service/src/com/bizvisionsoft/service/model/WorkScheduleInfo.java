package com.bizvisionsoft.service.model;

import java.util.Date;

import com.bizvisionsoft.annotations.md.mongocodex.GetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;

public class WorkScheduleInfo {

	private Work work;

	public WorkScheduleInfo setWork(Work work) {
		this.work = work;
		return this;
	}

	public Work getWork() {
		return work;
	}

	@ReadValue({ "start_date1" })
	public Date getStart_date() {
		return work.getPlanStart();
	}

	@ReadValue({ "start_date" })
	public Date getStart_date1() {
		return work.getStart_date();
	}

	@ReadValue({ "end_date1" })
	public Date getEnd_date() {
		return work.getPlanFinish();
	}

	@ReadValue({ "end_date" })
	public Date getEnd_date1() {
		return work.getEnd_date();
	}

	@ReadValue("fullName")
	public String getFullName() {
		return work.getFullName();
	}

	@ReadValue("type")
	public String getType() {
		if (work.isMilestone())
			return "milestone";
		else if (work.isSummary())
			return "project";
		else
			return "task";
	}

	@GetValue("manageLevel")
	public String getManageLevel() {
		return work.getManageLevel();
	}

	@ReadValue("editable")
	public Boolean getEditable() {
		return true;
	}

	@ReadValue("open")
	public Boolean getOpen() {
		return true;
	}

	@ReadValue("id")
	public String getId() {
		return work.getId();
	}

	@ReadValue("parent")
	public String getParent() {
		return work.getParent();
	}

	@ReadValue("project")
	public String getProjectId() {
		return work.getProjectId();
	}

	@ReadValue("index")
	private int getIndex() {
		return work.getIndex();
	}

	@ReadValue("text")
	@Label(Label.NAME_LABEL)
	private String getText() {
		return work.toString();
	}

	@Override
	@Label
	public String toString() {
		return work.toString();
	}
}
