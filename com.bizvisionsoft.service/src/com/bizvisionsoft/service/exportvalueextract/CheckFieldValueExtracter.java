package com.bizvisionsoft.service.exportvalueextract;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import com.bizvisionsoft.service.tools.Check;

public class CheckFieldValueExtracter extends CommonFieldExtracter {

	@Override
	public Object getExportValue() {
		Object value = super.getExportValue();

		if (Check.isNotAssigned(fieldConfig.optionText)) {
			return warningAndReturn(value);
		}

		String opv = fieldConfig.optionValue;
		if (Check.isNotAssigned(opv)) {
			opv = fieldConfig.optionText;
		}

		int idx = -1;
		String[] list = opv.split("#");
		for (int i = 0; i < list.length; i++) {
			if (list[i].trim().equals(value)) {
				idx = i;
				break;
			}
		}
		
		String[] text = fieldConfig.optionText.split("#");
		List<AbstractMap.SimpleEntry<String, Boolean>> result = new ArrayList<>();
		for (int i = 0; i < text.length; i++)
			result.add(new AbstractMap.SimpleEntry<String, Boolean>(text[i], i == idx));
		return result;
	}

	private String warningAndReturn(Object value) {
		logger.warn("导出多项选择字段，无法获得选项，已强制转换为字符串。" + fieldConfig.name);
		return value.toString();
	}

}
