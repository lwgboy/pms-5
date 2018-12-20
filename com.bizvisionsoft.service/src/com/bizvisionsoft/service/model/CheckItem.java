package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("checkList")
public class CheckItem {

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private String description;
	
	@ReadValue
	@WriteValue
	private boolean checked;
	
	@ReadValue
	@WriteValue
	private String remark;
	
	@ReadValue
	@WriteValue
	private String signInfo;

	@Override
	@Label
	public String toString() {
		return name;
	}

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "¼ì²éÏî";

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
		CheckItem other = (CheckItem) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}
	
	public boolean isChecked() {
		return checked;
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getName() {
		return name;
	}

	public String getRemark() {
		return remark;
	}
	
	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getSignInfo() {
		return signInfo;
	}
	
	public void setSignInfo(String signInfo) {
		this.signInfo = signInfo;
	}

}
