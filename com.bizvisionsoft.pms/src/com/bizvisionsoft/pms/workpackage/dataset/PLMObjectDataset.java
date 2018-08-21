package com.bizvisionsoft.pms.workpackage.dataset;

import java.util.ArrayList;
import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.jz.Distribution;
import com.bizvisionsoft.service.model.Work;
import com.mongodb.BasicDBObject;

public class PLMObjectDataset {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private String projectNumber;

	@Init
	private void init() {
		// TODO 获取传入的项目编号
		Object input = context.getParentContext().getInput();
		if (input instanceof Work) {
			projectNumber = ((Work) input).getProjectNumber();
		}
	}

	@DataSet(DataSet.LIST)
	private List<?> list(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		try {
			return Distribution.getPLMWorkspace(projectNumber);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}

	}
}
