package com.bizvisionsoft.pms.resource;

import java.util.Calendar;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.chart.AbstractChartASM;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class ResourceAllAnalysisASM extends AbstractChartASM {

	@Inject
	private IBruiService bruiService;

	@Inject
	private BruiAssemblyContext context;

	private ObjectId project_id = null;

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
		Object rootInput = context.getRootInput();
		if (rootInput instanceof Project) {
			project_id = ((Project) rootInput).get_id();
		}
		year = "" + Calendar.getInstance().get(Calendar.YEAR);
	}

	public Document getOptionDocument() {
		Document option;
		// if (project_id != null) {
		option = Services.get(WorkService.class).getResourceAllAnalysis(project_id, year);
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
