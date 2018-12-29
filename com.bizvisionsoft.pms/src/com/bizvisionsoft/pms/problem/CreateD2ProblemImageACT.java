package com.bizvisionsoft.pms.problem;

import org.bson.Document;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.rap.rwt.RWT;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateD2ProblemImageACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) BruiAssemblyContext context,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem) {
		Editor.create("D2-ÎÊÌâÕÕÆ¬", context, new Document("problem_id", problem.get_id()), true).ok((r, t) -> {
			t = Services.get(ProblemService.class).insertD2ProblemPhoto(t, RWT.getLocale().getLanguage());
			GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
			viewer.insert(viewer.getInput(), t, 1);
		});
	}

}
