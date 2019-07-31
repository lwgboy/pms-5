package com.bizvisionsoft.service.exportvalueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class ComboFieldValueExtracter extends CommonFieldExtracter {

	public ComboFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
