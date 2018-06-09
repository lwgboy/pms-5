package com.bizvisionsoft.pms.project.dataset;

import java.util.ArrayList;
import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.WorkLink;
import com.bizvisionsoft.service.model.WorkScheduleInfo;

public class ScheduleGantt {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private IWBSScope workScope;

	@Init
	private void init() {
		workScope = (IWBSScope) context.getRootInput();
	}

	@DataSet("data")
	public List<WorkScheduleInfo> data() {
		List<WorkScheduleInfo> workScheduleInfos = new ArrayList<WorkScheduleInfo>();
		workScope.createGanttTaskDataSet().forEach(work -> {
			workScheduleInfos.add(new WorkScheduleInfo().setWork(work));
		});
		;
		return workScheduleInfos;
	}

	@DataSet("links")
	public List<WorkLink> links() {
		return workScope.createGanttLinkDataSet();
	}
}
