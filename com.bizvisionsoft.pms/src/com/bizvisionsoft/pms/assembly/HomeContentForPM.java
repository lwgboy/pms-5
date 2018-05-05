package com.bizvisionsoft.pms.assembly;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class HomeContentForPM {

	@Inject
	private IBruiService bruiService;


	@CreateUI
	private void createUI(Composite parent) {
		Text control1 = new Text(parent, SWT.BORDER);
		control1.setText("abc");
		Text control2 = new Text(parent, SWT.BORDER);
		control2.setText("bcd");
	}

}
