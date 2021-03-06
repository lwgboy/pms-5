package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

public interface IWBSScope extends IScope{

	public Workspace getWorkspace();

	public ObjectId getParent_id();

	public ObjectId getProject_id();

	public String getProjectName();

	public String getProjectNumber();
	
	public List<WorkLink> createGanttLinkDataSet();

	public List<Work> createGanttTaskDataSet();


}
