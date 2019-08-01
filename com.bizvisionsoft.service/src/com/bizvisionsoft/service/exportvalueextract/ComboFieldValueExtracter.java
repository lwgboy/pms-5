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
		// ���ݾ�̬�ı�ȡֵ
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
				logger.warn("����ѡ���ֶΣ��޷���þ�̬ѡ��ֵ��Ӧ���ı������ÿ��ַ������档" + fieldConfig.name);
				return "";
			}
		}
		// ��̬�ֵ�ȡֵ,��̬�ֵ�ȡֵ����ѯȡֵ������ı���ֵ��ͬ
		if (Check.isAssigned(fieldConfig.dicCatalog)) {
			return value;
		}
		// ��̬�ֵ�ȡֵ
		if (Check.isAssigned(fieldConfig.dicCatalogField)) {
			return value;
		}
		// ��ѯȡֵ
		if (Check.isAssigned(fieldConfig.optionQueryCollection)) {
			return value;
		}
		logger.warn("����ѡ���ֶγ�����֧�ֵ��ı���ȡ��ʽ�����磺ע�⣩����ʹ��ֵ������ı���" + fieldConfig.name);
		return value;

	}

}
