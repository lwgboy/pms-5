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
	private IBruiService brui;

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
			ganttAssemblyName = "��Ŀ����ͼ����Դ�ƻ����䣩";
			stickerTitleText = "��Դ�ƻ�";
			gridExportActionText = "��Դ�ƻ�";
		} else {
			ganttAssemblyName = "��Ŀ����ͼ����Դʵ�ʷ��䣩";
			stickerTitleText = "��Դ����";
			gridExportActionText = "��Դ����";
		}
		parent.setLayout(new FormLayout());

		StickerTitlebar bar = Controls.handle(new StickerTitlebar(parent, null, null)).height(48).left().top().right()
				.get().setText(stickerTitleText).setActions(context.getAssembly().getActions());

		Composite content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FillLayout(SWT.VERTICAL)).get();

		// �޸Ŀؼ�title���Ա��ڵ�����ť������ʾ
		gantt = (GanttPart) new AssemblyContainer(content, context).setAssembly(brui.getAssembly(ganttAssemblyName))
				.setServices(brui).create().getContext().getContent();

		gantt.setExportActionText("����ͼ");
		ResourceTransfer rt = new ResourceTransfer();
		rt.setShowType(ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE);
		rt.setCheckTime(true);
		rt.setCanAdd(false);
		rt.setCanDelete(Boolean.TRUE.equals(editable));
		rt.setCanEditDateValue(Boolean.TRUE.equals(editable));
		rt.setCanClose(false);
		rt.setShowResPlan(true);
		rt.setShowResTypeInfo(true);
		rt.setShowFooter(true);

		if ("plan".equals(this.type)) {
			rt.setType(ResourceTransfer.TYPE_PLAN);
			rt.setShowConflict(true);
		} else {
			rt.setType(ResourceTransfer.TYPE_ACTUAL);
			rt.setShowResActual(true);
			rt.setShowConflict(false);
		}

		// �޸Ŀؼ�title���Ա��ڵ�����ť������ʾ
		grid = (EditResourceASM) new AssemblyContainer(content, context).setAssembly(brui.getAssembly("�༭��Դ���"))
				.setInput(rt).setServices(brui).create().getContext().getContent();
		grid.setExportActionText(gridExportActionText);
		// ����gantt��selection
		gantt.addGanttEventListener(GanttEventCode.onTaskSelected.name(), l -> select((Work) ((GanttEvent) l).task));

		bar.addListener(SWT.Selection, l -> {
			Action action = ((Action) l.data);
			if ("������Դ".equals(action.getName()) || "�����Դ����".equals(action.getName())) {
				// ��Դ�ƻ���ť
				if (this.work == null) {
					Layer.message("����ѡ��Ҫ" + action.getName() + "�Ĺ���");
					return;
				} else if (this.work.isSummary()) {
					Layer.message("������ܳ��͹���" + action.getName());
					return;
				} else if (this.work.isMilestone()) {
					Layer.message("�������̱�" + action.getName());
					return;
				}
				if (Boolean.TRUE.equals(editable))
					allocateResource();
			} else {
				UserSession.bruiToolkit().runAction(action, l, brui, context);
			}
		});
		if (Boolean.TRUE.equals(editable)) {
			gantt.addGanttEventListener(GanttEventCode.onTaskDblClick.name(), l -> {
				Work work = (Work) ((GanttEvent) l).task;
				if (work != null && !work.isSummary() && !work.isMilestone()) {
					allocateResource();
				}
			});
			Layer.message("��ʾ�� ������˫��Ҷ������ѡ��Ҫ��ӵ���Դ");
		}
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
			l.forEach(o -> {
				ResourceAssignment ra = new ResourceAssignment().setTypedResource(o).setWork_id(work.get_id());
				// ��Դ�ƻ�
				if ("plan".equals(this.type)) {
					if (o instanceof ResourceType) {
						InputDialog id = new InputDialog(brui.getCurrentShell(), "�༭��Դ����",
								"��������Դ " + o.toString() + " ����", null, t -> {
									if (t.trim().isEmpty())
										return "��������Դ����";
									try {
										Integer.parseInt(t);
									} catch (Exception e) {
										return "��������ʹ���";
									}
									return null;
								});
						if (InputDialog.OK == id.open()) {
							ra.qty = Integer.parseInt(id.getValue());
						}
					} else {
						ra.qty = 1;
					}
				} else {
					// ��Դ����
					ra.from = work.getStart_date();
					ra.to = work.getEnd_date();
				}

				resa.add(ra);
			});
			if ("plan".equals(this.type))
				// ��Դ�ƻ�
				Services.get(WorkService.class).addResourcePlan(resa);
			else
				// ��Դ����
				Services.get(WorkService.class).addResourceActual(resa);
			grid.doRefresh();
		});

	}

	private void select(Work work) {
		if (this.work != null && this.work.get_id().equals(work.get_id()))
			return;

		this.work = work;
		// ��ѯ
		ResourceTransfer rt = new ResourceTransfer();
		rt.addWorkIds(work.get_id());
		rt.setShowType(ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE);
		rt.setCanDelete(Boolean.TRUE.equals(editable));
		rt.setCanEditDateValue(Boolean.TRUE.equals(editable));
		rt.setCanClose(false);
		rt.setShowResPlan(true);
		rt.setShowResTypeInfo(true);
		rt.setShowFooter(true);
		if ("plan".equals(this.type)) {
			// ��Դ�ƻ�
			rt.setType(ResourceTransfer.TYPE_PLAN);
			rt.setFrom(work.getPlanStart());
			rt.setTo(work.getPlanEndDate());
			rt.setCanAdd(false);
			rt.setShowConflict(true);
			rt.setTitle(work.getFullName() + "������Դ�ƻ�����");
		} else {
			// ��Դ����
			rt.setType(ResourceTransfer.TYPE_ACTUAL);
			rt.setFrom(work.getStart_date());
			rt.setTo(work.getEnd_date());
			rt.setCanAdd(work.getActualStart() != null);
			rt.setShowResActual(true);
			rt.setShowConflict(false);
			rt.setTitle(work.getFullName() + "������Դʵ������");
		}
		grid.setResourceTransfer(rt);

	}

}
