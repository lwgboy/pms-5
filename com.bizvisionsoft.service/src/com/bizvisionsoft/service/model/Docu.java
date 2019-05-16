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
import com.bizvisionsoft.annotations.md.service.ReadEditorConfig;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.mongocodex.codec.JsonExternalizable;
import com.bizvisionsoft.service.sn.DocNumberGenerator;
import com.bizvisionsoft.service.tools.Check;

@PersistenceCollection("docu")
public class Docu implements JsonExternalizable {

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

	@Exclude
	public String domain;
	
	public String generateId() {
		return (String) new DocNumberGenerator().generate(this, Generator.DEFAULT_NAME, "docu", String.class,domain);
	}

	@ReadValue
	@WriteValue
	private ObjectId folder_id;

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "文档";

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

	@ReadValue
	@WriteValue
	private OperationInfo creationInfo;

	@ReadValue
	@WriteValue
	private String editorName;

	@ReadValue("createOn")
	private Date readCreateOn() {
		return Optional.ofNullable(creationInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("createByInfo")
	private String readCreateByInfo() {
		return Optional.ofNullable(creationInfo).map(c -> c.userName).orElse(null);
	}

	@ReadValue("createBy")
	private String readCreateBy() {
		return Optional.ofNullable(creationInfo).map(c -> c.userId).orElse(null);
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

	public Docu setCategory(List<String> category) {
		this.category = category;
		return this;
	}

	public Docu setTag(List<String> tag) {
		this.tag = tag;
		return this;
	}

	public Docu setDocuFiles(List<RemoteFile> docuFiles) {
		this.docuFiles = docuFiles;
		return this;
	}

	public Docu setEditorName(String editorName) {
		this.editorName = editorName;
		return this;
	}

	@ReadEditorConfig({ "打开", "编辑" })
	public String getEditorName() {
		return Check.option(editorName).orElse("通用文档编辑器");
	}
}
