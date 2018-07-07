package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("resourcePlan")
public class ResourcePlan {

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@Persistence
	private ObjectId work_id;

	public ObjectId getWork_id() {
		return work_id;
	}

	@ReadValue({ "type", ReadValue.TYPE })
	@SetValue
	private String type;

	@ReadValue
	@SetValue
	private String name;

	@ReadValue
	@SetValue
	private String resId;

	@ReadValue("conflict")
	private String getConflictIndentify() {
		if (conflict && (usedHumanResId != null || usedEquipResId != null))
			return "<a class='layui-badge layui-bg-red' href='conflict' target='_rwt'>冲突</a>";
		return "";
	}

	@Persistence
	private String usedHumanResId;

	public String getUsedEquipResId() {
		return usedEquipResId;
	}

	@Persistence
	private String usedEquipResId;

	public String getUsedHumanResId() {
		return usedHumanResId;
	}

	@Persistence
	private String usedTypedResId;

	public String getUsedTypedResId() {
		return usedTypedResId;
	}

	@SetValue
	private ResourceType resType;

	@ReadValue("basicRate")
	private double getBasicRate() {
		return resType.getBasicRate();
	}

	@ReadValue("overtimeRate")
	private double getOvertimeRate() {
		return resType.getOvertimeRate();
	}

	@ReadValue
	private double planBasicQty;

	public void setPlanBasicQty(double planBasicQty) {
		this.planBasicQty = planBasicQty;
	}

	@ReadValue
	private double planOverTimeQty;

	public void setPlanOverTimeQty(double planOverTimeQty) {
		this.planOverTimeQty = planOverTimeQty;
	}

	@Persistence
	private ObjectId resTypeId;

	@Persistence
	private Date id;

	@Persistence
	private int qty;

	public void setQty(int qty) {
		this.qty = qty;
	}

	public int getQty() {
		return qty;
	}

	@ReadValue("planAmount")
	private Double getPlanAmount() {
		return getBasicRate() * planBasicQty + getOvertimeRate() * planOverTimeQty;
	}

	@Override
	@Label
	public String toString() {
		return name + " [" + resId + "]";
	}

	@SetValue
	private boolean conflict;

	@ImageURL("resIds")
	private String getLogo() {
		if ("人力资源".equals(type))
			return "/img/user_c.svg";
		else if ("设备设施".equals(type))
			return "/img/equipment_c.svg";
		return "/img/resource_c.svg";
	}

	public ResourcePlan setWork_id(ObjectId work_id) {
		this.work_id = work_id;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	public ResourcePlan setUsedHumanResId(String usedHumanResId) {
		this.usedHumanResId = usedHumanResId;
		return this;
	}

	public ResourcePlan setUsedEquipResId(String usedEquipResId) {
		this.usedEquipResId = usedEquipResId;
		return this;
	}

	public ResourcePlan setUsedTypedResId(String usedTypedResId) {
		this.usedTypedResId = usedTypedResId;
		return this;
	}

	public ResourcePlan setResTypeId(ObjectId resTypeId) {
		this.resTypeId = resTypeId;
		return this;
	}

	public ObjectId getResTypeId() {
		return resTypeId;
	}

	public void setId(Date id) {
		this.id = id;
	}

	public Date getId() {
		return id;
	}

	public double getWorks() {
		return planBasicQty + planOverTimeQty;
	}

}
