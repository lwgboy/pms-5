package com.bizvisionsoft.pms.work.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Work;

public class FinishWork implements IWorkAction {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(e -> {
			finishWork((Work) e, w -> {
				GridPart grid = (GridPart) context.getContent();
				grid.remove(e);
				br.updateSidebarActionBudget("处理工作");
			});
		});
	}

	@Override
	public IBruiService getBruiService() {
		return br;
	}

}
