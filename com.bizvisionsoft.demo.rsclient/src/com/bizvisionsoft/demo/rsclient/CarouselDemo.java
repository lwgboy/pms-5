package com.bizvisionsoft.demo.rsclient;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bizivisionsoft.widgets.carousel.Carousel;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.GetContainer;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.bruiengine.BruiAssemblyEngine;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.IServiceWithId;
import com.bizvisionsoft.bruiengine.session.UserSession;
import com.bizvisionsoft.bruiengine.ui.BruiToolkit;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;

public class CarouselDemo {

	@Inject
	private IBruiService bruiService;

	@GetContainer
	private Composite content;

	@CreateUI
	private void createUI(Composite parent) {
		parent.setLayout(new FillLayout());
		createCarousel(parent);
	}

	private void createCarousel(Composite parent) {
		Carousel carousel = new Carousel(parent, SWT.NONE);

		// 放一个Label
		Label page = carousel.addPage(new Label(carousel, SWT.NONE));
		UserSession.bruiToolkit().enableMarkup(page);
		page.setText("<div style='font-size:24px'> 这个是轮播的第一页，后面是两个不同颜色的容器 </div>");

		carousel.addPage(new Composite(carousel, SWT.NONE)).setBackground(BruiColors.getColor(BruiColor.Indigo_500));

		carousel.addPage(new Composite(carousel, SWT.NONE)).setBackground(BruiColors.getColor(BruiColor.Teal_700));

		page = carousel.addPage(new Label(carousel, SWT.NONE));
		UserSession.bruiToolkit().enableMarkup(page);
		page.setText("<div style='font-size:24px'> 接下来显示一个TableViewer</div>");

		TableViewer viewer = new TableViewer(carousel, SWT.NONE);
		carousel.addPage(viewer.getControl());
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider());
		viewer.setInput(new String[] { "page3-1", "表格行2", "表格行3", "表格行4" });

		page = carousel.addPage(new Label(carousel, SWT.NONE));
		UserSession.bruiToolkit().enableMarkup(page);
		page.setText("<div style='vertical-align: middle;font-size:24px'> 因为可以放置容器，所以可以构造一个复杂一点界面</div>");

		Composite container = carousel.addPage(new Composite(carousel, SWT.NONE));
		container.setLayout(new FormLayout());
		Text text = new Text(container, SWT.BORDER);
		FormData fd = new FormData();
		text.setLayoutData(fd);
		fd.left = new FormAttachment(0, 16);
		fd.top = new FormAttachment(0, 16);
		fd.right = new FormAttachment(100, -16);

		Button btn = new Button(container, SWT.PUSH);
		btn.setData(RWT.CUSTOM_VARIANT, BruiToolkit.CSS_NORMAL);
		btn.setText("确定");
		fd = new FormData();
		btn.setLayoutData(fd);
		fd.left = new FormAttachment(0, 16);
		fd.top = new FormAttachment(text, 16);

		Button btn1 = new Button(container, SWT.PUSH);
		btn1.setData(RWT.CUSTOM_VARIANT, BruiToolkit.CSS_INFO);
		btn1.setText("查看详细");
		fd = new FormData();
		btn1.setLayoutData(fd);
		fd.left = new FormAttachment(btn, 16);
		fd.top = new FormAttachment(text, 16);

		TableViewer viewer1 = new TableViewer(container, SWT.BORDER);
		viewer1.setContentProvider(ArrayContentProvider.getInstance());
		viewer1.setLabelProvider(new LabelProvider());
		viewer1.setInput(new String[] { "行1", "行2", "行3", "行4" });
		fd = new FormData();
		viewer1.getTable().setLayoutData(fd);
		fd.left = new FormAttachment(0, 16);
		fd.top = new FormAttachment(btn1, 16);
		fd.right = new FormAttachment(100, -16);
		fd.bottom = new FormAttachment(100, -16);

		page = carousel.addPage(new Label(carousel, SWT.NONE));
		UserSession.bruiToolkit().enableMarkup(page);
		page.setText("<div style='vertical-align: middle;font-size:24px'> 当然也可以加一个重量级的组件</div>");

		container = carousel.addPage(new Composite(carousel, SWT.NONE));
		BruiAssemblyEngine brui = BruiAssemblyEngine.newInstance(ModelLoader.site.getAssemblyByName("用户管理"));
		brui.init(new IServiceWithId[] { bruiService, new BruiAssemblyContext().setEngine(brui) }).createUI(container);
	}

}
