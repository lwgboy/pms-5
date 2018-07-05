package com.bizvisionsoft.pms.resource;

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
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.service.model.ResourceTransfer;
import com.bizvisionsoft.service.model.Work;

public class ReadonlyResourceActualASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private GanttPart gantt;

	private EditResourceASM grid;

	private Work work;

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		StickerTitlebar bar = new StickerTitlebar(parent, null, null).setText("资源用量")
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
		fd.left = new FormAttachment(0, 8);
		fd.top = new FormAttachment(bar, 8);
		fd.right = new FormAttachment(100, -8);
		fd.bottom = new FormAttachment(100, -8);
		content.setLayout(new FillLayout(SWT.VERTICAL));

		gantt = (GanttPart) new AssemblyContainer(content, context).setAssembly(brui.getAssembly("项目甘特图（资源实际分配）"))
				.setServices(brui).create().getContext().getContent();
		ResourceTransfer rt = new ResourceTransfer();
		rt.setType(ResourceTransfer.TYPE_ACTUAL);
		rt.setShowType(ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE);
		rt.setCheckTime(true);
		rt.setCanAdd(false);
		rt.setCanClose(false);
		rt.setCanEditDateValue(false);
		rt.setShowResActual(true);
		rt.setShowResPlan(true);
		rt.setShowResTypeInfo(true);
		rt.setShowConflict(false);
		rt.setShowFooter(true);

		grid = (EditResourceASM) new AssemblyContainer(content, context).setAssembly(brui.getAssembly("编辑资源情况"))
				.setInput(rt).setServices(brui).create().getContext().getContent();
		// 侦听gantt的selection
		gantt.addGanttEventListener(GanttEventCode.onTaskSelected.name(), l -> select((Work) ((GanttEvent) l).task));

		bar.addListener(SWT.Selection, l -> {
			if ("添加资源用量".equals(((Action) l.data).getName())) {
				if (this.work == null) {
					Layer.message("请先选择将要添加资源用量的工作。");
					return;
				} else if (this.work.isSummary()) {
					Layer.message("无需对总成型工作添加资源用量。");
					return;
				}
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
		rt.setType(ResourceTransfer.TYPE_ACTUAL);
		rt.setShowType(ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE);
		rt.setFrom(work.getStart_date());
		rt.setTo(work.getEnd_date());
		rt.setCanAdd(false);
		rt.setCanClose(false);
		rt.setCanEditDateValue(false);
		rt.setShowResActual(true);
		rt.setShowResPlan(true);
		rt.setShowResTypeInfo(true);
		rt.setShowConflict(false);
		rt.setShowFooter(true);

		grid.setResourceTransfer(rt);
	}

}
