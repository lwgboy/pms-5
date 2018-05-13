package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class CheckSchedule {
	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Workspace workspace = null;
		Object rootInput = context.getRootInput();
		if (rootInput instanceof Project) {
			workspace = ((Project) rootInput).getWorkspace();
		} else if (rootInput instanceof Work) {
			workspace = ((Work) rootInput).getWorkspace();
		}
		if (workspace != null) {
			Result result = Services.get(WorkSpaceService.class).schedulePlanCheck(workspace,
					 !(rootInput instanceof Project));

			if (Result.CODE_SUCCESS == result.code) {
				MessageDialog.openFinished(bruiService.getCurrentShell(), "检查结果", result.message);
			} else {
				MessageDialog.openError(bruiService.getCurrentShell(), "检查结果", result.message);
			}
		}
	}
}
