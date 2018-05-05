package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

public interface ICBSScope {
	
	public ObjectId getCBS_id();

	public ObjectId getScope_id();

	public Date[] getCBSRange();


}
