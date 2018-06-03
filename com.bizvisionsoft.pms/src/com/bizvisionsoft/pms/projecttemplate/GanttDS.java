package com.bizvisionsoft.pms.projecttemplate;

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
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLinkInfo;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class GanttDS {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private WorkSpaceService workSpaceService;

	private Workspace workspace;

	@Init
	private void init() {
		workspace = ((IWBSScope) context.getRootInput()).getWorkspace();
		workSpaceService = Services.get(WorkSpaceService.class);
	}

	@DataSet("data")
	public List<WorkInfo> dataInSpace() {
		return workSpaceService.createTaskDataSet(new BasicDBObject("space_id", workspace.getSpace_id()).append("_id",
				new BasicDBObject("$ne", workspace.getWork_id())));
	}

	@DataSet("links")
	public List<WorkLinkInfo> linksInSpace() {
		return workSpaceService.createLinkDataSet(new BasicDBObject("space_id", workspace.getSpace_id()));
	}

	// @DataSet({ "ÏîÄ¿¸ÊÌØÍ¼£¨±à¼­£©/initDateRange" })
	// public Date[] initDateRangeBySpace() {
	// return
	// Services.get(ProjectService.class).getPlanDateRange(project_id).toArray(new
	// Date[0]);
	// }

	@Listener("onAfterTaskAdd")
	public void onAfterTaskAddInSpace(GanttEvent e) {
		WorkInfo workInfo = (WorkInfo) e.task;
		workInfo.setSpaceId(workspace.getSpace_id());
		if (workInfo.getParent_id() == null && workspace.getWork_id() != null) {
			workInfo.setParent_id(workspace.getWork_id());
		}
		workSpaceService.insertWork(workInfo);
		System.out.println(e.text);
	}

	@Listener({ "onAfterTaskUpdate", "onAfterTaskMove", "onAfterTaskResize", "onAfterTaskProgress" })
	public void onAfterTaskUpdateInSpace(GanttEvent e) {
		workSpaceService.updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", new ObjectId(e.id)))
				.set(Util.getBson((WorkInfo) e.task, "_id")).bson());
	}

	@Listener("onAfterTaskDelete")
	public void onAfterTaskDeleteInSpace(GanttEvent e) {
		workSpaceService.deleteWork(new ObjectId(e.id));
		System.out.println(e.text);
	}

	@Listener("onAfterLinkAdd")
	public void onAfterLinkAddInSpace(GanttEvent e) {
		WorkLinkInfo workLinkInfo = (WorkLinkInfo) e.link;
		workLinkInfo.setSpaceId(workspace.getSpace_id());
		workSpaceService.insertLink(workLinkInfo);
		System.out.println(e.text);
	}

	@Listener("onAfterLinkUpdate")
	public void onAfterLinkUpdateInSpace(GanttEvent e) {
		workSpaceService.updateLink(new FilterAndUpdate().filter(new BasicDBObject("_id", new ObjectId(e.id)))
				.set(Util.getBson((WorkLinkInfo) e.link, "_id")).bson());
		System.out.println(e.text);
	}

	@Listener("onAfterLinkDelete")
	public void onAfterLinkDeleteInSpace(GanttEvent e) {
		workSpaceService.deleteLink(new ObjectId(e.id));
		System.out.println(e.text);
	}

	@Listener("onAfterAutoSchedule")
	public void onAfterAutoScheduleInSpace(GanttEvent e) {

		System.out.println("--------------------onAfterAutoSchedule--------------------");
	}
}
