package com.bizvisionsoft.service.exportvalueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class BannerFieldValueExtracter extends CommonFieldExtracter {

	public BannerFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

	@Override
	public Object getExportValue() {
		// TODO Auto-generated method stub
		return super.getExportValue();
	}
}
