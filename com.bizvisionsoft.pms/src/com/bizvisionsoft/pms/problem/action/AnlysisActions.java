package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class AnlysisActions {

	@Inject
	private String actionType;

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
		} else if ("add".equals(action.getName())) {
			handleAdd(data, mType, _id);
		}
	}

	private void handleAdd(Document data, String mType, ObjectId _id) {
		// TODO Auto-generated method stub

	}

	private void handleDetail(Document data, String mType, ObjectId _id) {
		if ("d8EXP".equals(mType)) {
			Document doc = service.getD8Exp(_id);
			Editor.create("D8-经验总结-编辑器", context, doc, false).setEditable(false).open();
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

	@Behavior({ "add" })
	private boolean enableAdd(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document elem) {
		if (context.getRootInput() instanceof Problem) {// 只可在问题页面中使用
			Document data = (Document) elem.get("data");
			if (data == null) {
				return false;
			}
			return "action".equals(data.getString("mType"));
		} else {
			return false;
		}
	}

}
