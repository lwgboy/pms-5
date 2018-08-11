package com.bizvisionsoft.service.model;

import java.util.Date;

import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

public class Backup {
	
	@ReadValue
	@WriteValue
	private String id;

	@ReadValue
	@WriteValue
	private Date date;

	@ReadValue
	@WriteValue
	private String notes;

	public Backup setDate(Date date) {
		this.date = date;
		return this;
	}
	
	public Backup setNotes(String notes) {
		this.notes = notes;
		return this;
	}

	public Date getDate() {
		return date;
	}
	
	public String getNotes() {
		return notes;
	}
	
	public Backup setId(String id) {
		this.id = id;
		return this;
	}
	
	public String getId() {
		return id;
	}
}
