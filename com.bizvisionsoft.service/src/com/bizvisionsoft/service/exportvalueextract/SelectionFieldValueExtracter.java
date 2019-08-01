package com.bizvisionsoft.service.exportvalueextract;

import java.util.Map;
import java.util.stream.Stream;

public class SelectionFieldValueExtracter extends CommonFieldExtracter {

	@Override
	public Object getExportValue() {
		Object value = super.getExportValue();
		if (value == null)
			return "";
		// �жϱ����ǲ���Map
		if (value instanceof Map<?, ?>) {
			Object v = Stream.of("name", "label", "description").map(((Map<?, ?>) value)::get).filter(o -> o != null).findFirst()
					.orElse(null);
			if (v == null)
				return "";
			else
				return "" + v;
		}
		//�ж���û��meta
		String fieldName = fieldConfig.name+"_meta";
		Object meta = data.get(fieldName);
		if (meta instanceof Map<?, ?>) {
			Object v = Stream.of("name", "label", "description").map(((Map<?, ?>) meta)::get).filter(o -> o != null).findFirst()
					.orElse(null);
			if (v == null)
				return "";
			else
				return "" + v;
		}
		return ""+value;
	}

}
