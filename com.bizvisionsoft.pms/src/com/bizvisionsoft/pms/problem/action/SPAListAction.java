package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class SPAListAction {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.CONTEXT_SELECTION_1ST) Document spa,
			@MethodParam(Execute.ACTION) Action a) {
		if ("similar".equals(a.getName())) {
			
		} else if ("openProblem".equals(a.getName())) {
			ObjectId problem_id = spa.getObjectId("problem_id");
			Problem problem = Services.get(ProblemService.class).get(problem_id, br.getDomain());
			Editor.create("问题编辑器（编辑）.editoassy", context, problem, true).setEditable(false).open();
		}else if("TOPS".equals(a.getName())) {
			br.switchPage("问题解决-TOPS过程", spa.getObjectId("problem_id").toHexString());
		}

	}

}
