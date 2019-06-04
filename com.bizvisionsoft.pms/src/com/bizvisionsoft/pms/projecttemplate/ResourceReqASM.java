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
				.setActions(context.getAssembly().getActions()).setText("资源需求");

		Controls.handle(bar).height(48).left().top().right();

		Composite content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FillLayout(SWT.VERTICAL)).get();

		gantt = (GanttPart) new AssemblyContainer(content, context).setAssembly(br.getAssembly("项目模板甘特图（用于资源分配）.ganttassy"))
				.setServices(br).create().getContext().getContent();
		grid = (GridPart) new AssemblyContainer(content, context).setAssembly(br.getAssembly("项目模板资源分配表.gridassy"))
				.setServices(br).create().getContext().getContent();
		// 侦听gantt的selection
		gantt.addGanttEventListener(GanttEventCode.onTaskSelected.name(),
				l -> select((WorkInTemplate) ((GanttEvent) l).task));

		gantt.addGanttEventListener(GanttEventCode.onTaskDblClick.name(), l -> {
			WorkInTemplate work = (WorkInTemplate) ((GanttEvent) l).task;
			if (work == null) {
				Layer.message("请先选择将要分配资源的工作");
				return;
			} else if (work.isSummary()) {
				Layer.message("无需对总成型工作分配资源");
				return;
			} else if (work.isMilestone()) {
				Layer.message("无需对里程碑分配资源");
				return;
			}
			allocateResource();
		});

	}

	private void allocateResource() {
		// 显示资源选择框
		Action hrRes = new Action();
		hrRes.setName("hr");
		hrRes.setText("人力资源");
		hrRes.setImage("/img/team_w.svg");
		hrRes.setStyle("normal");

		Action eqRes = new Action();
		eqRes.setName("eq");
		eqRes.setText("设备资源");
		eqRes.setImage("/img/equipment_w.svg");
		eqRes.setStyle("normal");

		Action typedRes = new Action();
		typedRes.setName("tr");
		typedRes.setText("资源类型");
		typedRes.setImage("/img/resource_w.svg");
		typedRes.setStyle("info");

		// 弹出menu
		new ActionMenu(br).setActions(Arrays.asList(hrRes, eqRes, typedRes)).handleActionExecute("hr", a -> {
			addResource("人力资源选择器.selectorassy");
			return false;
		}).handleActionExecute("eq", a -> {
			addResource("设备设施选择器.selectorassy");
			return false;
		}).handleActionExecute("tr", a -> {
			addResource("资源类型选择器.selectorassy");
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
		// 查询
		List<ResourcePlanInTemplate> input = Services.get(ProjectTemplateService.class).listResourcePlan(work.get_id(), br.getDomain());
		grid.setViewerInput(input);
	}

}
