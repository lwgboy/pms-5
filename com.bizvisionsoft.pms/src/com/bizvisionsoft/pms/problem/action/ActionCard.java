package com.bizvisionsoft.pms.problem.action;

import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.serviceconsumer.Services;

public abstract class ActionCard {

	protected ProblemService service;

	protected String lang;

	public ActionCard() {
		service = Services.get(ProblemService.class);
		lang = RWT.getLocale().getLanguage();
	}

	protected abstract IBruiService getBruiService();

	protected void run(Document element, BruiAssemblyContext context, Event e, Action a) {
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

	private void finish(ObjectId _id, Document doc, GridTreeViewer viewer, String render) {
		if (getBruiService().confirm("完成", "请确认完成选择的" + getItemTypeName())) {
			Document d = doUpdate(new Document("_id", _id).append("finish", true), lang, render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		}
	}

	protected abstract Document doUpdate(Document append, String lang, String render);

	private void verify(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		Document document = getAction(_id);
		Document input = Optional.ofNullable((Document) document.get("verification")).orElse(new Document());
		String title = "验证" + getItemTypeName();
		String editorName = getVerfiyEditorName();
		Editor.create(editorName, context, input, true).setTitle(title).ok((r, t) -> {
			Document d = doUpdate(new Document("_id", _id).append("verification", t), lang, render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}

	protected String getVerfiyEditorName() {
		return "Dx-行动验证-编辑器";
	}
	
	protected String getEditorName() {
		return "Dx-行动计划-编辑器";
	}

	private void delete(ObjectId _id, Document doc, GridTreeViewer viewer) {
		if (getBruiService().confirm("删除", "请确认删除选择的" + getItemTypeName())) {
			doDelete(_id);
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}

	protected abstract void doDelete(ObjectId _id);

	protected abstract String getItemTypeName();

	private void edit(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		Document input = getAction(_id);
		String editorName = getEditorName();
		String title = getItemTypeName();
		Editor.create(editorName, context, input, true).setTitle(title).ok((r, t) -> {
			Document d = doUpdate(t, lang, render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}

	protected abstract Document getAction(ObjectId _id);

	private void read(ObjectId _id, BruiAssemblyContext context) {
		Document input = getAction(_id);
		String editorName = getEditorName();
		String title = getItemTypeName();
		Editor.create(editorName, context, input, true).setTitle(title).setEditable(false).open();
	}
}
