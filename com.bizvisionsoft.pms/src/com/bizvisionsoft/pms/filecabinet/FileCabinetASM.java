package com.bizvisionsoft.pms.filecabinet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.session.UserSession;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;

public class FileCabinetASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private GridPart folderPane;

	private GridPart filePane;

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		StickerTitlebar bar = new StickerTitlebar(parent, null, null).setActions(context.getAssembly().getActions())
				.setText(context.getAssembly().getTitle());

		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 48;

		Composite content = UserSession.bruiToolkit().newContentPanel(parent);
		fd = new FormData();
		content.setLayoutData(fd);
		fd.left = new FormAttachment(0, 12);
		fd.top = new FormAttachment(bar, 12);
		fd.right = new FormAttachment(100, -12);
		fd.bottom = new FormAttachment(100, -12);
		content.setLayout(new FormLayout());

		AssemblyContainer left = new AssemblyContainer(content, context).setAssembly(brui.getAssembly("项目档案库文件夹"))
				.setServices(brui).create();
		folderPane = (GridPart) left.getContext().getContent();
		
		Label sep = new Label(content, SWT.SEPARATOR|SWT.VERTICAL);
		
		AssemblyContainer right = new AssemblyContainer(content, context).setAssembly(brui.getAssembly("项目档案库文件列表"))
				.setServices(brui).create();
		filePane = (GridPart) right.getContext().getContent();

		fd = new FormData();
		left.getContainer().setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.top = new FormAttachment();
		fd.right = new FormAttachment(30);
		fd.bottom = new FormAttachment(100);
		
		fd = new FormData();
		sep.setLayoutData(fd);
		fd.left = new FormAttachment(left.getContainer());
		fd.top = new FormAttachment();
		fd.bottom = new FormAttachment(100);
		fd.width = 1;
		
		fd = new FormData();
		right.getContainer().setLayoutData(fd);
		fd.left = new FormAttachment(sep);
		fd.top = new FormAttachment();
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);
		
	}

}
