package com.bizvisionsoft.service.model;

import java.util.Optional;

import org.bson.types.ObjectId;

public class ResourceAssignment {

	public ObjectId work_id;

	public String usedHumanResId;

	public String usedEquipResId;

	public String usedTypedResId;

	public ObjectId resTypeId;

	public void setHumanResource(User res) {
		usedHumanResId = Optional.ofNullable(res).map(h -> h.getUserId()).orElse(null);
		resTypeId = res.getResourceType_id();
	}

	public void setEquipResource(Equipment res) {
		usedEquipResId = Optional.ofNullable(res).map(h -> h.getId()).orElse(null);
		resTypeId = res.getResourceType_id();
	}

	public ResourceAssignment setTypedResource(Object res) {
		if (res instanceof User) {
			setHumanResource((User) res);
		} else if (res instanceof Equipment) {
			setEquipResource((Equipment) res);
		} else if (res instanceof ResourceType) {
			usedTypedResId = Optional.ofNullable((ResourceType) res).map(h -> h.getId()).orElse(null);
			resTypeId = ((ResourceType) res).get_id();
		}
		return this;
	}

	public ResourceAssignment setWork_id(ObjectId work_id) {
		this.work_id = work_id;
		return this;
	}

	public String getResId() {
		if (usedHumanResId != null) {
			return usedHumanResId;
		} else if (usedEquipResId != null) {
			return usedEquipResId;
		} else {
			return usedTypedResId;
		}

	}

	public ResourcePlan getResourcePlan() {
		return new ResourcePlan().setWork_id(work_id).setUsedHumanResId(usedHumanResId)
				.setUsedEquipResId(usedEquipResId).setUsedTypedResId(usedTypedResId).setResTypeId(resTypeId);
	}

}
