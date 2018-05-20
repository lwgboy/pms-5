package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
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
	
	@ReadValue
	@WriteValue
	private double planBasicQty;
	
	@ReadValue
	@WriteValue
	private double planOverTimeQty;

}
