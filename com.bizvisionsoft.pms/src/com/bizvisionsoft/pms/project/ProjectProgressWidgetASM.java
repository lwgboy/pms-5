package com.bizvisionsoft.pms.project;

import java.text.DecimalFormat;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.tools.WidgetHandler;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.session.UserSession;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.News;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class ProjectProgressWidgetASM {
	
	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private Project project;

	@Init
	private void init() {
		project = context.getRootInput(Project.class, false);
	}
	
	@CreateUI
	public void createUI(Composite parent) {
		parent.setHtmlAttribute("class", "brui_borderRight brui_borderBottom");

		parent.setLayout(new FormLayout());

		StickerTitlebar bar = new StickerTitlebar(parent, null, null).setText("进展摘要");
				
		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 48;

		Composite content = UserSession.bruiToolkit().newContentPanel(parent);
//		content.setBackground(BruiColors.getColor(BruiColor));
		fd = new FormData();
		content.setLayoutData(fd);
		fd.left = new FormAttachment(0, 8);
		fd.top = new FormAttachment(bar, 8);
		fd.right = new FormAttachment(100, -8);
		fd.bottom = new FormAttachment(100, -8);
		
		FillLayout layout = new FillLayout();
		content.setLayout(layout);
		layout.marginHeight = 24;
		layout.marginWidth = 24;
		Composite info = new Composite(content,SWT.NONE);
		UserSession.bruiToolkit().enableMarkup(info);
		WidgetHandler.getHandler(info).setHtmlContent(getInfoHtml());
		
	}
	
	private String getInfoHtml() {
		StringBuffer sb = new StringBuffer();

		// 如果项目有阶段，显示阶段状态。
		if (project.isStageEnable()) {
			// height += 46;
			sb.append(getProjectStageInfo());
		}

		// 添加项目时间线
		List<News> news = Services.get(ProjectService.class).getRecentNews(project.get_id(), 5);
		if (news.size() > 0) {
			// height += 24;
			sb.append("<ul class='layui-timeline' style='margin-top:12px;'>");
			for (int i = 0; i < news.size(); i++) {
				sb.append("<li class='layui-timeline-item' style='padding-bottom:8px;'>");
				sb.append("<i class='layui-icon layui-timeline-axis'>&#xe63f;</i>");
				sb.append("<div class='layui-timeline-content layui-text'>");
				sb.append(
						"<div class='layui-timeline-title' style='white-space:nowrap;overflow:hidden;text-overflow:ellipsis;width:100%;'>"
								+ news.get(i).getSummary() + "</div>");
				sb.append("</div></li>");
				// height += 40;
			}
			sb.append("</ul>");
		}

		// 添加指标

		sb.append(
				"<div style='width:100%;display:inline-flex;justify-content:center;margin-top:12px;'>");

		Object sar = project.getSAR();
		if (sar != null) {
			sb.append(
					"<div class='brui_indicator normal' style='font-size:16px;font-weight:lighter;flex:auto;flex-basis:33%;margin:0px 8px;'>");
			sb.append(new DecimalFormat("#0.0%").format(sar));
			sb.append("<br/>进度完成率</div>");
		}

		Object war = project.getWAR();
		if (war != null) {
			sb.append(
					"<div class='brui_indicator info' style='font-size:16px;font-weight:lighter;flex:auto;flex-basis:33%;margin:0px 8px;'>");
			sb.append(new DecimalFormat("#0.0%").format(war));
			sb.append("<br/>工作量完成率</div>");
		}

		Object dar = project.getDAR();
		if (dar != null) {
			sb.append(
					"<div class='brui_indicator warning' style='font-size:16px;font-weight:lighter;flex:auto;flex-basis:33%;margin:0px 8px;'>");
			sb.append(new DecimalFormat("#0.0%").format(dar));
			sb.append("<br/>工期完成率</div>");
		}

		sb.append("</div>");

		// gridItem.setHeight(height);
		return sb.toString();
	}

	private String getProjectStageInfo() {
		StringBuffer text = new StringBuffer();
		text.append("<div class='layui-btn-group' "
				+ "style='width: 100%;display:inline-flex;justify-content:space-between;'>");
		Services.get(ProjectService.class).listStage(project.get_id()).forEach(work -> {
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
