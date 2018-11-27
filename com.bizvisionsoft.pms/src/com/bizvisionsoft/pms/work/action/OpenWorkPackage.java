package com.bizvisionsoft.pms.work.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.workpackage.action.SwitchWorkPackagePage;
import com.bizvisionsoft.service.model.IWorkPackageMaster;

public class OpenWorkPackage {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) IWorkPackageMaster work) {
		SwitchWorkPackagePage.openWorkPackage(brui, work);
	}

}
