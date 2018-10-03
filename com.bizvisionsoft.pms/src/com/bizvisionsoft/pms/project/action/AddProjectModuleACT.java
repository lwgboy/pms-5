package com.bizvisionsoft.pms.project.action;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.WBSModule;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLinkInTemplate;
import com.bizvisionsoft.service.model.WorkLinkInfo;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.service.tools.Checker;
import com.bizvisionsoft.serviceconsumer.Services;

public class AddProjectModuleACT {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		WorkInfo parent;
		if("onGridHeaderMenuClick".equals(event.text)) {
			parent = null;
		}else {
			StructuredSelection selection = context.getSelection();
			parent = (WorkInfo) selection.getFirstElement();
		}
		Selector.open("WBS模块选择器", context, null, r -> {
			GanttPart content = (GanttPart) context.getContent();
			Object input = context.getInput();
			if (input == null || input instanceof Workspace) {
				input = context.getRootInput();
			}
			append(content, input, (WBSModule) r.get(0), parent);
		});

	}

	private void append(GanttPart gantt, Object input, WBSModule module, WorkInfo parent) {
		ObjectId parentId = null;
		ObjectId projectId = null;
		ObjectId space_id = null;
		Date planStartInParent = null;
		boolean stageEnable = false;
		if (input instanceof Project) {
			projectId = ((Project) input).get_id();
			space_id = ((Project) input).getWorkspace().getSpace_id();
			stageEnable = Boolean.TRUE.equals(((Project) input).isStageEnable());
			planStartInParent = ((Project) input).getPlanStart();
		} else if (input instanceof Work) {
			projectId = ((Work) input).getProject_id();
			parentId = ((Work) input).get_id();
			space_id = ((Work) input).getWorkspace().getSpace_id();
			planStartInParent = ((Work) input).getPlanStart();
		}

		if (parent != null) {
			planStartInParent = parent.getPlanStart();
		}

		String var = module.getVar();
		Map<String, String> varMap = new HashMap<String, String>();
		if (!Checker.isNotAssigned(var)) {
			String[] v = var.trim().split(";");
			for (int i = 0; i < v.length; i++) {
				InputDialog id = new InputDialog(br.getCurrentShell(), "WBS模块参数", v[i], "", null);
				if (id.open() == InputDialog.OK) {
					varMap.put(v[i], id.getValue());
				}
			}
		}
		List<WorkInTemplate> works = Services.get(ProjectTemplateService.class).listWorks(module.get_id());
		Map<ObjectId, WorkInfo> idMap = new HashMap<ObjectId, WorkInfo>();
		for (int i = 0; i < works.size(); i++) {
			WorkInfo work = new WorkInfo();
			AUtil.simpleCopy(works.get(i), work);

			long duration = planStartInParent.getTime() - works.get(i).getPlanStart().getTime();

			Date planStart = works.get(i).getPlanStart();
			Date planFinish = works.get(i).getPlanFinish();

			Calendar planStartCal = Calendar.getInstance();
			planStartCal.setTimeInMillis(planStart.getTime() + duration);

			Calendar planFinishCal = Calendar.getInstance();
			planFinishCal.setTimeInMillis(planFinish.getTime() + duration);

			work.setPlanStart(planStartCal.getTime());
			work.setPlanFinish(planFinishCal.getTime());
			// 更改id
			ObjectId newId = new ObjectId();
			idMap.put(work.get_id(), work);
			work.set_id(newId);

			String text = updateName(work.getText(), varMap);
			work.setText(text);
			text = updateName(work.getFullName(), varMap);
			work.setFullName(text);

			ObjectId parent_id = work.getParent_id();
			if (parent_id != null) {
				work.setParent_id(idMap.get(parent_id).get_id());
			} else {
				if (parent != null) {
					work.setParent_id(parent.get_id());
				} else if (parentId != null) {
					work.setParent_id(parentId);
				}
				if (stageEnable) {
					work.setStatus(ProjectStatus.Created);
					work.setStage(true);
				}
			}

			if (projectId != null) {
				work.setProject_id(projectId);
			}

			if (space_id != null) {
				work.setSpaceId(space_id);
			}
			
			gantt.addTask(work);
		}

		List<WorkLinkInTemplate> links = Services.get(ProjectTemplateService.class).listLinks(module.get_id());
		for (int i = 0; i < links.size(); i++) {
			WorkLinkInfo link = new WorkLinkInfo();
			AUtil.simpleCopy(links.get(i), link);
			link.set_id(new ObjectId());

			ObjectId src = links.get(i).getSourceId();
			link.setSource(idMap.get(src));

			ObjectId tgt = links.get(i).getTargetId();
			link.setTarget(idMap.get(tgt));

			if (projectId != null) {
				link.setProject_id(projectId);
			}

			if (space_id != null) {
				link.setSpaceId(space_id);
			}

			gantt.addLink(link);
		}
	}

	private String updateName(String name, Map<String, String> varMap) {
		Iterator<String> iter = varMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			String value = varMap.get(key);
			name = name.replaceAll("\\[" + key + "\\]", value);
		}
		return name;
	}

}
