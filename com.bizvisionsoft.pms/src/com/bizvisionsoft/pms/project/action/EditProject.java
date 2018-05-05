package com.bizvisionsoft.pms.project.action;

import java.util.Optional;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class EditProject {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		String message = Optional.ofNullable(AUtil.readTypeAndLabel(project)).orElse("");

		new Editor<Project>(bruiService.getAssembly("ÏîÄ¿±à¼­Æ÷"), context).setInput(project).setTitle("±à¼­ " + message)
				.ok((r, proj) -> {
					Services.get(ProjectService.class).update(
							new FilterAndUpdate().filter(new BasicDBObject("_id", project.get_id())).set(r).bson());
					AUtil.simpleCopy(proj, project);
				});

	}

}
