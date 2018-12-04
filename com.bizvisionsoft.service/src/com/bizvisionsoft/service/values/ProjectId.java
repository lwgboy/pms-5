package com.bizvisionsoft.service.values;

import com.bizvisionsoft.mongocodex.tools.IValueGenerateService;

public class ProjectId implements IValueGenerateService {

	@Override
	public Object getValue(Object input) {
		return "" + System.currentTimeMillis();
	}

}
