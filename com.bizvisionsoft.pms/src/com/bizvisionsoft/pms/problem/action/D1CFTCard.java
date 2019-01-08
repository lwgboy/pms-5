package com.bizvisionsoft.pms.problem.action;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.serviceconsumer.Services;

public class D1CFTCard {
	@Inject
	private IBruiService br;
	private ProblemService service;

	public D1CFTCard() {
		service = Services.get(ProblemService.class);
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.ACTION) Action a) {
		ObjectId _id = element.getObjectId("_id");
		GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
		if ("删除".equals(a.getName()) || "delete".equals(e.text)) {
			removeD1CFT(_id, element, viewer);
		}

	}

	private void removeD1CFT(ObjectId _id, Document doc, GridTreeViewer viewer) {
		if (br.confirm("删除", "请确认删除选择的团队成员。")) {
			service.deleteD1CFT(_id);
			((List<?>) viewer.getInput()).remove(doc);
			viewer.remove(doc);
		}
	}
}
