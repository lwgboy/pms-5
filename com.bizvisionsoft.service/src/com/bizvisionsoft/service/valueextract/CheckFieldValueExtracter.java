package com.bizvisionsoft.service.valueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class CheckFieldValueExtracter extends CommonFieldExtracter {

	public CheckFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
