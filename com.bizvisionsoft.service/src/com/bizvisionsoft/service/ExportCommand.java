package com.bizvisionsoft.service;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableForm;

public class ExportCommand {
	
	public Document data;
	
	public ExportableForm form;

	public String fileName;

	public Document properties;
	
}
