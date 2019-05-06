package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;
import org.eclipse.rap.rwt.RWT;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class EditD4RootCauseDesc {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) BruiAssemblyContext context,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem) {
		ProblemService service = Services.get(ProblemService.class);
		Document d = service.getD4RootCauseDesc(problem.get_id());
		boolean insert = d == null;
		if (insert) {
			d = new Document();
		}
		Editor.create("D4-根本原因描述-编辑器", context, d, true).setEditable(problem.isSolving()).ok((r, t) -> {
			if (insert) {
				t.append("_id", problem.get_id());
				service.insertD4RootCauseDesc(t, RWT.getLocale().getLanguage());
			} else {
				r.remove("_id");
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", problem.get_id())).set(r)
						.bson();
				service.updateD4RootCauseDesc(fu, RWT.getLocale().getLanguage());
			}
		});
	}

}
