package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;

import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.InfopadPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class EditD8MeetingReport {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) BruiAssemblyContext context,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem) {
		Editor.create("tops/leoco/D8-会议纪要和最终报告", context, problem, true).ok((r, t) -> {
			r.remove("_id");
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", problem.get_id())).set(r).bson();
			long l = Services.get(ProblemService.class).updateProblems(fu,br.getDomain());
			if(l>0) {
				InfopadPart content = (InfopadPart) context.getContent();
				content.reload();
			}
		});
	}
	
	@Behavior({ProblemService.ACTION_EDIT})
	private boolean enableEdit(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if(!"解决中".equals(problem.getStatus()))
			return false;
		return true;
	}

}
