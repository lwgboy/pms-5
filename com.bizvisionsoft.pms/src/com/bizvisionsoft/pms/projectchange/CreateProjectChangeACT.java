package com.bizvisionsoft.pms.projectchange;

import java.util.Date;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateProjectChangeACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Project project = (Project) context.getRootInput();
		long check = Services.get(ProjectService.class).checkCreateProjectChange(project.get_id());
		if (check > 0) {
			Layer.message("存在未完成的项目变更，无法创建新变更申请。", Layer.ICON_CANCEL);
			return;
		}
		Editor.open("项目变更编辑器", context,
				new ProjectChange().setProject_id(project.get_id()).setApplicant(brui.getCurrentUserInfo())
						.setStatus(ProjectChange.STATUS_CREATE).setApplicantDate(new Date()),
				(r, o) -> {
					ProjectChange pc = Services.get(ProjectService.class).createProjectChange(o);
					GridPart grid = (GridPart) context.getContent();
					grid.insert(pc);
				});
	}
}
