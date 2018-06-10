package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("salesItem")
public class SalesItem {

	@ReadValue
	@WriteValue
	private ObjectId _id;
	
	@ReadValue
	@WriteValue
	private ObjectId project_id;

	@ReadValue
	@WriteValue
	private String productId;

	@ReadValue
	@WriteValue
	private double profit;

	@ReadValue
	@WriteValue
	private String period;

	public SalesItem setPeriod(String period) {
		this.period = period;
		return this;
	}

	public SalesItem setProductId(String productId) {
		this.productId = productId;
		return this;
	}

	public SalesItem setProfit(double profit) {
		this.profit = profit;
		return this;
	}
	
	public SalesItem setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}
}
