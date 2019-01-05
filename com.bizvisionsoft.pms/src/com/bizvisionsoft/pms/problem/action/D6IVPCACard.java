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

public class D6IVPCACard {
	@Inject
	private IBruiService br;
	private ProblemService service;
	private String lang;

	public D6IVPCACard() {
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
		if (e.text.startsWith("editPCA")) {
			edit(_id, element, viewer, context);
		} else if (e.text.startsWith("closePCA")) {
			close(_id, element, viewer, context);
		} else if (e.text.startsWith("deletePCA")) {
			delete(_id, element, viewer, context);
		}

	}

	private void delete(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("删除", "请确认删除选择的永久纠正措施的执行和确认项。")) {
			service.deleteD6IVPCA(_id);
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}

	private void close(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("关闭", "请确认关闭永久纠正措施的执行和确认项。")) {
			Document d = service.updateD6IVPCA(new Document("_id", _id).append("closed", true), lang);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		}
	}

	private void edit(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Document ivpca = service.getD6IVPCA(_id);
		Editor.create("D6-IVPCA-编辑器", context, ivpca, true).ok((r, t) -> {
			Document d = service.updateD6IVPCA(t, lang);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}

}
