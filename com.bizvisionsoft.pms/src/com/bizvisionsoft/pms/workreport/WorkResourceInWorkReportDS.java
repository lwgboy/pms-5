package com.bizvisionsoft.pms.workreport;

import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.model.WorkReportAssignment;
import com.bizvisionsoft.service.model.WorkResourceInWorkReport;
import com.bizvisionsoft.serviceconsumer.Services;

public class WorkResourceInWorkReportDS {
	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private WorkReportAssignment input;

	@Init
	private void init() {
		input = (WorkReportAssignment) context.getInput();
	}

	@DataSet({ "工作报告-工作资源用量/list" })
	public List<WorkResourceInWorkReport> list() {
		return Services.get(WorkReportService.class).createWorkResourceInWorkReportDataSet(input.getWork().get_id(),
				input.getWorkReport().get_id());

	}

	@DataSet({ "工作报告-工作资源用量/count" })
	public long createDeptUser() {
		return Services.get(WorkReportService.class).countWorkResourceInWorkReportDataSet(input.getWork().get_id(),
				input.getWorkReport().get_id());
	}
}
