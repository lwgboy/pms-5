package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("exportDocRule")
public class ExportDocRule {

	@Exclude
	public static String TYPE_FIELD_MAPPING = "映射";
	@Exclude
	public static String TYPE_FIELD_NUMBER = "数值";
	@Exclude
	public static String TYPE_FIELD_STRING = "文本";
	@Exclude
	public static String TYPE_FIELD_BOOLEAN = "布尔";
	@Exclude
	public static String TYPE_FIELD_ARRAY = "数组";
	@Exclude
	public static String TYPE_FIELD_TABLE = "表格";
	@Exclude
	public static String[] TYPE_FIELD_ALL = new String[] { TYPE_FIELD_MAPPING, TYPE_FIELD_NUMBER, TYPE_FIELD_STRING, TYPE_FIELD_BOOLEAN,
			TYPE_FIELD_ARRAY, TYPE_FIELD_TABLE };

	public String domain;

	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	@WriteValue
	@ReadValue({ "editorId", "editorTypeId" })
	@Label
	private String editorId;

	@ImageURL("editorTypeId")
	@Exclude
	public static String icon = "/img/exportdocrule_c.svg";

	public String getEditorId() {
		return editorId;
	}

	public void setEditorId(String editorId) {
		this.editorId = editorId;
	}

	@ReadValue
	@WriteValue
	@SetValue("formDef")
	private FormDef formDef;

	public FormDef getFormDef() {
		return formDef;
	}

	public void setFormDef(FormDef formDef) {
		this.formDef = formDef;
	}

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
