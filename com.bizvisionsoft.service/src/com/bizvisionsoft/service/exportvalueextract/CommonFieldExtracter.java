package com.bizvisionsoft.service.exportvalueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class CommonFieldExtracter {

	final protected Document data;

	final protected ExportableFormField fieldConfig;

	public CommonFieldExtracter(Document data, ExportableFormField f) {
		this.data = data;
		this.fieldConfig = f;
	}

	public Object getExportValue() {
		return null;
	}

}
