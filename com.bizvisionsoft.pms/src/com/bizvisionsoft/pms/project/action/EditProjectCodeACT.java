package com.bizvisionsoft.pms.project.action;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Project;
import com.mongodb.BasicDBObject;

public class EditProjectCodeACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(se -> {
			Project project = (Project) se;
			InputDialog id = new InputDialog(brui.getCurrentShell(), "÷˙º«¬Î", "±‡º≠÷˙º«¬Î", project.getCode(), t -> {
				return t.trim().isEmpty() ? "«Î ‰»Î÷˙º«¬Î" : null;
			});
			if (InputDialog.OK == id.open()) {
				String code = id.getValue();
				String workOrder = ServicesLoader.get(ProjectService.class).generateWorkOrder(project.get_id());
				ServicesLoader.get(ProjectService.class)
						.update(new FilterAndUpdate().filter(new BasicDBObject("_id", project.get_id()))
								.set(new BasicDBObject("code", code).append("workOrder", workOrder)).bson());
				GridPart gp = (GridPart) context.getContent();
				gp.refreshAll();
			}
		});
	}

}
