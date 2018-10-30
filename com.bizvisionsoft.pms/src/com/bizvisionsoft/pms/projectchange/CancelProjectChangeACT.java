package com.bizvisionsoft.pms.projectchange;

import java.util.Date;
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
	private void execute(@MethodParam(Execute.CONTEXT_INPUT_OBJECT) ProjectChange input) {
		InputDialog id = new InputDialog(brui.getCurrentShell(), "否决", "请填写否决意见", null, t -> {
			return t.trim().isEmpty() ? "请填写否决意见" : null;
		}).setTextMultiline(true);
		if (id.open() == InputDialog.OK) {
			ProjectChangeTask task = new ProjectChangeTask();
			task.user = brui.getCurrentUserId();
			task.projectChange_id = input.get_id();
			task.date = new Date();
			task.choice = ProjectChange.CHOICE_CANCEL;
			task.name = input.getConfimName(task.user);
			task.comment = id.getValue();
			List<Result> result = ServicesLoader.get(ProjectService.class).cancelProjectChange(task);
			if (result.isEmpty()) {
				Layer.message("变更申请已否决");
				brui.closeCurrentContent();
			}
		}
	}
}
