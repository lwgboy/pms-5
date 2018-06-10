package com.bizvisionsoft.pms.chart;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.chart.ECharts;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.GetContainer;
import com.bizvisionsoft.annotations.ui.common.GetContent;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public abstract class AbstractChartASM {

	@Inject
	private IBruiService bruiService;

	@GetContainer
	@GetContent("chart")
	private ECharts content;

	@Inject
	private BruiAssemblyContext context;
	
	protected void setBruiService(IBruiService bruiService) {
		this.bruiService = bruiService;
	}

	protected void setContext(BruiAssemblyContext context) {
		this.context = context;
	}

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FillLayout());
		content = new ECharts(parent, SWT.NONE);
		setOptionBefore();
		content.setOption(getOption());
		setOptionAfter();
	}

	protected void setOptionAfter() {
	}

	protected void setOptionBefore() {
	}

	protected abstract JsonObject getOption();

	public void refresh() {
		content.setOption(getOption());
	}
}
