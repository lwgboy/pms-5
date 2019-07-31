package com.bizvisionsoft.service.valueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class TextMultilineFieldValueExtracter extends CommonFieldExtracter {

	public TextMultilineFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
