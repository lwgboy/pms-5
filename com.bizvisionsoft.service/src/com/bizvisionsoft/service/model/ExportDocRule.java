package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.exporter.ExportableForm;
import com.bizvisionsoft.service.exporter.ExportableFormField;
import com.bizvisionsoft.service.tools.Check;
import com.mongodb.BasicDBObject;

@PersistenceCollection("exportDocRule")
public class ExportDocRule {

	@Exclude
	public static final String TYPE_FIELD_MAPPING = "映射";

	@Exclude
	public static final String TYPE_FIELD_NUMBER = "数值";

	@Exclude
	public static final String TYPE_FIELD_STRING = "文本";

	@Exclude
	public static final String TYPE_FIELD_BOOLEAN = "布尔";

	@Exclude
	public static final String TYPE_FIELD_ARRAY = "数组";

	@Exclude
	public static final String TYPE_FIELD_TABLE = "表格";

	@Exclude
	public static final String[] TYPE_FIELD_ALL = new String[] { TYPE_FIELD_MAPPING, TYPE_FIELD_NUMBER, TYPE_FIELD_STRING,
			TYPE_FIELD_BOOLEAN, TYPE_FIELD_ARRAY, TYPE_FIELD_TABLE };

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "导出文档规则";

	@ImageURL("editorId")
	@Exclude
	public static final String icon = "/img/exportdocrule_c.svg";

	public String domain;

	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	@WriteValue
	@ReadValue
	@Label
	private String editorId;

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

	private ObjectId formDef_id;

	public ObjectId getFormDef_id() {
		return formDef_id;
	}

	public void setFormDef_id(ObjectId formDef_id) {
		this.formDef_id = formDef_id;
	}

	public FormDef getFormDef() {
		return formDef;
	}

	public void setFormDef(FormDef formDef) {
		this.formDef = formDef;
		this.formDef_id = formDef.get_id();
	}

	public String getEditorName() {
		return editorId;
	}

	@ReadValue
	@WriteValue
	private ExportableForm exportableForm;

	public void setExportableForm(ExportableForm exportableForm) {
		this.exportableForm = exportableForm;
	}

	public ExportableForm getExportableForm() {
		return exportableForm;
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

	public List<Result> check(Map<String, String> formDFieldMap) {
		return check(formDFieldMap, new ArrayList<String>());
	}

	public List<Result> check(Map<String, String> formDFieldMap, List<String> exportDocRuleFields) {
		List<Result> results = new ArrayList<Result>();

		for (Document doc : getFieldMap()) {
			String field = doc.getString("field");
			Object type = doc.get("type");
			Object value = doc.get("value");
			if (TYPE_FIELD_MAPPING.equals(type) && !formDFieldMap.containsKey(value)) {// 判断“映射”类型字段所选的值是否在表单定义的字段列表中
				Result result = Result.error("文档导出规则错误，表单定义不存在字段“" + (String) doc.getOrDefault("fieldName", doc.getString("field")) + "”。");
				result.setResultDate(new BasicDBObject("editorId", editorId).append("type", "errorField"));
				result.code = Result.CODE_CHECK_EXPORTDOCRULE_FORM;
				results.add(result);
			}
			exportDocRuleFields.add(field);
		}

		if (exportableForm != null)
			checkExportableFields(exportDocRuleFields, exportableForm.fields, results);
		return results;
	}

	private void checkExportableFields(List<String> fields, List<ExportableFormField> exportableFields, List<Result> results) {
		if (Check.isAssigned(exportableFields))
			for (ExportableFormField field : exportableFields) {
				String type = field.type;
				if (ExportableFormField.TYPE_PAGE.equals(type) || ExportableFormField.TYPE_INLINE.equals(type)) {
					checkExportableFields(fields, field.formFields, results);
				} else if (!ExportableFormField.TYPE_BANNER.equals(type)) {// 去除掉横幅字段
					String name = field.name;
					if (!fields.contains(name)) {
						String text = field.text;
						// TODO
						Result r = Result.error("“" + editorId + "”导出文件的字段“" + (text != null ? text : "") + "[" + name + "]”不存在。");
						r.code = Result.CODE_CHECK_EXPORTDOCRULE_EXPORTABLE;
						r.setResultDate(new BasicDBObject("editorId", editorId).append("type", "errorExportableField"));
						results.add(r);
					}

				}
			}
	}

}
