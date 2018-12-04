package com.bizvisionsoft.service;

import java.util.List;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

public class ValueRuleSegment {

	@ReadValue
	@WriteValue
	public String type;

	@ReadValue
	@WriteValue
	public String value;

	@ReadValue
	@WriteValue
	public String name;

	@ReadValue
	@WriteValue
	public String function;

	@ReadValue
	@WriteValue
	public String collection;

	@ReadValue
	@WriteValue
	public List<String> params;

	@ReadValue
	@WriteValue
	public String script;

	@ReadValue
	@WriteValue
	public String query;

	@ReadValue
	@WriteValue
	public String returnField;

	@ReadValue
	@WriteValue
	public String format;

	@ReadValue
	@WriteValue
	public Integer length;

	@ReadValue
	@WriteValue
	public String supplymentDir;

	@ReadValue
	@WriteValue
	public String supplymentPlaceholder;

	@ReadValue
	@WriteValue
	public String caser;
	
	@Label
	public String label() {
		return type;
	}

	@Exclude
	@ReadValue(ReadValue.TYPE)
	public static final String _type = "÷µπÊ‘Ú∂Œ";
}
