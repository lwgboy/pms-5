package com.bizvisionsoft.pms.work;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.assembly.SchedulerPart;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.session.UserSession;
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
		fd.left = new FormAttachment(0, 12);
		fd.top = new FormAttachment(bar, 12);
		fd.right = new FormAttachment(100, -12);
		fd.bottom = new FormAttachment(100, -12);

		content.setLayout(new FormLayout());

		Composite grid = createTopAsm(content);
		grid.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		Label sep = new Label(content, SWT.SEPARATOR | SWT.HORIZONTAL);
		Composite cal = createBottomAsm(content);

		fd = new FormData();
		cal.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.right = new FormAttachment(100);
		fd.top = new FormAttachment();
		fd.height = 360;

		fd = new FormData();
		sep.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.right = new FormAttachment(100);
		fd.top = new FormAttachment(cal);
		fd.height = 1;

		fd = new FormData();
		grid.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.top = new FormAttachment(sep);
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);

	}

	private Composite createBottomAsm(Composite parent) {
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
//				Date start = cal.getTime();
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

	private Composite createTopAsm(Composite parent) {
		AssemblyContainer asm = new AssemblyContainer(parent, context).setAssembly(brui.getAssembly("我的待处理工作（工作抽屉）"))
				.setServices(brui).create();
		workPane = (GridPart) asm.getContext().getContent();
		return asm.getContainer();
	}

}
