package com.bizvisionsoft.pms.formdef.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class CheckFormDefACT {
	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object element, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.ACTION) Action a) {

	}
}
