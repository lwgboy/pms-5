package com.bizvisionsoft.pms.projecttemplate;

import java.util.Arrays;

import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.MessageDialog;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.md.service.Listener;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
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

	@Listener({ "��Ŀģ�����ͼ/onTaskLinkBefore" })
	public void onTaskLinkBeforeBySpace(GanttEvent event) {
		WorkLinkInTemplate input = WorkLinkInTemplate.newInstance(template_id, br.getDomain())
				.setSource((WorkInTemplate) event.linkSource).setTarget((WorkInTemplate) event.linkTarget)
				.setType(event.linkType);

		Editor.open("������ӹ�ϵ�༭����1��1��", context, input, (r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.addLink(wi);
		});
	}

	@Listener({ "��Ŀģ�����ͼ/onLinkDblClick" })
	public void onLinkDblClickBySpace(GanttEvent event) {
		// ����menu
		new ActionMenu(br).setAssembly(context.getAssembly()).setContext(context).setInput(event.link)
				.setActions(Arrays.asList(
						// �༭Link action
						new ActionFactory().text("�༭").name("�༭link").normalStyle().img("/img/edit_w.svg")
								.type("customized").exec((e, c) -> {
									Editor.open("������ӹ�ϵ�༭����1��1��", context, ((GanttEvent) event).link,
											(r, wi) -> ganttPart.updateLink(wi));
								}).get(),
						// ɾ��Link action
						new ActionFactory().text("ɾ��").name("ɾ��link").warningStyle().img("/img/minus_w.svg")
								.exec((e, c) -> {
									if (MessageDialog.openConfirm(br.getCurrentShell(), "ɾ��", "��ȷ�Ͻ�Ҫɾ��ѡ��Ĺ�����ӹ�ϵ��")) {
										WorkLinkInTemplate link = (WorkLinkInTemplate) ((GanttEvent) event).link;
										ganttPart.deleteLink(link.getId());
									}
								}).get()))
				.setEvent(event).open();
	}

	@Listener({ "��Ŀģ�����ͼ/onTaskDblClick" })
	public void onTaskDblClickBySpace(GanttEvent event) {
		String editor = "��Ŀģ�幤���༭��";
		Editor.create(editor, context, event.task, false).setTitle(((WorkInTemplate) event.task).toString())
				.ok((r, wi) -> ganttPart.updateTask(wi));
	}

}
