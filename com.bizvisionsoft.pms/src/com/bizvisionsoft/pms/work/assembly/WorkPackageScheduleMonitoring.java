package com.bizvisionsoft.pms.work.assembly;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.BruiActionEngine;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.session.UserSession;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;

public class WorkPackageScheduleMonitoring {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private Work work;

	private BruiAssemblyContext currentContext;

	private TrackView packageSettings;

	@CreateUI
	public void createUI(Composite parent) {
		work = (Work) context.getInput();
		packageSettings = work.scheduleMonitoring;

		parent.setLayout(new FormLayout());

		Action closeAction = new Action();
		closeAction.setName("close");
		closeAction.setImage("/img/close.svg");

		StickerTitlebar bar = new StickerTitlebar(parent, closeAction, null);
		String text = "工作包: " + work;
		// if (packageSettings != null && packageSettings.size() == 1) {
		// text += "-"+packageSettings.get(0).getName();
		// }

		bar.setText(text);
		bar.setActions(context.getAssembly().getActions());

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

		bar.addListener(SWT.Selection, e -> {
			Action action = ((Action) e.data);
			if ("close".equals(action.getName())) {
				brui.closeCurrentContent();
			} else {
				try {
					BruiActionEngine.create(action, brui).invokeExecute(action ,e, currentContext);
				} catch (Exception e2) {
					e2.printStackTrace();
					MessageDialog.openError(brui.getCurrentShell(), "系统错误", e2.getMessage());
				}
			}
		});

		createContent(content);
	}

	private void createContent(Composite parent) {
		if (packageSettings == null) {
			parent.setLayout(new FillLayout());
			AssemblyContainer c = new AssemblyContainer(parent, context).setAssembly(brui.getAssembly("工作包-基本"))
					.setServices(brui).create();
			this.currentContext = c.getContext();
		} else {
			parent.setLayout(new FillLayout());
			AssemblyContainer c = new AssemblyContainer(parent, context).setInput(packageSettings)
					.setAssembly(brui.getAssembly(packageSettings.getPackageAssembly())).setServices(brui).create();
			this.currentContext = c.getContext();
		}
	}

}
