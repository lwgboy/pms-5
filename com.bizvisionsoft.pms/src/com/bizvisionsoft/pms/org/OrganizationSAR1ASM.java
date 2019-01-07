package com.bizvisionsoft.pms.org;

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
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.serviceconsumer.Services;

public class OrganizationSAR1ASM extends AbstractChartASM {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	private String userId = null;

	private String year;

	@Init
	public void init() {
		setContext(context);
		setBruiService(br);
		userId = br.getCurrentUserId();
		year = "" + Calendar.getInstance().get(Calendar.YEAR);
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}

	public Document getOptionDocument() {
		return Services.get(ProjectService.class).getOrganizationSAR1(year, userId);
	}

	public void setYear(String year) {
		this.year = year;
	}

	@Override
	protected JsonObject getOption() {
		return JsonObject.readFrom(getOptionDocument().toJson());
	}

}
