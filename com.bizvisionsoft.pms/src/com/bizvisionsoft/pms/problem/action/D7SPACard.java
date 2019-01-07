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

public class D7SPACard {
	@Inject
	private IBruiService br;
	private ProblemService service;
	private String lang;

	public D7SPACard() {
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
		if ("editSimilar".equals(e.text)) {
			editSimilar(_id, element, viewer, context);
		} else if ("deleteSimilar".equals(e.text)) {
			deleteSimilar(_id, element, viewer, context);
		} else if ("readSPA".equals(e.text)) {
			readSPA(_id, element, viewer, context);
		} else if ("deleteSPA".equals(e.text)) {
			deleteSPA(_id, element, viewer, context);
		} else if ("editSPA".equals(e.text)) {
			editSPA(_id, element, viewer, context);
		} else if ("finishSPA".equals(e.text)) {
			finishSPA(_id, element, viewer, context);
		}

	}

	private void deleteSimilar(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("ɾ��", "��ȷ��ɾ��ѡ��������")) {
			service.deleteD7Similar(_id);
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}

	private void deleteSPA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("ɾ��", "��ȷ��ɾ��ѡ���ϵͳԤ����ʩ��")) {
			service.deleteD7SPA(_id);
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}

	private void editSimilar(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Document ivpca = service.getD7Similar(_id);
		Editor.create("D7-��������-�༭��", context, ivpca, true).ok((r, t) -> {
			Document d = service.updateD7Similar(t, lang);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}
	
	private void finishSPA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("���", "��ȷ��ѡ���Ԥ����ʩ�Ѿ���ɡ�")) {
			Document d = service.updateD7SPA(new Document("_id", _id).append("finish", true), lang);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		}
	}

	private void editSPA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Document ivpca = service.getD7SPA(_id);
		Editor.create("D7-Ԥ����ʩ-�༭��", context, ivpca, true).ok((r, t) -> {
			Document d = service.updateD7SPA(t, lang);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}

	private void readSPA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Document item = service.getD7SPA(_id);
		Editor.create("D7-Ԥ����ʩ-�༭��", context, item, true).setEditable(false).open();
	}

}