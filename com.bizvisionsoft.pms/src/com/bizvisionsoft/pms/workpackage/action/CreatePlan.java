package com.bizvisionsoft.pms.workpackage.action;

import java.util.Optional;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.pms.work.assembly.WorkPackagePlan;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkPackage;

public class CreatePlan {
	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		
		Object[] input = (Object[]) context.getInput();
		Work work = (Work) input[0];
		TrackView tv = (TrackView) input[1];
		
		String editorId = Optional.ofNullable(tv).map(t -> t.getEditAssembly()).orElse("编辑工作包-基本");

		WorkPackage wp = WorkPackage.newInstance(work, tv);
		Editor.open(editorId, context, wp, (r, o) -> {
			WorkPackagePlan wpp = (WorkPackagePlan) context.getContent();
			wpp.doCreate(null, o);
		});
	}

}
