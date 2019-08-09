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
			//TODO ��һ������ʹ��TOPS
			Problem problem=(Problem) em;
			if(Services.get(ProblemService.class).selectProblemsCard(((Problem) em).get_id()
					,br.getCurrentUserId(),problem.domain)) {
				br.switchPage("������-TOPS����-�鿴", ((Problem) em).get_id().toHexString());
				//br.switchPage("������-TOPS����", ((Problem) em).get_id().toHexString());
			}else {
				br.switchPage("������-TOPS����", ((Problem) em).get_id().toHexString());
			}
			if(problem.getStatus().equals("�ѹر�")) {
				Services.get(ProblemService.class).updateClickCount(problem, problem.domain);
			}
		});
	}

}
