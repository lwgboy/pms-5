package com.bizvisionsoft.pms.problem.action;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;

public class D7SPACard extends ActionCard{
	
	@Inject
	private IBruiService br;

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
		}else {
			run(element, context, e, a);
		}
	}

	@Override
	protected IBruiService getBruiService() {
		return br;
	}
	
	private void editSimilar(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		Document ivpca = service.getD7Similar(_id);
		Editor.create("D7-类似问题-编辑器", context, ivpca, true).ok((r, t) -> {
			Document d = service.updateD7Similar(t, lang,render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}

	private void deleteSimilar(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("删除", "请确认删除选择的相似项。")) {
			service.deleteD7Similar(_id);
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}
	
	@Override
	protected Document doUpdate(Document append, String lang, String render) {
		return service.updateD7SPA(append, lang, render);
	}

	@Override
	protected void doDelete(ObjectId _id) {
		service.deleteD7SPA(_id);
	}

	@Override
	protected String getItemTypeName() {
		return "系统预防措施";
	}

	@Override
	protected Document getAction(ObjectId _id) {
		return service.getD7SPA(_id);
	}
}
