package com.bizvisionsoft.service.exportvalueextract;

import java.util.Optional;

public class SpinnerFieldValueExtracter extends CommonFieldExtracter {

	@Override
	public Object getExportValue() {
		Object value = super.getExportValue();
		if (value == null)
			return "";
		int digits = Optional.ofNullable(fieldConfig.spinnerDigits).orElse(0);// 保留小数位数
		if (value instanceof Number) {
			double d = ((Number) value).doubleValue();
			return String.format("%." + digits + "f", d);
		} else {
			return warningAndReturn(value);
		}
	}

	private String warningAndReturn(Object value) {
		logger.warn("导出数值字段，值得类型不匹配，已强制转换为字符串。" + fieldConfig.name);
		return value.toString();
	}
}
