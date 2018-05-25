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
			Object element = ((GridItem) e.item).getData();
			if (element instanceof Project) {
				viewer.setExpandedElements(new Object[] { element });
			} else if (e.text != null) {
				if (e.text.startsWith("openStage/")) {
					String stageId = e.text.split("/")[1];
					openStage(new ObjectId(stageId));
				}
			}
		});
		if (((List<?>) viewer.getInput()).size() > 0) {
			viewer.setExpandedElements(new Object[] { ((List<?>) viewer.getInput()).get(0) });
		}
	}

	private void openStage(ObjectId workId) {
		Work work = Services.get(WorkService.class).getWork(workId);
		if (ProjectStatus.Created.equals(work.getStatus())) {
			bruiService.switchPage("阶段首页（启动）", work.get_id().toHexString());
		} else if (ProjectStatus.Processing.equals(work.getStatus())) {
			bruiService.switchPage("阶段首页（执行）", work.get_id().toHexString());
		} else if (ProjectStatus.Closing.equals(work.getStatus())) {
			bruiService.switchPage("阶段首页（收尾）", work.get_id().toHexString());
		} else if (ProjectStatus.Closed.equals(work.getStatus())) {
			bruiService.switchPage("阶段首页（关闭）", work.get_id().toHexString());
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
		sb.append("<div style='font-size: 22px;'>" + pj.getId() + " / " + pj.getName() + "</div>");

		sb.append("<div style='width:100%;display:inline-flex;justify-content:space-between;'><div>计划: "
				+ new SimpleDateFormat("yyyy/MM/dd").format(pj.getPlanStart()) + " ~ "
				+ new SimpleDateFormat("yyyy/MM/dd").format(pj.getPlanFinish()) + "</div>");
		sb.append("<div style='margin-right:18px;'>项目经理: " + pj.getPmInfo() + "</div></div>");

		cell.setText(sb.toString());
	}

	private void renderContent(ViewerCell cell, Project pj) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		gridItem.setHeight(360);
		gridItem.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		StringBuffer sb = new StringBuffer();
		// 如果项目有阶段，显示阶段状态。
		if (pj.isStageEnable()) {
			sb.append(getProjectStageInfo(pj));
		}
		// 添加项目时间线

		cell.setText(sb.toString());
	}

	private String getProjectStageInfo(Project pj) {
		String text = "<div " + "class='layui-btn-group' style='" + "width: 100%;" + "display:inline-flex;"
				+ "justify-content:space-between;" + "padding-right: 36px;" + "'>";
		List<Work> stages = Services.get(ProjectService.class).listStage(pj.get_id());
		for (int i = 0; i < stages.size(); i++) {
			Work work = stages.get(i);
			String style;
			if (work.getStartOn() != null && work.getFinishOn() != null) {
				style = "layui-btn layui-btn-sm";
			} else if (work.getStartOn() != null && work.getFinishOn() == null) {
				style = "layui-btn layui-btn-normal layui-btn-sm";
			} else {
				style = "layui-btn layui-btn-primary layui-btn-sm";
			}
			text += "<a class='" + style + "' style='flex:auto' href='openStage/" + work.getId() + "' target='_rwt'>"
					+ work.getText() + "</a>";
		}
		text += "</div>";
		return text;
	}

}
