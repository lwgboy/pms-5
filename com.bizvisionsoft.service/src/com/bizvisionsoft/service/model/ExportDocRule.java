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
	public static final String TYPE_FIELD_MAPPING = "ӳ��";

	@Exclude
	public static final String TYPE_FIELD_NUMBER = "��ֵ";

	@Exclude
	public static final String TYPE_FIELD_STRING = "�ı�";

	@Exclude
	public static final String TYPE_FIELD_BOOLEAN = "����";

	@Exclude
	public static final String TYPE_FIELD_ARRAY = "����";

	@Exclude
	public static final String TYPE_FIELD_TABLE = "���";

	@Exclude
	public static final String[] TYPE_FIELD_ALL = new String[] { TYPE_FIELD_MAPPING, TYPE_FIELD_NUMBER, TYPE_FIELD_STRING,
			TYPE_FIELD_BOOLEAN, TYPE_FIELD_ARRAY, TYPE_FIELD_TABLE };

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
		boolean nullField = false;
		List<Result> results = new ArrayList<Result>();

		for (Document doc : getFieldMap()) {
			String field = doc.getString("field");
			Object type = doc.get("type");
			Object value = doc.get("value");
			if (Check.isAssigned(field)) {
				if (exportDocRuleFields.contains(field)) {// �жϵ�ǰ�ֶ��Ƿ��ظ�
					Result result = Result.error((String) doc.getOrDefault("fieldName", doc.getString("field")));
					result.setResultDate(new BasicDBObject("editorId", editorId).append("type", "errorSameField"));
					results.add(result);
					continue;
				}
				if (type == null || value == null) {// �ж��ֶ����ͺ�ֵ�Ƿ�Ϊnull
					Result result = Result.error((String) doc.getOrDefault("fieldName", doc.getString("field")));
					result.setResultDate(new BasicDBObject("editorId", editorId).append("type", "errorCompleteField"));
					results.add(result);
					continue;
				}
				if (TYPE_FIELD_MAPPING.equals(type) && !formDFieldMap.containsKey(value)) {// �жϡ�ӳ�䡱�����ֶ���ѡ��ֵ�Ƿ��ڱ�������ֶ��б���
					Result result = Result.error((String) doc.getOrDefault("fieldName", doc.getString("field")));
					result.setResultDate(new BasicDBObject("editorId", editorId).append("type", "errorField"));
					results.add(result);
				}
				exportDocRuleFields.add(field);
			} else
				nullField = true;
		}

		if (exportableForm != null)
			checkExportableFields(exportDocRuleFields, exportableForm.fields, results);

		if (nullField) {
			Result result = Result.error("�ֶ������д���δȷ���ֶ������ֶ�.");
			result.setResultDate(new BasicDBObject("editorId", editorId).append("type", "nullField"));
			results.add(result);
		}
		return results;
	}

	private void checkExportableFields(List<String> fields, List<ExportableFormField> exportableFields, List<Result> results) {
		if (Check.isAssigned(exportableFields))
			for (ExportableFormField field : exportableFields) {
				String type = field.type;
				if (ExportableFormField.TYPE_PAGE.equals(type) || ExportableFormField.TYPE_INLINE.equals(type)) {
					checkExportableFields(fields, field.formFields, results);
				} else if (!ExportableFormField.TYPE_BANNER.equals(type)) {// ȥ��������ֶ�
					String name = field.name;
					if (!fields.contains(name)) {
						String text = field.text;
						Result r = Result.error((text != null ? text : "") + "[" + name + "]");
						r.setResultDate(new BasicDBObject("editorId", editorId).append("type", "errorExportableField"));
						results.add(r);
					}

				}
			}
	}

}
