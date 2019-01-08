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
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.ACTION) Action a) {
		ObjectId _id = element.getObjectId("_id");
		GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
		String render = "����".equals(a.getName()) ? "card" : "gridrow";
		if ("�༭".equals(a.getName()) || "editPCA".equals(e.text)) {
			edit(_id, element, viewer, context, render);
		} else if ("�ر�".equals(a.getName()) || "closePCA".equals(e.text)) {
			close(_id, element, viewer, context, render);
		} else if ("ɾ��".equals(a.getName()) || "deletePCA".equals(e.text)) {
			delete(_id, element, viewer, context);
		}

	}

	private void delete(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("ɾ��", "��ȷ��ɾ��ѡ������þ�����ʩ��ִ�к�ȷ���")) {
			service.deleteD6IVPCA(_id);
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}

	private void close(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		if (br.confirm("�ر�", "��ȷ�Ϲر����þ�����ʩ��ִ�к�ȷ���")) {
			Document d = service.updateD6IVPCA(new Document("_id", _id).append("closed", true), lang, render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		}
	}

	private void edit(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		Document ivpca = service.getD6IVPCA(_id);
		Editor.create("D6-IVPCA-�༭��", context, ivpca, true).ok((r, t) -> {
			Document d = service.updateD6IVPCA(t, lang, render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}

}
