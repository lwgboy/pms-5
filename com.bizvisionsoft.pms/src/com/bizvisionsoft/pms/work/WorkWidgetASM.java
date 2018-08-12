package com.bizvisionsoft.pms.work;

import java.util.Calendar;
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
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.assembly.SchedulerPart;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.tools.Util;
import com.mongodb.BasicDBObject;

public class WorkWidgetASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private GridPart workPane;

	private SchedulerPart calPan;

	@CreateUI
	public void createUI(Composite parent) {
		parent.setHtmlAttribute("class", "brui_borderRight brui_borderBottom");

		parent.setLayout(new FormLayout());

		StickerTitlebar bar = new StickerTitlebar(parent, null, null).setText("待处理工作")
				.setActions(context.getAssembly().getActions());
		bar.addListener(SWT.Selection, l -> {
			if ("打开".equals(((Action) l.data).getName())) {
				brui.switchContent(brui.getAssembly("我的工作"), null);
			} else if ("查询".equals(((Action) l.data).getName())) {
				workPane.openQueryEditor();
			}
		});
		
		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 48;

		Composite content = UserSession.bruiToolkit().newContentPanel(parent);
		content.setBackground(BruiColors.getColor(BruiColor.Grey_200));
		fd = new FormData();
		content.setLayoutData(fd);
		fd.left = new FormAttachment(0, 8);
		fd.top = new FormAttachment(bar, 8);
		fd.right = new FormAttachment(100, -8);
		fd.bottom = new FormAttachment(100, -8);

		content.setLayout(new FormLayout());

		Composite cal = createSlider(content);//
		// Label sep = new Label(content, SWT.SEPARATOR | SWT.HORIZONTAL);
		Composite grid = createBottomAsm(content);
		grid.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		fd = new FormData();
		cal.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.right = new FormAttachment(100);
		fd.top = new FormAttachment();
		fd.height = 360;

		// fd = new FormData();
		// sep.setLayoutData(fd);
		// fd.left = new FormAttachment();
		// fd.right = new FormAttachment(100);
		// fd.top = new FormAttachment(cal);
		// fd.height = 1;

		fd = new FormData();
		grid.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.top = new FormAttachment(cal, 1);
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);

	}

	private Composite createSlider(Composite parent) {
		Carousel carousel = new Carousel(parent, SWT.NONE);
		carousel.setAnimation("default");
		carousel.setInterval(3000);
		carousel.setIndicator("none");

		Composite page = carousel.addPage(new Composite(carousel, SWT.NONE));
		page.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		createCalendarSelector(page);
		
		page = carousel.addPage(new Composite(carousel, SWT.NONE));
		page.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		createShortcut(page);

		return carousel;
	}

	private void createShortcut(Composite parent) {
		GridLayout layout = new GridLayout(2, true);
		layout.horizontalSpacing = 24;
		layout.verticalSpacing = 24;
		layout.marginHeight = 24;
		layout.marginWidth = 24;
		parent.setLayout(layout);
		
		addIndicator(parent, "12", "待处理工作");
		addIndicator(parent, "4", "待指派工作");
		addIndicator(parent, "10", "待确认项目报告");
		addIndicator(parent, "12", "待批准的项目变更");
		
	}
	
	private Control addIndicator(Composite parent, String ind, String title, String css, String titleColor,
			String textColor) {
		Label btn = new Label(parent, SWT.CENTER);
		UserSession.bruiToolkit().enableMarkup(btn);
		btn.setHtmlAttribute("class", css);
		StringBuffer sb = new StringBuffer();
		sb.append("<div style='margin-top:8px;color:" + titleColor + ";'>" + title + "</div>");
		sb.append("<div style='font-size:48px;text-align:center;color:" + textColor + ";margin-top:16px;'>" + ind
				+ "</div>");
		btn.setText(sb.toString());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		btn.setLayoutData(data);
		return btn;
	}

	private Control addIndicator(Composite parent, String ind, String title) {
		return addIndicator(parent, ind, title, "brui_bg_lightgrey", "#757575", "#009688");
	}

	private Composite createCalendarSelector(Composite parent) {
		parent.setLayout(new FillLayout());
		AssemblyContainer asm = new AssemblyContainer(parent, context).setAssembly(brui.getAssembly("我的待处理工作日历选择器"))
				.setServices(brui).create();
		calPan = (SchedulerPart) asm.getContext().getContent();
		calPan.addPostSelectionChangedListener(i -> {
			String elem = (String) i.getStructuredSelection().getFirstElement();
			Date date = Util.str_date(elem);
			if (date != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				// Date start = cal.getTime();
				cal.add(Calendar.DATE, 1);
				cal.add(Calendar.SECOND, -1);
				Date end = cal.getTime();

				// 查询截至选中日之前要完成的工作
				BasicDBObject planFinish = new BasicDBObject("planFinish", new BasicDBObject("$lte", end));
				workPane.doQuery(planFinish);

			}
		});
		return asm.getContainer();

	}

	private Composite createBottomAsm(Composite parent) {
		AssemblyContainer asm = new AssemblyContainer(parent, context).setAssembly(brui.getAssembly("我的待处理工作（工作抽屉）"))
				.setServices(brui).create();
		asm.getContext().setName("worklist");
		workPane = (GridPart) asm.getContext().getContent();
		return asm.getContainer();
	}

}
