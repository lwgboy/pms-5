package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.EditorPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.model.SPAPrePlan;

public class SelectSPAPrePlan {

	
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Selector.open("SPAÔ¤°¸Ñ¡ÔñÆ÷", context, null, s->{
			SPAPrePlan plan = (SPAPrePlan) s.get(0);
			Document doc = (Document) context.getInput();
			doc.append("action", plan.getAction()).append("detail", plan.getDetail());
			EditorPart content = (EditorPart) context.getContent();
			content.reloadFieldValue(new String[] {"action","detail"});
		});
	}
}
