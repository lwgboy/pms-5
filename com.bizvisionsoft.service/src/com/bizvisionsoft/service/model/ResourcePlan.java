package com.bizvisionsoft.service.model;

import java.util.Optional;

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
	@WriteValue
	@Persistence
	private ObjectId work_id;

	public ObjectId getWork_id() {
		return work_id;
	}

	@ReadValue({ "type", ReadValue.TYPE })
	@WriteValue
	@SetValue
	private String type;

	@ReadValue
	@WriteValue
	@SetValue
	private String name;

	@ReadValue
	@WriteValue
	@SetValue
	private String resId;

	@ReadValue("resIds")
	public String getResIds() {
		StringBuffer sb = new StringBuffer();
		sb.append(resId);
		if (conflict) {
			sb.append(" <span style=\"color: red;\">[冲突]</span> ");
		}
		return sb.toString();
	}

	@Persistence
	private String usedHumanResId;

	@Persistence
	private String usedEquipResId;

	@Persistence
	private String usedTypedResId;

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

	@WriteValue("usedHumanRes")
	public void setHumanResource(User res) {
		usedHumanResId = Optional.ofNullable(res).map(h -> h.getUserId()).orElse(null);
	}

	@WriteValue("usedEquipRes")
	public void setEquipResource(Equipment res) {
		usedEquipResId = Optional.ofNullable(res).map(h -> h.getId()).orElse(null);
	}

	@WriteValue("usedTypedRes")
	public ResourcePlan setTypedResource(Object res) {
		if (res instanceof User) {
			setHumanResource((User) res);
		} else if (res instanceof Equipment) {
			setEquipResource((Equipment) res);
		} else if (res instanceof ResourceType) {
			usedTypedResId = Optional.ofNullable((ResourceType) res).map(h -> h.getId()).orElse(null);
		}
		return this;
	}

	@ReadValue
	private double planBasicQty;

	public void setPlanBasicQty(double planBasicQty) {
		this.planBasicQty = planBasicQty;
	}

	@WriteValue("planBasicQty")
	private void set_planBasicQty(String _planBasicQty) {
		double __planBasicQty;
		try {
			__planBasicQty = Double.parseDouble(_planBasicQty);
		} catch (Exception e) {
			throw new RuntimeException("输入类型错误。");
		}
		if (__planBasicQty < 0) {
			throw new RuntimeException("计划标准用量需大于零。");
		}
		planBasicQty = __planBasicQty;
	}

	@ReadValue
	private double planOverTimeQty;

	@Persistence
	private ObjectId resTypeId;

	@Persistence
	private String id;

	@WriteValue("planOverTimeQty")
	private void set_planOverTimeQty(String _planOverTimeQty) {
		double __planOverTimeQtyBasicQty;
		try {
			__planOverTimeQtyBasicQty = Double.parseDouble(_planOverTimeQty);
		} catch (Exception e) {
			throw new RuntimeException("输入类型错误。");
		}
		if (__planOverTimeQtyBasicQty < 0) {
			throw new RuntimeException("计划加班用量需大于零。");
		}
		planOverTimeQty = __planOverTimeQtyBasicQty;
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

	public void setId(String id) {
		this.id = id;
	}

}
