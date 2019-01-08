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
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.ACTION) Action a) {
		ObjectId _id = element.getObjectId("_id");
		GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
		String render = "操作".equals(a.getName()) ? "card" : "gridrow";

		if ("editSimilar".equals(a.getName())||"editSimilar".equals(e.text)) {
			editSimilar(_id, element, viewer, context,render);
		} else if ("deleteSimilar".equals(a.getName())||"deleteSimilar".equals(e.text)) {
			deleteSimilar(_id, element, viewer, context);
		} else if ("readSPA".equals(a.getName())||"readSPA".equals(e.text)) {
			readSPA(_id, element, viewer, context);
		} else if ("deleteSPA".equals(a.getName())||"deleteSPA".equals(e.text)) {
			deleteSPA(_id, element, viewer, context);
		} else if ("editSPA".equals(a.getName())||"editSPA".equals(e.text)) {
			editSPA(_id, element, viewer, context,render);
		} else if ("finishSPA".equals(a.getName())||"finishSPA".equals(e.text)) {
			finishSPA(_id, element, viewer, context,render);
		}

	}

	private void deleteSimilar(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("删除", "请确认删除选择的相似项。")) {
			service.deleteD7Similar(_id);
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}

	private void deleteSPA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("删除", "请确认删除选择的系统预防措施。")) {
			service.deleteD7SPA(_id);
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}

	private void editSimilar(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		Document ivpca = service.getD7Similar(_id);
		Editor.create("D7-类似问题-编辑器", context, ivpca, true).ok((r, t) -> {
			Document d = service.updateD7Similar(t, lang,render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}
	
	private void finishSPA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		if (br.confirm("完成", "请确认选择的预防措施已经完成。")) {
			Document d = service.updateD7SPA(new Document("_id", _id).append("finish", true), lang,render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		}
	}

	private void editSPA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		Document ivpca = service.getD7SPA(_id);
		Editor.create("D7-预防措施-编辑器", context, ivpca, true).ok((r, t) -> {
			Document d = service.updateD7SPA(t, lang,render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}

	private void readSPA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Document item = service.getD7SPA(_id);
		Editor.create("D7-预防措施-编辑器", context, item, true).setEditable(false).open();
	}

}
