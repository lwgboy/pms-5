package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

public class WorkspaceGanttData {

	private ObjectId space_id;

	private List<WorkInfo> tasks;

	private List<WorkLinkInfo> links;

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

}
