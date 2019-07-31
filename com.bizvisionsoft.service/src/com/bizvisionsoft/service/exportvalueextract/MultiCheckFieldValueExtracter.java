package com.bizvisionsoft.service.exportvalueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class MultiCheckFieldValueExtracter extends CommonFieldExtracter {

	public MultiCheckFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
