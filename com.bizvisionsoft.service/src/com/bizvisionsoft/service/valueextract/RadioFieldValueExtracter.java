package com.bizvisionsoft.service.valueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class RadioFieldValueExtracter extends CommonFieldExtracter {

	public RadioFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
