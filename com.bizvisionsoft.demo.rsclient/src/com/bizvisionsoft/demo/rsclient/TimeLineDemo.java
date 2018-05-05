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
		tl.append("3月17日上午", "        中国正在等待一个时刻到来，每个人都将是见证者，这一时刻前所未有。"
				+ "        <br>上午10时38分，在十三届全国人大一次会议第五次全体会议上，习近平全票当选中华人民共和国主席、中华人民共和国中央军事委员会主席。" + "      ");
		tl.append("8月17日", "        中国人民抗日战争胜利72周年" + "        <br>常常在想，尽管对这个国家有这样那样的抱怨，但我们的确生在了最好的时代"
				+ "        <br>铭记、感恩" + "        <br>所有为中华民族浴血奋战的英雄将士" + "        <br>永垂不朽" + "      ");
		tl.append("8月18日", "        中国人民抗日战争胜利72周年" + "        <br>常常在想，尽管对这个国家有这样那样的抱怨，但我们的确生在了最好的时代"
				+ "        <br>铭记、感恩" + "        <br>所有为中华民族浴血奋战的英雄将士" + "        <br>永垂不朽" + "      ");
		tl.append("8月20日", "" + "        中国人民抗日战争胜利72周年" + "        <br>常常在想，尽管对这个国家有这样那样的抱怨，但我们的确生在了最好的时代"
				+ "        <br>铭记、感恩" + "        <br>所有为中华民族浴血奋战的英雄将士" + "        <br>永垂不朽");
	}


}
