package com.bizvisionsoft.pms.workpackage.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.pms.work.assembly.WorkPackagePlan;

public class CreatePlan {

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		((WorkPackagePlan) context.getContent()).doCreate();
	}

}
