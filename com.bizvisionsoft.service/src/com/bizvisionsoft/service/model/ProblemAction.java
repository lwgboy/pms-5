package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("problemAction")
public class ProblemAction {

	@ReadValue
	@WriteValue
	public ObjectId _id;

	@ReadValue
	@WriteValue
	private ObjectId problem_id;

	@ReadValue
	@WriteValue
	private String action;

	@ReadValue
	@WriteValue
	private String actionType;

	@ReadValue
	@WriteValue
	private List<RemoteFile> attachments;

	@ReadValue
	@WriteValue
	private String charger;

	@SetValue
	private UserMeta charger_meta;

	@ReadValue
	@WriteValue
	private String detail;

	@ReadValue
	@WriteValue
	private Boolean finish;

	@ReadValue
	@WriteValue
	private int index;

	@ReadValue
	@WriteValue
	private String objective;

	@ReadValue
	@WriteValue
	private Date planFinish;

	@ReadValue
	@WriteValue
	private Date planStart;

	@ReadValue
	@WriteValue
	private String stage;

	@ReadValue
	@WriteValue
	private List<RemoteFile> verificationAttachment;

	@ReadValue
	@WriteValue
	private String verificationComment;

	@ReadValue
	@WriteValue
	private Date verificationDate;

	@ReadValue
	@WriteValue
	private String verificationTitle;

	@ReadValue
	@WriteValue
	private String verificationUser;

	@SetValue
	private UserMeta verificationUser_meta;

}
