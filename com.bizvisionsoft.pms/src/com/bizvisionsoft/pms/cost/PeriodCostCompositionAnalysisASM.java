package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;
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

public class PeriodCostCompositionAnalysisASM {

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
		// ((BruiAssemblyContext)context.getChildContextByAssemblyName("ĳĳͼ��")).getContent();
	}

	private void createChart(Composite parent) {
		content = new ECharts(parent, SWT.NONE);

		// ��ȡ��ǰ�ꡢ��
		Calendar currentCBSPeriod = Calendar.getInstance();
		int newYear = currentCBSPeriod.get(Calendar.YEAR);
		int newMonth = currentCBSPeriod.get(Calendar.MONTH);

		Date date = null;
		// ��ȡ�����CBSItem �ӳɱ��������Ŀ�ɱ�����ʱ����contextInput��ȡ
		Object input = context.getInput();
		if (input == null) {
			// ��ȡ�����CBSItem ����Ŀ���׶δ���Ŀ�ɱ�����ʱ����contextRootInput��ȡ
			Object rootInput = context.getRootInput();
			if (rootInput instanceof ICBSScope) {
				ICBSScope icbsScope = (ICBSScope) rootInput;
				input = Services.get(CBSService.class).get(icbsScope.getCBS_id());
			}
		}
		// input��Ϊ��ʱ��Ϊ����Ŀ�ɱ�������ʱ��ǰ�ڼ����Ŀ�л�ȡ����Ϊ��Ŀ��һ�����·�
		ObjectId cbsScope_id = null;
		if (input != null) {
			if (input instanceof CBSItem) {
				CBSItem cbsItem = (CBSItem) input;
				cbsScope_id = cbsItem.getScope_id();
				date = cbsItem.getNextSettlementDate();
				// �����Ŀ��һ�����·ݵ��ڵ�ǰ�·ݣ�������Ϊ��ǰ�����·�
				currentCBSPeriod.setTime(date);
				if (currentCBSPeriod.get(Calendar.YEAR) == newYear
						&& currentCBSPeriod.get(Calendar.MONTH) == newMonth) {
					currentCBSPeriod.add(Calendar.MONTH, -1);
				}
			}
		}
		// ����ҳ�򿪳ɱ����������·�Ϊ��ǰϵͳ��������ڼ�
		if (date == null) {
			date = Services.get(CommonService.class).getCurrentCBSPeriod();
			currentCBSPeriod.setTime(date);
		}
		String result = "" + currentCBSPeriod.get(Calendar.YEAR);
		result += String.format("%02d", currentCBSPeriod.get(java.util.Calendar.MONTH) + 1);
		setOption(result, result, cbsScope_id);
	}

	public void setOption(String startPeriod, String endPeriod, ObjectId cbsScope_id) {
		Document option;
		if (cbsScope_id != null) {
			option = Services.get(CBSService.class).getPeriodCostCompositionAnalysis(cbsScope_id, startPeriod,
					endPeriod);
		} else {
			option = Services.get(CBSService.class).getPeriodCostCompositionAnalysis(startPeriod, endPeriod);
		}
		content.setOption(JsonObject.readFrom(option.toJson()));
	}

}
