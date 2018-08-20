package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

public interface ICBSScope extends IScope{

	public ObjectId getCBS_id();

	public Date[] getCBSRange();

	public String getScopeName();

	public String getStatus();
}
