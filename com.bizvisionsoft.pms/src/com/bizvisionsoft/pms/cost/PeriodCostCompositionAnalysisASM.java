package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

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
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.serviceconsumer.Services;

@Deprecated
public class PeriodCostCompositionAnalysisASM extends AbstractChartASM {

	@Inject
	private IBruiService bruiService;

	@Inject
	private BruiAssemblyContext context;

	private ObjectId cbsScope_id = null;

	private String startPeriod;

	private String endPeriod;

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
		// 获取传入的CBSItem 从成本管理打开项目成本管理时，从contextInput获取
		// 获取当前年、月
		Calendar currentCBSPeriod = Calendar.getInstance();
		int newYear = currentCBSPeriod.get(Calendar.YEAR);
		int newMonth = currentCBSPeriod.get(Calendar.MONTH);

		Date date = null;
		// 获取传入的CBSItem 从成本管理打开项目成本管理时，从contextInput获取
		Object input = context.getInput();
		if (input == null) {
			// 获取传入的CBSItem 从项目、阶段打开项目成本管理时，从contextRootInput获取
			Object rootInput = context.getRootInput();
			if (rootInput instanceof ICBSScope) {
				ICBSScope icbsScope = (ICBSScope) rootInput;
				input = Services.get(CBSService.class).get(icbsScope.getCBS_id());
			}
		}
		// input不为空时，为打开项目成本管理，这时当前期间从项目中获取，并为项目下一结算月份
		if (input != null) {
			if (input instanceof CBSItem) {
				CBSItem cbsItem = (CBSItem) input;
				cbsScope_id = cbsItem.getScope_id();
				date = cbsItem.getNextSettlementDate();
				// 如果项目下一结算月份等于当前月份，则日期为当前结算月份
				currentCBSPeriod.setTime(date);
				if (currentCBSPeriod.get(Calendar.YEAR) == newYear
						&& currentCBSPeriod.get(Calendar.MONTH) == newMonth) {
					currentCBSPeriod.add(Calendar.MONTH, -1);
				}
			}
		}
		// 从首页打开成本管理，结算月份为当前系统整体结算期间
		if (date == null) {
			date = Services.get(CommonService.class).getCurrentCBSPeriod();
			currentCBSPeriod.setTime(date);
		}
		startPeriod = "" + currentCBSPeriod.get(Calendar.YEAR);
		startPeriod += String.format("%02d", currentCBSPeriod.get(java.util.Calendar.MONTH) + 1);

		endPeriod = startPeriod;
	}

	public Document getOptionDocument() {
		String userId = bruiService.getCurrentUserId();
		if (cbsScope_id != null) {
			return Services.get(CBSService.class).getPeriodCostCompositionAnalysis(cbsScope_id, startPeriod, endPeriod, userId);
		} else {
			// TODO 增加用户角色判断
			return Services.get(CBSService.class).getPeriodCostCompositionAnalysis(startPeriod, endPeriod,
					userId);
		}
	}

	public void setStartPeriod(String startPeriod) {
		this.startPeriod = startPeriod;
	}

	public void setEndPeriod(String endPeriod) {
		this.endPeriod = endPeriod;
	}

	@Override
	protected JsonObject getOption() {
		return JsonObject.readFrom(getOptionDocument().toJson());
	}
}
