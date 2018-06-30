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

public class SubmitProjectChangeACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		ProjectChange input = (ProjectChange) context.getInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "提交变更申请", "请确认提交变更申请。\n系统将记录现在时刻为变更申请提交时间，提交后该变更申请将无法进行修改。");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(ProjectService.class).submitProjectChange(Arrays.asList(input.get_id()));
		if (result.isEmpty()) {
			Layer.message("变更申请已提交。");
			brui.closeCurrentContent();
		} else {
			if (result.get(0).code == Result.CODE_PROJECTCHANGE_NOTASKUSER)
				Layer.message("请为所有审核环节指定审核人员后，再进行提交。", Layer.ICON_CANCEL);
		}
	}
}
