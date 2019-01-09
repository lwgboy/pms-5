package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.EditorPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.model.ProblemReactionPrePlan;

public class SelectReactionPrePlan {
	
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Selector.create("�ж�Ԥ��ѡ����", context, null).setTitle("ѡ��Ԥ��").open( s->{
			ProblemReactionPrePlan plan = (ProblemReactionPrePlan) s.get(0);
			Document doc = (Document) context.getInput();
			doc.append("action", plan.getAction()).append("detail", plan.getDetail()).append("objective", plan.getObjective());
			EditorPart content = (EditorPart) context.getContent();
			content.reloadFieldValue(new String[] {"action","detail","objective"});
		});
	}

}
