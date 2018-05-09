package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.Brui;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.serviceconsumer.Services;

public class OpenGantt {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		IWBSScope wbsScope = (IWBSScope) context.getRootInput();
		if (Services.get(WorkService.class).checkOutSchedulePlanUnauthorized(wbsScope.getWBS_id(), brui.getCurrentUserId(),
				Brui.sessionManager.getSessionId())) {
			brui.switchContent("项目甘特图(编辑)", null);
		} else {
			brui.switchContent("项目甘特图", null);
		}
	}

}
