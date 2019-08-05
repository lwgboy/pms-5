package com.bizvisionsoft.service.exportvalueextract;

import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;

public class BannerFieldValueExtracter extends CommonFieldExtracter {

	@Override
	public Object getExportValue() {
		Object value = super.getExportValue();
		if (!Boolean.TRUE.equals(fieldConfig.staticContent)) {
			if (Check.isAssigned(fieldConfig.format)) {
				return Formatter.getString(value, fieldConfig.format, locale);
			}else {
				return value;
			}
		}else {
			return fieldConfig.text;
		}
	}
}
