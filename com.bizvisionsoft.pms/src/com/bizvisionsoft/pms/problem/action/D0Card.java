package com.bizvisionsoft.pms.problem.action;

import java.util.List;

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
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.serviceconsumer.Services;

public class D0Card {
	@Inject
	private IBruiService br;
	private ProblemService service;
	private String lang;

	public D0Card() {
		service = Services.get(ProblemService.class);
		lang = RWT.getLocale().getLanguage();
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.ACTION) Action a) {
		ObjectId _id = element.getObjectId("_id");
		GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");

		if ("editERA".equals(a.getName())) {
			editERA(_id, element, viewer, context, "gridrow");

		} else if ("deleteERA".equals(a.getName())) {
			deleteERA(_id, element, viewer, context);

		} else if ("²Ù×÷".equals(a.getName())) {
			if ("editERA".equals(e.text)) {
				editERA(_id, element, viewer, context, "card");

			} else if ("deleteERA".equals(e.text)) {

				deleteERA(_id, element, viewer, context);
			}
		}

	}

	private void deleteERA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("É¾³ý", "ÇëÈ·ÈÏÉ¾³ýÑ¡Ôñ½ô¼±Ó¦±ä´ëÊ©¡£")) {
			service.deleteD0ERA(_id);
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}

	private void editERA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		Document ivpca = service.getD0ERA(_id);
		Editor.create("D0-ERA-±à¼­Æ÷", context, ivpca, true).ok((r, t) -> {
			Document d = service.updateD0ERA(t, lang, render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}

}
