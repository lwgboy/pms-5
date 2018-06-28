package com.bizvisionsoft.pms.projectchange;

import java.util.Date;
import java.util.List;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.model.ProjectChangeTask;
import com.bizvisionsoft.service.model.Result;

public class ConfirmProjectChangeACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		ProjectChange input = (ProjectChange) context.getInput();
		ProjectChangeTask task = new ProjectChangeTask();
		task.id = brui.getCurrentUserId();
		task.projectChange_id = input.get_id();
		task.date = new Date();
		task.choice = ProjectChange.CHOICE_CONFIRM;
		task.name = input.getConfimName(task.id);
		Editor.open("确认变更编辑器", context, task, (r, o) -> {
			List<Result> result = ServicesLoader.get(ProjectService.class).confirmProjectChange(o);
			if (result.isEmpty()) {
				Layer.message("变更申请已确认。");
				brui.closeCurrentContent();
			}
		});

	}
}
