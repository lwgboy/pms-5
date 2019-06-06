package com.bizvisionsoft.pms.project.action;

import java.util.Optional;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectScheduleInfo;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class EditProjectInfo {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Project project;
		ProjectScheduleInfo ps = context.search_sele_root(ProjectScheduleInfo.class);
		if (ps != null) {
			project = ps.getProject();
		} else {
			project = context.search_sele_root(Project.class);
		}

		String title = Optional.ofNullable(AUtil.readTypeAndLabel(project)).orElse("");

		Project pjForEdit = Services.get(ProjectService.class).get(project.get_id(), br.getDomain());
		new Editor<Project>(br.getAssembly("项目编辑器.editorassy"), context).setInput(true, pjForEdit).setTitle("编辑 " + title).ok((r, proj) -> {
			try {
				Services.get(ProjectService.class)
						.update(new FilterAndUpdate().filter(new BasicDBObject("_id", project.get_id())).set(r).bson(), br.getDomain());
				AUtil.simpleCopy(proj, project);
				if (ps != null)
					Check.instanceThen(context.getContent(), GridPart.class, grid -> grid.update(ps));
				else
					Check.instanceThen(context.getContent(), GridPart.class, grid -> grid.update(project));
			} catch (Exception e) {
				String message = e.getMessage();
				if (message.indexOf("index") >= 0) {
					Layer.message("请勿录入相同的项目编号", Layer.ICON_ERROR);
				}
			}
		});

	}

}
