package com.bizvisionsoft.service.exportvalueextract;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.TableCellValue;

public class TableFieldValueExtracter extends CommonFieldExtracter {

	@Override
	public Object getExportValue() {
		TableCellValue eValue = new TableCellValue();

		// 添加列宽比例
		eValue.columnWidths = fieldConfig.columnWidth;

		// 添加标题行
		if (Boolean.TRUE.equals(fieldConfig.headerVisible)) {
			eValue.headers = fieldConfig.columnText;
		}
		// 添加数据
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
