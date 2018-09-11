package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("docuTemplate")
public class DocuTemplate {

	////////////////////////////////////////////////////////////////////////////////////////////////
	// 基本的一些字段

	/** 标识 Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String name;
	
	public DocuTemplate setName(String name) {
		this.name = name;
		return this;
	}

	@ReadValue
	@WriteValue
	private String description;

	@ReadValue
	@WriteValue
	private List<RemoteFile> docuFiles;

	@ReadValue
	@WriteValue
	private List<String> tag;

	@ReadValue
	@WriteValue
	private List<String> category;
	
	@ReadValue
	@WriteValue
	private String assemblyId;

	@Override
	@Label
	public String toString() {
		return name ;
	}
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "文档模板";

}
