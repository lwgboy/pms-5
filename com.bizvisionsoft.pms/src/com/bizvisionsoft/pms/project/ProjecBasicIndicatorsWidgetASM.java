package com.bizvisionsoft.pms.project;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.eclipse.rap.rwt.RWT;
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
import com.bizvisionsoft.service.tools.Formatter;

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
		carousel.setInterval(5000);
		carousel.setIndicator("none");

		Composite page = createPage(parent, carousel, 3);
		addIndicator(page, Formatter.getString(project.getPlanStart(),null, RWT.getLocale()), "计划开始");
		addIndicator(page, Formatter.getString(project.getPlanFinish(),null, RWT.getLocale()), "计划完成");
		addIndicator(page, project.getPlanDuration() + "天", "计划工期");
		String overdue = getOverdueHtml();
		if ("超期".equals(overdue)) {
			addIndicator(page, overdue, "进度预警", "layui-bg-orange", "#ffffff", "#ffffff");
		} else if ("Ⅰ级".equals(overdue)) {
			addIndicator(page, overdue, "进度预警", "layui-bg-red", "#ffffff", "#ffffff");
		} else if ("Ⅱ级".equals(overdue)) {
			addIndicator(page, overdue, "进度预警", "layui-bg-orange", "#ffffff", "#ffffff");
		} else if ("Ⅲ级".equals(overdue)) {
			addIndicator(page, overdue, "进度预警", "layui-bg-blue", "#ffffff", "#ffffff");
		} else {
			addIndicator(page, "", "进度预警");
		}
		addIndicator(page, Formatter.getString(project.getEstimateFinish(),null, RWT.getLocale()), "估算完工日期");

		addIndicator(page, Formatter.getString(project.getEstimateDuration(),null, RWT.getLocale()) + "天", "估算工期");

		page = createPage(parent, carousel, 2);

		List<List<Double>> values = project.getDurationForcast();
		if (values != null) {
			// 乐观
			Double t = values.get(0).get(0);
			Calendar cal = Calendar.getInstance();
			cal.setTime(project.getPlanStart());
			cal.add(Calendar.DATE, t.intValue());
			String text = Formatter.getString(cal.getTime(),null, RWT.getLocale()) + "<br/>"
					+ toString(values.get(0).get(1).doubleValue() / 100);
			addIndicator(page, text, "乐观估计", "brui_bg_lightgrey", "#757575", "#009688");

			// 悲观
			t = values.get(1).get(0);
			cal.setTime(project.getPlanStart());
			cal.add(Calendar.DATE, t.intValue());
			text = Formatter.getString(cal.getTime(),null, RWT.getLocale());
			addIndicator(page, text, "悲观估计", "brui_bg_lightgrey", "#757575", "#ff9800");

			// 最可能
			t = values.get(2).get(0);
			cal.setTime(project.getPlanStart());
			cal.add(Calendar.DATE, t.intValue());
			text = Formatter.getString(cal.getTime(),null, RWT.getLocale()) + "<br/>" + toString(values.get(2).get(1).doubleValue() / 100);
			addIndicator(page, text, "最可能的完工日期", "brui_bg_lightgrey", "#757575", "#03a9f4");
		}else {
			addIndicator(page, "尚未计算", "乐观估计");
			addIndicator(page, "尚未计算", "悲观估计");
			addIndicator(page, "尚未计算", "最可能的完工日期");

		}

		Double probability = project.getDurationProbability();
		if (probability == null) {
			addIndicator(page, "尚未计算", "按期计划完工概率");
		} else {
			String text = Formatter.getString(project.getPlanFinish(),null, RWT.getLocale()) + "<br/>" + toString(probability)+"</span>";
			if (probability >= 0.9) {
				addIndicator(page, text, "按计划完工概率", "layui-bg-green", "#ffffff", "#ffffff");
			} else if (probability >= 0.7) {
				addIndicator(page, text, "按计划完工概率", "layui-bg-blue", "#ffffff", "#ffffff");
			} else if (probability >= 0.5) {
				addIndicator(page, text, "按计划完工概率", "layui-bg-orange", "#ffffff", "#ffffff");
			} else {
				addIndicator(page, text, "按期完工概率", "layui-bg-red", "#ffffff", "#ffffff");
			}
		}

		page = createPage(parent, carousel, 3);
		addCycleIndicator(page, project.getWAR(), "工作量完成率");
		addCycleIndicator(page, project.getDAR(), "工期完成率");
		addCycleIndicator(page, project.getCAR(), "预算使用率");
		

		page = createPage(parent, carousel, 2);

		addIndicator(page, toString(project.getSAR()), "计划完成率");
