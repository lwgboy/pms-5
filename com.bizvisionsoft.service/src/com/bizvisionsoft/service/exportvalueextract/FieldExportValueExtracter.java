package com.bizvisionsoft.service.exportvalueextract;

import java.util.Arrays;
import java.util.Locale;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.exporter.ExportableForm;
import com.bizvisionsoft.service.exporter.ExportableFormField;
import com.bizvisionsoft.service.exporter.Form2DocxExporter;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.RemoteFile;

public class FieldExportValueExtracter {

	private static final Logger logger = LoggerFactory.getLogger(FieldExportValueExtracter.class);

	private ExportableForm form;
	private Document data;

	private Locale locale;

	private String domain;

	/**
	 * STATIC_FIELD_LOGO_FILENAME,logo的文件名
	 * 
	 * STATIC_FIELD_LOGO_INPUTSTREAM,logo的inputStream
	 * 
	 * FIELD_DOC_STD_NUM, 文档标准号
	 * 
	 * FIELD_DOC_NAME,文件名称
	 * 
	 * FIELD_DOC_NUM, 文件编号
	 * 
	 * STATIC_FIELD_COMPANY_NAME，公司名称
	 */
	public final String[] SYSTEM_RESERVED_FIELDS = new String[] { Form2DocxExporter.STATIC_FIELD_LOGO_FILENAME,
			Form2DocxExporter.STATIC_FIELD_LOGO_INPUTSTREAM, Form2DocxExporter.FIELD_DOC_STD_NUM, Form2DocxExporter.FIELD_DOC_NAME,
			Form2DocxExporter.FIELD_DOC_NUM, Form2DocxExporter.STATIC_FIELD_COMPANY_NAME };

	public FieldExportValueExtracter(ExportableForm form, Document data, String domain, Locale locale) {
		this.form = form;
		this.data = data;
		this.domain = domain;
		this.locale = locale;
	}

	public Object getExportValue(String fieldName) {
		for (int i = 0; i < SYSTEM_RESERVED_FIELDS.length; i++) {
			if (SYSTEM_RESERVED_FIELDS[i].equals(fieldName)) {
				return getSystemFieldValue(fieldName);
			}
		}

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
			ex = new SpinnerFieldValueExtracter();
		} else if (ExportableFormField.TYPE_TABLE.equals(f.type)) {
			ex = new TableFieldValueExtracter();
		} else if (ExportableFormField.TYPE_MULTI_SELECTION.equals(f.type)) {
			ex = new TableFieldValueExtracter();
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

	private Object getSystemFieldValue(String fieldName) {
		Organization root = ServicesLoader.get(OrganizationService.class).getDomainRoot(domain);
		RemoteFile rf = root.getLogo();

		if (Form2DocxExporter.STATIC_FIELD_COMPANY_NAME.equals(fieldName)) {
			return root.getFullName();
		}
		if (Form2DocxExporter.STATIC_FIELD_LOGO_INPUTSTREAM.equals(fieldName)) {
			try {
				if (rf != null) {
					return rf.getInputStreamFromServer();
				}
			} catch (Exception e) {
				logger.error("读取公司logo文件失败，请为组织设置logo", e);
			}
			return null;
		}
		if (Form2DocxExporter.STATIC_FIELD_LOGO_FILENAME.equals(fieldName)) {
			if (rf != null)
				return rf.name;
			return null;
		}

		if (Arrays.asList(Form2DocxExporter.FIELD_DOC_NUM, Form2DocxExporter.FIELD_DOC_STD_NUM, Form2DocxExporter.FIELD_DOC_NAME)
				.contains(fieldName)) {
			return data.get(fieldName);
		}

		return null;
	}

}
