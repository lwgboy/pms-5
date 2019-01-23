package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.tools.Formatter;

public class ProblemActionInfo {

	@ReadValue
	@WriteValue
	public ObjectId _id;

	@ReadValue("id")
	private String getId() {
		return _id == null ? null : _id.toHexString();
	}

	@WriteValue("id")
	private ProblemActionInfo setId(String id) {
		this._id = id == null ? null : new ObjectId(id);
		return this;
	}

	@ReadValue
	@WriteValue
	public ObjectId parent_id;

	@ReadValue("parent")
	private String readParent() {
		return parent_id == null ? null : parent_id.toHexString();
	}

	@ReadValue
	@WriteValue
	private ObjectId problem_id;

	@WriteValue
	@ReadValue
	public int index;

	@ReadValue
	@WriteValue
	@Label(Label.NAME_LABEL)
	public String text;

	@ReadValue
	public Date start_date;

	@ReadValue
	@WriteValue
	public Date end_date;

	@ReadValue
	@WriteValue
	public String chargerInfo;

	@ReadValue
	@WriteValue
	public String stage;

	@ReadValue
	@WriteValue
	public String type;

	@ReadValue
	@WriteValue
	private boolean finished;

	private Document verification;

	@ReadValue("verifyInfo")
	private String readVerifyInfo() {
		if (verification == null)
			return "";
		String title = verification.getString("title");
		if(!"“——È÷§".equals(title)) {
			return "";
		}
		String on = Formatter.getString(verification.getDate("date"));
		String by = ((Document)verification.get("user_meta")).getString("name");
		return by+" "+on;
	}

}
