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

	@Listener({ "��Ŀ����ͼ���༭��/onTaskLinkBefore" })
	public void onTaskLinkBeforeBySpace(GanttEvent event) {
		WorkLinkInfo input = WorkLinkInfo.newInstance(wbsspace.getProject_id()).setSource((WorkInfo) event.linkSource)
				.setTarget((WorkInfo) event.linkTarget).setType(event.linkType);

		Editor.open("������ӹ�ϵ�༭����1��1��", context, input, (r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.addLink(wi);
		});
	}

	@Listener({ "��Ŀ����ͼ���༭��/onLinkDblClick" })
	public void onLinkDblClickBySpace(GanttEvent event) {
		List<Action> actions = new ArrayList<Action>();
		// �༭Link action
		Action editAction = new Action();
		editAction.setName("�༭link");
		editAction.setText("�༭");
		editAction.setImage("/img/edit_w.svg");
		editAction.setBundleId("com.bizvisionsoft.pms");
		editAction.setClassName("com.bizvisionsoft.pms.work.gantt.action.EditLink");
		editAction.setStyle("normal");
		editAction.setType("customized");
		actions.add(editAction);

		// ɾ��Link action
		Action deleteAction = new Action();
		deleteAction.setName("ɾ��link");
		deleteAction.setText("�༭");
		deleteAction.setImage("/img/minus_w.svg");
		deleteAction.setBundleId("com.bizvisionsoft.pms");
		deleteAction.setClassName("com.bizvisionsoft.pms.work.gantt.action.DeleteLink");
		deleteAction.setStyle("warning");
		actions.add(deleteAction);

		// ����menu
		new ActionMenu(bruiService).setAssembly(context.getAssembly()).setContext(context).setInput(event.link)
				.setActions(actions).setEvent(event).open();

	}

	@Listener({ "��Ŀ����ͼ���༭��/onTaskDblClick" })
	public void onTaskDblClickBySpace(GanttEvent event) {
		String editor;
		if (((WorkInfo) event.task).isStage()) {
			editor = "����ͼ�׶ι����༭��";
		} else if (((WorkInfo) event.task).isSummary()) {
			editor = "����ͼ�ܳɹ����༭��";
		} else {
			editor = "����ͼ�����༭��";
		}
		Editor.create(editor, context, event.task, false).setTitle(((WorkInfo) event.task).toString()).ok((r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.updateTask(wi);
		});
	}

}
