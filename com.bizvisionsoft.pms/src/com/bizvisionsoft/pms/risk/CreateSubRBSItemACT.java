package com.bizvisionsoft.pms.risk;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.RBSItem;

public class CreateSubRBSItemACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(c -> {
			Project project = (Project) context.getRootInput();
			Editor.open("RBSItem±à¼­Æ÷", context,
					new RBSItem().setProject_id(project.get_id()).setParent_id(((RBSItem) c).get_id()), (r, o) -> {
						GridPart grid = (GridPart) context.getContent();
						grid.doCreateSubItem(c, o);
					});
		});
	}

}
