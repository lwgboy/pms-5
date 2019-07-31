package com.bizvisionsoft.service.valueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class LabelFieldValueExtracter extends CommonFieldExtracter {

	public LabelFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
