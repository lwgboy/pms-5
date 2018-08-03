package com.bizvisionsoft.pms.projectchange;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class ConfirmProjectChangeACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		ProjectChange input = (ProjectChange) context.getInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "关闭变更申请", "请确认变更后的项目进度计划已编制完成并提交。");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(ProjectService.class).confirmProjectChange(Arrays.asList(input.get_id()),
				brui.getCurrentUserId());
		if (result.isEmpty()) {
			Layer.message("变更已关闭");
			brui.closeCurrentContent();
		}
	}
}
