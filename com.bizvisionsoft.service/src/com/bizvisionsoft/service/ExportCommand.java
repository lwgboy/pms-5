package com.bizvisionsoft.service;

import java.util.Locale;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableForm;

public class ExportCommand {

	private Document data;

	private ExportableForm form;

	private String fileName;

	private Document properties;

	private String language;

	private String country;

	public ExportCommand setLocale(Locale locale) {
		this.country = locale.getCountry();
		this.language = locale.getLanguage();
		return this;
	}

	public Locale getLocale() {
		return new Locale(language, country);
	}
	
	public Document getProperties() {
		return properties;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public Document getData() {
		return data;
	}
	
	public ExportableForm getForm() {
		return form;
	}
	
	public ExportCommand setFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}
	
	public ExportCommand setForm(ExportableForm form) {
		this.form = form;
		return this;
	}
	
	public ExportCommand setProperties(Document properties) {
		this.properties = properties;
		return this;
	}
	
	public ExportCommand setData(Document data) {
		this.data = data;
		return this;
	}

}
