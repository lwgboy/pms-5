package com.bizvisionsoft.service.exportvalueextract;

import java.util.Locale;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.exporter.ExportableForm;
import com.bizvisionsoft.service.exporter.ExportableFormField;

public class FieldExportValueExtracter {

	private static final Logger logger = LoggerFactory.getLogger(FieldExportValueExtracter.class);

	private ExportableForm form;
	private Document data;

	private Locale locale;

	private String domain;

	public FieldExportValueExtracter(ExportableForm form, Document data,String domain,Locale locale) {
		this.form = form;
		this.data = data;
		this.domain = domain;
		this.locale = locale;
	}

	public Object getExportValue(String fieldName) {
		ExportableFormField f = form.findField(fieldName);
		if (f == null) {
			logger.warn("文件导出配置中缺少字段:" + fieldName);
			return null;
		}
		CommonFieldExtracter ex;
		if (ExportableFormField.TYPE_BANNER.equals(f.type)) {
			ex = new BannerFieldValueExtracter();
		} else if (ExportableFormField.TYPE_CHECK.equals(f.type)) {
			ex = new CommonFieldExtracter();
		} else if (ExportableFormField.TYPE_COMBO.equals(f.type)) {
			ex = new ComboFieldValueExtracter();
		} else if (ExportableFormField.TYPE_DATETIME.equals(f.type)) {
			ex = new DateTimeFieldValueExtracter();
		} else if (ExportableFormField.TYPE_DATETIME_RANGE.equals(f.type)) {
			ex = new DateTimeRangeFieldValueExtracter();
		} else if (ExportableFormField.TYPE_FILE.equals(f.type)) {
			ex = new FileFieldValueExtracter();
		} else if (ExportableFormField.TYPE_IMAGE_FILE.equals(f.type)) {
			ex = new ImageFileFieldValueExtracter();
		} else if (ExportableFormField.TYPE_LABEL.equals(f.type)) {
			ex = new CommonFieldExtracter();
		} else if (ExportableFormField.TYPE_LABEL_MULTILINE.equals(f.type)) {
			ex = new CommonFieldExtracter();
		} else if (ExportableFormField.TYPE_MULTI_CHECK.equals(f.type)) {
			ex = new CheckFieldValueExtracter();
		} else if (ExportableFormField.TYPE_MULTI_FILE.equals(f.type)) {
			ex = new MultiFileFieldValueExtracter();
		} else if (ExportableFormField.TYPE_PAGE_HTML.equals(f.type)) {
			ex = new CommonFieldExtracter();
		} else if (ExportableFormField.TYPE_PAGE_NOTE.equals(f.type)) {
			ex = new CommonFieldExtracter();
		} else if (ExportableFormField.TYPE_RADIO.equals(f.type)) {
			ex = new CheckFieldValueExtracter();
		} else if (ExportableFormField.TYPE_SELECTION.equals(f.type)) {
			ex = new SelectionFieldValueExtracter();
		} else if (ExportableFormField.TYPE_SPINNER.equals(f.type)) {
			ex = new CommonFieldExtracter();
		} else if (ExportableFormField.TYPE_TABLE.equals(f.type)) {
			ex = new TableFieldValueExtracter();
		} else if (ExportableFormField.TYPE_MULTI_SELECTION.equals(f.type)) {
			ex = new MultiSelectionFieldValueExtracter();
		} else if (ExportableFormField.TYPE_TEXT.equals(f.type)) {
			ex = new CommonFieldExtracter();
		} else if (ExportableFormField.TYPE_TEXT_HTML.equals(f.type)) {
			ex = new CommonFieldExtracter();
		} else if (ExportableFormField.TYPE_TEXT_MULTILINE.equals(f.type)) {
			ex = new CommonFieldExtracter();
		} else if (ExportableFormField.TYPE_TEXT_RANGE.equals(f.type)) {
			ex = new TextRangeFieldValueExtracter();
		} else {
			ex = new CommonFieldExtracter();
		}
		ex.setData(data).setFieldConfig(f).setLocale(locale).setDomain(domain);
		return ex.getExportValue();
	}

}
