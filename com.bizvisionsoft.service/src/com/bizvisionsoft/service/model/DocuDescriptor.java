package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.mongocodex.codec.JsonExternalizable;

public class DocuDescriptor implements JsonExternalizable {

	public String domain;

	/** ฑ๊สถ Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String documentnumber;

	@ReadValue
	@WriteValue
	private String vid;

	@ReadValue
	@WriteValue
	private String desc;

	@ReadValue
	@WriteValue
	private ObjectId folder_id;

	@ReadValue
	@WriteValue
	private String phase;

	@ReadValue
	@WriteValue
	private String security;

	@ReadValue
	@WriteValue
	private String owner;

	@ReadValue
	@WriteValue
	private String plmTypeDesc;

	@ReadValue
	@WriteValue
	private String createBy;

	@ReadValue
	@WriteValue
	private Date createOn;

	@ReadValue
	@WriteValue
	private String path;
}
