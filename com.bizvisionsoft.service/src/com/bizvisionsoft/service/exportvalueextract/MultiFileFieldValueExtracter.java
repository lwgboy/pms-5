package com.bizvisionsoft.service.exportvalueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class MultiFileFieldValueExtracter extends CommonFieldExtracter {

	public MultiFileFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
