package com.bizvisionsoft.service.exportvalueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class MultiSelectionFieldValueExtracter extends CommonFieldExtracter {

	public MultiSelectionFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
