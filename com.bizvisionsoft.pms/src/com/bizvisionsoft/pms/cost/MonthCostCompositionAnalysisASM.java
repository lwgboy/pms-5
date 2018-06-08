package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

import org.bson.Document;
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
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.serviceconsumer.Services;

public class MonthCostCompositionAnalysisASM {

	@Inject
	private IBruiService bruiService;

	@GetContainer
	@GetContent("chart")
	private ECharts content;

	@Inject
	private BruiAssemblyContext context;

	@CreateUI
	private void createUI(Composite parent) {
		parent.setLayout(new FillLayout());
		createChart(parent);
		// ((BruiAssemblyContext)context.getChildContextByAssemblyName("某某图表")).getContent();
	}

	private void createChart(Composite parent) {
		content = new ECharts(parent, SWT.NONE);

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
		String result = "" + currentCBSPeriod.get(Calendar.YEAR);
		setOption(result);
	}

	public void setOption(String year) {
		Document option = Services.get(CBSService.class).getMonthCostCompositionAnalysis(year);
		content.setOption(JsonObject.readFrom(option.toJson()));
	}

}
