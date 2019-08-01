package com.bizvisionsoft.service.exportvalueextract;

import java.util.Optional;

public class SpinnerFieldValueExtracter extends CommonFieldExtracter {

	@Override
	public Object getExportValue() {
		Object value = super.getExportValue();
		if (value == null)
			return "";
		int digits = Optional.ofNullable(fieldConfig.spinnerDigits).orElse(0);// ����С��λ��
		if (value instanceof Number) {
			double d = ((Number) value).doubleValue();
			return String.format("%." + digits + "f", d);
		} else {
			return warningAndReturn(value);
		}
	}

	private String warningAndReturn(Object value) {
		logger.warn("������ֵ�ֶΣ�ֵ�����Ͳ�ƥ�䣬��ǿ��ת��Ϊ�ַ�����" + fieldConfig.name);
		return value.toString();
	}
}
