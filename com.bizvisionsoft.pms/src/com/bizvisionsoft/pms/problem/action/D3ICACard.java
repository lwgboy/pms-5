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
import com.bizvisionsoft.bruicommons.model.Action;
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
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.ACTION) Action a) {
		ObjectId _id = element.getObjectId("_id");
		GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
		String render = "����".equals(a.getName()) ? "card" : "gridrow";
		if ("editICA".equals(a.getName()) || "editICA".equals(e.text)) {
			editD3ICA(_id, element, viewer, context, render);
		} else if ("deleteICA".equals(a.getName()) || "deleteICA".equals(e.text)) {
			deleteD3ICA(_id, element, viewer, context, render);
		} else if ("verificationICA".equals(a.getName()) || "verificationICA".equals(e.text)) {
			verificationD3ICA(_id, element, viewer, context, render);
		} else if ("finishICA".equals(a.getName()) || "finishICA".equals(e.text)) {
			finishD3ICA(_id, element, viewer, context, render);
		}

	}

	private void finishD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		if (br.confirm("���", "��ȷ��ѡ�����ʱ�����ʩ�Ѿ���ɡ�")) {
			Document d = service.updateD3ICA(new Document("_id", _id).append("finish", true), lang,render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		}
	}

	private void verificationD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		Document input = Optional.ofNullable((Document) service.getD3ICA(_id).get("verification")).orElse(new Document());
		Editor.create("D3-ICA��֤-�༭��", context, input, true).ok((r, t) -> {
			Document d = service.updateD3ICA(new Document("_id", _id).append("verification", t), lang,render);
			viewer.update(AUtil.simpleCopy(d, doc), null);

		});
	}

	private void deleteD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		if (br.confirm("ɾ��", "��ȷ��ɾ��ѡ�����ʱ�����ʩ��")) {
			service.deleteD3ICA(_id);
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}

	private void editD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		Editor.create("D3-ICA-�༭��", context, service.getD3ICA(_id), true).ok((r, t) -> {
			Document d = service.updateD3ICA(t, lang,render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}
}
