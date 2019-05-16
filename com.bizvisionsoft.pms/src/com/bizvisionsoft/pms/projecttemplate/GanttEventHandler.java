package com.bizvisionsoft.pms.projecttemplate;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.MessageDialog;

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
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.WBSModule;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.service.model.WorkLinkInTemplate;

public class GanttEventHandler {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private ObjectId template_id;

	private GanttPart ganttPart;

	@Init
	private void init() {
		Object input = context.getInput();
		if (input instanceof WBSModule) {
			template_id = ((WBSModule) input).get_id();
		} else {
			template_id = context.getRootInput(ProjectTemplate.class, false).get_id();
		}
		ganttPart = (GanttPart) context.getContent();
	}

	@Listener({ "项目模板甘特图/onTaskLinkBefore" })
	public void onTaskLinkBeforeBySpace(GanttEvent event) {
		WorkLinkInTemplate input = WorkLinkInTemplate.newInstance(template_id)
				.setSource((WorkInTemplate) event.linkSource).setTarget((WorkInTemplate) event.linkTarget)
				.setType(event.linkType);

		Editor.open("工作搭接关系编辑器（1对1）", context, input, (r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.addLink(wi);
		});
	}

	@Listener({ "项目模板甘特图/onLinkDblClick" })
	public void onLinkDblClickBySpace(GanttEvent event) {
		List<Action> actions = new ArrayList<Action>();
		// 编辑Link action
		Action editAction = new Action();
		editAction.setName("编辑link");
		editAction.setText("编辑");
		editAction.setImage("/img/edit_w.svg");
		editAction.setStyle("normal");
		editAction.setType("customized");
		actions.add(editAction);

		// 删除Link action
		Action deleteAction = new Action();
		deleteAction.setName("删除link");
		deleteAction.setText("删除");
		deleteAction.setImage("/img/minus_w.svg");
		deleteAction.setStyle("warning");
		actions.add(deleteAction);

		// 弹出menu
		new ActionMenu(br).setAssembly(context.getAssembly()).setContext(context).setInput(event.link)
				.setActions(actions).setEvent(event).handleActionExecute("编辑link", t -> {
					Editor.open("工作搭接关系编辑器（1对1）", context, ((GanttEvent) event).link,
							(r, wi) -> ganttPart.updateLink(wi));
					return false;
				}).handleActionExecute("删除link", t -> {
					if (MessageDialog.openConfirm(br.getCurrentShell(), "删除", "请确认将要删除选择的工作搭接关系。")) {
						WorkLinkInTemplate link = (WorkLinkInTemplate) ((GanttEvent) event).link;
						ganttPart.deleteLink(link.getId());
					}
					return false;
				}).open();

	}

	@Listener({ "项目模板甘特图/onTaskDblClick" })
	public void onTaskDblClickBySpace(GanttEvent event) {
		String editor = "项目模板工作编辑器";
		Editor.create(editor, context, event.task, false).setTitle(((WorkInTemplate) event.task).toString())
				.ok((r, wi) -> ganttPart.updateTask(wi));
	}

}
