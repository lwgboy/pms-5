package com.bizvisionsoft.service.sn;

import com.bizvisionsoft.annotations.md.mongocodex.IAutoGenerator;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.Docu;

public class DocNumberGenerator implements IAutoGenerator<Docu> {

	@Override
	public Object generate(Docu doc, String name, String key, Class<?> t) {
		int index = ServicesLoader.get(CommonService.class).generateCode(name, key);
		return String.format("%06d", index);
	}

}
