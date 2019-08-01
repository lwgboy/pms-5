package com.bizvisionsoft.service.exportvalueextract;

import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.service.model.RemoteFile;

public class ImageFileFieldValueExtracter extends CommonFieldExtracter {

	@Override
	public Object getExportValue() {
		Object value = super.getExportValue();
		if (value == null)
			return null;
		if (value instanceof List<?>) {
			List<?> list = (List<?>) value;
			if (list.isEmpty()) {
				return null;
			}
			Document doc = (Document) list.get(0);
			RemoteFile file = new RemoteFile();
			file._id = doc.getObjectId("_id");
			file.contentType = doc.getString("contentType");
			file.domain = domain;
			file.name = doc.getString("name");
			file.namepace = doc.getString("namepace");
			return file;
		} else {
			return warningAndReturn(value);
		}
	}

	private String warningAndReturn(Object value) {
		logger.warn("导出图片字段，值得类型不是List<Document>类型，已忽略。" + fieldConfig.name);
		return value.toString();
	}

}
