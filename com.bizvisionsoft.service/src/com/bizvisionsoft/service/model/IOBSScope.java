package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

public interface IOBSScope extends IScope{
	
	public ObjectId getOBS_id();

	public OBSItem newOBSScopeRoot();

	public void updateOBSRootId(ObjectId obs_id);

}
