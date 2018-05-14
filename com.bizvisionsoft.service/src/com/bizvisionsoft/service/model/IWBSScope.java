package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

public interface IWBSScope {

	public Workspace getWorkspace();

	public ObjectId getParent_id();

	public ObjectId getProject_id();
	
	public ObjectId getScope_id();



}
