package com.bizvisionsoft.pms.problem.action;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;

public class GenericAction {

	private ProblemService service;

	private String lang;

	@Inject
	private IBruiService br;

	@Inject
	private String render;

	@Inject
	private String stage;

	@Inject
	private String editor;

	@Inject
	private String verifyEditor;

	private static final String[] actionType = new String[] { "era", "ica", "pca", "spa", "lra" };

	private static final String[] actionName = new String[] { "紧急反应行动", "临时控制行动", "永久纠正措施", "系统性预防措施", "挽回损失和善后措施" };

	public GenericAction() {
		service = Services.get(ProblemService.class);
		lang = RWT.getLocale().getLanguage();
	}

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element, @MethodParam(Execute.CONTEXT) BruiAssemblyContext context,
			@MethodParam(Execute.EVENT) Event e, @MethodParam(Execute.ACTION) Action a) {
		GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
		if ("edit".equals(a.getName()) || "edit".equals(e.text)) {
			doEdit(element, context, viewer);
		} else if ("read".equals(a.getName()) || "read".equals(e.text)) {
			doRead(context, element);
		} else if ("delete".equals(a.getName()) || "delete".equals(e.text)) {
			doDelete(element, viewer);
		} else if ("verify".equals(a.getName()) || "verify".equals(e.text)) {
			doVerify(element, context, viewer);
		} else if ("finish".equals(a.getName()) || "finish".equals(e.text)) {
			doFinish(element, viewer);
		} else if ("create".equals(a.getName()) || "create".equals(e.text)) {
			doCreate(problem, context, viewer);
		} else if ("editSimilar".equals(a.getName()) || "editSimilar".equals(e.text)) {
			editSimilar(element, viewer, context, render);
		} else if ("deleteSimilar".equals(a.getName()) || "deleteSimilar".equals(e.text)) {
			deleteSimilar(element, viewer, context);
		}
	}

	private void editSimilar(Document doc, GridTreeViewer viewer, BruiAssemblyContext context, String render) {
		Document ivpca = service.getD7Similar(doc.getObjectId("_id"));
		Editor.create("D7-类似问题-编辑器", context, ivpca, true).ok((r, t) -> {
			Document d = service.updateD7Similar(t, lang, render);
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}

	private void deleteSimilar(Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("删除", "请确认删除选择的相似项。")) {
			service.deleteD7Similar(doc.getObjectId("_id"));
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}

	@SuppressWarnings("unchecked")
	protected void doCreate(Problem problem, BruiAssemblyContext context, GridTreeViewer viewer) {
		String editorName = getEditorName();
		ObjectId problem_id = problem.get_id();
		Editor.create(editorName, context, new Document(), true).setTitle(getItemTypeName()).ok((r, t) -> {
			t = Services.get(ProblemService.class).insertAction(t, problem_id, stage, RWT.getLocale().getLanguage(), render);
			((List<Document>) viewer.getInput()).add(t);
			viewer.insert(viewer.getInput(), t, -1);
		});
	}

	protected void doEdit(Document element, BruiAssemblyContext context, GridTreeViewer viewer) {
		if (isFinished(element)) {// 已经完成
			doRead(context, element);
		} else {
			ObjectId _id = element.getObjectId("_id");
			Document input = service.getAction(_id);
			String editorName = getEditorName();
			String title = getItemTypeName();
			Editor.create(editorName, context, input, true).setTitle(title).ok((r, t) -> {
				Document d = service.updateAction(t, lang, render);
				viewer.update(AUtil.simpleCopy(d, element), null);
			});
		}
	}

	protected void doRead(BruiAssemblyContext context, Document element) {
		ObjectId _id = element.getObjectId("_id");
		Document input = service.getAction(_id);
		String editorName = getEditorName();
		String title = getItemTypeName();
		Editor.create(editorName, context, input, true).setTitle(title).setEditable(false).open();
	}

	protected void doDelete(Document element, GridTreeViewer viewer) {
		if (br.confirm("删除", "请确认删除选择的" + getItemTypeName())) {
			ObjectId _id = element.getObjectId("_id");
			service.deleteAction(_id);
			List<?> input = (List<?>) viewer.getInput();
			input.remove(element);
			viewer.remove(element);
		}
	}

	protected void doVerify(Document element, BruiAssemblyContext context, GridTreeViewer viewer) {
		ObjectId _id = element.getObjectId("_id");
		Document document = service.getAction(_id);
		Document input = Optional.ofNullable((Document) document.get("verification")).orElse(new Document());
		String title = "验证" + getItemTypeName();
		String editorName = getVerfiyEditorName();
		Editor.create(editorName, context, input, true).setTitle(title).ok((r, t) -> {
			Document d = service.updateAction(new Document("_id", _id).append("verification", t), lang, render);
			if ("已验证".equals(t.get("title")) && br.confirm("验证", "验证通过，是否立即完成本项行动？")) {
				d = service.updateAction(new Document("_id", _id).append("finish", true), lang, render);
			}
			viewer.update(AUtil.simpleCopy(d, element), null);
		});
	}

	protected void doFinish(Document element, GridTreeViewer viewer) {
		if (br.confirm("完成", "请确认完成选择的" + getItemTypeName())) {
			ObjectId _id = element.getObjectId("_id");
			Document d = service.updateAction(new Document("_id", _id).append("finish", true), lang, render);
			viewer.update(AUtil.simpleCopy(d, element), null);
		}
	}

	protected String getVerfiyEditorName() {
		return Check.option(verifyEditor).orElse("Dx-行动验证-编辑器");
	}

	protected String getEditorName() {
		return Check.option(editor).orElse("Dx-行动计划-编辑器");
	}

	private String getItemTypeName() {
		return actionName[Arrays.asList(actionType).indexOf(stage)];
	}

	@Behavior("delete")
	private boolean enableDelete(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if (isFinished(element)) // 已经完成
			return false;
		return true;
	}

	@Behavior("verify")
	private boolean enableVerify(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if (isFinished(element)) {// 已经完成
			return false;
		}
		return true;
	}

	@Behavior("finish")
	private boolean enableFinish(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if (isFinished(element)) // 已经完成
			return false;
		if (!isVerified(element))// 未验证的
			return false;

		return true;

	}

	private boolean isVerified(Document element) {
		return Optional.ofNullable((Document) element.get("verification")).map(d -> "已验证".equals(d.getString("title"))).orElse(false);
	}

	private boolean isFinished(Document element) {
		return Check.isTrue(element.get("finish"));
	}
}
