package com.bizvisionsoft.service.model;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("riskUrInd")
public class RiskUrgencyInd {

	@ReadValue
	@WriteValue
	private ObjectId _id;
	
	@ReadValue
	@WriteValue 
	public int index;

	@ReadValue
	@WriteValue
	public String text;

	@ReadValue
	@WriteValue
	public int max;

	@ReadValue
	@WriteValue
	public int min;

	@WriteValue("range")
	private void setRange(List<Double> input) {
		min = Optional.ofNullable(input.get(0)).map(d -> d.intValue()).orElse(0);
		max = Optional.ofNullable(input.get(1)).map(d -> d.intValue()).orElse(0);
	}

	@ReadValue("range")
	private Object getRange() {
		return new Number[] { min, max };
	}
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "临近性指标";

	@Override
	@Label
	public String toString() {
		return index+"." +text+ "（"+ min+" ~ "+max +"天）";
	}

}
