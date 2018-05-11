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
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLink;
import com.bizvisionsoft.service.model.WorkLinkInfo;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ProjectGantt {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private WorkService workService;
	
	private WorkSpaceService workSpaceService;

	private ObjectId space_id;

	private ObjectId project_id;

	private ObjectId work_id;

	@Init
	private void init() {
		space_id = ((Project) context.getRootInput()).getSpaceId();
		work_id = ((Project) context.getRootInput()).getWBS_id();
		project_id = ((Project) context.getRootInput()).get_id();
		workService = Services.get(WorkService.class);
		workSpaceService = Services.get(WorkSpaceService.class);
	}

	@DataSet({ "项目甘特图/data", "项目甘特图（无表格查看）/data" })
	public List<Work> data() {
		return workService.createTaskDataSet(new BasicDBObject("project_id", project_id));
	}

	@DataSet({ "项目甘特图/links", "项目甘特图（无表格查看）/links" })
	public List<WorkLink> links() {
		return workService.createLinkDataSet(new BasicDBObject("project_id", project_id));
	}

	@DataSet({ "项目甘特图/initDateRange", "项目甘特图（无表格查看）/initDateRange" })
	public Date[] initDateRange() {
		return Services.get(ProjectService.class).getPlanDateRange(project_id).toArray(new Date[0]);

	}

	@DataSet({ "项目甘特图(编辑)/data" })
	public List<WorkInfo> dataBySpace() {
		return workSpaceService.createTaskDataSet(
				new BasicDBObject("space_id", space_id).append("_id", new BasicDBObject("$ne", work_id)));
	}

	@DataSet({ "项目甘特图(编辑)/links" })
	public List<WorkLinkInfo> linksBySpace() {
		return workSpaceService.createLinkDataSet(new BasicDBObject("space_id", space_id));
	}

	// @DataSet({ "项目甘特图(编辑)/initDateRange" })
	// public Date[] initDateRangeBySpace() {
	// return
	// Services.get(ProjectService.class).getPlanDateRange(project_id).toArray(new
	// Date[0]);
	// }

	@Listener({ "项目甘特图(编辑)/onAfterTaskAdd" })
	public void onAfterTaskAddBySpace(GanttEvent e) {
		WorkInfo workInfo = (WorkInfo) e.task;
		workInfo.setSpaceId(space_id);
		workSpaceService.insertWork(workInfo);
		System.out.println(e.text);
	}

	@Listener({ "项目甘特图(编辑)/onAfterTaskUpdate" })
	public void onAfterTaskUpdateBySpace(GanttEvent e) {
		workSpaceService.updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", new ObjectId(e.id)))
				.set(Util.getBson((WorkInfo) e.task, "_id")).bson());
		System.out.println(e.text);
	}

	@Listener({ "项目甘特图(编辑)/onAfterTaskDelete" })
	public void onAfterTaskDeleteBySpace(GanttEvent e) {
		workSpaceService.deleteWork(new ObjectId(e.id));
		System.out.println(e.text);
	}

	@Listener({ "项目甘特图(编辑)/onAfterLinkAdd" })
	public void onAfterLinkAddBySpace(GanttEvent e) {
		WorkLinkInfo workLinkInfo = (WorkLinkInfo) e.link;
		workLinkInfo.setSpaceId(space_id);
		workSpaceService.insertLink(workLinkInfo);
		System.out.println(e.text);
	}

	@Listener({ "项目甘特图(编辑)/onAfterLinkUpdate" })
	public void onAfterLinkUpdateBySpace(GanttEvent e) {
		workSpaceService.updateLink(new FilterAndUpdate().filter(new BasicDBObject("_id", new ObjectId(e.id)))
				.set(Util.getBson((WorkLinkInfo) e.link, "_id")).bson());
		System.out.println(e.text);
	}

	@Listener({ "项目甘特图(编辑)/onAfterLinkDelete" })
	public void onAfterLinkDeleteBySpace(GanttEvent e) {
		workSpaceService.deleteLink(new ObjectId(e.id));
		System.out.println(e.text);
	}


	@Listener({ "项目甘特图(编辑)/onAfterTaskAutoSchedule" })
	public void onAfterTaskAutoSchedule(GanttEvent e) {
		System.out.println(">>>onAfterTaskAutoSchedule");
	}
}
