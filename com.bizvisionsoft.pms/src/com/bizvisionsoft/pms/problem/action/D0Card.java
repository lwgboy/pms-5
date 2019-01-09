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

public class D0Card {
	@Inject
	private IBruiService br;
	private ProblemService service;
	private String lang;

	public D0Card() {
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
		if ("edit".equals(a.getName()) || "edit".equals(e.text)) {
			edit(_id, element, viewer, context, render);
		} else if ("read".equals(a.getName()) || "read".equals(e.text)) {
			read(_id, context);
		} else if ("delete".equals(a.getName()) || "delete".equals(e.text)) {
			delete(_id, element, viewer);
		} else if ("verify".equals(a.getName()) || "verify".equals(e.text)) {
			verify(_id, element, viewer, context, render);
		} else if ("finish".equals(a.getName()) || "finish".equals(e.text)) {
			finish(_id, element, viewer, render);
		}
	}

	private void delete(ObjectId _id, Document doc, GridTreeViewer viewer) {
		if (br.confirm("删除", "请确认删除选择紧急应变措施。")) {
			service.deleteD0ERA(_id);
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}

	private void edit(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		Editor.create("D0-ERA-编辑器", context, service.getD0ERA(_id), true).ok((r, t) -> {
			Document d = service.updateD0ERA(t, lang, render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}

	private void read(ObjectId _id, BruiAssemblyContext context) {
		Editor.create("D0-ERA-编辑器", context, service.getD0ERA(_id), true).setEditable(false).open();
	}

	private void finish(ObjectId _id, Document doc, GridTreeViewer viewer, String render) {
		if (br.confirm("完成", "请确认选择的紧急应变措施已经完成。")) {
			Document d = service.updateD0ERA(new Document("_id", _id).append("finish", true), lang, render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		}
	}

	private void verify(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		Document data = service.getD0ERA(_id);
		Document input = Optional.ofNullable((Document) data.get("verification")).orElse(new Document());
		Editor.create("D0-ERA-编辑器", context, input, true).ok((r, t) -> {
			Document d = service.updateD0ERA(new Document("_id", _id).append("verification", t), lang, render);
			viewer.update(AUtil.simpleCopy(d, doc), null);

		});
	}

}
