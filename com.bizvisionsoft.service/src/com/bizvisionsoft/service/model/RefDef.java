package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.mongodb.BasicDBObject;

@PersistenceCollection("refDef")
public class RefDef {

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "参照定义";

	@ImageURL("editorId")
	@Exclude
	public static final String icon = "/img/exportdocrule_c.svg";

	public String domain;

	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	private ObjectId formDef_id;

	public void setFormDef_id(ObjectId formDef_id) {
		this.formDef_id = formDef_id;
	}

	public ObjectId getFormDef_id() {
		return formDef_id;
	}

	@ReadValue
	@WriteValue
	private String field;

	@ReadValue
	@WriteValue
	private String col;

	@ReadValue
	@WriteValue
	private String postProc;

	@ReadValue
	@WriteValue
	private String formDefField;

	public List<Result> check(Map<String, String> formDFieldMap) {
		List<Result> results = new ArrayList<Result>();
		if (!formDFieldMap.containsKey(formDefField)) {// 判断表单字段名是否在表单定义的字段列表中
			Result result = Result.error(toString() + "无法从表单定义中获取。");
			result.setResultDate(new BasicDBObject("type", "errorRefDefField"));
			result.code = Result.CODE_CHECK_EXPORTDOCRULE_FORM;
			results.add(result);
		}

		return results;
	}

	@Override
	@Label
	public String toString() {
		return "集合：“" + col + "”，字段：“" + field + "”，表单字段：“" + formDefField + "”";
	}

}
