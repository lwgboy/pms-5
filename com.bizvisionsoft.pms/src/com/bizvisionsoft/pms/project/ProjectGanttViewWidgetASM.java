package com.bizvisionsoft.pms.project;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.GetContainer;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Controls;
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
		Controls.handle(parent).layout(new FormLayout()).bg(BruiColor.White);

		Label title = Controls.label(parent).setText("进度进展甘特图").fg(BruiColor.Grey_600).left(0, 8).top(0, 8).get();

		Label btn = Controls.label(parent, SWT.RIGHT)//
				.margin(8).mTop().mRight().width(24)
				.html("<i class='layui-icon layui-icon-senior' style='font-size:18px;cursor:pointer;'></i>")//
				.tooltips("显示关键路径").listen(SWT.MouseUp, e -> showCriticalPath()).get();

		Controls.label(parent, SWT.RIGHT)//
				.margin(8).mTop().mRight(btn).width(24)
				.html("<i class='layui-icon layui-icon-util' style='font-size:18px;cursor:pointer;'></i>")//
				.tooltips("时间刻度").listen(SWT.MouseUp, e -> setTimeScale()).get();

		content = Controls.label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).bg(BruiColor.Grey_50).left().top(title, 4).right().height(1)
				.add(()->Controls.comp(parent).loc()).get();
	}

	private void showCriticalPath() {
		GanttPart part = (GanttPart) context.getChildContextByName("gantt").getContent();
		part.switchCriticalPathHighLight();
	}

	private void setTimeScale() {
		Editor.open("设置时间刻度", context, new TimeScaleType(), (d, r) -> {
			GanttPart part = (GanttPart) context.getChildContextByName("gantt").getContent();
			part.setScaleType(r.type);
		});
	}

}
