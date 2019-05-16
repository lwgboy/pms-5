package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.IQueryEnable;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class D1Action {
	@Inject
	private IBruiService br;
	private ProblemService service;
	
	@Inject
	private String render;

	public D1Action() {
		service = Services.get(ProblemService.class);
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.ACTION) Action a,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem) {
		if ("delete".equals(a.getName()) || "delete".equals(e.text)) {
			ObjectId _id = element.getObjectId("_id");
			removeD1CFT(_id, element, context);
		}else if("create".equals(a.getName()) ||"create".equals(e.text)) {
			createD1CFT(problem,context);
		}

	}

	private void createD1CFT(Problem problem, BruiAssemblyContext context) {
		Editor.create("D1-CTF成员-编辑器", context, new Document("problem_id", problem.get_id()), true).ok((r, t) -> {
			t = Services.get(ProblemService.class).insertD1Item(t, RWT.getLocale().getLanguage(),render, br.getDomain());
			((IQueryEnable)context.getContent()).doRefresh();
		});		
	}

	private void removeD1CFT(ObjectId _id, Document doc, BruiAssemblyContext context) {
		if (br.confirm("删除", "请确认删除选择的团队成员。")) {
			service.deleteD1CFT(_id, br.getDomain());
			((IQueryEnable)context.getContent()).doRefresh();
		}
	}
	
	@Behavior({"create","delete"})
	private boolean enableEdit(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CURRENT_USER_ID) String userId) {
		if(!"解决中".equals(problem.getStatus()))
			return false;
		if(!service.hasPrivate(problem.get_id(),ProblemService.ACTION_EDIT_TEAM,userId, br.getDomain()))
			return false;
		return true;
	}
}
