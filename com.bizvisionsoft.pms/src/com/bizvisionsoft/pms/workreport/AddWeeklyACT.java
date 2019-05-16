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

public class AddWeeklyACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Selector.create("�ܱ���Ŀѡ����", context, null).setTitle("��ѡ��Ҫ��д�ܱ�����Ŀ").open(em -> {
			String reporter = br.getCurrentUserId();
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.setFirstDayOfWeek(Calendar.SUNDAY);
			int i = cal.get(Calendar.DAY_OF_WEEK);

			cal.add(Calendar.DAY_OF_MONTH, cal.getFirstDayOfWeek() - i);
			WorkReport report = new WorkReport().setProject_id(((Project) em.get(0)).get_id())
					.setStage_id(((Project) em.get(0)).getStage_id()).setReporter(reporter)
					.setType(WorkReport.TYPE_WEEKLY).setPeriod(cal.getTime()).setReportDate(new Date())
					.setStatus(WorkReport.STATUS_CREATE);
			try {
				report = ServicesLoader.get(WorkReportService.class).insert(report, br.getDomain());
				((GridPart) context.getContent()).insert(report);
				br.openContent(br.getAssembly("��������"), report);
			} catch (Exception e) {
				Layer.message("��Ŀ:" + ((Project) em.get(0)).getName() + " " + e.getMessage(), Layer.ICON_ERROR);
			}

		});

	}
}
