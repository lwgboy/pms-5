package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("exportDocRule")
public class ExportDocRule {

	public static String TYPE_FIELD_MAPPING = "ӳ��";
	public static String TYPE_FIELD_NUMBER = "��ֵ";
	public static String TYPE_FIELD_STRING = "�ı�";
	public static String TYPE_FIELD_BOOLEAN = "����";
	public static String TYPE_FIELD_ARRAY = "����";
	public static String TYPE_FIELD_TABLE = "���";

	public String domain;

	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	@ReadValue
	@WriteValue
	private String editorId;

	@ImageURL("name")
	@Exclude
	private String icon = "/img/exportdocrule_c.svg";

	public String getEditorId() {
		return editorId;
	}

	@ReadValue
	@WriteValue
	@SetValue("formDef")
	private FormDef formDef;

	public FormDef getFormDef() {
		return formDef;
	}

	@WriteValue("editorName")
	public void setEditorName(String editorName) {
		this.editorId = editorName;
	}

	@ReadValue("editorName")
	public String getEditorName() {
		return editorId;
	}

	@ReadValue
	@WriteValue
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

	public String getPostProc() {
		return postProc;
	}

	public void setPostProc(String postProc) {
		this.postProc = postProc;
	}
}
