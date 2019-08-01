package com.bizvisionsoft.service.exportvalueextract;

import java.text.SimpleDateFormat;
import java.util.List;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class DateTimeRangeFieldValueExtracter extends CommonFieldExtracter {

	@Override
	public Object getExportValue() {
		Object value = super.getExportValue();
		if (value instanceof List<?>) {
			List<?> list = (List<?>) value;
			if (list.isEmpty())
				return "";

			SimpleDateFormat sdf;
			if (ExportableFormField.DATE_TYPE_YEAR.equals(fieldConfig.dateType)) {
				sdf = new SimpleDateFormat(ExportableFormField.FORMAT_YEAR);
			} else if (ExportableFormField.DATE_TYPE_MONTH.equals(fieldConfig.dateType)) {
				sdf = new SimpleDateFormat(ExportableFormField.FORMAT_MONTH);
			} else if (ExportableFormField.DATE_TYPE_TIME.equals(fieldConfig.dateType)) {
				sdf = new SimpleDateFormat(ExportableFormField.FORMAT_TIME);
			} else if (ExportableFormField.DATE_TYPE_DATETIME.equals(fieldConfig.dateType)) {
				sdf = new SimpleDateFormat(ExportableFormField.FORMAT_DATETIME);
			} else {
				sdf = new SimpleDateFormat(ExportableFormField.FORMAT_DATE);
			}

			String result = sdf.format(list.get(0));
			if (list.size() > 1) {
				result = result + " - " + sdf.format(list.get(1));
			}
			return result;
		} else if (value == null) {
			return "";
		} else {
			logger.warn("导出日期时间范围字段，值得类型不是Date类型，已强制转换为字符串。" + fieldConfig.name);
			return value.toString();
		}

	}
}