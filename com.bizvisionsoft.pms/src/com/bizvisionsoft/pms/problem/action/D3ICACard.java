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

public class D3ICACard {
	@Inject
	private IBruiService br;
	private ProblemService service;

	public D3ICACard() {
		service = Services.get(ProblemService.class);
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
		} else if (e.text.startsWith("deleteVerified")) {
			deleteD3ICAVerified(_id, element, viewer, context);
		}

	}

	private void finishD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("完成", "请确认选择的临时处理措施已经完成。")) {
			Document t = service.finishD3ICA(_id, RWT.getLocale().getLanguage());
			AUtil.simpleCopy(t, doc);
			viewer.refresh(doc);
		}
	}

	private void deleteD3ICAVerified(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("删除", "请确认删除选择的临时处理措施验证记录。")) {
			Document d = service.deleteD3ICAVerified(_id, RWT.getLocale().getLanguage());
			List<?> input = (List<?>) viewer.getInput();
			int i = input.indexOf(doc);
			input.remove(doc);
			viewer.remove(doc);
			AUtil.simpleCopy(d, input.get(i - 1));
			viewer.refresh(input.get(i - 1));
		}
	}

	@SuppressWarnings("unchecked")
	private void verificationD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Document d3ICAVerified = service.getD3ICAVerified(_id);
		boolean insert = (d3ICAVerified.get("title") == null);
		Editor.create("D3-ICA验证", context, d3ICAVerified, true).ok((r, t) -> {
			List<Document> u = service.updateD3ICAVerified(t, _id, RWT.getLocale().getLanguage());
			List<Document> input = (List<Document>) viewer.getInput();
			int i = input.indexOf(doc);
			if (insert) {
				AUtil.simpleCopy(u.get(0), doc);
				input.add(i + 1, u.get(1));
				viewer.insert(input, u.get(1), i + 1);
				viewer.refresh(doc);
			} else {
				AUtil.simpleCopy(u.get(0), doc);
				AUtil.simpleCopy(u.get(1), input.get(i + 1));
				viewer.refresh(doc);
				viewer.refresh(input.get(i + 1));
			}
		});
	}

	private void deleteD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("删除", "请确认删除选择的临时处理措施。")) {
			long l = service.deleteD3ICA(_id);
			List<?> input = (List<?>) viewer.getInput();
			if (l > 1) {
				int i = input.indexOf(doc);
				Object d = input.get(i + 1);
				input.remove(d);
				viewer.remove(d);
			}
			input.remove(doc);
			viewer.remove(doc);
		}
	}

	private void editD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Document d3ICA = service.getD3ICA(_id);
		Editor.create("D3-ICA", context, d3ICA, true).ok((r, t) -> {
			t = service.updateD3ICA(t, RWT.getLocale().getLanguage());
			AUtil.simpleCopy(t, doc);
			viewer.refresh(doc);
		});
	}
}
