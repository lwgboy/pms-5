package com.bizvisionsoft.pms.projectchange;

import java.util.ArrayList;
import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.model.ProjectChangeTask;

public class ProjectChangeProcessDS {
	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private ProjectChange pc;

	@Init
	private void init() {
		pc = (ProjectChange) context.getInput();
	}

	@DataSet("list")
	public List<ProjectChangeTask> data() {
		return pc != null ? pc.getReviewer() : new ArrayList<ProjectChangeTask>();
	}
}
