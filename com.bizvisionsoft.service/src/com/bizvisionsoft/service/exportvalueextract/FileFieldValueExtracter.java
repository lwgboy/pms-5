package com.bizvisionsoft.service.exportvalueextract;

import java.util.List;

import org.bson.Document;

public class FileFieldValueExtracter extends CommonFieldExtracter {

	@Override
	public Object getExportValue() {
		Object value = super.getExportValue();
		if (value == null)
			return "";
		if (value instanceof List<?>) {
			if (((List<?>) value).isEmpty()) {
				return "";
			}
			Object doc = ((List<?>) value).get(0);
			if (doc instanceof Document) {
				Object name = ((Document) doc).get("name");
				if (name == null) {
					return warningAndReturn(value);
				} else {
					return "" + name;
				}
			} else {
				return warningAndReturn(value);
			}
		} else {
			return warningAndReturn(value);
		}
	}

	private String warningAndReturn(Object value) {
		logger.warn("�����ļ��ֶΣ�ֵ�����Ͳ���List<Document>���ͣ���ǿ��ת��Ϊ�ַ�����" + fieldConfig.name);
		return value.toString();
	}

}
