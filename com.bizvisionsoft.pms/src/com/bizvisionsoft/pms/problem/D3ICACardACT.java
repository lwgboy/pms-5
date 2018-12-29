package com.bizvisionsoft.pms.problem;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.mongocodex.tools.BsonTools;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.serviceconsumer.Services;

public class D3ICACardACT {
	@Inject
	private IBruiService br;
	private ProblemService service;

	public D3ICACardACT() {
		service = Services.get(ProblemService.class);
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e) {
		if (e.text == null)
			return;
		ObjectId _id = element.getObjectId("_id");
		GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
		if (e.text.startsWith("editICA")) {
			editD3ICA(_id, element, viewer, context);
		} else if (e.text.startsWith("deleteICA")) {
			deleteD3ICA(_id, element, viewer, context);
		} else if (e.text.startsWith("verificationICA")) {
			verificationD3ICA(_id, element, viewer, context);
		} else if (e.text.startsWith("finishICA")) {
			deleteD3ICA(_id, element, viewer, context);
		}

	}

	private void verificationD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Document d3ICAVerified = service.getD3ICAVerified(_id);
		Editor.create("D3-ICA验证", context, d3ICAVerified, true).ok((r, t) -> {
			t.append("userId", br.getCurrentUserId());
			t.append("user_meta", BsonTools.encodeDocument(br.getCurrentUserInfo()));
			t = service.updateD3ICAVerified(t, _id, RWT.getLocale().getLanguage());
			// TODO 刷新
			// AUtil.simpleCopy(t, doc);
			// viewer.refresh(doc);
		});
	}

	private void deleteD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (MessageDialog.openConfirm(br.getCurrentShell(), "删除", "请确认将要删除选择的记录。")) {
			service.deleteD3ICA(_id);
			((List<?>) viewer.getInput()).remove(doc);
			viewer.remove(doc);
		}
	}

	private void editD3ICA(ObjectId _id, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Document d3ICA = service.getD3ICA(_id);
		Editor.create("D3-ICA", context, d3ICA, true).ok((r, t) -> {
			t = service.updateD3ICA(t, RWT.getLocale().getLanguage());
			AUtil.simpleCopy(t, doc);
			viewer.refresh(doc);
		});
	}
}
