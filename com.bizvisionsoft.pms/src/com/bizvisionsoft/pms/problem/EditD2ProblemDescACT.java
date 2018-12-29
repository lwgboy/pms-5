package com.bizvisionsoft.pms.problem;

import java.util.List;

import org.bson.Document;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.rap.rwt.RWT;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditD2ProblemDescACT {

	@Inject
	private IBruiService br;

	@SuppressWarnings("unchecked")
	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) BruiAssemblyContext context,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem) {
		ProblemService service = Services.get(ProblemService.class);
		Document d = service.getD2ProblemDesc(problem.get_id());
		boolean insert = (d.get("what") != null);
		Editor.create("D2-5W2HÎÊÌâÃèÊö", context, d, true).ok((r, t) -> {
			t = service.updateD2ProblemDesc(t, RWT.getLocale().getLanguage());
			GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
			if (insert) {
				Object doc = ((List<?>) viewer.getInput()).get(0);
				AUtil.simpleCopy(t, doc);
				viewer.refresh(doc);
			} else {
				((List<Document>) viewer.getInput()).add(0, t);
				viewer.refresh();
			}
		});
	}

}
