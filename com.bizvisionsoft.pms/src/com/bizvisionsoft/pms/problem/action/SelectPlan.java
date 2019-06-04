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

		Selector sel =Selector.create("�ж�Ԥ��ѡ����.selectorassy", context, null);
		sel.getContext().acceptParamters(cond.toJson());
		sel.setTitle("ѡ��Ԥ��").open(s -> {
			Document plan = (Document) s.get(0);
			Document doc = (Document) context.getInput();
			doc.append("action", plan.get("action")).append("detail", plan.get("detail")).append("objective", plan.get("objective"));
			EditorPart content = (EditorPart) context.getContent();
			content.reloadFieldValue(new String[] { "action", "detail", "objective" });
		});
	}

}
