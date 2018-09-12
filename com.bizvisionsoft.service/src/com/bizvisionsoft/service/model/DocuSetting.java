package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("docuSetting")
public class DocuSetting {

	////////////////////////////////////////////////////////////////////////////////////////////////
	// 基本的一些字段

	/** 标识 Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;
	
	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private ObjectId folder_id;
	
	@ReadValue
	@WriteValue
	private ObjectId docuTemplate_id;
	

	@Override
	@Label
	public String toString() {
		return name ;
	}
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "文档设置";
	
}
