package com.bizvisionsoft.service.model;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class WorkResourceAssignment {

	private ObjectId work_id;

	private ObjectId workReport_id;

	private String usedEquipResId;

	private String usedHumanResId;

	private String usedTypedResId;

	public ObjectId getWork_id() {
		return work_id;
	}

	public WorkResourceAssignment setWork_id(ObjectId work_id) {
		this.work_id = work_id;
		return this;
	}

	public ObjectId getWorkReport_id() {
		return workReport_id;
	}

	public WorkResourceAssignment setWorkReport_id(ObjectId workReport_id) {
		this.workReport_id = workReport_id;
		return this;
	}

	public String getUsedEquipResId() {
		return usedEquipResId;
	}

	public WorkResourceAssignment setUsedEquipResId(String usedEquipResId) {
		this.usedEquipResId = usedEquipResId;
		return this;
	}

	public String getUsedHumanResId() {
		return usedHumanResId;
	}

	public WorkResourceAssignment setUsedHumanResId(String usedHumanResId) {
		this.usedHumanResId = usedHumanResId;
		return this;
	}

	public String getUsedTypedResId() {
		return usedTypedResId;
	}

	public WorkResourceAssignment setUsedTypedResId(String usedTypedResId) {
		this.usedTypedResId = usedTypedResId;
		return this;
	}

	public Document getQuery() {
		return new Document("work_id", work_id).append("workReport_id", workReport_id)
				.append("usedEquipResId", usedEquipResId).append("usedHumanResId", usedHumanResId)
				.append("usedTypedResId", usedTypedResId);
	}

}
