package com.bizvisionsoft.pms.baseline;

import java.util.Date;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Baseline;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateBaselineACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Project project = (Project) context.getRootInput();
		Editor.open("项目基线编辑器", context, new Baseline().setProject_id(project.get_id()).setCreationDate(new Date()),
				(r, o) -> {
					Baseline baseline = Services.get(ProjectService.class).createBaseline(o);
					GridPart grid = (GridPart) context.getContent();
					grid.insert(baseline);
				});

	}
}
