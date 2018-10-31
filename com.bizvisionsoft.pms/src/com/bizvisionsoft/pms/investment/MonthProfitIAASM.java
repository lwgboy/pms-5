package com.bizvisionsoft.pms.investment;

import java.util.Calendar;
import java.util.List;

import org.bson.Document;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.chart.AbstractChartASM;
import com.bizvisionsoft.service.EPSService;
import com.bizvisionsoft.service.model.EPSInvestmentAnalysis;
import com.bizvisionsoft.serviceconsumer.Services;

public class MonthProfitIAASM extends AbstractChartASM {

	@Inject
	private IBruiService bruiService;

	@Inject
	private BruiAssemblyContext context;

	private String year;

	private List<EPSInvestmentAnalysis> epsIAs = null;

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
		return Services.get(EPSService.class).getMonthProfitIA(epsIAs, year);
	}

	public void setEpsIAs(List<EPSInvestmentAnalysis> epsIAs) {
		this.epsIAs = epsIAs;
	}

	public void setYear(String year) {
		this.year = year;
	}

	@Override
	protected JsonObject getOption() {
		return JsonObject.readFrom(getOptionDocument().toJson());
	}

}
