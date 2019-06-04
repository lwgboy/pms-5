package com.bizvisionsoft.pms.work;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.assembly.SchedulerPart;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.tools.Formatter;
import com.mongodb.BasicDBObject;

/**
 * 使用WorkCardRender取代
 * 
 * @author hua
 *
 */
@Deprecated
public class WorkWidgetASM {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	private GridPart workPane;

	private SchedulerPart calPan;

	@CreateUI
	public void createUI(Composite parent) {
		parent.setHtmlAttribute("class", "brui_borderRight brui_borderBottom");

		parent.setLayout(new FormLayout());

		StickerTitlebar bar = new StickerTitlebar(parent, null, null).setText("待处理工作").setActions(context.getAssembly().getActions());
		bar.addListener(SWT.Selection, l -> {
			if ("打开".equals(((Action) l.data).getName())) {
				br.switchContent(br.getAssembly("我的工作.gridassy"), null);
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

		Composite content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FormLayout()).bg(BruiColor.Grey_200).get();

		Composite cal = createCalendarSelector(content);
		// Label sep = new Label(content, SWT.SEPARATOR | SWT.HORIZONTAL);
		Composite grid = createBottomAsm(content);
		grid.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		fd = new FormData();
		cal.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.right = new FormAttachment(100, 1);
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
		fd.right = new FormAttachment(100, 1);
		fd.bottom = new FormAttachment(100);

	}

	private Composite createCalendarSelector(Composite parent) {
		AssemblyContainer asm = new AssemblyContainer(parent, context).setAssembly(br.getAssembly("我的待处理工作日历选择器.schedulerassy"))//TODO ???
				.setServices(br).create();
		calPan = (SchedulerPart) asm.getContext().getContent();
		calPan.addPostSelectionChangedListener(i -> {
			String elem = (String) i.getStructuredSelection().getFirstElement();
			Date date = Formatter.getDatefromJS(elem);
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
		AssemblyContainer asm = new AssemblyContainer(parent, context).setAssembly(br.getAssembly("我的待处理工作（工作抽屉）.assy"))// TODO ???
				.setServices(br).create();
		asm.getContext().setName("worklist");
		workPane = (GridPart) asm.getContext().getContent();
		return asm.getContainer();
	}

}
