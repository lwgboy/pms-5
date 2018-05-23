package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;

public class CheckSchedule {
	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		IWBSScope rootInput = (IWBSScope) context.getRootInput();
		if (rootInput != null) {
			Workspace workspace = rootInput.getWorkspace();
			if (workspace != null) {
				Boolean checkManageItem = true;
				if (rootInput instanceof Project) {
					checkManageItem = false;
				}
				Result result = Services.get(WorkSpaceService.class).schedulePlanCheck(workspace, checkManageItem);

				if (Result.CODE_WORK_SUCCESS == result.code) {
					MessageDialog.openInformation(bruiService.getCurrentShell(), "�����", result.message);
				} else {
					MessageDialog.openError(bruiService.getCurrentShell(), "�����",
							"����ڵ� <b style='color:red;'>" + result.data + "</b> ���ʱ�䳬���޶���");
				}
			}
		}
	}
}
