package com.bizvisionsoft.pms.projecttemplate.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.EPS;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateProjectTemplate {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(em -> {
			ProjectTemplate input = new ProjectTemplate();
			if (em instanceof EPS) {
				input.setEps_id(((EPS) em).get_id());
			} else {
				MessageDialog.openError(bruiService.getCurrentShell(), "创建项目模板",
						"只能在EPS节点下创建新的项目模板。\n请选择EPS节点。");
				return;
			}
			new Editor<ProjectTemplate>(bruiService.getAssembly("项目模板编辑器"), context)

					.setInput(input)

					.ok((r, pjset) -> {
						pjset = Services.get(ProjectTemplateService.class).insert(pjset);
						if (pjset != null) {
							GridPart grid = (GridPart) context.getContent();
							grid.add(em, pjset);
						}
					});

		});
	}

}
