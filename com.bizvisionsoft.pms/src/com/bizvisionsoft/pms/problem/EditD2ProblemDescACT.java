package com.bizvisionsoft.pms.problem;

import org.bson.Document;
import org.eclipse.rap.rwt.RWT;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditD2ProblemDescACT {
	
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.PAGE_CONTEXT_INPUT_OBJECT) Problem problem) {
		ProblemService service = Services.get(ProblemService.class);
		Document d = service.getD2ProblemDesc(problem.get_id());
		Editor.create("D2-5W2H问题描述", context, d, true).ok((r, t) -> {
			t = service.updateD2ProblemDesc(t, RWT.getLocale().getLanguage());
			//TODO 添加卡片项，注意排序问题
		});
	}

}
