package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

public interface IOBSScope {

	public ObjectId getOBS_id();

	public ObjectId getScope_id();

	public OBSItem newOBSScopeRoot();

	public void updateOBSRootId(ObjectId obs_id);

}
