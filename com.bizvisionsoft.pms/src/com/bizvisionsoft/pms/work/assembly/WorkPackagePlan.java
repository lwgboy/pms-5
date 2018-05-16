package com.bizvisionsoft.pms.work.assembly;

import java.util.List;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.model.Work;

public class WorkPackagePlan {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;


	@CreateUI
	private void createUI(Composite parent) {
		Work work = (Work) context.getInput();
		
		List<String> packageAssemblyId = work.getPackageAssemblyId();
		
		if (Util.isEmptyOrNull(packageAssemblyId)) {
			parent.setLayout(new FillLayout());
			new AssemblyContainer(parent, context).setInput(work).setAssembly(brui.getAssembly("工作包-基本"))
					.setServices(brui).create();
		} else {
			FillLayout layout = new FillLayout();
			layout.marginHeight = 16;
			layout.marginWidth = 16;
			parent.setLayout(layout);
			TabFolder folder = new TabFolder(parent, SWT.TOP | SWT.BORDER);
			Optional.ofNullable(packageAssemblyId).ifPresent(ids -> ids.forEach(id -> {
				Assembly assembly = brui.getAssembly(id);
				TabItem item = new TabItem(folder, SWT.NONE);
				item.setText(assembly.getTitle());
				Composite pageContent = new Composite(folder, SWT.NONE);
				pageContent.setLayout(new FillLayout());
				new AssemblyContainer(pageContent, context).setInput(work).setAssembly(brui.getAssembly(id))
						.setServices(brui).create();
				item.setControl(pageContent);
			}));
			folder.setSelection(0);
		}
	}

}
