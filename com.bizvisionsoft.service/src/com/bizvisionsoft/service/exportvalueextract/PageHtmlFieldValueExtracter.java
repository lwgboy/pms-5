package com.bizvisionsoft.service.exportvalueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class PageHtmlFieldValueExtracter extends CommonFieldExtracter {

	public PageHtmlFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
