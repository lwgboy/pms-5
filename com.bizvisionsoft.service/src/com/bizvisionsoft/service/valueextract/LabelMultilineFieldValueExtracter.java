package com.bizvisionsoft.service.valueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class LabelMultilineFieldValueExtracter extends CommonFieldExtracter {

	public LabelMultilineFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
