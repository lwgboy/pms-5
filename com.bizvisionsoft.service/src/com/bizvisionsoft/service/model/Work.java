package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

public class Work {

	private ObjectId _id;

	private ObjectId parent;

	private String wbsCode;

	private String name;
	
	private String fullName;
	
	private String desc;
	
	private Date planStart;

	private Date planFinish;
	
	private Date actualStart;
	
	private Date actualFinish;

	private Integer planDuration;
	
	private Integer actualDuration;

	private Float progress;
	
	private String manageLevel;
	
	public Object get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}



}
