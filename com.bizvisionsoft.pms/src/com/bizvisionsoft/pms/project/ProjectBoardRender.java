package com.bizvisionsoft.pms.project;

import java.text.SimpleDateFormat;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUICreated;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectBoardInfo;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class ProjectBoardRender {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService bruiService;

	private GridTreeViewer viewer;

	@GridRenderUICreated
	private void uiCreated() {
		viewer = (GridTreeViewer) context.getContent("viewer");
		viewer.getGrid().setBackground(BruiColors.getColor(BruiColor.Grey_50));
		viewer.getGrid().addListener(SWT.Selection, e -> {
			if (e.text != null) {
				if (e.text.startsWith("openStage/")) {
					String stageId = e.text.split("/")[1];
					openStage(new ObjectId(stageId));
				} else if (e.text.startsWith("openProject/")) {
					String id = e.text.split("/")[1];
					openProject(new ObjectId(id));
				}
			} else {
				Object element = ((GridItem) e.item).getData();
				if (element instanceof Project) {
					viewer.setExpandedElements(new Object[] { element });
				}
			}
		});
		if (((List<?>) viewer.getInput()).size() > 0) {
			viewer.setExpandedElements(new Object[] { ((List<?>) viewer.getInput()).get(0) });
		}
	}

	private void openProject(ObjectId _id) {
		Project pj = Services.get(ProjectService.class).get(_id);
		if (ProjectStatus.Created.equals(pj.getStatus())) {
			bruiService.switchPage("项目首页（启动）", _id.toHexString());
		} else if (ProjectStatus.Processing.equals(pj.getStatus())) {
			bruiService.switchPage("项目首页（执行）", _id.toHexString());
		} else if (ProjectStatus.Closing.equals(pj.getStatus())) {
			bruiService.switchPage("项目首页（收尾）", _id.toHexString());
		} else if (ProjectStatus.Closed.equals(pj.getStatus())) {
			bruiService.switchPage("项目首页（关闭）", _id.toHexString());
		}
	}

	private void openStage(ObjectId workId) {
		Work work = Services.get(WorkService.class).getWork(workId);
		if (ProjectStatus.Created.equals(work.getStatus())) {
			bruiService.switchPage("阶段首页（启动）", workId.toHexString());
		} else if (ProjectStatus.Processing.equals(work.getStatus())) {
			bruiService.switchPage("阶段首页（执行）", workId.toHexString());
		} else if (ProjectStatus.Closing.equals(work.getStatus())) {
			bruiService.switchPage("阶段首页（收尾）", workId.toHexString());
		} else if (ProjectStatus.Closed.equals(work.getStatus())) {
			bruiService.switchPage("阶段首页（关闭）", workId.toHexString());
		}
	}

	@GridRenderUpdateCell
	private void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell) {
		Object element = cell.getElement();
		if (element instanceof Project) {
			renderTitle(cell, (Project) element);
		} else if (element instanceof ProjectBoardInfo) {
			renderContent(cell, ((ProjectBoardInfo) element).getProject());
		}
	}

	private void renderTitle(ViewerCell cell, Project pj) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		gridItem.setHeight(64);
		gridItem.setBackground(BruiColors.getColor(BruiColor.Grey_50));
		StringBuffer sb = new StringBuffer();
		sb.append(
				"<div style='float:right;margin-right:16px;margin-top:0px;'><a class='layui-btn layui-btn-primary layui-btn-sm' href='openProject/"
						+ pj.get_id() + "' target='_rwt'><i class='layui-icon'>&#xe602;</i></a></div>");

		sb.append("<div style='font-size: 22px;'>" + pj.getId() + " / " + pj.getName() + "</div>");
		sb.append("<div style='width:100%;margin-top:4px;display:inline-flex;justify-content:space-between;'><div>计划: "
				+ new SimpleDateFormat("yyyy/MM/dd").format(pj.getPlanStart()) + " ~ "
				+ new SimpleDateFormat("yyyy/MM/dd").format(pj.getPlanFinish()) + "</div>");
		sb.append("<div style='margin-right:16px;'>项目经理: " + pj.getPmInfo() + "</div></div>");

		cell.setText(sb.toString());
	}

	private void renderContent(ViewerCell cell, Project pj) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		gridItem.setHeight(352);
		gridItem.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		StringBuffer sb = new StringBuffer();
		// 如果项目有阶段，显示阶段状态。
		if (pj.isStageEnable()) {
			sb.append(getProjectStageInfo(pj));
		}
		// 添加项目时间线
		sb.append(getProjectNews(pj));

		// 添加指标
		sb.append(getProjectIndicators(pj));

		cell.setText(sb.toString());
	}

	private String getProjectIndicators(Project pj) {
		StringBuffer text = new StringBuffer();
		text.append(
				"<div style='margin-top:0px;width: 100%;display:inline-flex;justify-content:center;padding-right: 36px;'>");
		// TODO
		text.append(
				"<div class='brui_indicator normal' style='font-size:16px;font-weight:lighter;flex:auto;flex-basis:33%;margin:0px 8px;'>63.0%<br/>进度完成率</div>");
		text.append(
				"<div class='brui_indicator info' style='font-size:16px;font-weight:lighter;flex:auto;flex-basis:33%;margin:0px 8px;'>32.0%<br/>工作量完成率</div>");
		text.append(
				"<div class='brui_indicator warning' style='font-size:16px;font-weight:lighter;flex:auto;flex-basis:33%;margin:0px 8px;'>52.0%<br/>工期完成率</div>");
		text.append("</div>");
		return text.toString();
	}

	private String getProjectNews(Project pj) {
		StringBuffer text = new StringBuffer();
		text.append("<ul class='layui-timeline' style='margin-top:24px;margin-right:32px;'>");
		Services.get(CommonService.class).getRecentNews(pj.get_id(), 5).forEach(n -> {
			text.append("<li class='layui-timeline-item' style='padding-bottom:8px;'>");
			text.append("<i class='layui-icon layui-timeline-axis'>&#xe63f;</i>");
			text.append("<div class='layui-timeline-content layui-text'>");
			text.append(
					"<div class='layui-timeline-title' style='white-space:nowrap;overflow:hidden;text-overflow:ellipsis;width:100%;'>"
							+ n.getSummary() + "</div>");
			text.append("</div></li>");
		});
		text.append("</ul>");
		return text.toString();
	}

	private String getProjectStageInfo(Project pj) {
		StringBuffer text = new StringBuffer();
		text.append("<div class='layui-btn-group' "
				+ "style='margin-top:16px;width: 100%;display:inline-flex;justify-content:space-between;padding-right: 36px;'>");
		Services.get(ProjectService.class).listStage(pj.get_id()).forEach(work -> {
			String style;
			if (work.getStartOn() != null && work.getFinishOn() != null) {
				style = "layui-btn layui-btn-sm";
			} else if (work.getStartOn() != null && work.getFinishOn() == null) {
				style = "layui-btn layui-btn-normal layui-btn-sm";
			} else {
				style = "layui-btn layui-btn-primary layui-btn-sm";
			}
			text.append("<a class='" + style + "' style='flex:auto;' href='openStage/" + work.getId()
					+ "' target='_rwt'>" + work.getText() + "</a>");
		});
		text.append("</div>");
		return text.toString();
	}

}
