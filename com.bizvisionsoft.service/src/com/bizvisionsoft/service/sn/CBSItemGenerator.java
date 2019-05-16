package com.bizvisionsoft.service.sn;

import com.bizvisionsoft.annotations.md.mongocodex.IAutoGenerator;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.tools.ColorTheme.BruiColor;

public class CBSItemGenerator implements IAutoGenerator<CBSItem> {

	@Override
	public Object generate(CBSItem cbsItem, String name, String key, Class<?> t,String domain) {
		int index = ServicesLoader.get(CommonService.class).generateCode(name, key, domain);
		return String.format("%05d", index);
	}

}
