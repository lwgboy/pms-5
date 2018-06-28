package com.bizvisionsoft.pms.project;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.model.Project;

public class ProjecBasicInfoWidgetASM {
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
		title.setText("��Ŀ������Ϣ");
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

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout l = new GridLayout(3, true);
		l.horizontalSpacing = 16;
		l.verticalSpacing = 16;
		l.marginHeight = 16;
		l.marginWidth = 16;
		panel.setLayout(l);
		fd = new FormData();
		panel.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.top = new FormAttachment(sep);
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);

		addIndicator(panel, Util.getFormatText(project.getPlanStart()), "�ƻ���ʼ");
		addIndicator(panel, Util.getFormatText(project.getPlanFinish()), "�ƻ����");
		addIndicator(panel, project.getPlanDuration() + "��", "�ƻ�����");
		

		String overdue = getOverdueHtml();
		if ("����".equals(overdue)) {
			addIndicator(panel, overdue, "����Ԥ��", "layui-bg-orange","#ffffff","#ffffff");
		} else if ("��".equals(overdue)) {
			addIndicator(panel, overdue, "����Ԥ��", "layui-bg-red","#ffffff","#ffffff");
		} else if ("��".equals(overdue)) {
			addIndicator(panel, overdue, "����Ԥ��", "layui-bg-orange","#ffffff","#ffffff");
		} else if ("��".equals(overdue)) {
			addIndicator(panel, overdue, "����Ԥ��", "layui-bg-blue","#ffffff","#ffffff");
		} else {
			addIndicator(panel, "", "����Ԥ��");
		}
		addIndicator(panel, Util.getFormatText(project.getEstimateFinish()), "Ԥ�����");
		
		addIndicator(panel, Util.getFormatText(project.getEstimateDuration())+"��", "Ԥ�ƹ���");

		
	}

	private String getOverdueHtml() {
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

	private Control addIndicator(Composite parent, String ind, String title, String css, String titleColor,
			String textColor) {
		Label btn = new Label(parent, SWT.CENTER);
		UserSession.bruiToolkit().enableMarkup(btn);
		btn.setHtmlAttribute("class", css);
		StringBuffer sb = new StringBuffer();
		sb.append("<div style='margin-top:16px;color:" + titleColor + ";'>" + title + "</div>");
		sb.append("<div class='label_headline' style='text-align:center;color:" + textColor + ";margin-top:8px;'>" + ind
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
