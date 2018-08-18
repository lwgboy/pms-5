package com.bizvisionsoft.pms.projectset;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectSetService;
import com.bizvisionsoft.service.model.ProjectSet;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateSubProjectSet {
	
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(em -> {
			if (em instanceof ProjectSet) {
				ProjectSet pjSet = new ProjectSet();
				pjSet.setParent_id(((ProjectSet) em).get_id());
				pjSet.setCreationInfo(br.operationInfo());
				new Editor<ProjectSet>(br.getAssembly("ÏîÄ¿¼¯±à¼­Æ÷"), context)
						.setInput(pjSet)

						.ok((r, t) -> {
							ProjectSet result = Services.get(ProjectSetService.class).insert(t);
							GridPart grid = (GridPart) context.getContent();
							grid.add(em, result);
						});
			}
		});
	}

}
