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
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLink;
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

	@Listener({ "��Ŀ����ͼ/onTaskLinkBefore" })
	public void onTaskLinkBefore(GanttEvent event) {
		WorkLink input = WorkLink.newInstance(wbsspace.getProject_id()).setSource((Work) event.linkSource)
				.setTarget((Work) event.linkTarget).setType(event.linkType);

		Editor.open("������ӹ�ϵ�༭����1��1��", context, input, (r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.addLink(wi);
		});
	}

	@Listener({ "��Ŀ����ͼ/onLinkDblClick" })
	public void onLinkDblClick(GanttEvent event) {
		Editor.open("������ӹ�ϵ�༭����1��1��", context, event.link, (r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.updateLink(wi);
		});
	}

	@Listener({ "��Ŀ����ͼ/onTaskDblClick" })
	public void onTaskDblClick(GanttEvent event) {
		String editor;
		if (((Work) event.task).isStage()) {
			editor = "����ͼ�׶ι����༭��";
		} else if (((Work) event.task).isSummary()) {
			editor = "����ͼ�ܳɹ����༭��";
		} else {
			editor = "����ͼ�����༭��";
		}
		Editor.create(editor, context, event.task, false).setTitle(((Work) event.task).toString()).ok((r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.updateTask(wi);
		});
	}

	@Listener({ "��Ŀ����ͼ(�༭)/onTaskLinkBefore" })
	public void onTaskLinkBeforeBySpace(GanttEvent event) {
		WorkLinkInfo input = WorkLinkInfo.newInstance(wbsspace.getProject_id()).setSource((WorkInfo) event.linkSource)
				.setTarget((WorkInfo) event.linkTarget).setType(event.linkType);

		Editor.open("������ӹ�ϵ�༭����1��1��", context, input, (r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.addLink(wi);
		});
	}

	@Listener({ "��Ŀ����ͼ(�༭)/onLinkDblClick" })
	public void onLinkDblClickBySpace(GanttEvent event) {
		List<Action> actions = new ArrayList<Action>();
		// "id": "162516cb740",
		// "name": "�༭",
		// "text": "�༭",
		// "image": "/img/edit_w.svg",
		// "bundleId": "com.bizvisionsoft.pms",
		// "className": "com.bizvisionsoft.pms.work.gantt.action.EditTask",
		// "openContent": false,
		// "propagate": false,
		// "forceText": true,
		// "style": "normal",
		// "editorAssemblyEditable": false,
		// "objectBehavier": false,
		// "type": "customized"
		// �༭Link action
		Action editAction = new Action();
		editAction.setName("�༭link");
		editAction.setName("�༭");
		editAction.setImage("/img/edit_w.svg");
		editAction.setBundleId("com.bizvisionsoft.pms");
		editAction.setClassName("com.bizvisionsoft.pms.work.gantt.action.EditLink");
		editAction.setOpenContent(false);
		editAction.setPropagate(false);
		editAction.setForceText(true);
		editAction.setStyle("normal");
		editAction.setEditorAssemblyEditable(false);
		editAction.setObjectBehavier(false);
		editAction.setType("customized");
		actions.add(editAction);

		// "id": "162516ce0ff",
		// "name": "ɾ������",
		// "text": "ɾ��",
		// "image": "/img/minus_w.svg",
		// "bundleId": "com.bizvisionsoft.pms",
		// "className": "com.bizvisionsoft.pms.work.gantt.action.DeleteTask",
		// "openContent": false,
		// "propagate": false,
		// "forceText": true,
		// "style": "warning",
		// "editorAssemblyEditable": false,
		// "objectBehavier": true,
		// "type": "customized"
		// ɾ��Link action
		Action deleteAction = new Action();
		deleteAction.setName("ɾ��link");
		deleteAction.setName("�༭");
		deleteAction.setImage("/img/minus_w.svg");
		deleteAction.setBundleId("com.bizvisionsoft.pms");
		deleteAction.setClassName("com.bizvisionsoft.pms.work.gantt.action.DeleteLink");
		deleteAction.setOpenContent(false);
		deleteAction.setPropagate(false);
		deleteAction.setForceText(true);
		deleteAction.setStyle("warning");
		deleteAction.setEditorAssemblyEditable(false);
		deleteAction.setObjectBehavier(false);
		deleteAction.setType("customized");
		actions.add(deleteAction);

		// ����menu
		// TODO ����ʱȱ������
		new ActionMenu(bruiService).setAssembly(context.getAssembly()).setContext(context).setInput(event.link)
				.setActions(actions).setEvent(event).open();

		// Editor.open("������ӹ�ϵ�༭����1��1��", context, event.link, (r, wi) -> {
		// GanttPart content = (GanttPart) context.getContent();
		// content.updateLink(wi);
		// });
	}

	@Listener({ "��Ŀ����ͼ(�༭)/onTaskDblClick" })
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
