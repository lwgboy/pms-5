package com.bizvisionsoft.pms.work.dataset;

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class StageInfoDS {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private WorkService service;

	private ObjectId work_id;

	@Init
	private void init() {
		work_id = ((Work) context.getRootInput()).get_id();
		service = Services.get(WorkService.class);
	}

	@DataSet("list")
	public List<Work> data() {
		return Arrays.asList(service.getWork(work_id));
	}

}
