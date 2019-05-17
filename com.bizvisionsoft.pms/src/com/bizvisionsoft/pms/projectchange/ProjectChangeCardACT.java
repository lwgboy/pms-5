package com.bizvisionsoft.pms.projectchange;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.model.ProjectChangeTask;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class ProjectChangeCardACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.CURRENT_USER_ID) String userid) {
		if (e.text == null)
			return;
		ObjectId _id = element.getObjectId("_id");
		ProjectChange pc = Services.get(ProjectService.class).getProjectChange(_id, br.getDomain());
		GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
		if (e.text.startsWith("pass")) {
			passProjectChange(pc, userid, element, viewer);
		} else if (e.text.startsWith("cancel")) {
			cancelProjectChange(pc, userid, element, viewer);
		}
	}

	private void cancelProjectChange(ProjectChange pc, String userid, Document doc, GridTreeViewer viewer) {
		InputDialog id = new InputDialog(br.getCurrentShell(), "否决", "请填写否决意见", null, t -> {
			return t.trim().isEmpty() ? "请填写否决意见" : null;
		}).setTextMultiline(true);
		if (id.open() == InputDialog.OK) {
			List<Result> result = ServicesLoader.get(ProjectService.class)
					.cancelProjectChange(ProjectChangeTask.getCancelInstance(userid, pc.get_id(), pc.getConfimName(userid), id.getValue(),br.getDomain()), br.getDomain());
			if (result.isEmpty()) {
				Layer.message("变更申请已否决");
				((List<?>) viewer.getInput()).remove(doc);
				viewer.remove(doc);
			}
		}
	}

	private void passProjectChange(ProjectChange pc, String userid, Document doc, GridTreeViewer viewer) {
		InputDialog id = new InputDialog(br.getCurrentShell(), "批准", "请填写批准意见", null, null).setTextMultiline(true);
		if (id.open() == InputDialog.OK) {
			List<Result> result = ServicesLoader.get(ProjectService.class)
					.passProjectChange(ProjectChangeTask.getPassInstance(userid, pc.get_id(), pc.getConfimName(userid), id.getValue(),br.getDomain()), br.getDomain());
			if (result.isEmpty()) {
				Layer.message("变更申请已确认");
				((List<?>) viewer.getInput()).remove(doc);
				viewer.remove(doc);
			}
		}
	}
}
