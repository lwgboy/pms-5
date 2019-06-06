package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.EditorPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.model.Problem;

public class SelectCriteria {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Problem pr = context.getRootInput(Problem.class, false);
		Document cond = new Document("problem_id",pr.get_id());
		
		Selector sel =Selector.create("决策准则选择器.selectorassy", context, null);
		sel.getContext().acceptParamters(cond.toJson());

		sel.setTitle("选择决策准则").open(s -> {
			Document plan = (Document) s.get(0);
			Document doc = (Document) context.getInput();
			doc.append("endResult", plan.get("endResult")).append("givens", plan.get("givens")).append("wants", plan.get("wants"));
			EditorPart content = (EditorPart) context.getContent();
			content.reloadFieldValue(new String[] { "endResult", "givens", "wants" });
		});
	}

}
