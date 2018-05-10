package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubmitSchedule {
	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		IWBSScope wbsScope = (IWBSScope) context.getRootInput();
		Result result = Services.get(WorkSpaceService.class).schedulePlanCheck(wbsScope.getCheckOutKey(),
				bruiService.getCurrentUserId());

		if (Result.CODE_SUCCESS == result.type) {
			result = Services.get(WorkSpaceService.class).checkInSchedulePlan(wbsScope.getCheckOutKey(),
					bruiService.getCurrentUserId());

			if (Result.CODE_SUCCESS == result.type) {
				bruiService.switchContent("ÏîÄ¿¸ÊÌØÍ¼", null);
			}
		}
	}
}
