package com.bizvisionsoft.service.exportvalueextract;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.TableCellValue;

public class TableFieldValueExtracter extends CommonFieldExtracter {

	@Override
	public Object getExportValue() {
		TableCellValue eValue = new TableCellValue();

		// ����п����
		eValue.columnWidths = fieldConfig.columnWidth;

		// ��ӱ�����
		if (Boolean.TRUE.equals(fieldConfig.headerVisible)) {
			eValue.headers = fieldConfig.columnText;
		}
		// �������
		Object value = super.getExportValue();
		if (value instanceof List<?>) {
			List<?> data = (List<?>) value;
			for (int i = 0; i < data.size(); i++) {
				ArrayList<String> row = new ArrayList<String>();
				eValue.rows.add(row);
				Document rowData = (Document) data.get(i);
				fieldConfig.columnName.forEach(n -> {
					Object v = rowData.get(n);
					if (v == null) {
						row.add("");
					} else {
						row.add("" + v);
					}
				});
			}
		}
		return eValue;
	}

}
