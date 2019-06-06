package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;
import org.eclipse.rap.rwt.RWT;

import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IQueryEnable;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateD7Similar {

	@Inject
	private IBruiService br;
	
	@Inject
	private String render;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem) {
		Editor.create("D7-类似问题-编辑器.editoassy", context, new Document("problem_id", problem.get_id()), true).ok((r, t) -> {
			Services.get(ProblemService.class).insertD7Similar(t, RWT.getLocale().getLanguage(),render, br.getDomain());
			IQueryEnable content = (IQueryEnable) context.getContent();
			content.doRefresh();
		});
	}
	
	@Behavior(ProblemService.ACTION_CREATE)
	private boolean enableCreate(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if (!"解决中".equals(problem.getStatus()))
			return false;
		return true;
		// TODO
	}

}
