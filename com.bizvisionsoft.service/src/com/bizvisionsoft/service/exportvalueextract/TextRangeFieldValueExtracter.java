package com.bizvisionsoft.service.exportvalueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class TextRangeFieldValueExtracter extends CommonFieldExtracter {

	public TextRangeFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
