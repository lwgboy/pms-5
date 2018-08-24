package com.bizvisionsoft.pms.project.action;

import java.util.Optional;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.EngUtil;
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
		Project project = context.search_sele_root(Project.class);
		
		String title = Optional.ofNullable(AUtil.readTypeAndLabel(project)).orElse("");

		Project pjForEdit = Services.get(ProjectService.class).get(project.get_id());
		new Editor<Project>(bruiService.getAssembly("项目编辑器"), context).setInput(true,pjForEdit).setTitle("编辑 " + title)
				.ok((r, proj) -> {
					try {
						Services.get(ProjectService.class).update(
								new FilterAndUpdate().filter(new BasicDBObject("_id", project.get_id())).set(r).bson());
						AUtil.simpleCopy(proj, project);
						EngUtil.ifInstanceThen(context.getContent(), GridPart.class, grid->grid.update(project));
					} catch (Exception e) {
						String message = e.getMessage();
						if (message.indexOf("index") >= 0) {
							Layer.message("请勿录入相同的项目编号", Layer.ICON_CANCEL);
						}
					}
				});

	}

}
