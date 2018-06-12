package com.bizvisionsoft.service.model;

import java.util.Date;

import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

public class DateMark {
	
	@ReadValue
	@WriteValue
	public Date date;
	
	@ReadValue
	@WriteValue
	public String style;
	
	@ReadValue
	@WriteValue
	public String text;
	
	@ReadValue
	@WriteValue
	public String type;//"dhx_time_block"

}
