package com.bizvisionsoft.pms.problem.action;

import java.util.List;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class ConfirmD3ICA {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem) {
		if (br.confirm("证实", "证实ICA已经满足要求并未产生新的问题。")) {
			List<Result> result = Services.get(ProblemService.class).confirmD3ICA(problem.get_id());
			if (result.isEmpty()) {
				Layer.message("ICA 已证实");
			}
		}
	}
}
