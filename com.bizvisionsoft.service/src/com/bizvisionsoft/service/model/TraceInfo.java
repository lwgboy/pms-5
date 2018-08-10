package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("traceInfo")
public class TraceInfo {
	
	@ReadValue
	@WriteValue
	public ObjectId _id;
	
	@ReadValue
	@WriteValue
	public String userName;
	
	@ReadValue
	@WriteValue
	public String userId;
	
	@ReadValue
	@WriteValue
	public String consignerName;
	
	@ReadValue
	@WriteValue
	public String consignerId;

	@ReadValue
	@WriteValue
	public Date date;

	@ReadValue
	@WriteValue
	public String actionName;

	@ReadValue
	@WriteValue
	public String actionId;
	
	@ReadValue
	@WriteValue
	public String contextInput;
	
	@ReadValue
	@WriteValue
	public String contextRootInput;

	@ReadValue
	@WriteValue
	public String contextSelection;
	
	@ReadValue
	@WriteValue
	public String assemblyName;
	
	@ReadValue
	@WriteValue
	public String assemblyId;
}
