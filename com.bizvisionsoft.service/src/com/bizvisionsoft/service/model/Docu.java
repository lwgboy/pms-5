package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.sn.DocNumberGenerator;

@PersistenceCollection("docu")
public class Docu {

	////////////////////////////////////////////////////////////////////////////////////////////////
	// 基本的一些字段

	/** 标识 Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	/** 标识 Y **/
	@ReadValue
	@WriteValue
	@Generator(name = Generator.DEFAULT_NAME, key = "docu", generator = DocNumberGenerator.class, callback = Generator.NONE_CALLBACK)
	private String id;

	@ReadValue
	@WriteValue
	private ObjectId folder_id;

	@ReadValue
	@WriteValue
	private String name;
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "文档";
	
	public Docu setName(String name) {
		this.name = name;
		return this;
	}

	@ReadValue
	@WriteValue
	private String description;

	@ReadValue
	@WriteValue
	private String summary;

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
	private List<ObjectId> workPackage_id;

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
	

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	public ObjectId get_id() {
		return _id;
	}

	public Docu setFolder_id(ObjectId folder_id) {
		this.folder_id = folder_id;
		return this;
	}

	public Docu setCreationInfo(OperationInfo creationInfo) {
		this.creationInfo = creationInfo;
		return this;
	}

	public Docu addWorkPackageId(ObjectId workPackage_id) {
		if (this.workPackage_id == null) {
			this.workPackage_id = new ArrayList<ObjectId>();
		}
		this.workPackage_id.add(workPackage_id);
		return this;
	}
	
}
