package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

public class ResourceAssignment {

	public ObjectId work_id;

	public String usedHumanResId;

	public String usedEquipResId;

	public String usedTypedResId;

	public ObjectId resTypeId;

	@WriteValue("usedHumanRes")
	public void setHumanResource(User res) {
		usedHumanResId = Optional.ofNullable(res).map(h -> h.getUserId()).orElse(null);
		resTypeId = res.getResourceType_id();
	}

	@WriteValue("usedEquipRes")
	public void setEquipResource(Equipment res) {
		usedEquipResId = Optional.ofNullable(res).map(h -> h.getId()).orElse(null);
		resTypeId = res.getResourceType_id();
	}

	@WriteValue("usedTypedRes")
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

	public ResourcePlanInTemplate getResourcePlanInTemplate() {
		return new ResourcePlanInTemplate().setWork_id(work_id).setUsedHumanResId(usedHumanResId)
				.setUsedEquipResId(usedEquipResId).setUsedTypedResId(usedTypedResId).setResTypeId(resTypeId);
	}

	@ReadValue
	public double actualBasicQty;

	@WriteValue("actualBasicQty")
	private void set_actualBasicQty(String _actualBasicQty) {
		double __actualBasicQty;
		try {
			__actualBasicQty = Double.parseDouble(_actualBasicQty);
		} catch (Exception e) {
			throw new RuntimeException("输入类型错误。");
		}
		if (__actualBasicQty < 0) {
			throw new RuntimeException("实际标准用量需大于零。");
		}
		actualBasicQty = __actualBasicQty;
	}

	@ReadValue
	public double actualOverTimeQty;

	@WriteValue("actualOverTimeQty")
	private void set_actualOverTimeQty(String _actualOverTimeQty) {
		double __actualOverTimeQtyBasicQty;
		try {
			__actualOverTimeQtyBasicQty = Double.parseDouble(_actualOverTimeQty);
		} catch (Exception e) {
			throw new RuntimeException("输入类型错误。");
		}
		if (__actualOverTimeQtyBasicQty < 0) {
			throw new RuntimeException("实际加班用量需大于零。");
		}
		actualOverTimeQty = __actualOverTimeQtyBasicQty;
	}

	public ResourceActual getResourceActual() {
		return new ResourceActual().setWork_id(work_id).setUsedHumanResId(usedHumanResId)
				.setUsedEquipResId(usedEquipResId).setUsedTypedResId(usedTypedResId).setResTypeId(resTypeId);
	}

	public Document getResourceActualDocument() {
		return new Document("work_id", work_id).append("usedHumanResId", usedHumanResId)
				.append("usedEquipResId", usedEquipResId).append("usedTypedResId", usedTypedResId)
				.append("resTypeId", resTypeId);
	}

	@ReadValue
	@WriteValue
	public Date from;

	@ReadValue
	@WriteValue
	public Date to;

}
