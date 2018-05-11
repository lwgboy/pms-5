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
	 * ��������Ϣ�ֶ�
	 * 
	 * @return BasicDBObject ��project_id��work_id��space_id�����ֶ���Ϲ���
	 */
	public BasicDBObject getCheckOutKey();

}
