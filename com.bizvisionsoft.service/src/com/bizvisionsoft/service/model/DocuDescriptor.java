package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.mongocodex.codec.JsonExternalizable;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;

@PersistenceCollection("docu")
public class DocuDescriptor implements JsonExternalizable {

	/** 标识 Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	/** 标识 Y **/
	@ReadValue
	@WriteValue
	private String documentnumber;

	public String domain;

	@ReadValue
	@WriteValue
	private ObjectId folder_id;

	@ReadValue
	@WriteValue
	private ObjectId formDef_id;

	@ReadValue
	@WriteValue
	private String desc;

	@ReadValue(ReadValue.TYPE)
	@Exclude	
	public static final String typeName = "文档";

	public DocuDescriptor setDesc(String desc) {
		this.desc = desc;
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
	private List<String> documenttype;

	@ReadValue
	@WriteValue
	private List<ObjectId> workPackage_id;

	@ReadValue
	@WriteValue
	private OperationInfo creationInfo;

	@ReadValue
	@WriteValue
	private String editorName;

	private String status;

	@ReadValue
	@WriteValue
	private String phase;
	@ReadValue
	@WriteValue
	private String security;

	private String owner;
	@ReadValue
	@WriteValue
	private String plmtype;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@ReadValue("status")
	public String getStatusText() {
		if (DocuStatus.STATUS_APPROVING_ID.equals(status))
			return DocuStatus.STATUS_APPROVING_TEXT;
		else if (DocuStatus.STATUS_DEPOSED_ID.endsWith(status))
			return DocuStatus.STATUS_DEPOSED_TEXT;
		else if (DocuStatus.STATUS_RELEASED_ID.endsWith(status))
			return DocuStatus.STATUS_RELEASED_TEXT;
		else
			return DocuStatus.STATUS_WORKING_TEXT;

	}

	@ReadValue("owner")
	private User getOwner() {
		return Optional.ofNullable(owner).map(id -> ServicesLoader.get(UserService.class).get(id, domain)).orElse(null);
	}

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
		return desc + " [" + documentnumber + "]";
	}

	public ObjectId get_id() {
		return _id;
	}

	public DocuDescriptor setFolder_id(ObjectId folder_id) {
		this.folder_id = folder_id;
		return this;
	}

	public DocuDescriptor setCreationInfo(OperationInfo creationInfo) {
		this.creationInfo = creationInfo;
		return this;
	}

	public DocuDescriptor addWorkPackageId(ObjectId workPackage_id) {
		if (this.workPackage_id == null) {
			this.workPackage_id = new ArrayList<ObjectId>();
		}
		this.workPackage_id.add(workPackage_id);
		return this;
	}

	public DocuDescriptor setDocumentType(List<String> documenttype) {
		this.documenttype = documenttype;
		return this;
	}

	public DocuDescriptor setDocuFiles(List<RemoteFile> docuFiles) {
		this.docuFiles = docuFiles;
		return this;
	}

	public DocuDescriptor setEditorName(String editorName) {
		this.editorName = editorName;
		return this;
	}
}
