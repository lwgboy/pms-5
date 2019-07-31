package com.bizvisionsoft.service.exportvalueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class ImageFileFieldValueExtracter extends CommonFieldExtracter {

	public ImageFileFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
