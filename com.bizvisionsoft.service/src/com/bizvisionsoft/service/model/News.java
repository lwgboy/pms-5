package com.bizvisionsoft.service.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.bizvisionsoft.service.tools.Util;

public class News {

	public Date date;

	public String title;

	public String content;

	public String summary;

	public String getSummary() {
		return new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(date) + ", " + (summary == null ? content : summary);
	}

	public News setDate(Date date) {
		this.date = date;
		return this;
	}

	public News setTitle(String title) {
		this.title = title;
		return this;
	}

	public News setContent(String content) {
		this.content = content;
		return this;
	}

	public News setSummary(String summary) {
		this.summary = summary;
		return this;
	}

}
