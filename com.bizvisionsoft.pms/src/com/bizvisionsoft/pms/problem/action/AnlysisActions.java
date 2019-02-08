package com.bizvisionsoft.pms.problem.action;

import java.util.Arrays;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.CauseConsequence;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class AnlysisActions {

	@Inject
	private IBruiService br;

	@Inject
	private IBruiContext context;

	private ProblemService service;

	@Execute
	public void execute(@MethodParam(Execute.EVENT) Event e, @MethodParam(Execute.ACTION) Action action) {
		service = Services.get(ProblemService.class);
		Document elem = Document.parse("" + e.data);
		Document data = (Document) elem.get("data");
		String mType = data.getString("mType");
		ObjectId _id = new ObjectId(data.getString("id"));
		if ("detail".equals(action.getName())) {
			handleDetail(data, mType, _id);
		} else if ("addERA".equals(action.getName())) {
			handleAdd(data, "era", _id);
		} else if ("addPCA".equals(action.getName())) {
			handleAdd(data, "pca", _id);
		} else if ("addICA".equals(action.getName())) {
			handleAdd(data, "ica", _id);
		}
	}

	private void handleAdd(Document data, String stage, ObjectId _id) {
		Document action = service.getAction(_id);
		boolean b = br.confirm("复制行动计划", "请确认复制行动计划到当前的问题：<br>" + action.getString("action"));
		if (!b)
			return;
		Problem problem = (Problem) context.getRootInput();
		Document doc = new Document();
		Arrays.asList("action", "detail", "objective").forEach(f -> doc.append(f, action.get(f)));
		String editorName = getEditorName(stage);
		ObjectId problem_id = problem.get_id();
		Editor.create(editorName, context, doc, true).setTitle(getItemTypeName(stage)).ok((r, t) -> {
			Services.get(ProblemService.class).insertAction(t, problem_id, stage, RWT.getLocale().getLanguage(), "gridrow");
			Layer.message("行动计划已创建");
		});
	}

	private String getItemTypeName(String stage) {
		return ProblemService.actionName[Arrays.asList(ProblemService.actionType).indexOf(stage)];
	}

	private String getEditorName(String stage) {
		if ("pca".equals(stage)) {
			return "Dx-分类行动计划-编辑器";
		} else {
			return "Dx-行动计划-编辑器";
		}
	}

	private void handleDetail(Document data, String mType, ObjectId _id) {
		if ("d8EXP".equals(mType)) {
			Document doc = service.getD8Exp(_id);
			Editor.create("D8-经验总结-编辑器", context, doc, true).setEditable(false).open();
		} else if ("causeRelation".equals(mType)) {
			CauseConsequence cc = service.listCauseConsequences(new BasicDBObject("_id", _id)).get(0);
			Editor.create("因素编辑器", context, cc, true).setEditable(false).open();
		} else if ("problemAction".equals(mType)) {
			Document doc = service.getAction(_id);
			Editor.create("Dx-行动计划-编辑器（查看）", context, doc, true).setEditable(false).open();
		}
	}

	@Behavior({ "detail" })
	private boolean enableOpenDetail(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document elem) {
		Document data = (Document) elem.get("data");
		if (data == null)
			return false;
		String mType = data.getString("mType");
		return mType != null && !"root".equals(mType);
	}

	@Behavior({ "addERA","addICA","addPCA" })
	private boolean enableAdd(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document elem) {
		if (context.getRootInput() instanceof Problem) {// 只可在问题页面中使用
			Problem problem = (Problem) context.getRootInput();
			if ("解决中".equals(problem.getStatus())) {
				Document data = (Document) elem.get("data");
				if (data == null) {
					return false;
				}
				return "problemAction".equals(data.getString("mType"));
			}
		}
		return false;
	}

}
