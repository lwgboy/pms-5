package com.bizvisionsoft.service.exportvalueextract;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.exporter.ExportableForm;
import com.bizvisionsoft.service.exporter.ExportableFormField;

public class FieldExportValueExtracter {

	private static final Logger logger = LoggerFactory.getLogger(FieldExportValueExtracter.class);

	private ExportableForm form;
	private Document data;

	public FieldExportValueExtracter(ExportableForm form, Document data) {
		this.form = form;
		this.data = data;
	}

	public Object getExportValue(String fieldName) {
		ExportableFormField f = form.findField(fieldName);
		if (f == null) {
			logger.warn("文件导出配置中缺少字段:" + fieldName);
			return null;
		}
		CommonFieldExtracter ex;
		if (ExportableFormField.TYPE_BANNER.equals(f.type)) {
			ex = new BannerFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_CHECK.equals(f.type)) {
			ex = new CheckFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_COMBO.equals(f.type)) {
			ex = new ComboFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_DATETIME.equals(f.type)) {
			ex = new DateTimeFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_DATETIME_RANGE.equals(f.type)) {
			ex = new DateTimeRangeFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_FILE.equals(f.type)) {
			ex = new FileFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_IMAGE_FILE.equals(f.type)) {
			ex = new ImageFileFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_LABEL.equals(f.type)) {
			ex = new LabelFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_LABEL_MULTILINE.equals(f.type)) {
			ex = new LabelMultilineFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_MULTI_CHECK.equals(f.type)) {
			ex = new MultiCheckFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_MULTI_FILE.equals(f.type)) {
			ex = new MultiFileFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_PAGE_HTML.equals(f.type)) {
			ex = new PageHtmlFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_PAGE_NOTE.equals(f.type)) {
			ex = new PageNoteFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_RADIO.equals(f.type)) {
			ex = new RadioFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_SELECTION.equals(f.type)) {
			ex = new SelectionFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_SPINNER.equals(f.type)) {
			ex = new SpinnerFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_TABLE.equals(f.type)) {
			ex = new TableFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_MULTI_SELECTION.equals(f.type)) {
			ex = new MultiSelectionFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_TEXT.equals(f.type)) {
			ex = new TextFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_TEXT_HTML.equals(f.type)) {
			ex = new HtmlFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_TEXT_MULTILINE.equals(f.type)) {
			ex = new TextMultilineFieldValueExtracter(data, f);
		} else if (ExportableFormField.TYPE_TEXT_RANGE.equals(f.type)) {
			ex = new TextRangeFieldValueExtracter(data, f);
		} else {
			ex = new CommonFieldExtracter(data, f);
		}
		return ex.getExportValue();
	}

}
