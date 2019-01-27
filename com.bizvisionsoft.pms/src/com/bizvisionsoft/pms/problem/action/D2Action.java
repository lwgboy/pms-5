package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
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

public class D2Action {
	@Inject
	private IBruiService br;

	private ProblemService service;
	
	@Inject
	private String render;

	public D2Action() {
		service = Services.get(ProblemService.class);
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.ACTION) Action a,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem) {
		if ("editpd".equals(e.text)) {
			ObjectId _id = element.getObjectId("_id");
			GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
			editProblemDesc(_id, element, viewer, context);
		} else if ("deletephoto".equals(a.getName()) || "deletephoto".equals(e.text)) {
			ObjectId _id = element.getObjectId("_id");
			deleteProblemPhoto(_id, element, context);
		}else if("createPhoto".equals(a.getName())||"createPhoto".equals(e.text)) {
			createProblemPhoto(problem, context);
		}
	}

	private void createProblemPhoto( Problem problem,IBruiContext context) {
		Editor.create("D2-ÎÊÌâÕÕÆ¬-±à¼­Æ÷", context, new Document("problem_id", problem.get_id()), true).ok((r, t) -> {
			Services.get(ProblemService.class).insertD2ProblemPhoto(t, RWT.getLocale().getLanguage(),render);
			((IQueryEnable)context.getContent()).doRefresh();
		});		
	}

	private void deleteProblemPhoto(ObjectId _id, Document doc, BruiAssemblyContext context) {
		if (br.confirm("É¾³ý", "ÇëÈ·ÈÏÉ¾³ýÑ¡ÔñµÄÍ¼Æ¬×ÊÁÏ¡£")) {
			service.deleteD2ProblemPhotos(_id);
			((IQueryEnable)context.getContent()).doRefresh();
		}
	}

	private void editProblemDesc(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Document d2ProblemDesc = service.getD2ProblemDesc(_id);
		Editor.create("D2-5W2HÎÊÌâÃèÊö-±à¼­Æ÷", context, d2ProblemDesc, true).ok((r, t) -> {
			t = service.updateD2ProblemDesc(t, RWT.getLocale().getLanguage());
			AUtil.simpleCopy(t, doc);
			viewer.refresh(doc);
		});
	}
}
