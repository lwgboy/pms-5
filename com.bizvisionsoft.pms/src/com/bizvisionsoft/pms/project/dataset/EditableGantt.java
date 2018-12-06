package com.bizvisionsoft.pms.project.dataset;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.Export;
import com.bizvisionsoft.annotations.md.service.Listener;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.exporter.MPPExporter;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLinkInfo;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.service.model.WorkspaceGanttData;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

import net.sf.mpxj.Duration;
import net.sf.mpxj.RelationType;
import net.sf.mpxj.Task;
import net.sf.mpxj.TimeUnit;

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
		return workSpaceService.createTaskDataSet(
				new BasicDBObject("space_id", workspace.getSpace_id()).append("_id", new BasicDBObject("$ne", workspace.getWork_id())));
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
	// }
	//
	// @Listener({ "onAfterTaskUpdate", "onAfterTaskMove", "onAfterTaskResize",
	// "onAfterTaskProgress" })
	// public void onAfterTaskUpdateInSpace(GanttEvent e) {
	// workSpaceService.updateWork(new FilterAndUpdate().filter(new
	// BasicDBObject("_id", new ObjectId(e.id)))
	// .set(BsonTools.getBson((WorkInfo) e.task, "_id")).bson());
	// }
	//
	// @Listener("onAfterTaskDelete")
	// public void onAfterTaskDeleteInSpace(GanttEvent e) {
	// workSpaceService.deleteWork(new ObjectId(e.id));
	// }
	//
	// @Listener("onAfterLinkAdd")
	// public void onAfterLinkAddInSpace(GanttEvent e) {
	// WorkLinkInfo workLinkInfo = (WorkLinkInfo) e.link;
	// workLinkInfo.setSpaceId(workspace.getSpace_id());
	// workSpaceService.insertLink(workLinkInfo);
	// }
	//
	// @Listener("onAfterLinkUpdate")
	// public void onAfterLinkUpdateInSpace(GanttEvent e) {
	// workSpaceService.updateLink(new FilterAndUpdate().filter(new
	// BasicDBObject("_id", new ObjectId(e.id)))
	// .set(BsonTools.getBson((WorkLinkInfo) e.link, "_id")).bson());
	// }
	//
	// @Listener("onAfterLinkDelete")
	// public void onAfterLinkDeleteInSpace(GanttEvent e) {
	// workSpaceService.deleteLink(new ObjectId(e.id));
	// }
	//
	// @Listener("onAfterAutoSchedule")
	// public void onAfterAutoScheduleInSpace(GanttEvent e) {
	// ArrayList<String> ts = e.updatedTasks;
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
		try {
			Result result = workSpaceService.updateGanttData(ganttData);
			// TODO 错误处理
			if (result.type != Result.TYPE_ERROR) {
				Layer.message("计划数据已保存");
				e.gantt.setDirty(false);
			} else {
				e.doit = false;// 设置为false,禁止保存完后的callback,例如，关闭页面等
				Layer.message(result.message, Layer.ICON_CANCEL);
			}
		} catch (Exception ex) {
			e.doit = false;// 设置为false,禁止保存完后的callback,例如，关闭页面等
			Layer.message(ex.getMessage(), Layer.ICON_CANCEL);
		}
	}

	@Export(Export.DEFAULT)
	public void export() {
		GanttPart ganttPart = (GanttPart) context.getContent();
		if (ganttPart.isDirty()) {
			ganttPart.save((t, l) -> export());
			return;
		}

		ObjectId pj_id = workspace.getProject_id();
		String name = Services.get(ProjectService.class).get(pj_id).getProjectName();
		try {
			new MPPExporter<WorkInfo, WorkLinkInfo>().setTasks(dataInSpace()).setLinks(linksInSpace()).setProjectName(name)
					.setTaskConvertor((w, t, m) -> {
						m.put(w.get_id(), t);
						t.setName(w.getText());
						t.setNotes(w.getFullName());
						Date planStart = w.getPlanStart();
						t.setStart(planStart);
						Date planFinish = w.getPlanFinish();
						t.setFinish(planFinish);
						t.setDuration(Duration.getInstance(w.getPlanDuration(), TimeUnit.DAYS));
						ObjectId parent_id = w.getParent_id();
						if (parent_id != null) {
							Task parentTask = m.get(parent_id);
							parentTask.addChildTask(t);
						}
					}).setLinkConvertor((w, taskMap) -> {
						String type = w.getType();
						RelationType rt;
						if ("FF".equals(type)) {
							rt = RelationType.FINISH_FINISH;
						} else if ("SS".equals(type)) {
							rt = RelationType.START_START;
						} else if ("SF".equals(type)) {
							rt = RelationType.START_FINISH;
						} else {
							rt = RelationType.FINISH_START;
						}
						ObjectId sourceId = w.getSourceId();
						ObjectId targetId = w.getTargetId();
						Task src = taskMap.get(sourceId);
						Task tgt = taskMap.get(targetId);
						tgt.addPredecessor(src, rt, Duration.getInstance(w.getLag(), TimeUnit.DAYS));
					}).export();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
