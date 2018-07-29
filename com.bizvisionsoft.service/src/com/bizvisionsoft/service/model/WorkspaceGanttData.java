package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

public class WorkspaceGanttData {

	private ObjectId space_id;

	private List<WorkInfo> tasks;

	private List<WorkLinkInfo> links;

	private ObjectId template_id;

	private ObjectId work_id;

	private ObjectId project_id;

	private List<WorkInTemplate> taskInTemplates;

	private List<WorkLinkInTemplate> linkInTemplates;

	public WorkspaceGanttData setWorkspaceId(ObjectId space_id) {
		this.space_id = space_id;
		return this;
	}

	public ObjectId getSpace_id() {
		return space_id;
	}

	public WorkspaceGanttData setWorks(List<WorkInfo> tasks) {
		this.tasks = tasks;
		return this;
	}

	public WorkspaceGanttData setLinks(List<WorkLinkInfo> links) {
		this.links = links;
		return this;
	}

	public List<WorkLinkInfo> getLinks() {
		return links;
	}

	public List<WorkInfo> getTasks() {
		return tasks;
	}

	public WorkspaceGanttData setTemplateId(ObjectId template_id) {
		this.template_id = template_id;
		return this;
	}

	public ObjectId getTemplate_id() {
		return template_id;
	}

	public WorkspaceGanttData setWorkInTemplates(List<WorkInTemplate> taskInTemplates) {
		this.taskInTemplates = taskInTemplates;
		return this;
	}

	public List<WorkInTemplate> getWorkInTemplates() {
		return taskInTemplates;
	}

	public WorkspaceGanttData setLinkInTemplates(List<WorkLinkInTemplate> linkInTemplates) {
		this.linkInTemplates = linkInTemplates;
		return this;
	}

	public List<WorkLinkInTemplate> getWorkLinkInTemplates() {
		return linkInTemplates;
	}

	public WorkspaceGanttData setWork_id(ObjectId work_id) {
		this.work_id = work_id;
		return this;
	}

	public ObjectId getWork_id() {
		return work_id;
	}

	public WorkspaceGanttData setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}

	public ObjectId getProject_id() {
		return project_id;
	}

}
