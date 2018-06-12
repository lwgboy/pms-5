package com.bizvisionsoft.pms.workreport;

import java.util.Date;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.WorkReport;

public class AddDailyACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Selector.open("我的项目选择器", context, null, em -> {
			String reporter = brui.getCurrentUserId();
			WorkReport workReport = new WorkReport().setProject_id(((Project) em.get(0)).get_id())
					.setStage_id(((Project) em.get(0)).getStage_id()).setReporter(reporter)
					.setType(WorkReport.TYPE_DAILY).setPeriod(new Date()).setReportDate(new Date());
			workReport = ServicesLoader.get(WorkService.class).insertWorkReport(workReport);
			((GridPart) context.getContent()).insert(workReport);
			brui.openContent(brui.getAssembly("日报详情"), workReport);
		});

	}
}
