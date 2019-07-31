package com.bizvisionsoft.service.valueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class TextFieldValueExtracter extends CommonFieldExtracter {

	public TextFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
