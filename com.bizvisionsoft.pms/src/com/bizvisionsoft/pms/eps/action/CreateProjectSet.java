package com.bizvisionsoft.pms.eps.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectSetService;
import com.bizvisionsoft.service.model.EPS;
import com.bizvisionsoft.service.model.ProjectSet;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateProjectSet {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(em -> {
			ProjectSet input = new ProjectSet();
			if (em instanceof EPS) {
				input.setEps_id(((EPS) em).get_id());
			} else if (em instanceof ProjectSet) {
				input.setParent_id(((ProjectSet) em).get_id());
			} else {
				MessageDialog.openError(bruiService.getCurrentShell(), "创建项目集",
						"只能在EPS节点或项目集下创建新的项目集。\n请选择EPS节点或项目集节点。");
				return;
			}
			new Editor<ProjectSet>(bruiService.getAssembly("项目集编辑器"), context)

					.setInput(input)

					.ok((r, pjset) -> {
						pjset = Services.get(ProjectSetService.class).insert(pjset);
						if (pjset != null) {
							GridPart grid = (GridPart) context.getContent();
							grid.add(em, pjset);
						}
					});

		});
	}

}
