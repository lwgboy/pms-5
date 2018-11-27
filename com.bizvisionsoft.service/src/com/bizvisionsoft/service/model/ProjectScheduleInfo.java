package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.service.tools.Check;

public class ProjectScheduleInfo {

	private Project project;

	private Work work;

	private String type;

	public ProjectScheduleInfo setProject(Project project) {
		this.project = project;
		this.type = Project.class.getName();
		return this;
	}

	public ProjectScheduleInfo setWork(Work work) {
		this.work = work;
		this.type = Work.class.getName();
		return this;
	}

	public Work getWork() {
		return work;
	}

	public Project getProject() {
		return project;
	}

	public boolean typeEquals(Class<?> clas) {
		return clas.getName().equals(getType());
	}

	public String getType() {
		return type;
	}

	public ObjectId get_id() {
		if (typeEquals(Project.class))
			return project.get_id();
		else
			return work.get_id();
	}

	@ReadValue("name")
	public String getName() {
		if (typeEquals(Project.class))
			return project.getProjectName();
		else
			return work.getText();
	}

	@ReadValue("id")
	public String getId() {
		if (typeEquals(Project.class))
			return project.getId();
		else
			return work.getWBSCode();
	}

	@ReadValue("statusHtml")
	public String getStatusHtml() {
		if (typeEquals(Project.class))
			return project.getStatusHtml();
		else
			return work.getStatusHtml();
	}

	@ReadValue("manageLevelHtml")
	public String getManageLevelHtml() {
		if (typeEquals(Work.class))
			return work.getManageLevelHtml();
		return "";
	}

	@ImageURL("milestoneIcon")
	public String getMilestoneIcon() {
		if (typeEquals(Work.class))
			return work.getMilestoneIcon();
		return null;
	}

	@ImageURL("name")
	public String getImage() {
		if (typeEquals(Project.class))
			return project.getIcon();
		// TODO 工作图标的显示
		return null;
	}

	@ReadValue("planStart")
	public Date getPlanStart() {
		if (typeEquals(Project.class))
			return project.getPlanStart();
		else
			return work.getPlanStart();
	}

	@ReadValue("planFinish")
	public Date getPlanFinish() {
		if (typeEquals(Project.class))
			return project.getPlanFinish();
		else
			return work.getPlanFinish();
	}

	@ReadValue("planDuration")
	public int getPlanDuration() {
		if (typeEquals(Project.class))
			return project.getPlanDuration();
		else
			return work.getPlanDuration();
	}

	@ReadValue("planWorks")
	public double getPlanWorks() {
		if (typeEquals(Project.class))
			return project.getPlanWorks();
		else
			return work.getPlanWorks();
	}

	@ReadValue("actualStart")
	public Date getActualStart() {
		if (typeEquals(Project.class))
			return project.getActualStart();
		else
			return work.getActualStart();
	}

	@ReadValue("actualFinish")
	public Date getActualFinish() {
		if (typeEquals(Project.class))
			return project.getActualFinish();
		else
			return work.getActualFinish();
	}

	@ReadValue("actualDuration")
	public int getActualDuration() {
		if (typeEquals(Project.class))
			return project.getActualDuration();
		else
			return work.getActualDuration();
	}

	@ReadValue("actualWorks")
	public double getActualWorks() {
		if (typeEquals(Project.class))
			return project.getActualWorks();
		else
			return work.getActualWorks();
	}

	@ReadValue("warningIcon")
	public String getWarningIcon() {
		if (typeEquals(Project.class))
			return project.getOverdueHtml();
		else
			return work.getWarningIcon();
	}

	@ReadValue("chargerInfoHtml")
	public String getChargerInfoHtml() {
		if (typeEquals(Project.class))
			return project.readPMInfoHtml();
		else
			return work.getChargerInfoHtml();
	}

	@ReadValue("assignerInfoHtml")
	public String getAssignerInfoHtml() {
		if (typeEquals(Work.class))
			return work.getAssignerInfoHtml();
		return "";
	}

	@ReadValue("war")
	public Double getWAR() {
		if (typeEquals(Project.class))
			return project.getWAR();
		else
			return work.getWAR();
	}

	@ReadValue("dar")
	public Double getDAR() {
		if (typeEquals(Project.class))
			return project.getDAR();
		else
			return work.getDAR();
	}

	@ReadValue("sar")
	public Double getSAR() {
		if (typeEquals(Project.class))
			return project.getSAR();
		else
			return work.getSAR();
	}

	@ReadValue("estFinish")
	public Date getEstimateFinish() {
		if (typeEquals(Project.class))
			return project.getEstimateFinish();
		else
			return work.getEstimateFinish();
	}

	@ReadValue("estOverdual")
	public Integer getEstimateOverdue() {
		if (typeEquals(Project.class))
			return project.getEstimateOverdue();
		else
			return work.getEstimateOverdue();
	}

	@ReadValue("estDuration")
	public Integer getEstimateDuration() {
		if (typeEquals(Project.class))
			return project.getEstimateDuration();
		else
			return work.getEstimateDuration();
	}

	@ReadValue("TF")
	public Double getTF() {
		if (typeEquals(Work.class))
			return work.getTF();
		return null;
	}

	@ReadValue("FF")
	public Double getFF() {
		if (typeEquals(Work.class))
			return work.getFF();

		return null;
	}

	@ReadValue("ACP")
	public Double getACP() {
		if (typeEquals(Work.class))
			return work.getACP();
		return null;
	}

	@ReadValue("ACI")
	public Double getACI() {
		if (typeEquals(Work.class))
			return work.getACI();
		return null;
	}

	@Behavior({ "打开" })
	public boolean behaviourOpen() {
		return (typeEquals(Work.class) && work.behaviourOpenWorkPackage()) || typeEquals(Project.class);
	}

	@Behavior({ "菜单" })
	public boolean behaviourMenu() {
		return typeEquals(Project.class);
	}

	@Behavior({ "删除", "编辑" })
	private boolean behaviourEditProjectInfo() {
		return typeEquals(Project.class) && ProjectStatus.Created.equals(project.getStatus());
	}

	@Behavior("设置编号")
	private boolean behaviourEditProjectId() {
		return typeEquals(Project.class) && ProjectStatus.Created.equals(project.getStatus()) && Check.isNotAssigned(project.getId());
	}

	@Behavior("批准启动")
	private boolean behaviourApproveProjectStart() {
		return typeEquals(Project.class) && ProjectStatus.Created.equals(project.getStatus())
				&& !Boolean.TRUE.equals(project.getStartApproved());
	}
}
