package com.bizvisionsoft.pms.workpackage.dataset;

import java.util.ArrayList;
import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ScheduleMonitoringDateset {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private Work work;

	private Project project;

	private String userid;

	@Init
	private void init() {
		Object rootInput = context.getRootInput();
		if (rootInput instanceof Project) {
			project = (Project) rootInput;
		} else if (rootInput instanceof Work) {
			work = (Work) rootInput;
		} else {
			userid = brui.getCurrentUserId();
		}
	}

	@DataSet("研发进度监控/" + DataSet.LIST)
	public List<Work> listDesign() {
		if (project != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInProject(project.get_id(),
					TrackView.CATAGORY_DEVELOPMENT);
		} else if (work != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInStage(work.get_id(),
					TrackView.CATAGORY_DEVELOPMENT);
		} else {
			return new ArrayList<Work>();
		}
	}

	@DataSet("研发进度监控/" + DataSet.COUNT)
	public long countDesign() {
		if (project != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInProject(project.get_id(),
					TrackView.CATAGORY_DEVELOPMENT);
		} else if (work != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInStage(work.get_id(),
					TrackView.CATAGORY_DEVELOPMENT);
		} else {
			return 0;
		}
	}

	@DataSet("采购计划监控/" + DataSet.LIST)
	public List<Work> listPurchase() {
		if (project != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInProject(project.get_id(),
					TrackView.CATAGORY_PURCHASE);
		} else if (work != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInStage(work.get_id(),
					TrackView.CATAGORY_PURCHASE);
		} else {
			return new ArrayList<Work>();
		}
	}

	@DataSet("采购计划监控/" + DataSet.COUNT)
	public long countPurchase() {
		if (project != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInProject(project.get_id(),
					TrackView.CATAGORY_PURCHASE);
		} else if (work != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInStage(work.get_id(),
					TrackView.CATAGORY_PURCHASE);
		} else {
			return 0;
		}
	}

	@DataSet("生产计划监控/" + DataSet.LIST)
	public List<Work> listProduce() {
		if (project != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInProject(project.get_id(),
					TrackView.CATAGORY_PRODUCTION);
		} else if (work != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInStage(work.get_id(),
					TrackView.CATAGORY_PRODUCTION);
		} else {
			return new ArrayList<Work>();
		}
	}

	@DataSet("生产计划监控/" + DataSet.COUNT)
	public long countProduce() {
		if (project != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInProject(project.get_id(),
					TrackView.CATAGORY_PRODUCTION);
		} else if (work != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInStage(work.get_id(),
					TrackView.CATAGORY_PRODUCTION);
		} else {
			return 0;
		}
	}

	@DataSet("检验进度监控/" + DataSet.LIST)
	public List<Work> listInspection() {
		if (project != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInProject(project.get_id(),
					TrackView.CATAGORY_QUALITY);
		} else if (work != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInStage(work.get_id(),
					TrackView.CATAGORY_QUALITY);
		} else {
			return new ArrayList<Work>();
		}
	}

	@DataSet("检验进度监控/" + DataSet.COUNT)
	public long countInspection() {
		if (project != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInProject(project.get_id(),
					TrackView.CATAGORY_QUALITY);
		} else if (work != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInStage(work.get_id(),
					TrackView.CATAGORY_QUALITY);
		} else {
			return 0;
		}
	}

	@DataSet("采购计划监控（项目管理）/" + DataSet.LIST)
	public List<Work> listPurchaseForManagement(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		List<Work> listWorkPackageForSchedule = Services.get(WorkService.class).listWorkPackageForSchedule(condition,
				userid, TrackView.CATAGORY_PURCHASE);
		return listWorkPackageForSchedule;
	}

	@DataSet("采购计划监控（项目管理）/" + DataSet.COUNT)
	public long countPurchaseForManagement(@MethodParam(MethodParam.FILTER) BasicDBObject filter) {
		return Services.get(WorkService.class).countWorkPackageForSchedule(filter, userid, TrackView.CATAGORY_PURCHASE);
	}

	@DataSet("生产计划监控（项目管理）/" + DataSet.LIST)
	public List<Work> listProduceForManagement(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		return Services.get(WorkService.class).listWorkPackageForSchedule(condition, userid,
				TrackView.CATAGORY_PRODUCTION);
	}

	@DataSet("生产计划监控（项目管理）/" + DataSet.COUNT)
	public long countProduceForManagement(@MethodParam(MethodParam.FILTER) BasicDBObject filter) {
		return Services.get(WorkService.class).countWorkPackageForSchedule(filter, userid,
				TrackView.CATAGORY_PRODUCTION);
	}
}
