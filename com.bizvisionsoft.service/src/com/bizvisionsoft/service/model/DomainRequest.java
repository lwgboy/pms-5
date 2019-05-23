package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("request")
public class DomainRequest {

	@ReadValue
	@WriteValue
	public ObjectId _id;
	@ReadValue
	@WriteValue
	public String email;
	@ReadValue
	@WriteValue
	public String company;
	@ReadValue
	@WriteValue
	public List<String> site;
	@ReadValue
	@WriteValue
	public String client;
	@ReadValue
	@WriteValue
	public String request;
	@ReadValue
	@WriteValue
	public String scheme;
	@ReadValue
	@WriteValue
	public boolean loadBasicData;

}
