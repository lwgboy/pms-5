package com.bizvisionsoft.pms.workreport;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class MyDailyDS {
	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@DataSet({ "日报/list" })
	private List<WorkReport> list(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition) {
		String userid = brui.getCurrentUserId();
		return Services.get(WorkService.class).createWorkReportDataSet(condition, userid, WorkReport.TYPE_DAILY);
	}

	@DataSet({ "日报/count" })
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
}
