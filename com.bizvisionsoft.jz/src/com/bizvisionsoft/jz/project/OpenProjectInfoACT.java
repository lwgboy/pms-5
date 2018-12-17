package com.bizvisionsoft.jz.project;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
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

public class OpenProjectInfoACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.EVENT) Event event) {

		ObjectId project_id = context.getRootInput(Project.class, false).get_id();
		Project project = Services.get(ProjectService.class).get(project_id);

		String title = Optional.ofNullable(AUtil.readTypeAndLabel(project)).orElse("");

		String assemblyName = "项目编辑器";
		if ("dept".equals(project.getProjectType()))
			assemblyName = "部门项目编辑器";
		else if ("external".equals(project.getProjectType()))
			assemblyName = "外协项目编辑器";

		new Editor<Project>(bruiService.getAssembly(assemblyName), context).setInput(project).setTitle(title).setEditable(false)
				.ok((r, proj) -> {
					try {
						Services.get(ProjectService.class)
								.update(new FilterAndUpdate().filter(new BasicDBObject("_id", project.get_id())).set(r).bson());
						AUtil.simpleCopy(proj, project);
					} catch (Exception e) {
						String message = e.getMessage();
						if (message.indexOf("index") >= 0) {
							Layer.error("请勿录入相同的项目编号");
						}
					}
				});

	}

}
