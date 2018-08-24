package com.bizvisionsoft.pms.project;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
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
import com.bizvisionsoft.bruiengine.util.EngUtil;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class AdministeredProjectProgressWidgetASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	@Init
	private void init() {
	}

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FillLayout());

		Carousel carousel = new Carousel(parent, SWT.NONE);
		carousel.setAnimation("default");
		carousel.setInterval(5000);
		carousel.setIndicator("none");

		Services.get(ProjectService.class).listAdministratedProjects(new BasicDBObject(), brui.getCurrentUserId())
				.forEach(project -> createPage(carousel, project));
		

	}

	private void createPage(Carousel carousel, Project project) {
		Composite parent =  carousel.addPage(new Composite(carousel, SWT.NONE));
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		FormLayout layout = new FormLayout();
		parent.setLayout(layout);

		Label title = new Label(parent, SWT.NONE);
		title.setText(project.toString());
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

		Composite content = new Composite(parent,SWT.NONE);
		GridLayout ly = new GridLayout(3, true);
		ly.horizontalSpacing = 16;
		ly.verticalSpacing = 16;
		ly.marginHeight = 16;
		ly.marginWidth = 16;
		content.setLayout(ly);

		fd = new FormData();
		content.setLayoutData(fd);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(sep);
		fd.bottom = new FormAttachment(100);
		
		addIndicator(content, EngUtil.getFormatText(project.getPlanStart()), "�ƻ���ʼ");
		addIndicator(content, EngUtil.getFormatText(project.getPlanFinish()), "�ƻ����");
		addIndicator(content, project.getPlanDuration() + "��", "�ƻ�����");
		String overdue = getOverdueHtml(project);
		if ("����".equals(overdue)) {
			addIndicator(content, overdue, "����Ԥ��", "layui-bg-orange", "#ffffff", "#ffffff");
		} else if ("��".equals(overdue)) {
			addIndicator(content, overdue, "����Ԥ��", "layui-bg-red", "#ffffff", "#ffffff");
		} else if ("��".equals(overdue)) {
			addIndicator(content, overdue, "����Ԥ��", "layui-bg-orange", "#ffffff", "#ffffff");
		} else if ("��".equals(overdue)) {
			addIndicator(content, overdue, "����Ԥ��", "layui-bg-blue", "#ffffff", "#ffffff");
		} else {
			addIndicator(content, "", "����Ԥ��");
		}
		addIndicator(content, EngUtil.getFormatText(project.getEstimateFinish()), "�����깤����");
		addIndicator(content, EngUtil.getFormatText(project.getEstimateDuration()) + "��", "���㹤��");

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

	private String getOverdueHtml(Project project) {
		Date _actual = project.getActualFinish();
		Date _plan = project.getPlanFinish();
		if (_actual == null) {
			_actual = new Date();
		}
		// �����ǰʱ������ʱ���Ѿ������˼ƻ���ɣ���ʾΪ����
		if (_actual.after(_plan)) {
			return "����";
		}

		if (project.getOverdueIndex() != null) {
			switch (project.getOverdueIndex()) {
			case 0:
				return "��";
			case 1:
				return "��";
			case 2:
				return "��";
			}
		}
		return "";
	}
}