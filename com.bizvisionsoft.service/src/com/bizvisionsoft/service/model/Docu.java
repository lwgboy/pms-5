package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
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
	private String createBy;

	@ReadValue
	@SetValue
	private String createByInfo;

	@ReadValue
	@WriteValue
	private Date createOn;

	@ReadValue
	@WriteValue
	private ObjectId workPackage_id;

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	public Docu setFolder_id(ObjectId folder_id) {
		this.folder_id = folder_id;
		return this;
	}

	public Docu setCreationInfo(CreationInfo ci) {
		this.createOn = ci.date;
		this.createBy = ci.userId;
		this.createByInfo = ci.userName;
		return this;
	}

	public Docu addWorkPackageId(ObjectId workPackage_id) {
		this.workPackage_id = workPackage_id;
		return this;
	}
}
