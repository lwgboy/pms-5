package com.bizvisionsoft.service.model;

import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;

@PersistenceCollection("equipment")
public class Equipment {
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//基本的一些字段
	
	/** 标识 Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	/** 编号 Y **/
	@ReadValue
	@WriteValue
	private String id;
	
	@ImageURL("id")
	@Exclude
	private String logo = "/img/equipment_c.svg";
	
	@ReadValue
	@WriteValue
	private String name;
	
	@ReadValue
	@WriteValue
	private String description;
	
	@Persistence("org_id")
	private ObjectId organizationId;
	
	@SetValue
	@ReadValue
	private String orgFullName;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "设备设施";
	
	@WriteValue("organization ")
	public void writeOrganization(Organization org) {
		this.organizationId = Optional.ofNullable(org).map(o -> o.get_id()).orElse(null);
	}

	@ReadValue("organization ")
	public Organization readOrganization() {
		return Optional.ofNullable(organizationId).map(_id -> ServicesLoader.get(OrganizationService.class).get(_id))
				.orElse(null);
	}
	
	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}
	////////////////////////////////////////////////////////////////////////////////////////////////

	@Persistence
	@ReadValue
	@WriteValue
	private ObjectId resourceType_id;

	public ObjectId get_id() {
		return _id;
	}

}
