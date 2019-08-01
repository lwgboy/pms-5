package com.bizvisionsoft.service.exportvalueextract;

import java.util.Locale;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class CommonFieldExtracter {
	
	final protected Logger logger = LoggerFactory.getLogger(getClass());
	
	protected Document data;

	protected ExportableFormField fieldConfig;

	protected Locale locale;
	
	protected String domain;

	
	public CommonFieldExtracter setData(Document data) {
		this.data = data;
		return this;
	}
	
	public CommonFieldExtracter setFieldConfig(ExportableFormField fieldConfig) {
		this.fieldConfig = fieldConfig;
		return this;
	}
	
	public CommonFieldExtracter setLocale(Locale locale) {
		this.locale = locale;
		return this;
	}
	
	public CommonFieldExtracter setDomain(String domain) {
		this.domain = domain;
		return this;
	}

	public Object getExportValue() {
		return data.get(fieldConfig.name);
	}

}
