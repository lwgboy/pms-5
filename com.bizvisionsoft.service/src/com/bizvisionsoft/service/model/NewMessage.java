package com.bizvisionsoft.service.model;

import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

public class NewMessage {
	
	@ReadValue
	@WriteValue
	public String subject;

	@ReadValue
	@WriteValue
	public String content;

	@ReadValue
	@WriteValue
	public User receiver;
	
	@ReadValue
	@WriteValue
	public User sender;

}
