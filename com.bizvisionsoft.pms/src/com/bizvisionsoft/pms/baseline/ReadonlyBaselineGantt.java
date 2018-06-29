package com.bizvisionsoft.pms.baseline;

import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Baseline;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkLink;

public class ReadonlyBaselineGantt {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private Baseline baseline;

	@Init
	private void init() {
		baseline = (Baseline) context.getInput();
	}

	@DataSet("data")
	public List<Work> data() {
		return baseline.createGanttTaskDataSet();
	}

	@DataSet("links")
	public List<WorkLink> links() {
		return baseline.createGanttLinkDataSet();
	}
}
