package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

/**
 * @author hua
 */
@PersistenceCollection("revenueRealizeItem")
public class RevenueRealizeItem {
	
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private ObjectId scope_id;
	
	/**
	 * 201812
	 */
	@ReadValue
	@WriteValue
	private String id;

	@ReadValue
	@WriteValue
	private String subject;

	/**
	 * ½ð¶î
	 */
	@ReadValue
	@WriteValue
	private Double amount;
	
	public ObjectId getScope_id() {
		return scope_id;
	}

	public RevenueRealizeItem setScope_id(ObjectId scope_id) {
		this.scope_id = scope_id;
		return this;

	}

	public String getSubject() {
		return subject;
	}

	public RevenueRealizeItem setSubject(String subject) {
		this.subject = subject;
		return this;

	}

	public Double getAmount() {
		return amount;
	}

	public RevenueRealizeItem setAmount(Double amount) {
		this.amount = amount;
		return this;
	}
	
	public String getId() {
		return id;
	}
	
	public RevenueRealizeItem setId(String id) {
		this.id = id;
		return this;
	}

	
}
