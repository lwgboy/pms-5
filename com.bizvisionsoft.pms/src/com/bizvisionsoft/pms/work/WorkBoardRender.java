package com.bizvisionsoft.pms.work;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUICreated;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkBoardInfo;
import com.bizvisionsoft.serviceconsumer.Services;

public class WorkBoardRender {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private GridTreeViewer viewer;

	@GridRenderUICreated
	private void uiCreated() {
		viewer = (GridTreeViewer) context.getContent("viewer");
		viewer.getGrid().setBackground(BruiColors.getColor(BruiColor.Grey_50));
		viewer.getGrid().addListener(SWT.Selection, e -> {
			if (e.text != null) {
				if (e.text.startsWith("startWork/")) {
					startWork((Work) ((GridItem) e.item).getData());
				} else if (e.text.startsWith("finishWork/")) {
					finishWork((Work) ((GridItem) e.item).getData());
				} else {
					ObjectId id = new ObjectId(e.text.split("/")[1]);
					if (e.text.startsWith("openWorkPackage/")) {
						openWorkPackage(id);
					} else if (e.text.startsWith("assignWork/")) {
						assignWork(id);
					}
				}
			} else {
				Object element = ((GridItem) e.item).getData();
				if (element instanceof Work) {
					viewer.setExpandedElements(new Object[] { element });
				}
			}
		});
		if (((List<?>) viewer.getInput()).size() > 0) {
			viewer.setExpandedElements(new Object[] { ((List<?>) viewer.getInput()).get(0) });
		}
	}

	private void assignWork(ObjectId work_id) {
		// TODO Auto-generated method stub

	}

	private void openWorkPackage(ObjectId work_id) {
		brui.openContent(brui.getAssembly("�������ƻ�"), Services.get(WorkService.class).getWork(work_id));
	}

	private void finishWork(Work work) {
		if (brui.confirm("��ɹ���", "��ȷ����ɹ���<span style='color:red;'>" + work + "</span>��\nϵͳ����¼����ʱ��Ϊ������ʵ�����ʱ�䡣")) {
			List<Result> result = Services.get(WorkService.class).finishWork(work.get_id());
			if (result.isEmpty()) {
				Layer.message("��������ɡ�");
				viewer.remove(work);
			}
		}
	}

	private void startWork(Work work) {
		if (brui.confirm("��������", "��ȷ����������<span style='color:red;'>" + work + "</span>��\nϵͳ����¼����ʱ��Ϊ������ʵ�ʿ�ʼʱ�䡣")) {
			List<Result> result = Services.get(WorkService.class).startWork(work.get_id());
			if (result.isEmpty()) {
				Layer.message("������������");
				Work t = Services.get(WorkService.class).getWork(work.get_id());
				viewer.update(AUtil.simpleCopy(t, work), null);
			}
		}
	}

