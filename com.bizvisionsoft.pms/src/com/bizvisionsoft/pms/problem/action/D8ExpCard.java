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
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.serviceconsumer.Services;

public class D8ExpCard {
	@Inject
	private IBruiService br;
	private ProblemService service;
	private String lang;

	public D8ExpCard() {
		service = Services.get(ProblemService.class);
		lang = RWT.getLocale().getLanguage();
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e) {
		if (e.text == null)
			return;
		ObjectId _id = element.getObjectId("_id");
		GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
		if ("editExp".equals(e.text)) {
			editExp(_id, element, viewer, context);
		} else if ("deleteExp".equals(e.text)) {
			deleteExp(_id, element, viewer, context);
		}

	}

	private void deleteExp(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("删除", "请确认删除选择的经验和教训总结。")) {
			service.deleteD8Exp(_id);
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}


	private void editExp(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Document ivpca = service.getD8Exp(_id);
		Editor.create("D8-经验总结-编辑器", context, ivpca, true).ok((r, t) -> {
			Document d = service.updateD8Exp(t, lang);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}


}
