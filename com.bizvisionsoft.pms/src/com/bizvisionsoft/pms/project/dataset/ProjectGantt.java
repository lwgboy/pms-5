package com.bizvisionsoft.pms.project.dataset;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.Listener;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLink;
import com.bizvisionsoft.service.model.WorkLinkInfo;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ProjectGantt {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private WorkService workService;

	private WorkSpaceService workSpaceService;

	private Workspace workspace;

	@Init
	private void init() {
		workspace = ((IWBSScope) context.getRootInput()).getWorkspace();
		workService = Services.get(WorkService.class);
		workSpaceService = Services.get(WorkSpaceService.class);
	}

	@DataSet({ "项目甘特图/data", "项目甘特图（无表格查看）/data" })
	public List<Work> data() {
		return workService.createTaskDataSet(new BasicDBObject("project_id", workspace.getProject_id()));
	}

	@DataSet({ "项目甘特图/links", "项目甘特图（无表格查看）/links" })
	public List<WorkLink> links() {
		return workService.createLinkDataSet(new BasicDBObject("project_id", workspace.getProject_id()));
	}

	@DataSet({ "项目甘特图/initDateRange", "项目甘特图（无表格查看）/initDateRange" })
	public Date[] initDateRange() {
		return Services.get(ProjectService.class).getPlanDateRange(workspace.getProject_id()).toArray(new Date[0]);

	}

	@DataSet({ "项目甘特图(编辑)/data" })
	public List<WorkInfo> dataInSpace() {
		return workSpaceService.createTaskDataSet(new BasicDBObject("space_id", workspace.getSpace_id()).append("_id",
				new BasicDBObject("$ne", workspace.getWork_id())));
	}

	@DataSet({ "项目甘特图(编辑)/links" })
	public List<WorkLinkInfo> linksInSpace() {
		return workSpaceService.createLinkDataSet(new BasicDBObject("space_id", workspace.getSpace_id()));
	}

	// @DataSet({ "项目甘特图(编辑)/initDateRange" })
	// public Date[] initDateRangeBySpace() {
	// return
	// Services.get(ProjectService.class).getPlanDateRange(project_id).toArray(new
	// Date[0]);
	// }

	@Listener({ "项目甘特图(编辑)/onAfterTaskAdd" })
	public void onAfterTaskAddInSpace(GanttEvent e) {
		WorkInfo workInfo = (WorkInfo) e.task;
		workInfo.setSpaceId(workspace.getSpace_id());
		workSpaceService.insertWork(workInfo);
		System.out.println(e.text);
	}

	@Listener({ "项目甘特图(编辑)/onAfterTaskUpdate", "项目甘特图(编辑)/onAfterTaskMove", "项目甘特图(编辑)/onAfterTaskResize",
			"项目甘特图(编辑)/onAfterTaskProgress" })
	public void onAfterTaskUpdateInSpace(GanttEvent e) {
		workSpaceService.updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", new ObjectId(e.id)))
				.set(Util.getBson((WorkInfo) e.task, "_id")).bson());
	}

	@Listener({ "项目甘特图(编辑)/onAfterTaskDelete" })
	public void onAfterTaskDeleteInSpace(GanttEvent e) {
		workSpaceService.deleteWork(new ObjectId(e.id));
		System.out.println(e.text);
	}

	@Listener({ "项目甘特图(编辑)/onAfterLinkAdd" })
	public void onAfterLinkAddInSpace(GanttEvent e) {
		WorkLinkInfo workLinkInfo = (WorkLinkInfo) e.link;
		workLinkInfo.setSpaceId(workspace.getSpace_id());
		workSpaceService.insertLink(workLinkInfo);
		System.out.println(e.text);
	}

	@Listener({ "项目甘特图(编辑)/onAfterLinkUpdate" })
	public void onAfterLinkUpdateInSpace(GanttEvent e) {
		workSpaceService.updateLink(new FilterAndUpdate().filter(new BasicDBObject("_id", new ObjectId(e.id)))
				.set(Util.getBson((WorkLinkInfo) e.link, "_id")).bson());
		System.out.println(e.text);
	}

	@Listener({ "项目甘特图(编辑)/onAfterLinkDelete" })
	public void onAfterLinkDeleteInSpace(GanttEvent e) {
		workSpaceService.deleteLink(new ObjectId(e.id));
		System.out.println(e.text);
	}

	@Listener({ "项目甘特图(编辑)/onAfterAutoSchedule" })
	public void onAfterAutoScheduleInSpace(GanttEvent e) {
		System.out.println("--------------------onAfterAutoSchedule--------------------");
	}
}