//		addIndicator(page, toString(0.12), "一级计划如期完成率");// TODO
		addIndicator(page, toString(project.getBDR()), "预算偏差率");

		new Composite(page, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		new Composite(page, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fd = new FormData();
		carousel.setLayoutData(fd);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(sep);
		fd.bottom = new FormAttachment(100);
	}

	private Composite createPage(Composite parent, Carousel carousel, int colCount) {
		Composite page = carousel.addPage(new Composite(carousel, SWT.NONE));
		page.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		GridLayout layout = new GridLayout(colCount, true);
		layout.horizontalSpacing = 16;
		layout.verticalSpacing = 16;
		layout.marginHeight = 16;
		layout.marginWidth = 16;
		page.setLayout(layout);
		return page;
	}

	private String toString(Object ind) {
		return Optional.ofNullable(ind).map(d -> Formatter.getString(d, "#0.0%", null)).orElse("</br>");
	}

	private Control addCycleIndicator(Composite parent, Double ind, String title) {
		Label btn = new Label(parent, SWT.CENTER);
		UserSession.bruiToolkit().enableMarkup(btn);
		btn.setHtmlAttribute("class", "brui_bg_lightgrey");
		StringBuffer sb = new StringBuffer();
		sb.append("<div style='margin-top:8px;color:#757575;'>" + title + "</div>");
		ind = (ind==null||ind<0)?0:(double)Math.round(ind*100)/100;
		
		String url = "/bvs/svg?type=progress&percent="+ind+"&bgColor=81d4fa&fgColor=0091ea";
		sb.append("<img style='margin-top:32px;' src='"+url+"' width=140 height=140/>");
		btn.setText(sb.toString());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		btn.setLayoutData(data);
		return btn;
	}
	
	
	private Control addIndicator(Composite parent, String ind, String title, String css, String titleColor,
			String textColor) {
		Label btn = new Label(parent, SWT.CENTER);
		UserSession.bruiToolkit().enableMarkup(btn);
		btn.setHtmlAttribute("class", css);
		StringBuffer sb = new StringBuffer();
		sb.append("<div style='margin-top:8px;color:" + titleColor + ";'>" + title + "</div>");
		sb.append("<div style='font-size:24px;text-align:center;color:" + textColor + ";margin-top:8px;'>" + ind
				+ "</div>");
		btn.setText(sb.toString());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		btn.setLayoutData(data);
		return btn;
	}

	private Control addIndicator(Composite parent, String ind, String title) {
		return addIndicator(parent, ind, title, "brui_bg_lightgrey", "#757575", "#009688");
	}

	private String getOverdueHtml() {
		Date _actual = project.getActualFinish();
		Date _plan = project.getPlanFinish();
		if (_actual == null) {
			_actual = new Date();
		}
		// 如果当前时间或完成时间已经超过了计划完成，提示为超期
		if (_actual.after(_plan)) {
			return "超期";
		}

		if (project.getOverdueIndex() != null) {
			switch (project.getOverdueIndex()) {
			case 0:
				return "Ⅰ级";
			case 1:
				return "Ⅱ级";
			case 2:
				return "Ⅲ级";
			}
		}
		return "";
	}

}