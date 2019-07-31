package com.bizvisionsoft.service.valueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class FileFieldValueExtracter extends CommonFieldExtracter {

	public FileFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
