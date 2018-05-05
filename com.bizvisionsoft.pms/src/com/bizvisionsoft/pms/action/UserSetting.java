package com.bizvisionsoft.pms.action;

import org.eclipse.jface.dialogs.InputDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class UserSetting {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute() {
		InputDialog id = new InputDialog(bruiService.getCurrentShell(), "Demo JFace Dialog", "演示Jface输入对话框",
				"hello world!", null);
		id.open();

	}

}
