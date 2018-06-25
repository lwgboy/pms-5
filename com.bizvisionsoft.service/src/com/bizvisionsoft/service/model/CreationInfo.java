package com.bizvisionsoft.service.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.service.tools.Util;

public class CreationInfo {

	public String userName;

	public String userId;

	public Date date;

	@Override
	@Label
	public String toString() {
		return userName + " " + new SimpleDateFormat(Util.DATE_FORMAT_DATETIME).format(date);
	}

}
