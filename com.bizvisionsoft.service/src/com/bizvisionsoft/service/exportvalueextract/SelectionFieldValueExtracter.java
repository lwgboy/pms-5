package com.bizvisionsoft.service.exportvalueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class SelectionFieldValueExtracter extends CommonFieldExtracter {

	public SelectionFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
