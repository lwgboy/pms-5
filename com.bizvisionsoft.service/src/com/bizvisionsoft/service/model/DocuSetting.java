package com.bizvisionsoft.service.model;

import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
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
	private String description;

	@ReadValue
	@WriteValue
	private ObjectId folderInTemplate_id;

	@ReadValue
	@SetValue
	private FolderInTemplate folderInTemplate;

	@WriteValue("folderInTemplate")
	private void writeFolderInTemplate(FolderInTemplate folderInTemplate) {
		this.folderInTemplate = folderInTemplate;
		this.folderInTemplate_id = Optional.ofNullable(folderInTemplate).map(e -> e.get_id()).orElse(null);
	}

	@ReadValue
	@WriteValue
	private ObjectId docuTemplate_id;

	@ReadValue
	@SetValue
	private DocuTemplate docuTemplate;

	@WriteValue("docuTemplate")
	private void writeDocuTemplate(DocuTemplate docuTemplate) {
		this.docuTemplate = docuTemplate;
		this.docuTemplate_id = Optional.ofNullable(docuTemplate).map(e -> e.get_id()).orElse(null);
	}

	@ReadValue
	@WriteValue
	private ObjectId workPackage_id;

	@Override
	@Label
	public String toString() {
		return name;
	}

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "文档设置";

	public DocuSetting setWorkPackage_id(ObjectId workPackage_id) {
		this.workPackage_id = workPackage_id;
		return this;
	}

	public DocuSetting setName(String name) {
		this.name = name;
		return this;
	}
}
