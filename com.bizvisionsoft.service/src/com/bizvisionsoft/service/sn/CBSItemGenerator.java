package com.bizvisionsoft.service.sn;

import com.bizvisionsoft.annotations.md.mongocodex.IAutoGenerator;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.CBSItem;

public class CBSItemGenerator implements IAutoGenerator<CBSItem> {

	@Override
	public Object generate(CBSItem cbsItem, String name, String key, Class<?> t) {
		int index = ServicesLoader.get(CommonService.class).generateCode(name, key);
		return String.format("%05d", index);
	}

}
