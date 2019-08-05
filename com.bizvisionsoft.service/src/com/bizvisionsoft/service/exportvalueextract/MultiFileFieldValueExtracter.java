package com.bizvisionsoft.service.exportvalueextract;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;

public class MultiFileFieldValueExtracter extends CommonFieldExtracter {

	@Override
	public Object getExportValue() {
		Object value = super.getExportValue();
		if (value == null)
			return null;
		if (value instanceof List<?>) {
			return ((List<?>) value).stream().map(itm -> ((Document) itm).getString("name")).collect(Collectors.toList());
		} else {
			return warningAndReturn(value);
		}
	}

	private String warningAndReturn(Object value) {
		logger.warn("�������ļ��ֶΣ�ֵ�����Ͳ���List<Document>���ͣ���ǿ��ת��Ϊ�ַ�����" + fieldConfig.name);
		return value.toString();
	}

}
