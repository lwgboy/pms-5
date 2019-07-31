package com.bizvisionsoft.service.valueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class PageNoteFieldValueExtracter extends CommonFieldExtracter {

	public PageNoteFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
