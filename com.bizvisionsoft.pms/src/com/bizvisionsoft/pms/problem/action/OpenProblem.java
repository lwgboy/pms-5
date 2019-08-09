package com.bizvisionsoft.pms.problem.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class OpenProblem  {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(em -> {
			//TODO 不一定都是使用TOPS
			Problem problem=(Problem) em;
			if(Services.get(ProblemService.class).selectProblemsCard(((Problem) em).get_id()
					,br.getCurrentUserId(),problem.domain)) {
				br.switchPage("问题解决-TOPS过程-查看", ((Problem) em).get_id().toHexString());
				//br.switchPage("问题解决-TOPS过程", ((Problem) em).get_id().toHexString());
			}else {
				br.switchPage("问题解决-TOPS过程", ((Problem) em).get_id().toHexString());
			}
			if(problem.getStatus().equals("已关闭")) {
				Services.get(ProblemService.class).updateClickCount(problem, problem.domain);
			}
		});
	}

}
