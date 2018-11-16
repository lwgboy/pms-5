package com.bizvisionsoft.pms.projectchange;

import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.model.ProjectChangeTask;
import com.bizvisionsoft.service.model.Result;

public class CancelProjectChangeACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.CONTEXT_INPUT_OBJECT) ProjectChange input,
			@MethodParam(Execute.CURRENT_USER_ID) String userid) {
		InputDialog id = new InputDialog(brui.getCurrentShell(), "否决", "请填写否决意见", null, t -> {
			return t.trim().isEmpty() ? "请填写否决意见" : null;
		}).setTextMultiline(true);
		if (id.open() == InputDialog.OK) {
			List<Result> result = ServicesLoader.get(ProjectService.class).cancelProjectChange(
					ProjectChangeTask.getCancelInstance(userid, input.get_id(), input.getConfimName(userid), id.getValue()));
			if (result.isEmpty()) {
				Layer.message("变更申请已否决");
				brui.closeCurrentContent();
			}
		}
	}
}
