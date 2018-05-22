package com.bizvisionsoft.service.model;

import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("riskUrInd")
public class RiskUrgencyInd {

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String text;

	@ReadValue
	@WriteValue
	private int max;

	@ReadValue
	@WriteValue
	private int min;

	@WriteValue("range")
	private void setRange(Double[] input) {
		min = Optional.ofNullable(input[0]).map(d -> d.intValue()).orElse(0);
		max = Optional.ofNullable(input[1]).map(d -> d.intValue()).orElse(0);
	}

	@ReadValue("range")
	private Object getRange() {
		return new Number[] { min, max };
	}

}
