package com.bizvisionsoft.pms.workreport;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class MyWorkReportDS {
	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private ObjectId workReport_id;

	@Init
	private void init() {
		Object input = context.getInput();
		if (input != null && input instanceof WorkReport) {
			workReport_id = ((WorkReport) input).get_id();
		}
	}

	@DataSet({ "�ձ�/list" })
	private List<WorkReport> list(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition) {
		String userid = brui.getCurrentUserId();
		return Services.get(WorkService.class).createWorkReportDataSet(condition, userid, WorkReport.TYPE_DAILY);
	}

	@DataSet({ "�ձ�/count" })
	private long count(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter) {
		String userid = brui.getCurrentUserId();
		return Services.get(WorkService.class).countWorkReportDataSet(filter, userid, WorkReport.TYPE_DAILY);
	}

	@DataSet(DataSet.INSERT)
	private WorkReport insert(WorkReport workReport) {
		return Services.get(WorkService.class).insertWorkReport(workReport);
	}

	@DataSet(DataSet.DELETE)
	private long delete(@ServiceParam(ServiceParam._ID) ObjectId _id) {
		return Services.get(WorkService.class).deleteWorkReport(_id);
	}

	@DataSet({ "�ձ�-����/list" })
	private List<Work> listDailyWork() {
		String userid = brui.getCurrentUserId();
		return Services.get(WorkService.class)
				.createProcessingWorkDataSet(new Query().filter(new BasicDBObject()).bson(), userid);
	}

	@DataSet({ "�ձ�-����/count" })
	private long count() {
		String userid = brui.getCurrentUserId();
		return Services.get(WorkService.class).countProcessingWorkDataSet(new BasicDBObject(), userid);
	}
}
