package com.bizvisionsoft.service.exportvalueextract;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.bizvisionsoft.service.tools.Check;

public class ComboFieldValueExtracter extends CommonFieldExtracter {

	@Override
	public Object getExportValue() {
		Object value = getExportValue();
		if (value == null)
			return "";
		// 根据静态文本取值
		if (Check.isAssigned(fieldConfig.optionText)) {
			String opt = fieldConfig.optionValue;
			if (Check.isNotAssigned(opt))
				opt = fieldConfig.optionText;
			List<String> values = Arrays.asList(opt.split("#")).stream().map(s -> s.trim()).collect(Collectors.toList());
			List<String> labels = Arrays.asList(fieldConfig.optionText.split("#")).stream().map(s -> s.trim()).collect(Collectors.toList());
			int idx = values.indexOf(value);
			if (idx >= 0 && idx < labels.size()) {
				return labels.get(idx);
			} else {
				logger.warn("导出选项字段，无法获得静态选项值对应的文本，已用空字符串代替。" + fieldConfig.name);
				return "";
			}
		}
		// 静态字典取值,动态字典取值，查询取值等情况文本和值相同
		if (Check.isAssigned(fieldConfig.dicCatalog)) {
			return value;
		}
		// 动态字典取值
		if (Check.isAssigned(fieldConfig.dicCatalogField)) {
			return value;
		}
		// 查询取值
		if (Check.isAssigned(fieldConfig.optionQueryCollection)) {
			return value;
		}
		logger.warn("导出选项字段出错，不支持的文本提取方式（例如：注解），已使用值替代了文本。" + fieldConfig.name);
		return value;

	}

}
