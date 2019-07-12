package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.IQueryEnable;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class D4Action {
	@Inject
	private IBruiService br;

	private ProblemService service;
	
	@Inject
	private BruiAssemblyContext context;
	
	@Inject
	private String render;
	
	private GridTreeViewer viewer;
	
	@Init
	private void init() {
		service = Services.get(ProblemService.class);
		viewer = (GridTreeViewer) context.getContent("viewer");
	}

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.ACTION) Action a) {
		if ("delete".equals(a.getName()) || "delete".equals(e.text)) {
			ObjectId _id = element.getObjectId("_id");
			deleteVerify(_id, element, context);
		}else if("create".equals(a.getName())||"create".equals(e.text)) {
			createVerify(problem, context);
		}else if("验证记录".equals(a.getName())||"验证记录".equals(e.text)) {
			createVerify(problem, context);
		}else if("edit".equals(a.getName()) || "edit".equals(e.text)) {
			ObjectId _id = element.getObjectId("_id");
			editorVerify(_id, element, context);
		}
	}

	private void createVerify( Problem problem,IBruiContext context) {
		Editor.create("D4-原因分析验证记录-编辑器", context, new Document("problem_id", problem.get_id()), true).ok((r, t) -> {
			Services.get(ProblemService.class).insertD4Verify(t, RWT.getLocale().getLanguage(),"Grid",br.getDomain());
			((IQueryEnable)context.getContent()).doRefresh();
		});		
	}
	

	private void deleteVerify(ObjectId _id, Document doc, BruiAssemblyContext context) {
		if (br.confirm("删除", "请确认删除选择验证记录。")) {
			service.deleteD4Verify(_id,br.getDomain());
			((IQueryEnable)context.getContent()).doRefresh();
		}
	}
	
	private void editorVerify(ObjectId _id, Document doc, BruiAssemblyContext context) {
		Document input = service.getD4Verify(doc.getObjectId("_id"), br.getDomain());
		Editor.create("D4-原因分析验证记录-编辑器", context, input, true).ok((r, t) -> {
			service.updateD4Verify(t, RWT.getLocale().getLanguage(), "Grid", br.getDomain());
			viewer.update(AUtil.simpleCopy(t, doc), null);
		});
	}
	

	@Behavior({ProblemService.ACTION_CREATE,ProblemService.ACTION_DELETE,ProblemService.ACTION_EDIT})
	private boolean enableEdit(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if(!"解决中".equals(problem.getStatus()))
			return false;
		return true;
	}
}
