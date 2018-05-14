package com.bizvisionsoft.pms.project.dataset;

import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class ComparableGantt {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private List<?> workSet;

	private List<?> linkSet;

	@Init
	private void init() {
		Object[] input = (Object[]) context.getInput();
		workSet = (List<?>) input[0];
		linkSet = (List<?>) input[1];
	}

	@DataSet("data")
	public List<?> data() {
		return workSet;
	}

	@DataSet("links")
	public List<?> links() {
		return linkSet;
	}

}
