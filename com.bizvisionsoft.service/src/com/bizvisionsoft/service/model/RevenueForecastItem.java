package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

/**
 * Ψһ���� project_id, yeart, halfyear,quarter,month
 * @author hua
 */
@PersistenceCollection("revenueForecastItem")
public class RevenueForecastItem {
	
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private ObjectId scope_id;
	
	/**
	 * ��,����,����,��
	 */
	@ReadValue
	@WriteValue
	private String type;

	/**
	 * 1 0 ��ʾ�ϰ��꣬1��ʾ�°���
	 * 0��1��2��3�ֱ��ʾ1���ȵ�4
	 * 0,1...11 �ֱ��ʾ1�µ�12��
	 */
	@ReadValue
	@WriteValue
	private int index;

	
	@ReadValue
	@WriteValue
	private String subject;

	/**
	 * ���
	 */
	@ReadValue
	@WriteValue
	private Double amount;
	
	public ObjectId getScope_id() {
		return scope_id;
	}

	public RevenueForecastItem setScope_id(ObjectId scope_id) {
		this.scope_id = scope_id;
		return this;

	}

	public String getType() {
		return type;
	}

	public RevenueForecastItem setType(String type) {
		this.type = type;
		return this;

	}

	public int getIndex() {
		return index;
	}

	public RevenueForecastItem setIndex(int index) {
		this.index = index;
		return this;

	}

	public String getSubject() {
		return subject;
	}

	public RevenueForecastItem setSubject(String subject) {
		this.subject = subject;
		return this;

	}

	public Double getAmount() {
		return amount;
	}

	public RevenueForecastItem setAmount(Double amount) {
		this.amount = amount;
		return this;
	}

	
}
