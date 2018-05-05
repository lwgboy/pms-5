package com.bizvisionsoft.pms.assembly;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.GetContainer;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.session.UserSession;
import com.bizvisionsoft.bruiengine.ui.BruiToolkit;

public class NestTestContainer {

	@Inject
	private IBruiService bruiService;

	@GetContainer
	private Composite content;


	@CreateUI
	private void createUI(Composite parent) {
		parent.setLayout(new FormLayout());
		Label title = UserSession.bruiToolkit().newStyledControl(Label.class, parent, SWT.NONE, BruiToolkit.CSS_TEXT_TITLE);
		title.setText("²âÊÔµÄÇ¶Ì×ÈÝÆ÷×é¼þ");
		Label sep = UserSession.bruiToolkit().newStyledControl(Label.class, parent, SWT.SEPARATOR|SWT.HORIZONTAL, BruiToolkit.CSS_WARNING);
		content = new Composite(parent,SWT.NONE);
		
		FormData fd = new FormData();
		title.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.top = new FormAttachment();
		fd.right= new FormAttachment(100);
		fd.height = 48;
		
		fd = new FormData();
		sep.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.top = new FormAttachment(title,8);
		fd.right = new FormAttachment(100);
		
		fd = new FormData();
		content.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.top = new FormAttachment(sep,8);
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);

				
				
	}

}
