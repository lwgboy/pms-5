package com.bizvisionsoft.pms.workreport;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkBoardInfo;
import com.bizvisionsoft.serviceconsumer.Services;

public class WorkInReportBoardRender {

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
				// TODO ����
				if (e.text.startsWith("startWork/")) {
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

	private void finishWork(Work work) {
		if (brui.confirm("��ɹ���", "��ȷ����ɹ���<span style='color:red;'>" + work + "</span>��\nϵͳ����¼����ʱ��Ϊ������ʵ�����ʱ�䡣")) {
			List<Result> result = Services.get(WorkService.class).finishWork(brui.command(work.get_id(), new Date()));
			if (result.isEmpty()) {
				Layer.message("��������ɡ�");
				viewer.remove(work);
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
		StringBuffer sb = new StringBuffer();
		// ��ʼ����ɰ�ť
		Date actualStart = work.getActualStart();
		Date actualFinish = work.getActualFinish();
		if (actualStart == null) {
			sb.append("<div style='float:right;margin-right:16px;margin-top:0px;'><a href='editor/" + work.get_id()
					+ "' target='_rwt'><img class='layui-btn layui-btn-sm' style='padding:6px 10px;' src='rwt-resources/extres/img/start_w.svg'/></a></div>");
		} else if (actualFinish == null) {
			sb.append("<div style='float:right;margin-right:16px;margin-top:0px;'><a href='finishWork/" + work.get_id()
					+ "' target='_rwt'><img class='layui-btn layui-btn-normal layui-btn-sm' style='padding:6px 10px;' src='rwt-resources/extres/img/finish_w.svg'/></a></div>");
		}

		sb.append("<div style='font-size: 22px;'>" + work.getFullName() + "</div>");
		sb.append("<div style='width:100%;margin-top:2px;display:-webkit-flex;justify-content:space-between;'><div>�ƻ�: "
				+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(work.getPlanStart()) + " ~ "
				+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(work.getPlanFinish()));
		sb.append("</div>");
		String chargerInfo = work.getChargerInfo();
		sb.append("<div style='margin-right:16px;'>����: " + (chargerInfo == null ? "" : chargerInfo) + "</div></div>");
		sb.append("<div style='width:100%;margin-top:2px;display:-webkit-flex;justify-content:space-between;'><div>ʵ��: "
				+ (actualStart == null ? "" : new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(actualStart)) + " ~ "
				+ (actualFinish == null ? "" : new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(actualFinish)));
		sb.append("</div>");
		String assignerInfo = work.getAssignerInfo();
		sb.append("<div style='margin-right:16px;'>ָ��: " + (assignerInfo == null ? "" : assignerInfo) + "</div></div>");

		cell.setText(sb.toString());
	}

	private void renderContent(ViewerCell cell, Work work) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		gridItem.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		gridItem.setHeight(132);

		String perc;
		Double ind;

		StringBuffer sb = new StringBuffer();
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ��ʾ����ָ����Ϣ
		sb.append(
				"<div style='padding-right:32px;margin-top:8px;width:100%;'><div style='display:inline-flex;justify-content:space-between;width:100%;'>");
		ind = work.getWAR();
		// ind = 0.2365555d;
		sb.append("<div style='width:112px;'>�������ȣ�</div>");
		sb.append("<div class='layui-progress layui-progress-big' style='margin-left:16px;flex:auto;'>");
		if (ind != null) {
			NumberFormat df = DecimalFormat.getInstance();
			df.setMaximumFractionDigits(1);
			perc = df.format(100 * ind.doubleValue()) + "%";
			sb.append("<div class='layui-progress-bar' style='width:" + perc + ";'><span class='layui-progress-text'>"
					+ perc + "</span></div>");
		}
		sb.append("</div></div></div>");

		/////////////////////////////////////////////////////////////////////////////////////////////////////////
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

		sb.append("<div style='display:inline-flex;width:100%;justify-content:flex-end;padding-right:24px'>");
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ��������ť
		List<TrackView> wps = work.getWorkPackageSetting();
		if (Util.isEmptyOrNull(wps)) {
			sb.append(
					"<a class='layui-btn layui-btn-sm layui-btn-primary' style='float:right;margin-top:16px;margin-right:4px;' href='"
							+ "openWorkPackage/default" + "' target='_rwt'>" + "������" + "</a>");
		} else if (wps.size() == 1) {
			sb.append(
					"<a class='layui-btn layui-btn-sm layui-btn-primary' style='float:right;margin-top:16px;margin-right:4px;' href='"
							+ "openWorkPackage/0" + "' target='_rwt'>" + wps.get(0).getName() + "</a>");

		} else {
			for (int i = 0; i < wps.size(); i++) {
				sb.append(
						"<a class='layui-btn layui-btn-sm layui-btn-primary' style='float:right;margin-top:16px;margin-right:4px;' href='"
								+ "openWorkPackage/" + i + "' target='_rwt'>" + wps.get(i).getName() + "</a>");
			}
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ָ�ɰ�ť
		if (brui.getCurrentUserId().equals(work.getAssignerId())) {
			sb.append(
					"<a class='layui-btn layui-btn-sm layui-btn-normal' style='float:right; width:60px;margin-top:16px;margin-right:4px;' href='assignWork/"
							+ work.get_id() + "' target='_rwt'>ָ��</a>");
		}
		sb.append("</div>");

		cell.setText(sb.toString());
	}

}
