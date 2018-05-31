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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkResourcePlanDetail other = (WorkResourcePlanDetail) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}

	public Double getWorks(Date date) {
		return children.stream().filter(i -> i.getId().equals(date)).findFirst().map(r -> r.getWorks()).orElse(null);
	}

}
