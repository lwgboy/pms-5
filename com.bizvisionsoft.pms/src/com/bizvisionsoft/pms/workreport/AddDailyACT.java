package com.bizvisionsoft.pms.workreport;

import java.util.Calendar;

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

public class AddDailyACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Selector.create("日报项目选择器.selectorassy", context, null).setTitle("请选择要填写日报的项目").open(em -> {
			String reporter = br.getCurrentUserId();
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			WorkReport report = new WorkReport().setProject_id(((Project) em.get(0)).get_id())
					.setStage_id(((Project) em.get(0)).getStage_id()).setReporter(reporter)
					.setType(WorkReport.TYPE_DAILY).setPeriod(cal.getTime()).setReportDate(cal.getTime())
					.setStatus(WorkReport.STATUS_CREATE);
			try {
				report = ServicesLoader.get(WorkReportService.class).insert(report, br.getDomain());
				((GridPart) context.getContent()).insert(report);
				br.openContent(br.getAssembly("报告详情.assy"), report);
			} catch (Exception e) {
				Layer.message("项目:" + ((Project) em.get(0)).getName() + " " + e.getMessage(), Layer.ICON_ERROR);
			}

		});

	}
}
