package com.bizvisionsoft.pms.workreport;

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.serviceconsumer.Services;

public class WorkReportInfo {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private ObjectId workReport_id;

	@Init
	private void init() {
		workReport_id = ((WorkReport) context.getInput()).get_id();
	}

	@DataSet("list")
	public List<WorkReport> data() {
		return Arrays.asList(Services.get(WorkReportService.class).get(workReport_id));
	}

}
