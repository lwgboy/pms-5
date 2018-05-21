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
	private String id;

	@Persistence
	private String usedHumanResId;

	@Persistence
	private String usedEquipResId;

	@Persistence
	private String usedTypedResId;

	@SetValue
	private ResourceType resType;

	@ReadValue("pricingModel")
	private String getPricingModel() {
		return resType.getPricingModel();
	}

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
	public void setTypedResource(Object res) {
		if (res instanceof User) {
			setHumanResource((User) res);
		} else if (res instanceof Equipment) {
			setEquipResource((Equipment) res);
		} else if (res instanceof ResourceType) {
			usedTypedResId = Optional.ofNullable((ResourceType) res).map(h -> h.getId()).orElse(null);
		}

	}

	@ReadValue
	private Integer planBasicQty;

	@WriteValue("planBasicQty")
	private void set_planBasicQty(String _planBasicQty) {
		if (_planBasicQty.isEmpty()) {
			planBasicQty = 0;
		} else {
			try {
				planBasicQty = Integer.parseInt(_planBasicQty);
			} catch (Exception e) {
				throw new RuntimeException("请输入整数类型的数量。");
			}
		}
	}

	@ReadValue
	private Integer planOverTimeQty;

	@WriteValue("planOverTimeQty")
	private void set_planOverTimeQty(String _planOverTimeQty) {
		if (_planOverTimeQty.isEmpty()) {
			planOverTimeQty = 0;
		} else {
			try {
				planOverTimeQty = Integer.parseInt(_planOverTimeQty);
			} catch (Exception e) {
				throw new RuntimeException("请输入整数类型的数量。");
			}
		}
	}

	@ReadValue("planAmount")
	private Double getPlanAmount() {
		return getBasicRate() * planBasicQty + getOvertimeRate() * planOverTimeQty;
	}

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@ImageURL("id")
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

}
