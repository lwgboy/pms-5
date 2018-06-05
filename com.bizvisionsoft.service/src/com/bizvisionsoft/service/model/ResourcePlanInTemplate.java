package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;

@PersistenceCollection("resourcePlanInTemplate")
public class ResourcePlanInTemplate {

	@Persistence
	private ObjectId _id;

	@Persistence
	private ObjectId work_id;


	@ReadValue({ "type", ReadValue.TYPE })
	@SetValue
	private String type;

	@ReadValue
	@SetValue
	private String name;

	@ReadValue
	@SetValue
	private String resId;

	@Persistence
	private String usedHumanResId;

	@Persistence
	private String usedEquipResId;

	@Persistence
	private String usedTypedResId;

	@Persistence
	private ObjectId resTypeId;


	@Override
	@Label
	public String toString() {
		return name + " [" + resId + "]";
	}

	@ImageURL("resId")
	private String getLogo() {
		if ("人力资源".equals(type))
			return "/img/user_c.svg";
		else if ("设备设施".equals(type))
			return "/img/equipment_c.svg";
		return "/img/resource_c.svg";
	}

	public ResourcePlanInTemplate setWork_id(ObjectId work_id) {
		this.work_id = work_id;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	public ResourcePlanInTemplate setUsedHumanResId(String usedHumanResId) {
		this.usedHumanResId = usedHumanResId;
		return this;
	}

	public ResourcePlanInTemplate setUsedEquipResId(String usedEquipResId) {
		this.usedEquipResId = usedEquipResId;
		return this;
	}

	public ResourcePlanInTemplate setUsedTypedResId(String usedTypedResId) {
		this.usedTypedResId = usedTypedResId;
		return this;
	}

	public ResourcePlanInTemplate setResTypeId(ObjectId resTypeId) {
		this.resTypeId = resTypeId;
		return this;
	}

	public String getUsedEquipResId() {
		return usedEquipResId;
	}
	
	public String getUsedHumanResId() {
		return usedHumanResId;
	}
	
	public String getUsedTypedResId() {
		return usedTypedResId;
	}
	
	public ObjectId getWork_id() {
		return work_id;
	}

}
