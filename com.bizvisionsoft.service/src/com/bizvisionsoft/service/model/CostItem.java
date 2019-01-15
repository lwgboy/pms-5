package com.bizvisionsoft.service.model;

import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

public class CostItem {
	
	@ReadValue
	@WriteValue
	public double drAmount;
	
	@ReadValue
	@WriteValue
	public double crAmount;
	
	@ReadValue
	@WriteValue
	public double summary;

}
