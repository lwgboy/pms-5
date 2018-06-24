package com.bizvisionsoft.pms.workreport;

import java.util.Calendar;
import java.util.Date;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.WorkReport;

public class AddMonthlyACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Selector.create("我的月报项目选择器", context, null).setTitle("请选择要填写月报的项目").open(em -> {
			String reporter = brui.getCurrentUserId();
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			WorkReport report = new WorkReport().setProject_id(((Project) em.get(0)).get_id())
					.setStage_id(((Project) em.get(0)).getStage_id()).setReporter(reporter)
					.setType(WorkReport.TYPE_MONTHLY).setPeriod(cal.getTime()).setReportDate(new Date())
					.setStatus(WorkReport.STATUS_CREATE);
			try {
				report = ServicesLoader.get(WorkReportService.class).insert(report);
				((GridPart) context.getContent()).insert(report);
				brui.openContent(brui.getAssembly("报告详情"), report);
			} catch (Exception e) {
				Layer.message("项目:" + ((Project) em.get(0)).getName() + " " + e.getMessage(), Layer.ICON_CANCEL);
			}

		});

	}
}
