package com.bizvisionsoft.pms.resource;

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
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	private ObjectId project_id = null;

	@Init
	public void init() {
		setContext(context);
		setBruiService(br);
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
	}

	public Document getOptionDocument() {
		Document option;
		// if (project_id != null) {
		option = Services.get(WorkService.class).getResourceAllAnalysis(project_id, br.getDomain());
		// } else {
		// option = Services.get(WorkService.class).getResourcePlanAnalysis(year);
		// }
		return option;
	}

	@Override
	protected JsonObject getOption() {
		return JsonObject.readFrom(getOptionDocument().toJson());
	}

}
