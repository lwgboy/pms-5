package com.bizvisionsoft.service.exportvalueextract;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class DateTimeFieldValueExtracter extends CommonFieldExtracter {

	@Override
	public Object getExportValue() {
		Object value = super.getExportValue();
		if (value instanceof Date) {
			if (ExportableFormField.DATE_TYPE_YEAR.equals(fieldConfig.dateType)) {
				return new SimpleDateFormat(ExportableFormField.FORMAT_YEAR).format((Date) value);
			} else if (ExportableFormField.DATE_TYPE_MONTH.equals(fieldConfig.dateType)) {
				return new SimpleDateFormat(ExportableFormField.FORMAT_MONTH).format((Date) value);
			} else if (ExportableFormField.DATE_TYPE_TIME.equals(fieldConfig.dateType)) {
				return new SimpleDateFormat(ExportableFormField.FORMAT_TIME).format((Date) value);
			} else if (ExportableFormField.DATE_TYPE_DATETIME.equals(fieldConfig.dateType)) {
				return new SimpleDateFormat(ExportableFormField.FORMAT_DATETIME).format((Date) value);
			} else {
				return new SimpleDateFormat(ExportableFormField.FORMAT_DATE).format((Date) value);
			}
		} else if (value == null) {
			return "";
		} else {
			logger.warn("��������ʱ���ֶΣ�ֵ�����Ͳ���Date���ͣ���ǿ��ת��Ϊ�ַ�����" + fieldConfig.name);
			return value.toString();
		}
	}

}
