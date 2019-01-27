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
import com.bizvisionsoft.bruiengine.assembly.IQueryEnable;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;

public class GenericAction {

	private static final String ACTION_DELETE_SIMILAR = "deleteSimilar";

	private static final String ACTION_EDIT_SIMILAR = "editSimilar";

	private static final String ACTION_CREATE = "create";

	private static final String ACTION_FINISH = "finish";

	private static final String ACTION_VERIFY = "verify";

	private static final String ACTION_DELETE = "delete";

	private static final String ACTION_READ = "read";

	private static final String ACTION_EDIT = "edit";

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
		if (ACTION_EDIT.equals(a.getName()) || ACTION_EDIT.equals(e.text)) {
			doEdit(element, context, viewer);
		} else if (ACTION_READ.equals(a.getName()) || ACTION_READ.equals(e.text)) {
			doRead(context, element);
		} else if (ACTION_DELETE.equals(a.getName()) || ACTION_DELETE.equals(e.text)) {
			doDelete(element, context);
		} else if (ACTION_VERIFY.equals(a.getName()) || ACTION_VERIFY.equals(e.text)) {
			doVerify(element, context, viewer);
		} else if (ACTION_FINISH.equals(a.getName()) || ACTION_FINISH.equals(e.text)) {
			doFinish(element, viewer);
		} else if (ACTION_CREATE.equals(a.getName()) || ACTION_CREATE.equals(e.text)) {
			doCreate(problem, context);
		} else if (ACTION_EDIT_SIMILAR.equals(a.getName()) || ACTION_EDIT_SIMILAR.equals(e.text)) {
			editSimilar(element, viewer, context, render);
		} else if (ACTION_DELETE_SIMILAR.equals(a.getName()) || ACTION_DELETE_SIMILAR.equals(e.text)) {
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

	protected void doCreate(Problem problem, BruiAssemblyContext context) {
		String editorName = getEditorName();
		ObjectId problem_id = problem.get_id();
		Editor.create(editorName, context, new Document(), true).setTitle(getItemTypeName()).ok((r, t) -> {
			t = Services.get(ProblemService.class).insertAction(t, problem_id, stage, RWT.getLocale().getLanguage(), render);
			((IQueryEnable) context.getContent()).doRefresh();
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

	protected void doDelete(Document element, BruiAssemblyContext context) {
		if (br.confirm("删除", "请确认删除选择的" + getItemTypeName())) {
			ObjectId _id = element.getObjectId("_id");
			service.deleteAction(_id);
			((IQueryEnable) context.getContent()).doRefresh();
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
				d = service.updateAction(new Document("_id", _id).append(ACTION_FINISH, true), lang, render);
			}
			viewer.update(AUtil.simpleCopy(d, element), null);
		});
	}

	protected void doFinish(Document element, GridTreeViewer viewer) {
		if (br.confirm("完成", "请确认完成选择的" + getItemTypeName())) {
			ObjectId _id = element.getObjectId("_id");
			Document d = service.updateAction(new Document("_id", _id).append(ACTION_FINISH, true), lang, render);
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
	
	@Behavior(ACTION_EDIT)
	private boolean enableEdit(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if(!"解决中".equals(problem.getStatus()))
			return false;
		return true;
		//TODO
	}

	
	@Behavior(ACTION_CREATE)
	private boolean enableCreate(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if(!"解决中".equals(problem.getStatus()))
			return false;
		return true;
		//TODO
	}

	@Behavior(ACTION_DELETE)
	private boolean enableDelete(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if(!"解决中".equals(problem.getStatus()))
			return false;
		if (isFinished(element)) // 已经完成
			return false;
		return true;
	}

	@Behavior(ACTION_VERIFY)
	private boolean enableVerify(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if(!"解决中".equals(problem.getStatus()))
			return false;
		if (isFinished(element)) {// 已经完成
			return false;
		}
		return true;
	}

	@Behavior(ACTION_FINISH)
	private boolean enableFinish(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if(!"解决中".equals(problem.getStatus()))
			return false;
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
