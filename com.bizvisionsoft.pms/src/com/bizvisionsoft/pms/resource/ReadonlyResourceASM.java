package com.bizvisionsoft.pms.resource;

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
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.model.ResourceTransfer;
import com.bizvisionsoft.service.model.Work;

public class ReadonlyResourceASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private String ganttAssemblyName;

	@Inject
	private String stickerTitleText;

	@Inject
	private String type;

	private GanttPart gantt;

	private EditResourceASM grid;

	private Work work;

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		StickerTitlebar bar = Controls.handle(new StickerTitlebar(parent, null, null)).height(48).left().top().right()
				.get().setText(stickerTitleText).setActions(context.getAssembly().getActions());

		Composite content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FillLayout(SWT.VERTICAL)).get();

		gantt = (GanttPart) new AssemblyContainer(content, context).setAssembly(brui.getAssembly(ganttAssemblyName))
				.setServices(brui).create().getContext().getContent();

		ResourceTransfer rt = new ResourceTransfer();
		rt.setShowType(ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE);
		rt.setCheckTime(true);
		rt.setCanAdd(false);
		rt.setCanClose(false);
		rt.setCanEditDateValue(false);
		rt.setShowResPlan(true);
		rt.setShowResTypeInfo(true);
		rt.setShowFooter(true);

		if ("plan".equals(this.type)) {
			// 资源计划
			rt.setType(ResourceTransfer.TYPE_PLAN);
			rt.setShowConflict(true);
		} else {
			// 资源用量
			rt.setType(ResourceTransfer.TYPE_ACTUAL);
			rt.setShowResActual(true);
			rt.setShowConflict(false);
		}

		grid = (EditResourceASM) new AssemblyContainer(content, context).setAssembly(brui.getAssembly("编辑资源情况"))
				.setInput(rt).setServices(brui).create().getContext().getContent();
		// 侦听gantt的selection
		gantt.addGanttEventListener(GanttEventCode.onTaskSelected.name(), l -> select((Work) ((GanttEvent) l).task));

		bar.addListener(SWT.Selection, l -> {
			Action action = ((Action) l.data);
			if ("分配资源".equals(action.getName()) || "添加资源用量".equals(action.getName())) {
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
			} else {
				UserSession.bruiToolkit().runAction(action, l, brui, context);
			}
		});

	}

	private void select(Work work) {
		if (this.work != null && this.work.get_id().equals(work.get_id())) {
			return;
		}
		this.work = work;
		// 查询
		ResourceTransfer rt = new ResourceTransfer();
		rt.addWorkIds(work.get_id());
		rt.setShowType(ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE);
		rt.setCanAdd(false);
		rt.setCanClose(false);
		rt.setCanEditDateValue(false);
		rt.setShowResPlan(true);
		rt.setShowResTypeInfo(true);
		rt.setShowFooter(true);

		if ("plan".equals(this.type)) {
			// 资源计划
			rt.setType(ResourceTransfer.TYPE_PLAN);
			rt.setFrom(work.getPlanStart());
			rt.setTo(work.getPlanFinish());
			rt.setShowConflict(true);
			rt.setTitle(work.getFullName() + "工作资源计划用量");
		} else {
			// 资源用量
			rt.setType(ResourceTransfer.TYPE_ACTUAL);
			rt.setFrom(work.getStart_date());
			rt.setTo(work.getEnd_date());
			rt.setShowResActual(true);
			rt.setShowConflict(false);
			rt.setTitle(work.getFullName() + "工作资源实际用量");
		}
		grid.setResourceTransfer(rt);
	}

}
