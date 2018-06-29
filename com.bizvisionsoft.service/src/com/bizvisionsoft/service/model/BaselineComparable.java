package com.bizvisionsoft.service.model;

import java.util.Date;

import com.bizvisionsoft.annotations.md.mongocodex.GetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;

public class BaselineComparable {

	private Work work1;

	public BaselineComparable setWork1(Work work1) {
		this.work1 = work1;
		return this;
	}

	private Work work2;

	public BaselineComparable setWork2(Work work2) {
		this.work2 = work2;
		return this;
	}

	public Work getWork1() {
		return work1;
	}

	@ReadValue({ "start_date1" })
	public Date getStart_date() {
		return work2 != null ? work2.getStart_date() : null;
	}

	@ReadValue({ "start_date" })
	public Date getStart_date1() {
		return work1 != null ? work1.getStart_date() : null;
	}

	@ReadValue({ "end_date1" })
	public Date getEnd_date() {
		return work2 != null ? work2.getEnd_date() : null;
	}

	@ReadValue({ "end_date" })
	public Date getEnd_date1() {
		return work1 != null ? work1.getEnd_date() : null;
	}

	@ReadValue("fullName")
	public String getFullName() {
		return work1 != null ? work1.getFullName() : (work2 != null ? work2.getFullName() : "");
	}

	@ReadValue("type")
	public String getType() {
		if ((work1 != null && work1.isMilestone()) || (work2 != null && work2.isMilestone()))
			return "milestone";
		else if ((work1 != null && work1.isSummary()) || (work2 != null && work2.isSummary()))
			return "project";
		else
			return "task";
	}

	@GetValue("manageLevel")
	public String getManageLevel() {
		return work1 != null ? work1.getManageLevel() : (work2 != null ? work2.getManageLevel() : "");
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
		return work1 != null ? work1.getId() : (work2 != null ? work2.getId() : "");
	}

	@ReadValue("parent")
	public String getParent() {
		return work1 != null ? work1.getParent() : (work2 != null ? work2.getParent() : "");
	}

	@ReadValue("project")
	public String getProjectId() {
		return work1 != null ? work1.getProjectId() : (work2 != null ? work2.getProjectId() : "");
	}

	@ReadValue("index")
	private int getIndex() {
		return work1 != null ? work1.getIndex() : (work2 != null ? work2.getIndex() : 999);
	}

	@ReadValue("text")
	@Label(Label.NAME_LABEL)
	private String getText() {
		return work1 != null ? work1.toString() : (work2 != null ? work2.toString() : "");
	}

	@Override
	@Label
	public String toString() {
		return work1 != null ? work1.toString() : (work2 != null ? work2.toString() : "");
	}

}
