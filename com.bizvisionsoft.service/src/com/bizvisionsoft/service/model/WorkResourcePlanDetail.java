package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ReadValue;

public class WorkResourcePlanDetail {

	@ReadValue
	@SetValue
	public ObjectId _id;

	@ReadValue
	@SetValue
	public String id;

	@ReadValue
	@SetValue
	public String name;

	@ReadValue
	@SetValue
	public String projectId;

	@ReadValue
	@SetValue
	public String projectName;

	@ReadValue
	@SetValue
	public Date planStart;

	@ReadValue
	@SetValue
	public Date planFinish;

	@ReadValue
	@SetValue
	public Date actualStart;

	@ReadValue
	@SetValue
	public Date actualFinish;

	@SetValue
	public List<ResourcePlan> children;

}
