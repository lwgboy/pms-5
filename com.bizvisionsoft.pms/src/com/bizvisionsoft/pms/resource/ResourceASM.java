package com.bizvisionsoft.pms.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizivisionsoft.widgets.gantt.GanttEventCode;
import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.ResourceTransfer;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class ResourceASM {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private Boolean editable;

	@Inject
	private String type;

	private GanttPart gantt;

	private EditResourceASM grid;

	private Work work;

	@CreateUI
	public void createUI(Composite parent) {

		String ganttAssemblyName;

		String stickerTitleText;

		String gridExportActionText;

		if ("plan".equals(this.type)) {
			ganttAssemblyName = "项目甘特图（资源计划分配）.ganttassy";
			stickerTitleText = "资源计划";
			gridExportActionText = "资源计划";
		} else {
			ganttAssemblyName = "项目甘特图（资源实际分配）.ganttassy";
			stickerTitleText = "资源用量";
			gridExportActionText = "资源用量";
		}
		parent.setLayout(new FormLayout());

		StickerTitlebar bar = Controls.handle(new StickerTitlebar(parent, null, null)).height(48).left().top().right()
				.get().setText(stickerTitleText).setActions(context.getAssembly().getActions());

		Composite content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FillLayout(SWT.VERTICAL)).get();

		// 修改控件title，以便在导出按钮进行显示
		gantt = (GanttPart) new AssemblyContainer(content, context).setAssembly(br.getAssembly(ganttAssemblyName))
				.setServices(br).create().getContext().getContent();

		gantt.setExportActionText("甘特图");
		ResourceTransfer rt = new ResourceTransfer();
		rt.setShowType(ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE);
		rt.setCheckTime(true);
		rt.setCanAdd(false);
		rt.setCanDelete(Boolean.TRUE.equals(editable));
		rt.setCanEditDateValue(Boolean.TRUE.equals(editable));
		rt.setCanClose(false);
		rt.setShowResTypeInfo(true);
		rt.setShowFooter(true);

		if ("plan".equals(this.type)) {
			rt.setType(ResourceTransfer.TYPE_PLAN);
			rt.setShowResPlan(true);
			rt.setShowConflict(true);
		} else {
			rt.setType(ResourceTransfer.TYPE_ACTUAL);
			rt.setShowResActual(true);
			rt.setShowConflict(false);
		}

		// 修改控件title，以便在导出按钮进行显示
		grid = (EditResourceASM) new AssemblyContainer(content, context).setAssembly(br.getAssembly("编辑资源情况.assy")).setInput(rt)
				.setServices(br).create().getContext().getContent();
		grid.setExportActionText(gridExportActionText);
		// 侦听gantt的selection
		gantt.addGanttEventListener(GanttEventCode.onTaskSelected.name(), l -> select((Work) ((GanttEvent) l).task));

		bar.addListener(SWT.Selection, l -> {
			Action action = ((Action) l.data);
			if ("分配资源".equals(action.getName()) || "添加资源用量".equals(action.getName())) {
				// 资源计划按钮
				if (this.work == null) {
					Layer.message("请先选择将要" + action.getName() + "的工作");
					return;
				} else if (this.work.isSummary()) {
					Layer.message("无需对总成型工作" + action.getName());
					return;
				} else if (this.work.isMilestone()) {
					Layer.message("无需对里程碑" + action.getName());
					return;
				}
				if (Boolean.TRUE.equals(editable))
					allocateResource();
			} else {
				UserSession.bruiToolkit().runAction(action, l, br, context);
			}
		});
		if (Boolean.TRUE.equals(editable)) {
			gantt.addGanttEventListener(GanttEventCode.onTaskDblClick.name(), l -> {
				Work work = (Work) ((GanttEvent) l).task;
				if (work != null && !work.isSummary() && !work.isMilestone()) {
					allocateResource();
				}
			});
			Layer.message("提示： 您可以双击叶子任务选择要添加的资源");
		}
	}

	private void allocateResource() {
		// 弹出menu
		new ActionMenu(br).setActions(Arrays.asList(
				//
				new ActionFactory().name("hr").text("人力资源").img("/img/team_w.svg").normalStyle()
						.exec((e, c) -> addResource("人力资源选择器.selectorassy")).get(),
				//
				new ActionFactory().name("eq").text("设备资源").img("/img/equipment_w.svg").normalStyle()
						.exec((e, c) -> addResource("设备设施选择器.selectorassy")).get(),
				//
				new ActionFactory().name("tr").text("资源类型").img("/img/resource_w.svg").infoStyle()
						.exec((e, c) -> addResource("资源类型选择器.selectorassy")).get()))
				.open();
	}

	private void addResource(String selectorId) {
		Selector.open(selectorId, context, null, l -> {
			List<ResourceAssignment> resa = new ArrayList<ResourceAssignment>();
			l.forEach(o -> {
				ResourceAssignment ra = new ResourceAssignment().setTypedResource(o).setWork_id(work.get_id());
				if (o instanceof ResourceType) {
					ra.qty = inputQty();
				} else {
					ra.qty = 1;
				}
				// 资源计划
				if ("plan".equals(this.type)) {
					ra.from = work.getPlanStart();
					ra.to = work.getPlanFinish();
				} else {
					// 资源用量
					ra.from = work.getStart_date();
					ra.to = work.getEnd_date();
				}

				resa.add(ra);
			});
			if ("plan".equals(this.type))
				// 资源计划
				Services.get(WorkService.class).addResourcePlan(resa, br.getDomain());
			else
				// 资源用量
				Services.get(WorkService.class).addResourceActual(resa, br.getDomain());
			grid.doRefresh();
		});

	}

	private int inputQty() {
		InputDialog id = new InputDialog(br.getCurrentShell(), "编辑资源数量", "请输入资源数量", "1", t -> {
			if (t.trim().isEmpty())
				return "请输入资源数量";
			try {
				Integer.parseInt(t);
			} catch (Exception e) {
				return "输入的类型错误";
			}
			return null;
		});
		if (InputDialog.OK == id.open()) {
			return Integer.parseInt(id.getValue());
		} else {
			return 1;
		}
	}

	private void select(Work work) {
		if (this.work != null && this.work.get_id().equals(work.get_id()))
			return;

		this.work = work;
		// 查询
		ResourceTransfer rt = new ResourceTransfer();
		rt.addWorkIds(work.get_id());
		rt.setShowType(ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE);
		rt.setCanDelete(Boolean.TRUE.equals(editable));
		rt.setCanEditDateValue(Boolean.TRUE.equals(editable));
		rt.setCanClose(false);
		rt.setShowResTypeInfo(true);
		rt.setShowFooter(true);
		if ("plan".equals(this.type)) {
			// 资源计划
			rt.setType(ResourceTransfer.TYPE_PLAN);
			rt.setFrom(work.getPlanStart());
			rt.setTo(work.getPlanFinish());
			rt.setCanAdd(false);
			rt.setShowResPlan(true);
			rt.setShowConflict(true);
			rt.setTitle(work.getFullName() + "工作资源计划用量");
		} else {
			// 资源用量
			rt.setType(ResourceTransfer.TYPE_ACTUAL);
			rt.setFrom(work.getStart_date());
			rt.setTo(work.getEnd_date());
			rt.setCanAdd(work.getActualStart() != null);
			rt.setShowResActual(true);
			rt.setShowConflict(false);
			rt.setTitle(work.getFullName() + "工作资源实际用量");
		}
		grid.setResourceTransfer(rt);

	}

}
