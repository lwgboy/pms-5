package com.bizvisionsoft.demo.rsclient;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.timeline.TimeLine;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.GetContainer;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class TimeLineDemo {

	@Inject
	private IBruiService bruiService;

	@GetContainer
	private Composite content;

	@CreateUI
	private void createUI(Composite parent) {
		parent.setLayout(new FillLayout());
		createTimeLine(parent);
	}

	private void createTimeLine(Composite parent) {
		TimeLine tl = new TimeLine(parent, SWT.BORDER);
		tl.append("3月17日上午", "样机完成结构力学试验，试验结果满足技术规格要求。有关试验机构已出具试验报告。");
		tl.append("8月17日", "因客户要求的变化，涉及到A模块多处研发更改，部分组件必须重新研发。杨文韬发起项目变更，预计项目将延期30天。");
		tl.append("8月18日", "方案研发完成。");
		tl.append("8月20日", "杨文韬下达了A模块结构研发，B模块结构研发等工作的计划。");
		tl.append("8月22日", "样机试验结果满足了要求。样机开发完成。");
		tl.append("9月24日", "杨文韬提交了A模块最终的研发成果物。");
		tl.append("10月9日", "A模块研发评审工作已完成。");
		tl.append("11月20日", "杨文韬发起到了A模块的研发评审工作。");
	}


}