	@GridRenderUpdateCell
	private void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell) {
		Object element = cell.getElement();
		if (element instanceof Work) {
			renderTitle(cell, (Work) element);
		} else if (element instanceof WorkBoardInfo) {
			renderContent(cell, ((WorkBoardInfo) element).getWork());
		}
	}

	private void renderTitle(ViewerCell cell, Work work) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		gridItem.setHeight(84);
		gridItem.setBackground(BruiColors.getColor(BruiColor.Grey_50));
		String warrningText = work.getOverdue();
		StringBuffer sb = new StringBuffer();
		// ��ʼ����ɰ�ť
		if (work.getActualStart() == null) {
			sb.append("<div style='float:right;margin-right:16px;margin-top:0px;'><a href='startWork/" + work.get_id()
					+ "' target='_rwt'><img class='layui-btn layui-btn-primary layui-btn-sm' style='padding:6px 10px;' src='rwt-resources/extres/img/start.svg'/></a></div>");
		} else if (work.getActualFinish() == null) {
			sb.append("<div style='float:right;margin-right:16px;margin-top:0px;'><a href='finishWork/" + work.get_id()
					+ "' target='_rwt'><img class='layui-btn layui-btn-primary layui-btn-sm' style='padding:6px 10px;' src='rwt-resources/extres/img/finish.svg'/></a></div>");
		}

		sb.append("<div style=''>" + work.getProjectName() + "</div>");
		sb.append("<div style='font-size: 22px;'>" + work.getFullName() + "</div>");
		sb.append("<div style='width:100%;margin-top:2px;display:inline-flex;justify-content:space-between;'><div>�ƻ�: "
				+ new SimpleDateFormat("yyyy/MM/dd").format(work.getPlanStart()) + " ~ "
				+ new SimpleDateFormat("yyyy/MM/dd").format(work.getPlanFinish()));
		if (!"".equals(warrningText))
			sb.append(" <span class='layui-badge layui-bg-red'>" + warrningText + "</span>");
		sb.append("</div>");
		String chargerInfo = work.getChargerInfo();
		sb.append("<div style='margin-right:16px;'>����: "
				+ (chargerInfo == null ? "<a class='layui-btn layui-btn-xs layui-btn-radius layui-btn-warm'>��ָ��</a>"
						: chargerInfo)
				+ "</div></div>");
		cell.setText(sb.toString());
	}

	private void renderContent(ViewerCell cell, Work work) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		gridItem.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		gridItem.setHeight(132);

		String perc;
		Double ind;

		StringBuffer sb = new StringBuffer();
		// ��ʾ����ָ����Ϣ
		sb.append(
				"<div style='padding-right:32px;margin-top:8px;width:100%;'><div style='display:inline-flex;justify-content:space-between;width:100%;'>");
		ind = work.getWAR();
		// ind = 0.2365555d;
		sb.append("<div style='width:112px;'>��������ɣ�</div>");
		sb.append("<div class='layui-progress layui-progress-big' style='margin-left:16px;flex:auto;'>");
		if (ind != null) {
			NumberFormat df = DecimalFormat.getInstance();
			df.setMaximumFractionDigits(1);
			perc = df.format(100 * ind.doubleValue()) + "%";
			sb.append("<div class='layui-progress-bar' style='width:" + perc + ";'><span class='layui-progress-text'>"
					+ perc + "</span></div>");
		}
		sb.append("</div></div></div>");

		// ��ʾ����ָ����Ϣ
		ind = work.getDAR();
		// ind = 0.9365555d;
		sb.append(
				"<div style='padding-right:32px;margin-top:8px;width:100%;'><div style='display:inline-flex;justify-content:space-between;width:100%;'>");
		sb.append("<div style='width:112px;'>����(��)��" + work.getActualDuration() + "/" + work.getPlanDuration()
				+ "</div>");
		sb.append("<div class='layui-progress layui-progress-big' style='margin-left:16px;flex:auto;'>");
		if (ind != null) {
			NumberFormat df = DecimalFormat.getInstance();
			df.setMaximumFractionDigits(1);
			perc = df.format(100 * ind.doubleValue()) + "%";
			sb.append("<div class='layui-progress-bar' style='width:" + perc + ";'><span class='layui-progress-text'>"
					+ perc + "</span></div>");
		}
		sb.append("</div></div></div>");

		// ��ť

		sb.append(
				"<a class='layui-btn layui-btn-sm layui-btn-primary' style='float:right;width:60px;margin-top:16px;margin-right:32px;' href='openWorkPackage/"
						+ work.get_id() + "' target='_rwt'>������</a>");

		if (brui.getCurrentUserId().equals(work.getAssignerId())) {
			sb.append(
					"<a class='layui-btn layui-btn-sm layui-btn-primary' style='float:right; width:60px;margin-top:16px;margin-right:8px;' href='assignWork/"
							+ work.get_id() + "' target='_rwt'>ָ��</a>");
		}

		cell.setText(sb.toString());
	}

}
