package com.bizvisionsoft.pms.resource;

import java.util.Calendar;

import org.bson.Document;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.chart.AbstractChartASM;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.serviceconsumer.Services;

public class DeptResourceAllAnalysisASM extends AbstractChartASM {

	@Inject
	private IBruiService bruiService;

	@Inject
	private BruiAssemblyContext context;

	private String year;

	@Init
	public void init() {
		setContext(context);
		setBruiService(bruiService);
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}

	@Override
	protected void setOptionBefore() {
		year = "" + Calendar.getInstance().get(Calendar.YEAR);
	}

	public Document getOptionDocument() {
		Document option;
		// if (project_id != null) {
		option = Services.get(WorkService.class).getResourceAllAnalysisByDept(year, bruiService.getCurrentUserId());
		// } else {
		// option = Services.get(WorkService.class).getResourcePlanAnalysis(year);
		// }
		return option;
	}

	public void setYear(String year) {
		this.year = year;
	}

	@Override
	protected JsonObject getOption() {
		return JsonObject.readFrom(getOptionDocument().toJson());
	}

}
