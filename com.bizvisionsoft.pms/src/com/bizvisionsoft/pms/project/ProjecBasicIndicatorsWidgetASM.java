package com.bizvisionsoft.pms.project;

import java.text.DecimalFormat;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bizivisionsoft.widgets.carousel.Carousel;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.model.Project;

public class ProjecBasicIndicatorsWidgetASM {

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
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		FormLayout layout = new FormLayout();
		parent.setLayout(layout);

		Label title = new Label(parent, SWT.NONE);
		title.setText("主要指标");
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

		Carousel carousel = new Carousel(parent, SWT.NONE);
		carousel.setAnimation("default");
		carousel.setIndicator("none");
		Composite page = createPage(parent, carousel);
		addIndicator(page, toString(project.getWAR()), "工作进度完成率");
		addIndicator(page, toString(project.getDAR()), "工期完成率");
		addIndicator(page, toString(project.getCAR()), "成本进度完成率");
		addIndicator(page, toString(project.getBDR()), "预算偏差率");

		page = createPage(parent, carousel);

		Double probability = project.getDurationProbability();
		if (probability == null) {
			addIndicator(page, "尚未计算", "项目按期完工概率");
		} else if (probability >= 0.9) {
			addIndicator(page, toString(probability), "项目按期完工概率", "layui-bg-green", "#ffffff", "#ffffff");
		} else if (probability >= 0.7) {
			addIndicator(page, toString(probability), "项目按期完工概率", "layui-bg-blue", "#ffffff", "#ffffff");
		} else if (probability >= 0.5) {
			addIndicator(page, toString(probability), "项目按期完工概率", "layui-bg-orange", "#ffffff", "#ffffff");
		} else {
			addIndicator(page, toString(probability), "项目按期完工概率", "layui-bg-red", "#ffffff", "#ffffff");
		}

		addIndicator(page, toString(project.getSAR()), "计划完成率");
		addIndicator(page, toString(0.12), "一级计划如期完成率");// TODO

		new Composite(page, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fd = new FormData();
		carousel.setLayoutData(fd);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(sep);
		fd.bottom = new FormAttachment(100);
	}

	private Composite createPage(Composite parent, Carousel carousel) {
		Composite page = carousel.addPage(new Composite(carousel, SWT.NONE));
		page.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		GridLayout layout = new GridLayout(2, true);
		layout.horizontalSpacing = 16;
		layout.verticalSpacing = 16;
		layout.marginHeight = 16;
		layout.marginWidth = 16;
		page.setLayout(layout);
		return page;
	}

	private String toString(Object ind) {
		return Optional.ofNullable(ind).map(d -> new DecimalFormat("#0.0%").format(d)).orElse("</br>");
	}

	private Control addIndicator(Composite parent, String ind, String title, String css, String titleColor,
			String textColor) {
		Label btn = new Label(parent, SWT.CENTER);
		UserSession.bruiToolkit().enableMarkup(btn);
		btn.setHtmlAttribute("class", css);
		StringBuffer sb = new StringBuffer();
		sb.append("<div style='margin-top:16px;color:" + titleColor + ";'>" + title + "</div>");
		sb.append("<div style='font-size:30px;text-align:center;color:" + textColor + ";margin-top:8px;'>" + ind
				+ "</div>");
		btn.setText(sb.toString());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		btn.setLayoutData(data);
		return btn;
	}

	private Control addIndicator(Composite parent, String ind, String title) {
		return addIndicator(parent, ind, title, "brui_bg_lightgrey", "#757575", "#3f51b5");
	}

}