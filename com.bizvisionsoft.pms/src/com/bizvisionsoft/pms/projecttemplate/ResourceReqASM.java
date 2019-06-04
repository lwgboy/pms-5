package com.bizvisionsoft.pms.projecttemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
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
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.ResourcePlanInTemplate;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.serviceconsumer.Services;

public class ResourceReqASM {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	private GanttPart gantt;

	private GridPart grid;

	private WorkInTemplate work;

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		StickerTitlebar bar = new StickerTitlebar(parent, null, null)
				.setActions(context.getAssembly().getActions()).setText("��Դ����");

		Controls.handle(bar).height(48).left().top().right();

		Composite content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FillLayout(SWT.VERTICAL)).get();

		gantt = (GanttPart) new AssemblyContainer(content, context).setAssembly(br.getAssembly("��Ŀģ�����ͼ��������Դ���䣩.ganttassy"))
				.setServices(br).create().getContext().getContent();
		grid = (GridPart) new AssemblyContainer(content, context).setAssembly(br.getAssembly("��Ŀģ����Դ�����.gridassy"))
				.setServices(br).create().getContext().getContent();
		// ����gantt��selection
		gantt.addGanttEventListener(GanttEventCode.onTaskSelected.name(),
				l -> select((WorkInTemplate) ((GanttEvent) l).task));

		gantt.addGanttEventListener(GanttEventCode.onTaskDblClick.name(), l -> {
			WorkInTemplate work = (WorkInTemplate) ((GanttEvent) l).task;
			if (work == null) {
				Layer.message("����ѡ��Ҫ������Դ�Ĺ���");
				return;
			} else if (work.isSummary()) {
				Layer.message("������ܳ��͹���������Դ");
				return;
			} else if (work.isMilestone()) {
				Layer.message("�������̱�������Դ");
				return;
			}
			allocateResource();
		});

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
		new ActionMenu(br).setActions(Arrays.asList(hrRes, eqRes, typedRes)).handleActionExecute("hr", a -> {
			addResource("������Դѡ����.selectorassy");
			return false;
		}).handleActionExecute("eq", a -> {
			addResource("�豸��ʩѡ����.selectorassy");
			return false;
		}).handleActionExecute("tr", a -> {
			addResource("��Դ����ѡ����.selectorassy");
			return false;
		}).open();
	}

	private void addResource(String selectorId) {
		Selector.open(selectorId, context, null, l -> {
			List<ResourceAssignment> resa = new ArrayList<ResourceAssignment>();
			l.forEach(o -> resa.add(new ResourceAssignment().setTypedResource(o).setWork_id(work.get_id())));
			Services.get(ProjectTemplateService.class).addResourcePlan(resa, br.getDomain());

			List<ResourcePlanInTemplate> input = Services.get(ProjectTemplateService.class)
					.listResourcePlan(work.get_id(), br.getDomain());
			grid.setViewerInput(input);
		});
	}

	private void select(WorkInTemplate work) {
		if (this.work != null && this.work.get_id().equals(work.get_id())) {
			return;
		}
		this.work = work;
		// ��ѯ
		List<ResourcePlanInTemplate> input = Services.get(ProjectTemplateService.class).listResourcePlan(work.get_id(), br.getDomain());
		grid.setViewerInput(input);
	}

}
