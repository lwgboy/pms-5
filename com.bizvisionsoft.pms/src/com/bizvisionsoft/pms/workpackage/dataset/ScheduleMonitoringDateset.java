package com.bizvisionsoft.pms.workpackage.dataset;

import java.util.ArrayList;
import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
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
	public List<TrackView> listDesign() {
		String catagory = "研发";
		if (project != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return new ArrayList<TrackView>();
		}
	}

	@DataSet("研发进度监控/" + DataSet.COUNT)
	public long countDesign() {
		String catagory = "研发";
		if (project != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return 0;
		}
	}

	@DataSet("采购计划监控/" + DataSet.LIST)
	public List<TrackView> listPurchase() {
		String catagory = "采购";
		if (project != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return new ArrayList<TrackView>();
		}
	}

	@DataSet("采购计划监控/" + DataSet.COUNT)
	public long countPurchase() {
		String catagory = "采购";
		if (project != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return 0;
		}
	}

	@DataSet("生产计划监控/" + DataSet.LIST)
	public List<TrackView> listProduce() {
		String catagory = "生产";
		if (project != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return new ArrayList<TrackView>();
		}
	}

	@DataSet("生产计划监控/" + DataSet.COUNT)
	public long countProduce() {
		String catagory = "生产";
		if (project != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return 0;
		}
	}

	@DataSet("检验进度监控/" + DataSet.LIST)
	public List<TrackView> listInspection() {
		String catagory = "质量";
		if (project != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return new ArrayList<TrackView>();
		}
	}

	@DataSet("检验进度监控/" + DataSet.COUNT)
	public long countInspection() {
		String catagory = "质量";
		if (project != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return 0;
		}
	}

	@DataSet("采购计划监控（项目管理）/" + DataSet.LIST)
	public List<Work> listPurchaseForManagement(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition) {
		String catagory = "采购";
		return Services.get(WorkService.class).listWorkPackageForSchedule(condition, userid, catagory);
	}

	@DataSet("采购计划监控（项目管理）/" + DataSet.COUNT)
	public long countPurchaseForManagement(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter) {
		String catagory = "采购";
		return Services.get(WorkService.class).countWorkPackageForSchedule(filter, userid, catagory);
	}

	@DataSet("生产计划监控（项目管理）/" + DataSet.LIST)
	public List<Work> listProduceForManagement(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition) {
		String catagory = "生产";
		return Services.get(WorkService.class).listWorkPackageForSchedule(condition, userid, catagory);
	}

	@DataSet("生产计划监控（项目管理）/" + DataSet.COUNT)
	public long countProduceForManagement(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter) {
		String catagory = "生产";
		return Services.get(WorkService.class).countWorkPackageForSchedule(filter, userid, catagory);
	}
}
