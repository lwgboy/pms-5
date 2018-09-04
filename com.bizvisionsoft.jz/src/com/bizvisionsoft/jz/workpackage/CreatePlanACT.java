package com.bizvisionsoft.jz.workpackage;

import java.util.Optional;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.WorkPackage;

public class CreatePlanACT {
	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {

		Object[] input = (Object[]) context.getInput();
		IWorkPackageMaster work = (IWorkPackageMaster) input[0];
		TrackView tv = (TrackView) input[1];

		String editorId = Optional.ofNullable(tv).map(t -> t.getEditAssembly()).orElse("编辑工作包-基本");

		WorkPackage wp = WorkPackage.newInstance(work, tv);
		Editor.open(editorId, context, wp, (r, o) -> {
			WorkPackagePlanASM wpp = (WorkPackagePlanASM) context.getContent();
			wpp.doCreate(null, o);
		});
	}

}
