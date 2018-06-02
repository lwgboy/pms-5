package com.bizvisionsoft.pms.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizivisionsoft.widgets.gantt.GanttEventCode;
import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.session.UserSession;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class ResourcePlanASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private GanttPart gantt;

	private GridPart grid;

	private Work work;

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		StickerTitlebar bar = new StickerTitlebar(parent, null, null).setText("��Դ�ƻ�")
				.setActions(context.getAssembly().getActions());
		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 48;

		Composite content = UserSession.bruiToolkit().newContentPanel(parent);
		fd = new FormData();
		content.setLayoutData(fd);
		fd.left = new FormAttachment(0, 12);
		fd.top = new FormAttachment(bar, 12);
		fd.right = new FormAttachment(100, -12);
		fd.bottom = new FormAttachment(100, -12);
		content.setLayout(new FillLayout(SWT.VERTICAL));

		gantt = (GanttPart) new AssemblyContainer(content, context).setAssembly(brui.getAssembly("��Ŀ����ͼ����Դ���䣩"))
				.setServices(brui).create().getContext().getContent();
		grid = (GridPart) new AssemblyContainer(content, context).setAssembly(brui.getAssembly("��Դ�����"))
				.setServices(brui).create().getContext().getContent();
		// ����gantt��selection
		gantt.addGanttEventListener(GanttEventCode.onTaskSelected.name(), l -> select((Work) ((GanttEvent) l).task));

		bar.addListener(SWT.Selection, l -> {
			if ("������Դ".equals(((Action) l.data).getName())) {
				if (this.work == null) {
					Layer.message("����ѡ��Ҫ������Դ�Ĺ�����");
					return;
				} else if (this.work.isSummary()) {
					Layer.message("������ܳ��͹���������Դ��");
					return;
				}
				allocateResource();
			}
		});

		gantt.addGanttEventListener(GanttEventCode.onTaskDblClick.name(), l -> {
			Work work = (Work) ((GanttEvent) l).task;
			if (work != null && !work.isSummary()) {
				allocateResource();
			}
		});

		grid.getViewer().getGrid().addListener(SWT.Selection, l -> {
			if ("conflict".equals(l.text)) {
				openResourceConflict((ResourcePlan) l.item.getData());
			}
		});

	}

	private void openResourceConflict(ResourcePlan rp) {
		brui.openContent(brui.getAssembly("�����һ��Դ��ͻ"), rp);
	}
	

	private void allocateResource() {
		// ��ʾ��Դѡ���
		Action hrRes = new Action();
		hrRes.setName("hr");
		hrRes.setText("������Դ");
		hrRes.setImage("/img/team_w.svg");
		hrRes.setStyle("normal");

		Action eqRes = new Action();
		eqRes.setName("eq");
		eqRes.setText("�豸��Դ");
		eqRes.setImage("/img/equipment_w.svg");
		eqRes.setStyle("normal");

		Action typedRes = new Action();
		typedRes.setName("tr");
		typedRes.setText("��Դ����");
		typedRes.setImage("/img/resource_w.svg");
		typedRes.setStyle("info");

		// ����menu
		new ActionMenu(brui).setActions(Arrays.asList(hrRes, eqRes, typedRes)).handleActionExecute("hr", a -> {
			addResource("������Դѡ����");
			return false;
		}).handleActionExecute("eq", a -> {
			addResource("�豸��ʩѡ����");
			return false;
		}).handleActionExecute("tr", a -> {
			addResource("��Դ����ѡ����");
			return false;
		}).open();
	}

	private void addResource(String selectorId) {
		Selector.open(selectorId, context, null, l -> {
			List<ResourceAssignment> resa = new ArrayList<ResourceAssignment>();
			l.forEach(o -> resa.add(new ResourceAssignment().setTypedResource(o).setWork_id(work.get_id())));
			Services.get(WorkService.class).addResourcePlan(resa);

			grid.setViewerInput(Services.get(WorkService.class).listResourcePlan(work.get_id()));
		});
	}

	private void select(Work work) {
		if (this.work != null && this.work.get_id().equals(work.get_id())) {
			return;
		}
		this.work = work;
		// ��ѯ
		List<ResourcePlan> input = Services.get(WorkService.class).listResourcePlan(work.get_id());
		grid.setViewerInput(input);
	}

}