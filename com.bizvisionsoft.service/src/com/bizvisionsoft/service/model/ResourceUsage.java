package com.bizvisionsoft.service.model;

import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("resourceUsage")
public class ResourceUsage {

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private ObjectId work_id;

	@ReadValue
	@WriteValue
	private String usedTypedResId;

	@ReadValue
	@WriteValue
	private String usedHumanResId;

	@ReadValue
	@WriteValue
	private String usedEquipResId;

	@ReadValue("id")
	private String getId() {
		if (usedHumanResId != null)
			return usedHumanResId;
		if (usedEquipResId != null)
			return usedEquipResId;
		if (usedTypedResId != null)
			return usedTypedResId;
		return "";
	}

	@ReadValue("name")
	private String getName() {
		return "";
	}
	
	@WriteValue("usedHumanRes")
	public void setHumanResource(User hr) {
		this.usedHumanResId = Optional.ofNullable(hr).map(h -> h.getUserId()).orElse(null);
	}
	

	@ReadValue
	private Integer planBasicQty;

	@ReadValue
	private Integer planOverTimeQty;

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
	
	@Override
	@Label
	public String toString() {
		return getName() + " [" + getId() + "]";
	}

	public ResourceUsage setWork_id(ObjectId work_id) {
		this.work_id = work_id;
		return this;
	}

}
