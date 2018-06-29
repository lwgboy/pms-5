package com.bizvisionsoft.pms.project;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.GetContainer;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.pms.work.gantt.action.TimeScaleType;

public class ProjectGanttViewWidgetASM {
	
	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	@GetContainer
	private Composite content;


	@CreateUI
	public void createUI(Composite parent) {
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		FormLayout layout = new FormLayout();
		parent.setLayout(layout);
		
		Label title = new Label(parent, SWT.NONE);
		title.setText("进度进展甘特图");
		title.setForeground(BruiColors.getColor(BruiColor.Grey_600));

		FormData fd = new FormData();
		title.setLayoutData(fd);
		fd.left = new FormAttachment(0, 8);
		fd.top = new FormAttachment(0, 8);

		Label btn = new Label(parent,SWT.RIGHT);
		UserSession.bruiToolkit().enableMarkup(btn);
		fd = new FormData();
		btn.setLayoutData(fd);
		fd.top = new FormAttachment(0, 8);
		fd.right = new FormAttachment(100, -8);
		fd.width = 24;
		btn.setText("<i class='layui-icon layui-icon-senior' style='font-size:18px;cursor:pointer;'></i>");
		btn.addListener(SWT.MouseUp, e->showCriticalPath());
		btn.setToolTipText("显示关键路径");
		
		Label btn1 = new Label(parent,SWT.RIGHT);
		UserSession.bruiToolkit().enableMarkup(btn1);
		fd = new FormData();
		btn1.setLayoutData(fd);
		fd.top = new FormAttachment(0, 8);
		fd.right = new FormAttachment(btn, -8);
		fd.width = 24;
		btn1.setText("<i class='layui-icon layui-icon-util' style='font-size:18px;cursor:pointer;'></i>");
		btn1.addListener(SWT.MouseUp, e->setTimeScale());
		btn1.setToolTipText("时间刻度");
		
		Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setBackground(BruiColors.getColor(BruiColor.Grey_50));
		
		fd = new FormData();
		sep.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(title, 4);
		fd.right = new FormAttachment(100);
		fd.height = 1;
		
		content = new Composite(parent,SWT.NONE);
		fd = new FormData();
		content.setLayoutData(fd);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(sep);
		fd.bottom = new FormAttachment(100);
	}


	private void showCriticalPath() {
		GanttPart part = (GanttPart) context.getChildContextByName("gantt").getContent();
		part.switchCriticalPathHighLight();
	}
	
	private void setTimeScale() {
		Editor.open("设置时间刻度", context, new TimeScaleType(), (d,r)->{
			GanttPart part = (GanttPart) context.getChildContextByName("gantt").getContent();
			part.setScaleType(r.type);
		});
	}

}
