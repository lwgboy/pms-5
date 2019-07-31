package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("refDef")
public class RefDef {

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "≤Œ’’∂®“Â";

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

}
