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

public class D2Card {
	@Inject
	private IBruiService br;
	
	private ProblemService service;

	public D2Card() {
		service = Services.get(ProblemService.class);
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e) {
		if (e.text == null)
			return;
		ObjectId _id = element.getObjectId("_id");
		GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
		if (e.text.startsWith("editpd")) {
			editProblemDesc(_id, element, viewer, context);
		} else if (e.text.startsWith("deletephoto")) {
			deleteProblemPhoto(_id, element, viewer);
		}

	}

	private void deleteProblemPhoto(ObjectId _id, Document doc, GridTreeViewer viewer) {
		if (br.confirm("删除", "请确认删除选择的图片资料。")) {
			service.deleteD2ProblemPhotos(_id);
			((List<?>) viewer.getInput()).remove(doc);
			viewer.remove(doc);
		}
	}

	private void editProblemDesc(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Document d2ProblemDesc = service.getD2ProblemDesc(_id);
		Editor.create("D2-5W2H问题描述", context, d2ProblemDesc, true).ok((r, t) -> {
			t = service.updateD2ProblemDesc(t, RWT.getLocale().getLanguage());
			AUtil.simpleCopy(t, doc);
			viewer.refresh(doc);
		});
	}
}
