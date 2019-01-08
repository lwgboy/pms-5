package com.bizvisionsoft.pms.project;

import java.util.List;

import org.bson.Document;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.bizivisionsoft.widgets.carousel.Carousel;
import com.bizivisionsoft.widgets.chart.ECharts;
import com.bizivisionsoft.widgets.tools.WidgetHandler;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.model.News;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

/**
 * 使用轮播布局可替代本类
 * 
 * @author hua
 *
 */
@Deprecated
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
		parent.setLayout(new FillLayout());
		Carousel carousel = new Carousel(parent, SWT.NONE);
		carousel.setAnimation("default");
		carousel.setInterval(5000);
		carousel.setIndicator("none");

		createProgressPage(carousel.addPage(new Composite(carousel, SWT.NONE)));

		createRiskPage(carousel.addPage(new Composite(carousel, SWT.NONE)));

	}

	private void createRiskPage(Composite parent) {
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		parent.setLayout(new FillLayout());
		ECharts chart = new ECharts(parent, SWT.NONE);
		Document data = Services.get(RiskService.class).getRiskProximityChart(project.get_id());
		chart.setOption(JsonObject.readFrom(data.toJson()));
	}

	private void createProgressPage(Composite parent) {
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		FormLayout layout = new FormLayout();
		parent.setLayout(layout);

		Label title = new Label(parent, SWT.NONE);
		title.setText("项目进展摘要");
		title.setForeground(BruiColors.getColor(BruiColor.Grey_600));

		FormData fd = new FormData();
		title.setLayoutData(fd);
		fd.left = new FormAttachment(0, 8);
		fd.top = new FormAttachment(0, 8);
		fd.right = new FormAttachment(100, -8);

		Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setBackground(BruiColors.getColor(BruiColor.Grey_50));

		fd = new FormData();
		sep.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(title, 4);
		fd.right = new FormAttachment(100);
		fd.height = 1;

		Composite stage = null;
		if (project.isStageEnable()) {
			stage = createStagePanel(parent);
			fd = new FormData();
			stage.setLayoutData(fd);
			fd.right = new FormAttachment(100, -16);
			fd.height = 32;
			fd.left = new FormAttachment(0, 16);
			fd.top = new FormAttachment(sep, 16);
		}

		Composite timeline = new Composite(parent, SWT.NONE);
		WidgetHandler.getHandler(timeline).setHtmlContent(getTimeHtml());

		fd = new FormData();
		timeline.setLayoutData(fd);
		fd.right = new FormAttachment(100, -16);
		fd.bottom = new FormAttachment(100);
		if (stage == null) {
			fd.top = new FormAttachment();
		} else {
			fd.top = new FormAttachment(stage, 16);
		}
		fd.left = new FormAttachment(0, 16);
	}

	private Composite createStagePanel(Composite content) {
		Composite panel = new Composite(content, SWT.NONE);
		panel.setLayout(new FillLayout());

		List<Work> stage = Services.get(ProjectService.class).listStage(project.get_id());
		for (int i = 0; i < stage.size(); i++) {
			Button b = new Button(panel, SWT.PUSH);
			final Work work = stage.get(i);
			b.setText(work.getText());
			if (i == 0) {
				b.setData(RWT.CUSTOM_VARIANT, "segmentleft");
			} else if (i == stage.size() - 1) {
				b.setData(RWT.CUSTOM_VARIANT, "segmentright");
			} else {
				b.setData(RWT.CUSTOM_VARIANT, "segment");
			}

			if (work.getStartOn() != null && work.getFinishOn() != null) {
				b.setBackground(BruiColors.getColor(BruiColor.Teal_500));
				b.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			} else if (work.getStartOn() != null && work.getFinishOn() == null) {
				b.setBackground(BruiColors.getColor(BruiColor.Light_blue_500));
				b.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		}

		return panel;
	}

	private String getTimeHtml() {
		StringBuffer sb = new StringBuffer();
		// 添加项目时间线
		List<News> news = Services.get(ProjectService.class).getRecentNews(project.get_id(), 5);
		if (news.size() > 0) {
			// height += 24;
			sb.append("<ul class='layui-timeline' style='margin-left:12px;'>");
			for (int i = 0; i < news.size(); i++) {
				sb.append("<li class='layui-timeline-item' style='padding-bottom:4px;'>");
				sb.append("<i class='layui-icon layui-timeline-axis'>&#xe63f;</i>");
				sb.append("<div class='layui-timeline-content layui-text'>");
				sb.append(
						"<div class='layui-timeline-title' style='White-space:nowrap;overflow:hidden;text-overflow:ellipsis;width:100%;'>"
								+ news.get(i).getSummary() + "</div>");
				sb.append("</div></li>");
				// height += 40;
			}
			sb.append("</ul>");
		}

		return sb.toString();
	}

}