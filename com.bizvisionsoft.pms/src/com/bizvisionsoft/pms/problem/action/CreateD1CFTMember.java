package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.rap.rwt.RWT;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateD1CFTMember {

	@Inject
	private IBruiService br;
	
	@Inject
	private String render;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem) {
		Editor.create("D1-CTF³ÉÔ±-±à¼­Æ÷", context, new Document("problem_id", problem.get_id()), true).ok((r, t) -> {
			int i = Integer.parseInt(t.getString("role"));
			t = Services.get(ProblemService.class).insertD1Item(t, RWT.getLocale().getLanguage(),render);
			GridTreeViewer viewer = (GridTreeViewer) ((BruiAssemblyContext) context).getContent("viewer");
			viewer.insert(viewer.getInput(), t, i);
		});
	}

}
