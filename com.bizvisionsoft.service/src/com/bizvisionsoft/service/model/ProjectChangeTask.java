package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.WriteValue;

public class ProjectChangeTask {

	public String id;

	public Date date;

	public String choice;

	@WriteValue
	public String comment;

	public String name;

	public ObjectId projectChange_id;

}
