package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;

public interface IWBSScope {

	public ObjectId getWBS_id();

	public ObjectId getScope_id();

	public String getCheckOutUserId();

	public ObjectId getSpaceId();

	public ObjectId getProject_id();

	public BasicDBObject getCheckOutKey();

}
