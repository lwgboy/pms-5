package com.bizvisionsoft.pms.problem.gantt;

import java.util.List;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.Export;
import com.bizvisionsoft.annotations.md.service.Listener;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.model.ProblemActionInfo;
import com.bizvisionsoft.service.model.ProblemActionLinkInfo;
import com.bizvisionsoft.serviceconsumer.Services;

public class ProblemActionGantt {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private Problem problem;

	private ProblemService service;
	
	@Init
	private void init() {
		problem = context.getRootInput(Problem.class,false);
		service = Services.get(ProblemService.class);
	}


	@DataSet("data")
	public List<ProblemActionInfo> listActions() {
		return service.listGanttActions(problem.get_id(), br.getDomain());
	}

	@DataSet("links")
	public List<ProblemActionLinkInfo> listLinks() {
		return service.listGanttActionLinks(problem.get_id(), br.getDomain());
	}



	@Listener("save")
	public void onSave(GanttEvent e) {
//		ObjectId space_id = workspace.getSpace_id();
//		List<WorkInfo> tasks = new ArrayList<WorkInfo>();
//		e.tasks.forEach(task -> {
//			WorkInfo task2 = (WorkInfo) task;
//			task2.setSpaceId(space_id);
//			tasks.add(task2);
//		});
//		List<WorkLinkInfo> links = new ArrayList<WorkLinkInfo>();
//		e.links.forEach(link -> {
//			WorkLinkInfo link2 = (WorkLinkInfo) link;
//			link2.setSpaceId(space_id);
//			links.add(link2);
//		});
//
//		WorkspaceGanttData ganttData = new WorkspaceGanttData().setWorkspaceId(space_id).setWorks(tasks).setLinks(links)
//				.setWork_id(workspace.getWork_id()).setProject_id(workspace.getProject_id());
//		try {
//			Result result = workSpaceService.updateGanttData(ganttData);
//			// TODO 错误处理
//			if (result.type != Result.TYPE_ERROR) {
//				Layer.message("计划数据已保存");
//				e.gantt.setDirty(false);
//			} else {
//				e.doit = false;// 设置为false,禁止保存完后的callback,例如，关闭页面等
//				Layer.message(result.message, Layer.ICON_ERROR);
//			}
//		} catch (Exception ex) {
//			e.doit = false;// 设置为false,禁止保存完后的callback,例如，关闭页面等
//			Layer.message(ex.getMessage(), Layer.ICON_ERROR);
//		}
	}

	@Export(Export.DEFAULT)
	public void export() {
//		GanttPart ganttPart = (GanttPart) context.getContent();
//		if (ganttPart.isDirty()) {
//			ganttPart.save((t, l) -> export());
//			return;
//		}
//
//		ObjectId pj_id = workspace.getProject_id();
//		String name = Services.get(ProjectService.class).get(pj_id).getProjectName();
//		try {
//			new MPPExporter<WorkInfo, WorkLinkInfo>().setTasks(dataInSpace()).setLinks(linksInSpace()).setProjectName(name)
//					.setTaskConvertor((w, p, m) -> {
//						Task t = Optional.ofNullable(w.getParent_id()).map(_id -> {
//							Task parentTask = m.get(_id);
//							return parentTask.addTask();
//						}).orElseGet(() -> p.addTask());
//
//						m.put(w.get_id(), t);
//						t.setName(w.getText());
//						t.setNotes(w.getFullName());
//						Date planStart = w.getPlanStart();
//						t.setStart(planStart);
//						Date planFinish = w.getPlanFinish();
//						t.setFinish(planFinish);
//						t.setDuration(Duration.getInstance(w.getPlanDuration(), TimeUnit.DAYS));
//						t.setConstraintDate(planStart);
//						t.setConstraintType(ConstraintType.MUST_START_ON);
//
//					}).setLinkConvertor((w, taskMap) -> {
//						String type = w.getType();
//						RelationType rt;
//						if ("FF".equals(type)) {
//							rt = RelationType.FINISH_FINISH;
//						} else if ("SS".equals(type)) {
//							rt = RelationType.START_START;
//						} else if ("SF".equals(type)) {
//							rt = RelationType.START_FINISH;
//						} else {
//							rt = RelationType.FINISH_START;
//						}
//						ObjectId sourceId = w.getSourceId();
//						ObjectId targetId = w.getTargetId();
//						Task src = taskMap.get(sourceId);
//						Task tgt = taskMap.get(targetId);
//						tgt.addPredecessor(src, rt, Duration.getInstance(w.getLag(), TimeUnit.DAYS));
//					}).export();
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
	}
}
