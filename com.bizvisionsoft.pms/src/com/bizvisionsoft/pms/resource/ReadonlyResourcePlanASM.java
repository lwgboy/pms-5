package com.bizvisionsoft.pms.resource;

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
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class ReadonlyResourcePlanASM {

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
		grid = (GridPart) new AssemblyContainer(content, context).setAssembly(brui.getAssembly("��Դ������鿴��"))
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
			}
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
