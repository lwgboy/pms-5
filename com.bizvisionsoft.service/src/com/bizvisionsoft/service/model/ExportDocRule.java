package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("exportDocRule")
public class ExportDocRule {

	public String domain;

	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	private ObjectId formDef_id;

	@ReadValue
	@WriteValue
	private String editorId;

	public String getEditorId() {
		return editorId;
	}

	@WriteValue("editorName")
	public void setEditorName(String editorName) {
		this.editorId = editorName;
	}

	@ReadValue("editorName")
	public String getEditorName() {
		return editorId;
	}

	private Document exportableForm;

	public void setExportableForm(Document exportableForm) {
		this.exportableForm = exportableForm;
	}

	private List<Document> fieldMap;

	public List<Document> getFieldMap() {
		return fieldMap;
	}

	public void setFieldMap(List<Document> fieldMap) {
		this.fieldMap = fieldMap;
	}

	@ReadValue
	@WriteValue
	private String postProc;

	public void setFormDef_id(ObjectId formDef_id) {
		this.formDef_id = formDef_id;
	}

	@Structure("list")
	public List<Document> listSubAccountItems() {
		return fieldMap != null ? fieldMap : new ArrayList<Document>();
	}

	@Structure("count")
	public long countSubAccountItems() {
		return fieldMap != null ? fieldMap.size() : 0;
	}
}
