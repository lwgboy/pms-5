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
import com.bizvisionsoft.service.model.Project;
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
		//�༭Link action
		Action editAction = new Action();
		actions.add(editAction);
		

		//ɾ��Link action
		Action deleteAction = new Action();
		actions.add(deleteAction);
		
		// ����menu
		new ActionMenu(bruiService).setContext(context).setInput(event.link).setActions(actions).setEvent(event).open();
				
		Editor.open("������ӹ�ϵ�༭����1��1��", context, event.link, (r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.updateLink(wi);
		});
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
