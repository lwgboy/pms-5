package com.bizvisionsoft.pms.problem.action;

import java.util.List;
import java.util.Optional;

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

public class D3ICACard {
	@Inject
	private IBruiService br;

	private ProblemService service;

	private String lang;

	public D3ICACard() {
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
		if (e.text.startsWith("editICA")) {
			editD3ICA(_id, element, viewer, context);
		} else if (e.text.startsWith("deleteICA")) {
			deleteD3ICA(_id, element, viewer, context);
		} else if (e.text.startsWith("verificationICA")) {
			verificationD3ICA(_id, element, viewer, context);
		} else if (e.text.startsWith("finishICA")) {
			finishD3ICA(_id, element, viewer, context);
		}

	}

	private void finishD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("完成", "请确认选择的临时处理措施已经完成。")) {
			Document d = service.updateD3ICA(new Document("_id", _id).append("finish", true), lang);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		}
	}

	private void verificationD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Document input = Optional.ofNullable((Document) service.getD3ICA(_id).get("verification")).orElse(new Document());
		Editor.create("D3-ICA验证-编辑器", context, input, true).ok((r, t) -> {
			Document d = service.updateD3ICA(new Document("_id", _id).append("verification", t), lang);
			viewer.update(AUtil.simpleCopy(d, doc), null);

		});
	}

	private void deleteD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("删除", "请确认删除选择的临时处理措施。")) {
			service.deleteD3ICA(_id);
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}

	private void editD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Editor.create("D3-ICA-编辑器", context, service.getD3ICA(_id), true).ok((r, t) -> {
			Document d = service.updateD3ICA(t, lang);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}
}
