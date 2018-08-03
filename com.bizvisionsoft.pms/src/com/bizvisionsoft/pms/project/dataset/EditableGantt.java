package com.bizvisionsoft.pms.project.dataset;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.Listener;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLinkInfo;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.service.model.WorkspaceGanttData;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class EditableGantt {

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

	// @DataSet({ "项目甘特图（编辑）/initDateRange" })
	// public Date[] initDateRangeBySpace() {
	// return
	// Services.get(ProjectService.class).getPlanDateRange(project_id).toArray(new
	// Date[0]);
	// }

	// @Listener("onAfterTaskAdd")
	// public void onAfterTaskAddInSpace(GanttEvent e) {
	// WorkInfo workInfo = (WorkInfo) e.task;
	// workInfo.setSpaceId(workspace.getSpace_id());
	// if (workInfo.getParent_id() == null && workspace.getWork_id() != null) {
	// workInfo.setParent_id(workspace.getWork_id());
	// }
	// workSpaceService.insertWork(workInfo);
	// System.out.println(e.text);
	// }
	//
	// @Listener({ "onAfterTaskUpdate", "onAfterTaskMove", "onAfterTaskResize",
	// "onAfterTaskProgress" })
	// public void onAfterTaskUpdateInSpace(GanttEvent e) {
	// workSpaceService.updateWork(new FilterAndUpdate().filter(new
	// BasicDBObject("_id", new ObjectId(e.id)))
	// .set(Util.getBson((WorkInfo) e.task, "_id")).bson());
	// System.out.println(e.text);
	// }
	//
	// @Listener("onAfterTaskDelete")
	// public void onAfterTaskDeleteInSpace(GanttEvent e) {
	// workSpaceService.deleteWork(new ObjectId(e.id));
	// System.out.println(e.text);
	// }
	//
	// @Listener("onAfterLinkAdd")
	// public void onAfterLinkAddInSpace(GanttEvent e) {
	// WorkLinkInfo workLinkInfo = (WorkLinkInfo) e.link;
	// workLinkInfo.setSpaceId(workspace.getSpace_id());
	// workSpaceService.insertLink(workLinkInfo);
	// System.out.println(e.text);
	// }
	//
	// @Listener("onAfterLinkUpdate")
	// public void onAfterLinkUpdateInSpace(GanttEvent e) {
	// workSpaceService.updateLink(new FilterAndUpdate().filter(new
	// BasicDBObject("_id", new ObjectId(e.id)))
	// .set(Util.getBson((WorkLinkInfo) e.link, "_id")).bson());
	// System.out.println(e.text);
	// }
	//
	// @Listener("onAfterLinkDelete")
	// public void onAfterLinkDeleteInSpace(GanttEvent e) {
	// workSpaceService.deleteLink(new ObjectId(e.id));
	// System.out.println(e.text);
	// }
	//
	// @Listener("onAfterAutoSchedule")
	// public void onAfterAutoScheduleInSpace(GanttEvent e) {
	// ArrayList<String> ts = e.updatedTasks;
	// System.out.println(ts);
	// System.out.println("--------------------onAfterAutoSchedule--------------------");
	// }

	@Listener("save")
	public void onSave(GanttEvent e) {
		ObjectId space_id = workspace.getSpace_id();
		List<WorkInfo> tasks = new ArrayList<WorkInfo>();
		e.tasks.forEach(task -> {
			WorkInfo task2 = (WorkInfo) task;
			task2.setSpaceId(space_id);
			tasks.add(task2);
		});
		List<WorkLinkInfo> links = new ArrayList<WorkLinkInfo>();
		e.links.forEach(link -> {
			WorkLinkInfo link2 = (WorkLinkInfo) link;
			link2.setSpaceId(space_id);
			links.add(link2);
		});

		WorkspaceGanttData ganttData = new WorkspaceGanttData().setWorkspaceId(space_id).setWorks(tasks).setLinks(links)
				.setWork_id(workspace.getWork_id()).setProject_id(workspace.getProject_id());
		Result result = workSpaceService.updateGanttData(ganttData);
		// TODO 错误处理
		if (result.type != Result.TYPE_ERROR) {
			Layer.message("计划数据已保存");
			e.gantt.setDirty(false);
		} else {
			Layer.message(result.message, Layer.ICON_CANCEL);
		}
	}
}
