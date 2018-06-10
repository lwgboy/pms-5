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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((period == null) ? 0 : period.hashCode());
		result = prime * result + ((productId == null) ? 0 : productId.hashCode());
		result = prime * result + ((project_id == null) ? 0 : project_id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SalesItem other = (SalesItem) obj;
		if (period == null) {
			if (other.period != null)
				return false;
		} else if (!period.equals(other.period))
			return false;
		if (productId == null) {
			if (other.productId != null)
				return false;
		} else if (!productId.equals(other.productId))
			return false;
		if (project_id == null) {
			if (other.project_id != null)
				return false;
		} else if (!project_id.equals(other.project_id))
			return false;
		return true;
	}

	public double getProfit() {
		return profit;
	}

	public String getId() {
		return period;
	}
}
