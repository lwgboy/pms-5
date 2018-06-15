package com.bizvisionsoft.pms.project;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.bizivisionsoft.widgets.tools.WidgetHandler;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.session.UserSession;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.News;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Work;
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
		fd = new FormData();
		content.setLayoutData(fd);
		fd.left = new FormAttachment(0, 8);
		fd.top = new FormAttachment(bar, 8);
		fd.right = new FormAttachment(100, -8);
		fd.bottom = new FormAttachment(100, -8);

		FormLayout layout = new FormLayout();
		content.setLayout(layout);
		layout.marginHeight = 16;
		layout.marginWidth = 16;
		layout.spacing = 16;

		Composite stage = null;

		if (project.isStageEnable()) {
			stage = createStagePanel(content);
			fd = new FormData();
			stage.setLayoutData(fd);
			fd.right = new FormAttachment(100);
			fd.height = 38;
			fd.left = new FormAttachment();
			fd.top = new FormAttachment();
		}
		Composite info = new Composite(content, SWT.NONE);
		WidgetHandler.getHandler(info).setHtmlContent(getInfoHtml());

		Composite ind = new Composite(content, SWT.NONE);
		WidgetHandler.getHandler(ind).setHtmlContent(getIndicatorsHtml());

		fd = new FormData();
		info.setLayoutData(fd);
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(ind);
		if (stage != null) {
			fd.top = new FormAttachment(stage);
		} else {
			fd.top = new FormAttachment();
		}
		fd.left = new FormAttachment();
		
		fd = new FormData();
		ind.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);
		fd.height = 48;
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
				b.setBackground(BruiColors.getColor(BruiColor.light_blue_500));
				b.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
			b.addListener(SWT.Selection, e -> openStage(work));
		}

		return panel;
	}

	private void openStage(Work work) {
		if (ProjectStatus.Created.equals(work.getStatus())) {
			brui.switchPage("阶段首页（启动）", work.get_id().toHexString());
		} else if (ProjectStatus.Processing.equals(work.getStatus())) {
			brui.switchPage("阶段首页（执行）", work.get_id().toHexString());
		} else if (ProjectStatus.Closing.equals(work.getStatus())) {
			brui.switchPage("阶段首页（收尾）", work.get_id().toHexString());
		} else if (ProjectStatus.Closed.equals(work.getStatus())) {
			brui.switchPage("阶段首页（关闭）", work.get_id().toHexString());
		}
	}

	private String getInfoHtml() {
		StringBuffer sb = new StringBuffer();
		// 添加项目时间线
		List<News> news = Services.get(ProjectService.class).getRecentNews(project.get_id(), 5);
		if (news.size() > 0) {
			// height += 24;
			sb.append("<ul class='layui-timeline' style='margin-left:12px;'>");
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


		return sb.toString();
	}

	private String getIndicatorsHtml() {
		// 添加指标
		StringBuffer sb = new StringBuffer();
		sb.append("<div style='width:100%;display:inline-flex;justify-content:space-around;'>");

		String ind = Optional.ofNullable(project.getWAR()).map(d -> new DecimalFormat("#0.0%").format(d)).orElse("</br>");
		sb.append("<div class='brui_indicator info' style='padding:8px 16px;font-size:14px;font-weight:lighter;'>");
		sb.append("<div>"+ind+"</div>");
		sb.append("<div>工作执行</div></div>");

		ind = Optional.ofNullable(project.getDAR()).map(d -> new DecimalFormat("#0.0%").format(d)).orElse("</br>");
		sb.append("<div class='brui_indicator info' style='padding:8px 16px;font-size:14px;font-weight:lighter;'>");
		sb.append("<div>"+ind+"</div>");
		sb.append("<div>工期完成</div></div>");
		
		ind = Optional.ofNullable(project.getSAR()).map(d -> new DecimalFormat("#0.0%").format(d)).orElse("</br>");
		sb.append("<div class='brui_indicator info' style='padding:8px 16px;font-size:14px;font-weight:lighter;'>");
		sb.append("<div>"+ind+"</div>");
		sb.append("<div>进度完成</div></div>");

		ind = Optional.ofNullable(project.getCAR()).map(d -> new DecimalFormat("#0.0%").format(d)).orElse("</br>");
		sb.append("<div class='brui_indicator normal' style='padding:8px 16px;font-size:14px;font-weight:lighter;'>");
		sb.append("<div>"+ind+"</div>");
		sb.append("<div>成本执行</div></div>");

		ind = Optional.ofNullable(project.getBDR()).map(d -> new DecimalFormat("#0.0%").format(d)).orElse("</br>");
		sb.append("<div class='brui_indicator normal' style='padding:8px 16px;font-size:14px;font-weight:lighter;'>");
		sb.append("<div>"+ind+"</div>");
		sb.append("<div>预算偏差</div></div>");

		sb.append("</div>");
		return sb.toString();
	}


}