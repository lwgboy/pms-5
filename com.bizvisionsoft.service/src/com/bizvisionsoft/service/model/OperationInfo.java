package com.bizvisionsoft.service.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.mongocodex.codec.JsonExternalizable;
import com.bizvisionsoft.service.tools.Formatter;

public class OperationInfo implements JsonExternalizable{

	public String userName;

	public String userId;
	
	public String consignerName;
	
	public String consignerId;

	public Date date;

	@Override
	@Label
	public String toString() {
		return userName + " " + new SimpleDateFormat(Formatter.DATE_FORMAT_DATETIME).format(date);
	}

}
