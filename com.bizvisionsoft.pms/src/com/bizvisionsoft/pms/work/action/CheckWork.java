package com.bizvisionsoft.pms.work.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Work;

public class CheckWork implements IWorkAction {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(e -> {
			checkWork((Work) e, context, w -> {
				GridPart grid = (GridPart) context.getContent();
				grid.replaceItem(e, w);
			});
		});
	}

	@Override
	public IBruiService getBruiService() {
		return brui;
	}

}
