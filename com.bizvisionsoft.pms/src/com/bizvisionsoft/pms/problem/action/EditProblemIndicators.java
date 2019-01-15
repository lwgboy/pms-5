package com.bizvisionsoft.pms.problem.action;

import java.util.List;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IStructuredDataPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class EditProblemIndicators {

	@Inject
	private IBruiService br;

	@Inject
	private String render;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) BruiAssemblyContext context,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Problem problem, @MethodParam(Execute.CURRENT_USER_ID) String userId) {
		Editor.open("问题定性指标编辑器", context, problem, true, (d, t) -> {
			Check.instanceThen(context.getContent(), IStructuredDataPart.class, c -> {
				ProblemService service = Services.get(ProblemService.class);
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", problem.get_id())).set(d).bson();
				if (service.updateProblems(fu) > 0) {
					BasicDBObject cond = new Query().filter(new BasicDBObject("_id", problem.get_id())).bson();
					List<Problem> result = service.listProblems(cond, problem.getStatus(), userId);
					if (result.size() > 0) {
						c.replaceItem(problem, result.get(0));
					}
				}
			});
		});

	}

}
