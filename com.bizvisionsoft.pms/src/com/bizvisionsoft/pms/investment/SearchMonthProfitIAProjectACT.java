package com.bizvisionsoft.pms.investment;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.EPSService;
import com.bizvisionsoft.service.model.EPS;
import com.bizvisionsoft.service.model.EPSInvestmentAnalysis;
import com.bizvisionsoft.serviceconsumer.Services;

public class SearchMonthProfitIAProjectACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		Selector.open("EPS选择器", context, null, d -> {
			if (d.size() == 0) {
				Layer.message("请选择需要对比分析的范围。", Layer.ICON_CANCEL);
				return;
			}
			if (d.size() > 5) {
				Layer.message("最多选择5个范围进行对比分析。", Layer.ICON_CANCEL);
				return;
			}
			List<EPSInvestmentAnalysis> epsIAs = new ArrayList<EPSInvestmentAnalysis>();
			d.forEach(a -> {
				if (a instanceof EPS) {
					EPSInvestmentAnalysis epsIA = new EPSInvestmentAnalysis();
					epsIA.name = ((EPS) a).getName();
					epsIA.project_ids = Services.get(EPSService.class).getSubProjectId(((EPS) a).get_id());
					epsIAs.add(epsIA);
				}
			});
			MonthProfitIAASM content = (MonthProfitIAASM) context.getChildContextByAssemblyName("销售利润分析组件")
					.getContent();
			content.setEpsIAs(epsIAs);
			content.refresh();

		});

	}

}
