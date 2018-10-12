package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
	
	public ObjectId get_id() {
		return _id;
	}

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
	private String editorName;

	@Override
	@Label
	public String toString() {
		return name ;
	}
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "文档模板";
	
	@ReadValue
	@WriteValue
	private OperationInfo creationInfo;
	
	@ReadValue("createOn")
	private Date readCreateOn() {
		return Optional.ofNullable(creationInfo).map(c->c.date).orElse(null);
	}

	@ReadValue("createByInfo")
	private String readCreateByInfo() {
		return Optional.ofNullable(creationInfo).map(c->c.userName).orElse(null);
	}
	
	@ReadValue("createBy")
	private String readCreateBy() {
		return Optional.ofNullable(creationInfo).map(c->c.userId).orElse(null);
	}
	
	public String getEditorName() {
		return editorName;
	}

}
