package com.bizvisionsoft.service.exportvalueextract;

import java.util.List;

import com.bizvisionsoft.service.tools.Formatter;

public class TextRangeFieldValueExtracter extends CommonFieldExtracter {

	@Override
	public Object getExportValue() {
		Object value = super.getExportValue();
		if (value == null)
			return "";
		Object from;
		Object to;
		if (value instanceof Object[] && ((Object[]) value).length > 1) {
			from = ((Object[]) value)[0];
			to = ((Object[]) value)[1];
		} else if (value instanceof List<?> && ((List<?>) value).size() > 1) {
			from = ((List<?>) value).get(0);
			to = ((List<?>) value).get(1);
		} else {
			return warningAndReturn(value);
		}
		return getText(from) + " - " + getText(to);
	}

	private String getText(Object value) {
		if (value == null)
			return "";
		String format = fieldConfig.format;
		if (format != null && !format.isEmpty())
			return Formatter.getString(value, format, locale);
		else
			return value.toString();
	}

	private String warningAndReturn(Object value) {
		logger.warn("导出范围字段，值得类型不是List 或 数组类型，已强制转换为字符串。" + fieldConfig.name);
		return value.toString();
	}

}
