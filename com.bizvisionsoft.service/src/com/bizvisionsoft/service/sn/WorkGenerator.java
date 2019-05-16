package com.bizvisionsoft.service.sn;

import com.bizvisionsoft.annotations.md.mongocodex.IAutoGenerator;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.WorkInfo;

public class WorkGenerator implements IAutoGenerator<WorkInfo> {

	@Override
	public Object generate(WorkInfo wi, String name, String key, Class<?> t,String domain) {
		int index = ServicesLoader.get(CommonService.class).generateCode(name, key + wi.getProjectIdInString(), domain);
		return String.format("%06d", index);
	}

}
