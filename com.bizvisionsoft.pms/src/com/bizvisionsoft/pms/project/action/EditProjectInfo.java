package com.bizvisionsoft.pms.project.action;

import java.util.Optional;

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

public class EditProjectInfo {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		String title = Optional.ofNullable(AUtil.readTypeAndLabel(project)).orElse("");

		new Editor<Project>(bruiService.getAssembly("项目编辑器"), context).setInput(project).setTitle("编辑 " + title)
				.ok((r, proj) -> {
					try {
						Services.get(ProjectService.class).update(
								new FilterAndUpdate().filter(new BasicDBObject("_id", project.get_id())).set(r).bson());
						AUtil.simpleCopy(proj, project);
					} catch (Exception e) {
						String message = e.getMessage();
						if (message.indexOf("index") >= 0) {
							Layer.message("请勿录入相同的项目编号", Layer.ICON_CANCEL);
						}
					}
				});

	}

}
