package com.bizvisionsoft.pms.work.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Work;

public class AssignerWorkACT implements IWorkAction {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(e -> {
			assignWork((Work) e,context, w -> {
				GridPart view = (GridPart) context.getContent();
				view.refreshAll();
			});
		});
	}

	@Override
	public IBruiService getBruiService() {
		return brui;
	}

}
