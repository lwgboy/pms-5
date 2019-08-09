package com.bizvisionsoft.pms.problem.action;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Init;
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

	@Inject
	private BruiAssemblyContext context;

	private GridTreeViewer viewer;

	public GenericAction() {
	}

	@Init
	private void init() {
		service = Services.get(ProblemService.class);
		lang = RWT.getLocale().getLanguage();
		viewer = (GridTreeViewer) context.getContent("viewer");
	}

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.ACTION) Action a) {
		if(Services.get(ProblemService.class).selectProblemsCard(problem.get_id()
				,br.getCurrentUserId(),problem.domain)&&problem.getStatus().equals("解决中")) {
			Layer.error("您没有访问权限!");
		}else {
			if (ProblemService.ACTION_EDIT.equals(a.getName()) || ProblemService.ACTION_EDIT.equals(e.text)) {
				doEdit(element);
			} else if (ProblemService.ACTION_READ.equals(a.getName()) || ProblemService.ACTION_READ.equals(e.text)) {
				doRead(element);
			} else if (ProblemService.ACTION_DELETE.equals(a.getName()) || ProblemService.ACTION_DELETE.equals(e.text)) {
				doDelete(element);
			} else if (ProblemService.ACTION_VERIFY.equals(a.getName()) || ProblemService.ACTION_VERIFY.equals(e.text)) {
				doVerify(element);
			} else if (ProblemService.ACTION_FINISH.equals(a.getName()) || ProblemService.ACTION_FINISH.equals(e.text)) {
				doFinish(element);
			} else if (ProblemService.ACTION_CREATE.equals(a.getName()) || ProblemService.ACTION_CREATE.equals(e.text)) {
				doCreate(problem);
			} else if (ProblemService.ACTION_EDIT_SIMILAR.equals(a.getName())
					|| ProblemService.ACTION_EDIT_SIMILAR.equals(e.text)) {
				editSimilar(element, render);
			} else if (ProblemService.ACTION_DELETE_SIMILAR.equals(a.getName())
					|| ProblemService.ACTION_DELETE_SIMILAR.equals(e.text)) {
				deleteSimilar(element);
			}
		}
	}

	private void editSimilar(Document doc, String render) {
		Document ivpca = service.getD7Similar(doc.getObjectId("_id"), br.getDomain());
		Editor.create("D7-类似问题-编辑器.editorassy", context, ivpca, true).ok((r, t) -> {
			Document d = service.updateD7Similar(t, lang, render, br.getDomain());
			viewer.update(AUtil.simpleCopy(d, doc), null);
		});
	}

	private void deleteSimilar(Document doc) {
		if (br.confirm("删除", "请确认删除选择的相似项。")) {
			service.deleteD7Similar(doc.getObjectId("_id"), br.getDomain());
			List<?> input = (List<?>) viewer.getInput();
			input.remove(doc);
			viewer.remove(doc);
		}
	}

	protected void doCreate(Problem problem) {
		String editorName = getEditorName();
		ObjectId problem_id = problem.get_id();
		Editor.create(editorName, context, new Document(), true).setTitle(getItemTypeName()).ok((r, t) -> {
			t = Services.get(ProblemService.class).insertAction(t, problem_id, stage, RWT.getLocale().getLanguage(),
					render, br.getDomain());
			((IQueryEnable) context.getContent()).doRefresh();
		});
	}

	protected void doEdit(Document element) {
		if (isFinished(element)) {// 已经完成
			doRead(element);
		} else {
			ObjectId _id = element.getObjectId("_id");
			Document input = service.getAction(_id, br.getDomain());
			String editorName = getEditorName();
			String title = getItemTypeName();
			Editor.create(editorName, context, input, true).setTitle(title).ok((r, t) -> {
				Document d = service.updateAction(t, lang, render, "updated", br.getDomain());
				viewer.update(AUtil.simpleCopy(d, element), null);
			});
		}
	}

	protected void doRead(Document element) {
		ObjectId _id = element.getObjectId("_id");
		Document input = service.getAction(_id, br.getDomain());
		String editorName = getEditorNameForRead();
		String title = getItemTypeName();
		Editor.create(editorName, context, input, true).setTitle(title).setEditable(false).open();
	}

	protected void doDelete(Document element) {
		if (br.confirm("删除", "请确认删除选择的" + getItemTypeName())) {
			ObjectId _id = element.getObjectId("_id");
			service.deleteAction(_id, br.getDomain());
			((IQueryEnable) context.getContent()).doRefresh();
		}
	}

	protected void doVerify(Document element) {
		ObjectId _id = element.getObjectId("_id");
		Document document = service.getAction(_id, br.getDomain());
		Document input = Optional.ofNullable((Document) document.get("verification")).orElse(new Document());
		String title = "验证" + getItemTypeName();
		String editorName = getVerfiyEditorName();
		Editor.create(editorName, context, input, true).setTitle(title).ok((r, t) -> {
			Document d = service.updateAction(new Document("_id", _id).append("verification", t), lang, render,
					"verified", br.getDomain());
			if ("已验证".equals(t.get("title")) && br.confirm("验证", "验证通过，是否立即完成本项行动？")) {
				d = service.updateAction(new Document("_id", _id).append(ProblemService.ACTION_FINISH, true), lang,
						render, "finished", br.getDomain());
			}
			viewer.update(AUtil.simpleCopy(d, element), null);
		});
	}

	protected void doFinish(Document element) {
		if (br.confirm("完成", "请确认完成选择的" + getItemTypeName())) {
			ObjectId _id = element.getObjectId("_id");
			Document d = service.updateAction(new Document("_id", _id).append(ProblemService.ACTION_FINISH, true), lang,
					render, "finished", br.getDomain());
			viewer.update(AUtil.simpleCopy(d, element), null);
		}
	}

	protected String getVerfiyEditorName() {
		return Check.option(verifyEditor).orElse("Dx-行动验证-编辑器.editorassy");
	}

	protected String getEditorName() {
		return Check.option(editor).orElse("Dx-行动计划-编辑器.editorassy");
	}

	protected String getEditorNameForRead() {
		return Check.option(editor).orElse("Dx-行动计划-编辑器（查看）.editorassy");
	}

	private String getItemTypeName() {
		return ProblemService.actionName[Arrays.asList(ProblemService.actionType).indexOf(stage)];
	}

	@Behavior({ ProblemService.ACTION_EDIT, ProblemService.ACTION_EDIT_SIMILAR })
	private boolean enableEdit(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if (!"解决中".equals(problem.getStatus()))
			return false;
		return true;
		// TODO
	}

	@Behavior(ProblemService.ACTION_CREATE)
	private boolean enableCreate(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if (!"解决中".equals(problem.getStatus()))
			return false;
		return true;
		// TODO
	}

	@Behavior({ ProblemService.ACTION_DELETE, ProblemService.ACTION_DELETE_SIMILAR })
	private boolean enableDelete(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if (!"解决中".equals(problem.getStatus()))
			return false;
		if (isFinished(element)) // 已经完成
			return false;
		return true;
	}

	@Behavior(ProblemService.ACTION_VERIFY)
	private boolean enableVerify(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if (!"解决中".equals(problem.getStatus()))
			return false;
		if (isFinished(element)) {// 已经完成
			return false;
		}
		return true;
	}

	@Behavior(ProblemService.ACTION_FINISH)
	private boolean enableFinish(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if (!"解决中".equals(problem.getStatus()))
			return false;
		if (isFinished(element)) // 已经完成
			return false;
		if (!isVerified(element))// 未验证的
			return false;

		return true;

	}

	private boolean isVerified(Document element) {
		return Optional.ofNullable((Document) element.get("verification")).map(d -> "已验证".equals(d.getString("title")))
				.orElse(false);
	}

	private boolean isFinished(Document element) {
		return Check.isTrue(element.get("finish"));
	}
}
