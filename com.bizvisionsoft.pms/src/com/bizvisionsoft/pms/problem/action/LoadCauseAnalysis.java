package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.EditorPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class LoadCauseAnalysis {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Problem pr = context.getRootInput(Problem.class, false);
		
		Document d = Services.get(ProblemService.class).loadCauseAnalysis(pr.get_id());

		Document doc = (Document) context.getInput();
		doc.putAll(d);
		EditorPart content = (EditorPart) context.getContent();
		content.reloadFieldValue(new String[] { "rootCauseDesc", "escapePoint" });
		
	}
}
