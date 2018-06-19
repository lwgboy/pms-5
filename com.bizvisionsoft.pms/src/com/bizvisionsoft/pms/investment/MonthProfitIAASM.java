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
		// // 获取传入的CBSItem 从成本管理打开项目成本管理时，从contextInput获取
		// Object input = context.getInput();
		// if (input == null) {
		// // 获取传入的CBSItem 从项目、阶段打开项目成本管理时，从contextRootInput获取
		// Object rootInput = context.getRootInput();
		// if (rootInput instanceof ICBSScope) {
		// ICBSScope icbsScope = (ICBSScope) rootInput;
		// input = Services.get(CBSService.class).get(icbsScope.getCBS_id());
		// }
		// }
		// // input不为空时，为打开项目成本管理，这时当前期间从项目中获取，并为项目下一结算月份
		// if (input != null) {
		// if (input instanceof CBSItem) {
		// CBSItem cbsItem = (CBSItem) input;
		// cbsScope_id = cbsItem.getScope_id();
		// }
		// }
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
