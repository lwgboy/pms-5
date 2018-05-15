package com.bizvisionsoft.pms.work.gantt.event;

import java.util.ArrayList;
import java.util.List;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.md.service.Listener;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLinkInfo;

public class EventHandler {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService bruiService;

	private IWBSScope wbsspace;

	@Init
	private void init() {
		wbsspace = (IWBSScope) context.getRootInput();
	}

	@Listener({ "项目甘特图（编辑）/onTaskLinkBefore" })
	public void onTaskLinkBeforeBySpace(GanttEvent event) {
		WorkLinkInfo input = WorkLinkInfo.newInstance(wbsspace.getProject_id()).setSource((WorkInfo) event.linkSource)
				.setTarget((WorkInfo) event.linkTarget).setType(event.linkType);

		Editor.open("工作搭接关系编辑器（1对1）", context, input, (r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.addLink(wi);
		});
	}

	@Listener({ "项目甘特图（编辑）/onLinkDblClick" })
	public void onLinkDblClickBySpace(GanttEvent event) {
		List<Action> actions = new ArrayList<Action>();
		// 编辑Link action
		Action editAction = new Action();
		editAction.setName("编辑link");
		editAction.setText("编辑");
		editAction.setImage("/img/edit_w.svg");
		editAction.setBundleId("com.bizvisionsoft.pms");
		editAction.setClassName("com.bizvisionsoft.pms.work.gantt.action.EditLink");
		editAction.setStyle("normal");
		editAction.setType("customized");
		actions.add(editAction);

		// 删除Link action
		Action deleteAction = new Action();
		deleteAction.setName("删除link");
		deleteAction.setText("编辑");
		deleteAction.setImage("/img/minus_w.svg");
		deleteAction.setBundleId("com.bizvisionsoft.pms");
		deleteAction.setClassName("com.bizvisionsoft.pms.work.gantt.action.DeleteLink");
		deleteAction.setStyle("warning");
		actions.add(deleteAction);

		// 弹出menu
		new ActionMenu(bruiService).setAssembly(context.getAssembly()).setContext(context).setInput(event.link)
				.setActions(actions).setEvent(event).open();

	}

	@Listener({ "项目甘特图（编辑）/onTaskDblClick" })
	public void onTaskDblClickBySpace(GanttEvent event) {
		String editor;
		if (((WorkInfo) event.task).isStage()) {
			editor = "甘特图阶段工作编辑器";
		} else if (((WorkInfo) event.task).isSummary()) {
			editor = "甘特图总成工作编辑器";
		} else {
			editor = "甘特图工作编辑器";
		}
		Editor.create(editor, context, event.task, false).setTitle(((WorkInfo) event.task).toString()).ok((r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.updateTask(wi);
		});
	}

}
