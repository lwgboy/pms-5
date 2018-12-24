package com.bizvisionsoft.pms.work.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Work;

public class StartWork implements IWorkAction   {

	@Inject
	private IBruiService brui;

	@Execute
	// TODO selected作为参数传入到方法中
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(elem -> {
			startWork((Work) elem, work -> {
				GridPart grid = (GridPart) context.getContent();
				grid.replaceItem(elem, work);
			});

		});
	}

	@Override
	public IBruiService getBruiService() {
		return brui;
	}
}
