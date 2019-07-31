package com.bizvisionsoft.service.exportvalueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class TableFieldValueExtracter extends CommonFieldExtracter {

	public TableFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
