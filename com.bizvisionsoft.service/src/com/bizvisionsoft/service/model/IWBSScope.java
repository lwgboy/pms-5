package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;

public interface IWBSScope {

	public ObjectId getWBS_id();

	public ObjectId getScope_id();

	public String getCheckOutUserId();

	public ObjectId getSpaceId();

	public ObjectId getProject_id();

	/**
	 * 构造检出信息字段
	 * 
	 * @return BasicDBObject 由project_id、work_id和space_id三个字段组合构成
	 */
	public BasicDBObject getCheckOutKey();

}
