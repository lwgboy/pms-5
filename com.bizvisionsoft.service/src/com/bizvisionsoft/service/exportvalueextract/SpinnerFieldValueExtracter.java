package com.bizvisionsoft.service.exportvalueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class SpinnerFieldValueExtracter extends CommonFieldExtracter {

	public SpinnerFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
