package com.bizvisionsoft.pms.problem.action;

import java.util.Arrays;
import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.EditorPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.model.ProblemActionPlan;

public class SelectPlan {

	@Inject
	private IBruiService br;

	private static final String[] actionTypeCodes = new String[] { "era", "ica", "pca", "spa", "lra" };

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Problem pr = context.getRootInput(Problem.class, false);
		Document cond = new Document("problem_id",pr.get_id());
		Optional.ofNullable(context.getParentContext()).map(c -> c.getParameter("stage"))
				.map(s -> Arrays.asList(actionTypeCodes).indexOf(s)).ifPresent(i->{
					if(i>=0 && i<actionTypeCodes.length) {
						cond.append("stage", actionTypeCodes[i]);
					}
				});

		Selector sel =Selector.create("行动预案选择器", context, null);
		sel.getContext().acceptParamters(cond.toJson());
		sel.setTitle("选择预案").open(s -> {
			ProblemActionPlan plan = (ProblemActionPlan) s.get(0);
			Document doc = (Document) context.getInput();
			doc.append("action", plan.getAction()).append("detail", plan.getDetail()).append("objective", plan.getObjective());
			EditorPart content = (EditorPart) context.getContent();
			content.reloadFieldValue(new String[] { "action", "detail", "objective" });
		});
	}

}
