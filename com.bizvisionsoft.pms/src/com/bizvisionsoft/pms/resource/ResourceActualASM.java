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
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class ResourceActualASM {

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

		StickerTitlebar bar = new StickerTitlebar(parent, null, null).setText("��Դ����")
				.setActions(context.getAssembly().getActions());
		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 48;

		Composite content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FillLayout(SWT.VERTICAL)).get();

		// �޸Ŀؼ�title���Ա��ڵ�����ť������ʾ
		gantt = (GanttPart) new AssemblyContainer(content, context).setAssembly(brui.getAssembly("��Ŀ����ͼ����Դʵ�ʷ��䣩"))
				.setServices(brui).create().getContext().getContent();
		gantt.setExportActionText("����ͼ");
		ResourceTransfer rt = new ResourceTransfer();
		rt.setType(ResourceTransfer.TYPE_ACTUAL);
		rt.setShowType(ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE);
		rt.setCheckTime(true);
		rt.setCanAdd(false);
		rt.setCanDelete(true);
		rt.setCanClose(false);
		rt.setShowResActual(true);
		rt.setShowResPlan(true);
		rt.setShowResTypeInfo(true);
		rt.setShowConflict(false);
		rt.setShowFooter(true);

		// �޸Ŀؼ�title���Ա��ڵ�����ť������ʾ
		grid = (EditResourceASM) new AssemblyContainer(content, context).setAssembly(brui.getAssembly("�༭��Դ���"))
				.setInput(rt).setServices(brui).create().getContext().getContent();
		grid.setExportActionText("��Դ����");
		// ����gantt��selection
		gantt.addGanttEventListener(GanttEventCode.onTaskSelected.name(), l -> select((Work) ((GanttEvent) l).task));

		bar.addListener(SWT.Selection, l -> {
			Action action = ((Action) l.data);
			if ("�����Դ����".equals(action.getName())) {
				if (this.work == null) {
					Layer.message("����ѡ��Ҫ�����Դ�����Ĺ���");
					return;
				} else if (this.work.isSummary()) {
					Layer.message("������ܳ��͹��������Դ����");
					return;
				} else if (this.work.isMilestone()) {
					Layer.message("�������̱������Դ����");
					return;
				}
				allocateResource();
			} else {
				UserSession.bruiToolkit().runAction(action, l, brui, context);
			}
		});

		gantt.addGanttEventListener(GanttEventCode.onTaskDblClick.name(), l -> {
			Work work = (Work) ((GanttEvent) l).task;
			if (work != null && !work.isSummary() && !work.isMilestone()) {
				allocateResource();
			}
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
		// TODO
		Selector.open(selectorId, context, null, l -> {
			List<ResourceAssignment> resa = new ArrayList<ResourceAssignment>();
			l.forEach(o -> {
				ResourceAssignment ra = new ResourceAssignment().setTypedResource(o).setWork_id(work.get_id());
				ra.from = work.getStart_date();
				ra.to = work.getEnd_date();
				resa.add(ra);
			});
			Services.get(WorkService.class).addResourceActual(resa);
			grid.doRefresh();
		});
	}

	private void select(Work work) {
		if (this.work != null && this.work.get_id().equals(work.get_id())) {
			return;
		}
		this.work = work;
		// ��ѯ
		ResourceTransfer rt = new ResourceTransfer();
		rt.addWorkIds(work.get_id());
		rt.setType(ResourceTransfer.TYPE_ACTUAL);
		rt.setShowType(ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE);
		rt.setFrom(work.getStart_date());
		rt.setTo(work.getEnd_date());
		rt.setCanAdd(work.getActualStart() != null);
		rt.setCanDelete(true);
		rt.setCanClose(false);
		rt.setShowResActual(true);
		rt.setShowResPlan(true);
		rt.setShowResTypeInfo(true);
		rt.setShowConflict(false);
		rt.setShowFooter(true);
		rt.setTitle(work.getFullName() + "������Դʵ������");

		grid.setResourceTransfer(rt);
	}

}
