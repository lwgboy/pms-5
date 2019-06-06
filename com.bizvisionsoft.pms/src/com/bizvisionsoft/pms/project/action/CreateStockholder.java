package com.bizvisionsoft.pms.project.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Stockholder;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateStockholder {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) Event event) {
		Editor.open("项目干系人编辑器.editorassy", context, new Stockholder().setProject_id(((Project) context.getRootInput()).get_id()), (d, c) -> {
			Stockholder item = Services.get(ProjectService.class).insertStockholder(c, br.getDomain());
			GridPart grid = (GridPart) context.getContent();
			grid.insert(item);
		});

	}
}
