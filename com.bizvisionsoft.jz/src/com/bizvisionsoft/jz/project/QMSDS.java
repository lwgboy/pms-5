package com.bizvisionsoft.jz.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.jz.Distribution;
import com.bizvisionsoft.jz.PLMObject;
import com.bizvisionsoft.service.model.Project;
import com.mongodb.BasicDBObject;

public class QMSDS {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private String projectNumber;

	@Init
	private void init() {
		Object rootInput = context.getRootInput();
		if (rootInput instanceof Project) {
			projectNumber = ((Project) rootInput).getProjectNumber();
		}
	}

	@DataSet(DataSet.LIST)
	private List<PLMObject> list(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) String userid) {
		try {
			return new Distribution().getQMSObject(Arrays.asList(projectNumber));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<PLMObject>();
	}

	@DataSet(DataSet.COUNT)
	private long count(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) String userid) {
		return 1;
	}
}
