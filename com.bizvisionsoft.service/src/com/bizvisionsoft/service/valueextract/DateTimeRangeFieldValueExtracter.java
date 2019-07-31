package com.bizvisionsoft.service.valueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class DateTimeRangeFieldValueExtracter extends CommonFieldExtracter {

	public DateTimeRangeFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
